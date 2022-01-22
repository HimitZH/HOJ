package top.hcode.hoj.service.judge.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.judge.remote.RemoteJudgeDispatcher;
import top.hcode.hoj.judge.self.JudgeDispatcher;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.user.UserAcproblem;
import top.hcode.hoj.service.contest.impl.ContestRecordServiceImpl;
import top.hcode.hoj.service.judge.RejudgeService;
import top.hcode.hoj.service.problem.impl.ProblemServiceImpl;
import top.hcode.hoj.service.user.impl.UserAcproblemServiceImpl;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/7 18:12
 * @Description:
 */
@Service
@RefreshScope
public class RejudgeServiceImpl implements RejudgeService {

    @Resource
    private JudgeServiceImpl judgeService;

    @Resource
    private UserAcproblemServiceImpl userAcproblemService;

    @Resource
    private ContestRecordServiceImpl contestRecordService;

    @Resource
    private JudgeCaseServiceImpl judgeCaseService;

    @Resource
    private ProblemServiceImpl problemService;

    @Value("${hoj.judge.token}")
    private String judgeToken;

    @Resource
    private JudgeDispatcher judgeDispatcher;

    @Resource
    private RemoteJudgeDispatcher remoteJudgeDispatcher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult rejudge(Long submitId) {
        Judge judge = judgeService.getById(submitId);

        boolean isContestSubmission = judge.getCid() != 0;
        boolean resetContestRecordResult = true;

        // 如果是非比赛题目
        if (!isContestSubmission) {
            // 重判前，需要将该题目对应记录表一并更新
            // 如果该题已经是AC通过状态，更新该题目的用户ac做题表 user_acproblem
            if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus().intValue()) {
                QueryWrapper<UserAcproblem> userAcproblemQueryWrapper = new QueryWrapper<>();
                userAcproblemQueryWrapper.eq("submit_id", judge.getSubmitId());
                userAcproblemService.remove(userAcproblemQueryWrapper);
            }
        } else {
            // 将对应比赛记录设置成默认值
            UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("submit_id", submitId).setSql("status=null,score=null");
            resetContestRecordResult = contestRecordService.update(updateWrapper);
        }

        // 清除该提交对应的测试点结果
        QueryWrapper<JudgeCase> judgeCaseQueryWrapper = new QueryWrapper<>();
        judgeCaseQueryWrapper.eq("submit_id", submitId);
        judgeCaseService.remove(judgeCaseQueryWrapper);

        boolean hasSubmitIdRemoteRejudge = isHasSubmitIdRemoteRejudge(judge.getVjudgeSubmitId(), judge.getStatus());

        // 设置默认值
        judge.setStatus(Constants.Judge.STATUS_PENDING.getStatus()); // 开始进入判题队列
        judge.setVersion(judge.getVersion() + 1);
        judge.setJudger("")
                .setTime(null)
                .setMemory(null)
                .setErrorMessage(null)
                .setOiRankScore(null)
                .setScore(null);

        boolean result = judgeService.updateById(judge);


        if (result && resetContestRecordResult) {
            // 调用判题服务
            Problem problem = problemService.getById(judge.getPid());
            if (problem.getIsRemote()) { // 如果是远程oj判题
                remoteJudgeDispatcher.sendTask(judge, judgeToken, problem.getProblemId(),
                        isContestSubmission, hasSubmitIdRemoteRejudge);
            } else {
                judgeDispatcher.sendTask(judge, judgeToken, isContestSubmission);
            }
            return CommonResult.successResponse(judge, "重判成功！该提交已进入判题队列！");
        } else {
            return CommonResult.successResponse(judge, "重判失败！请重新尝试！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult rejudgeContestProblem(Long cid, Long pid) {
        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid).eq("pid", pid);
        List<Judge> rejudgeList = judgeService.list(queryWrapper);

        if (rejudgeList.size() == 0) {
            return CommonResult.errorResponse("当前该题目无提交，不可重判！");
        }
        List<Long> submitIdList = new LinkedList<>();
        HashMap<Long, Integer> idMapStatus = new HashMap<>();
        // 全部设置默认值
        for (Judge judge : rejudgeList) {
            idMapStatus.put(judge.getSubmitId(), judge.getStatus());
            judge.setStatus(Constants.Judge.STATUS_PENDING.getStatus()); // 开始进入判题队列
            judge.setVersion(judge.getVersion() + 1);
            judge.setJudger("")
                    .setTime(null)
                    .setMemory(null)
                    .setErrorMessage(null)
                    .setOiRankScore(null)
                    .setScore(null);
            submitIdList.add(judge.getSubmitId());
        }
        boolean resetJudgeResult = judgeService.updateBatchById(rejudgeList);
        // 清除每个提交对应的测试点结果
        QueryWrapper<JudgeCase> judgeCaseQueryWrapper = new QueryWrapper<>();
        judgeCaseQueryWrapper.in("submit_id", submitIdList);
        judgeCaseService.remove(judgeCaseQueryWrapper);
        // 将对应比赛记录设置成默认值
        UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("submit_id", submitIdList).setSql("status=null,score=null");
        boolean resetContestRecordResult = contestRecordService.update(updateWrapper);

        if (resetContestRecordResult && resetJudgeResult) {
            // 调用重判服务
            Problem problem = problemService.getById(pid);
            if (problem.getIsRemote()) { // 如果是远程oj判题
                for (Judge judge : rejudgeList) {
                    // 进入重判队列，等待调用判题服务
                    remoteJudgeDispatcher.sendTask(judge, judgeToken, problem.getProblemId(),
                            judge.getCid() != 0,
                            isHasSubmitIdRemoteRejudge(judge.getVjudgeSubmitId(), idMapStatus.get(judge.getSubmitId())));
                }
            } else {
                for (Judge judge : rejudgeList) {
                    // 进入重判队列，等待调用判题服务
                    judgeDispatcher.sendTask(judge, judgeToken, judge.getCid() != 0);
                }
            }

            return CommonResult.successResponse(null, "重判成功！该题目对应的全部提交已进入判题队列！");
        } else {
            return CommonResult.errorResponse("重判失败！请重新尝试！");
        }
    }

    private boolean isHasSubmitIdRemoteRejudge(Long vjudgeSubmitId, int status) {
        boolean isHasSubmitIdRemoteRejudge = false;
        if (vjudgeSubmitId != null &&
                (status == Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus()
                        || status == Constants.Judge.STATUS_COMPILING.getStatus()
                        || status == Constants.Judge.STATUS_PENDING.getStatus()
                        || status == Constants.Judge.STATUS_JUDGING.getStatus()
                        || status == Constants.Judge.STATUS_SYSTEM_ERROR.getStatus())) {
            isHasSubmitIdRemoteRejudge = true;
        }
        return isHasSubmitIdRemoteRejudge;
    }
}
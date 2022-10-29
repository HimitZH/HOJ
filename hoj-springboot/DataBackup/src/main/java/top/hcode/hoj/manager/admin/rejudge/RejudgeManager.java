package top.hcode.hoj.manager.admin.rejudge;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.contest.ContestRecordEntityService;
import top.hcode.hoj.dao.judge.JudgeCaseEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.user.UserAcproblemEntityService;
import top.hcode.hoj.judge.remote.RemoteJudgeDispatcher;
import top.hcode.hoj.judge.self.JudgeDispatcher;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.user.UserAcproblem;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 16:21
 * @Description:
 */
@Component
public class RejudgeManager {

    @Resource
    private JudgeEntityService judgeEntityService;

    @Resource
    private UserAcproblemEntityService userAcproblemEntityService;

    @Resource
    private ContestRecordEntityService contestRecordEntityService;

    @Resource
    private JudgeCaseEntityService judgeCaseEntityService;

    @Resource
    private ProblemEntityService problemEntityService;

    @Resource
    private JudgeDispatcher judgeDispatcher;

    @Resource
    private RemoteJudgeDispatcher remoteJudgeDispatcher;

    private static List<Integer> penaltyStatus = Arrays.asList(
            Constants.Judge.STATUS_PRESENTATION_ERROR.getStatus(),
            Constants.Judge.STATUS_WRONG_ANSWER.getStatus(),
            Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus(),
            Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus(),
            Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());


    public Judge rejudge(Long submitId) throws StatusFailException {
        Judge judge = judgeEntityService.getById(submitId);

        boolean isContestSubmission = judge.getCid() != 0;

        boolean hasSubmitIdRemoteRejudge = checkAndUpdateJudge(isContestSubmission, judge, submitId);
        // 调用判题服务
        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.select("id", "is_remote", "problem_id")
                .eq("id", judge.getPid());
        Problem problem = problemEntityService.getOne(problemQueryWrapper);
        if (problem.getIsRemote()) { // 如果是远程oj判题
            remoteJudgeDispatcher.sendTask(judge.getSubmitId(), judge.getPid(), problem.getProblemId(),
                    isContestSubmission, hasSubmitIdRemoteRejudge);
        } else {
            judgeDispatcher.sendTask(judge.getSubmitId(), judge.getPid(), isContestSubmission);
        }
        return judge;
    }

    @Transactional(rollbackFor = Exception.class)
    public void rejudgeContestProblem(Long cid, Long pid) throws StatusFailException {
        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid).eq("pid", pid);
        List<Judge> rejudgeList = judgeEntityService.list(queryWrapper);

        if (rejudgeList.size() == 0) {
            throw new StatusFailException("当前该题目无提交，不可重判！");
        }
        HashMap<Long, Integer> idMapStatus = new HashMap<>();
        // 全部设置默认值
        checkAndUpdateJudgeBatch(rejudgeList, idMapStatus);
        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.select("id", "is_remote", "problem_id")
                .eq("id", pid);
        Problem problem = problemEntityService.getOne(problemQueryWrapper);
        // 调用重判服务
        if (problem.getIsRemote()) { // 如果是远程oj判题
            for (Judge judge : rejudgeList) {
                // 进入重判队列，等待调用判题服务
                remoteJudgeDispatcher.sendTask(judge.getSubmitId(),
                        pid,
                        problem.getProblemId(),
                        judge.getCid() != 0,
                        isHasSubmitIdRemoteRejudge(judge.getVjudgeSubmitId(), idMapStatus.get(judge.getSubmitId())));
            }
        } else {
            for (Judge judge : rejudgeList) {
                // 进入重判队列，等待调用判题服务
                judgeDispatcher.sendTask(judge.getSubmitId(), judge.getPid(), judge.getCid() != 0);
            }
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndUpdateJudge(Boolean isContestSubmission, Judge judge, Long submitId) throws StatusFailException {
        // 如果是非比赛题目
        boolean resetContestRecordResult = true;
        if (!isContestSubmission) {
            // 重判前，需要将该题目对应记录表一并更新
            // 如果该题已经是AC通过状态，更新该题目的用户ac做题表 user_acproblem
            if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus().intValue()) {
                QueryWrapper<UserAcproblem> userAcproblemQueryWrapper = new QueryWrapper<>();
                userAcproblemQueryWrapper.eq("submit_id", judge.getSubmitId());
                userAcproblemEntityService.remove(userAcproblemQueryWrapper);
            }
        } else {
            // 将对应比赛记录设置成默认值
            UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("submit_id", submitId).setSql("status=null,score=null");
            resetContestRecordResult = contestRecordEntityService.update(updateWrapper);
        }

        // 清除该提交对应的测试点结果
        QueryWrapper<JudgeCase> judgeCaseQueryWrapper = new QueryWrapper<>();
        judgeCaseQueryWrapper.eq("submit_id", submitId);
        judgeCaseEntityService.remove(judgeCaseQueryWrapper);

        boolean hasSubmitIdRemoteRejudge = isHasSubmitIdRemoteRejudge(judge.getVjudgeSubmitId(), judge.getStatus());

        // 设置默认值
        judge.setStatus(Constants.Judge.STATUS_PENDING.getStatus()); // 开始进入判题队列
        judge.setVersion(judge.getVersion() + 1);
        judge.setJudger("")
                .setIsManual(false)
                .setTime(null)
                .setMemory(null)
                .setErrorMessage(null)
                .setOiRankScore(null)
                .setScore(null);
        boolean isUpdateJudgeOk = judgeEntityService.updateById(judge);

        if (!resetContestRecordResult || !isUpdateJudgeOk) {
            throw new StatusFailException("重判失败！请重新尝试！");
        }
        return hasSubmitIdRemoteRejudge;
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkAndUpdateJudgeBatch(List<Judge> rejudgeList, HashMap<Long, Integer> idMapStatus) throws StatusFailException {
        List<Long> submitIdList = new LinkedList<>();
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
                    .setIsManual(false)
                    .setScore(null);
            submitIdList.add(judge.getSubmitId());
        }
        boolean resetJudgeResult = judgeEntityService.updateBatchById(rejudgeList);
        // 清除每个提交对应的测试点结果
        QueryWrapper<JudgeCase> judgeCaseQueryWrapper = new QueryWrapper<>();
        judgeCaseQueryWrapper.in("submit_id", submitIdList);
        judgeCaseEntityService.remove(judgeCaseQueryWrapper);
        // 将对应比赛记录设置成默认值
        UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("submit_id", submitIdList).setSql("status=null,score=null");
        boolean resetContestRecordResult = contestRecordEntityService.update(updateWrapper);

        if (!resetContestRecordResult || !resetJudgeResult) {
            throw new StatusFailException("重判失败！请重新尝试！");
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

    @Transactional(rollbackFor = Exception.class)
    public Judge manualJudge(Long submitId, Integer status, Integer score) throws StatusFailException {
        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper
                .select("submit_id", "status", "judger", "cid", "pid", "uid")
                .eq("submit_id", submitId);
        Judge judge = judgeEntityService.getOne(judgeQueryWrapper);
        if (judge == null) {
            throw new StatusFailException("错误：该提交数据已不存在！");
        }
        if (judge.getStatus().equals(Constants.Judge.STATUS_JUDGING.getStatus())
                || judge.getStatus().equals(Constants.Judge.STATUS_COMPILING.getStatus())
                || judge.getStatus().equals(Constants.Judge.STATUS_PENDING.getStatus())) {
            throw new StatusFailException("错误：该提交正在评测中，无法修改，请稍后再尝试！");
        }
        if (judge.getStatus().equals(Constants.Judge.STATUS_COMPILE_ERROR.getStatus())) {
            throw new StatusFailException("错误：编译失败的提交无法修改！");
        }
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
        judgeUpdateWrapper
                .set("status", status)
                .set("is_manual", true)
                .set("judger", userRolesVo.getUsername())
                .eq("submit_id", judge.getSubmitId());
        Integer oiRankScore = null;
        if (score != null) {
            Problem problem = problemEntityService.getById(judge.getPid());
            if (problem != null && Objects.equals(problem.getType(), Constants.Contest.TYPE_OI.getCode())
                    && problem.getIoScore() != null) {
                if (score > problem.getIoScore()) {
                    score = problem.getIoScore();
                } else if (score < 0) {
                    score = 0;
                }
                oiRankScore = (int) Math.round(problem.getDifficulty() * 2 + 0.1 * score);
                judgeUpdateWrapper.set("score", score)
                        .set("oi_rank_score", oiRankScore);
            } else {
                score = null;
            }
        }

        boolean isUpdateOK = judgeEntityService.update(judgeUpdateWrapper);
        if (!isUpdateOK) {
            throw new StatusFailException("错误：该提交正在评测中，无法取消，请稍后再尝试！");
        }

        // 如果原是AC,现在人工评测后不是AC,就移除user_acproblem表对应的记录
        if (Objects.equals(judge.getStatus(), Constants.Judge.STATUS_ACCEPTED.getStatus())
                && !Objects.equals(status, Constants.Judge.STATUS_ACCEPTED.getStatus())) {
            QueryWrapper<UserAcproblem> userAcproblemQueryWrapper = new QueryWrapper<>();
            userAcproblemQueryWrapper.eq("submit_id", judge.getSubmitId());
            userAcproblemEntityService.remove(userAcproblemQueryWrapper);
        } else if (!Objects.equals(judge.getStatus(), Constants.Judge.STATUS_ACCEPTED.getStatus())
                && Objects.equals(status, Constants.Judge.STATUS_ACCEPTED.getStatus())) {
            // 如果原先不是AC,现在人工评测后是AC,就更新user_acproblem表
            if (status.intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus() && judge.getGid() == null) {
                userAcproblemEntityService.saveOrUpdate(new UserAcproblem()
                        .setPid(judge.getPid())
                        .setUid(judge.getUid())
                        .setSubmitId(submitId)
                );
            }
        }

        if (judge.getCid() != 0) {
            UpdateWrapper<ContestRecord> contestRecordUpdateWrapper = new UpdateWrapper<>();
            contestRecordUpdateWrapper.eq("submit_id", submitId)
                    .eq("cid", judge.getCid());
            if (Objects.equals(status, Constants.Judge.STATUS_ACCEPTED.getStatus())) {
                contestRecordUpdateWrapper.set("status", Constants.Contest.RECORD_AC.getCode());
            } else if (penaltyStatus.contains(status)) {
                contestRecordUpdateWrapper.set("status", Constants.Contest.RECORD_NOT_AC_PENALTY.getCode());
            } else {
                contestRecordUpdateWrapper.set("status", Constants.Contest.RECORD_NOT_AC_NOT_PENALTY.getCode());
            }
            contestRecordUpdateWrapper.set(score != null, "score", score);
            contestRecordEntityService.update(contestRecordUpdateWrapper);
        }
        Judge res = new Judge();
        res.setSubmitId(submitId)
                .setJudger(userRolesVo.getUsername())
                .setStatus(status)
                .setScore(score)
                .setOiRankScore(oiRankScore);
        return res;
    }

    @Transactional(rollbackFor = Exception.class)
    public Judge cancelJudge(Long submitId) throws StatusFailException {
        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper
                .select("submit_id", "status", "judger", "cid")
                .eq("submit_id", submitId)
                .last("for update");
        Judge judge = judgeEntityService.getOne(judgeQueryWrapper);
        if (judge == null) {
            throw new StatusFailException("错误：该提交数据已不存在！");
        }
        if (judge.getStatus().equals(Constants.Judge.STATUS_JUDGING.getStatus())
                || judge.getStatus().equals(Constants.Judge.STATUS_COMPILING.getStatus())
                || (judge.getStatus().equals(Constants.Judge.STATUS_PENDING.getStatus())
                && !StringUtils.isEmpty(judge.getJudger()))) {
            throw new StatusFailException("错误：该提交正在评测中，无法取消，请稍后再尝试！");
        }
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
        judgeUpdateWrapper
                .setSql("status=-4,score=null,oi_rank_score=null,is_manual=true,judger='" + userRolesVo.getUsername() + "'")
                .eq("submit_id", judge.getSubmitId());
        boolean isUpdateOK = judgeEntityService.update(judgeUpdateWrapper);
        if (!isUpdateOK) {
            throw new StatusFailException("错误：该提交正在评测中，无法取消，请稍后再尝试！");
        }

        // 如果该题已经是AC通过状态，更新该题目的用户ac做题表 user_acproblem
        if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus().intValue()) {
            QueryWrapper<UserAcproblem> userAcproblemQueryWrapper = new QueryWrapper<>();
            userAcproblemQueryWrapper.eq("submit_id", judge.getSubmitId());
            userAcproblemEntityService.remove(userAcproblemQueryWrapper);
        }

        if (judge.getCid() != 0) {
            UpdateWrapper<ContestRecord> contestRecordUpdateWrapper = new UpdateWrapper<>();
            contestRecordUpdateWrapper.eq("submit_id", submitId)
                    .eq("cid", judge.getCid())
                    .setSql("score=null")
                    .set("status", Constants.Contest.RECORD_NOT_AC_NOT_PENALTY.getCode());
            contestRecordEntityService.update(contestRecordUpdateWrapper);
        }
        Judge res = new Judge();
        res.setSubmitId(submitId)
                .setJudger(userRolesVo.getUsername())
                .setStatus(Constants.Judge.STATUS_CANCELLED.getStatus());
        return res;
    }
}
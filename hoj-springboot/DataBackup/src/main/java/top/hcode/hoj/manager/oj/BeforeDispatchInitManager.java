package top.hcode.hoj.manager.oj;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.exception.StatusAccessDeniedException;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.contest.ContestProblemEntityService;
import top.hcode.hoj.dao.contest.ContestRecordEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.training.TrainingEntityService;
import top.hcode.hoj.dao.training.TrainingProblemEntityService;
import top.hcode.hoj.dao.training.TrainingRecordEntityService;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.pojo.entity.training.TrainingRecord;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.validator.ContestValidator;
import top.hcode.hoj.validator.GroupValidator;
import top.hcode.hoj.validator.TrainingValidator;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 14:29
 * @Description:
 */
@Component
public class BeforeDispatchInitManager {

    @Resource
    private ContestEntityService contestEntityService;

    @Resource
    private ContestRecordEntityService contestRecordEntityService;

    @Resource
    private ContestProblemEntityService contestProblemEntityService;

    @Resource
    private JudgeEntityService judgeEntityService;

    @Resource
    private ProblemEntityService problemEntityService;

    @Resource
    private TrainingEntityService trainingEntityService;

    @Resource
    private TrainingProblemEntityService trainingProblemEntityService;

    @Resource
    private TrainingRecordEntityService trainingRecordEntityService;

    @Resource
    private TrainingValidator trainingValidator;

    @Resource
    private ContestValidator contestValidator;

    @Resource
    private TrainingManager trainingManager;

    @Autowired
    private GroupValidator groupValidator;

    public void initCommonSubmission(String problemId, Long gid, Judge judge) throws StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.select("id", "problem_id", "auth", "is_group", "gid");
        problemQueryWrapper.eq("problem_id", problemId);
        Problem problem = problemEntityService.getOne(problemQueryWrapper, false);

        if (problem == null){
            throw new StatusForbiddenException("错误！当前题目已不存在，不可提交！");
        }

        if (problem.getAuth() == 2) {
            throw new StatusForbiddenException("错误！当前题目不可提交！");
        }

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        if (problem.getIsGroup()) {
            if (gid == null){
                throw new StatusForbiddenException("提交失败，该题目为团队所属，请你前往指定团队内提交！");
            }
            if (!isRoot && !groupValidator.isGroupMember(userRolesVo.getUid(), problem.getGid())) {
                throw new StatusForbiddenException("对不起，您并非该题目所属的团队内成员，无权进行提交！");
            }
            judge.setGid(problem.getGid());
        }

        judge.setCpid(0L)
                .setPid(problem.getId())
                .setDisplayPid(problem.getProblemId());

        // 将新提交数据插入数据库
        judgeEntityService.save(judge);

        trainingManager.checkAndSyncTrainingRecord(problem.getId(), judge.getSubmitId(), judge.getUid());
    }


    @Transactional(rollbackFor = Exception.class)
    public void initContestSubmission(Long cid, String displayId, AccountProfile userRolesVo, Judge judge) throws StatusNotFoundException, StatusForbiddenException {
        Contest contest = contestEntityService.getById(cid);
        if (contest == null) {
            throw new StatusNotFoundException("对不起，该比赛不存在！");
        }
        // 首先判断一下比赛的状态是否是正在进行，结束状态都不能提交(除非开启允许赛后提交)，比赛前比赛管理员可以提交
        if (Objects.equals(contest.getAllowEndSubmit(), false)){
            if (contest.getStatus().intValue() == Constants.Contest.STATUS_ENDED.getCode()) {
                throw new StatusForbiddenException("比赛已结束，不可再提交！");
            }
        }

        // 是否为超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        if (!isRoot && !contest.getUid().equals(userRolesVo.getUid())
                && !(contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()))) {
            if (contest.getStatus().intValue() == Constants.Contest.STATUS_SCHEDULED.getCode()) {
                throw new StatusForbiddenException("比赛未开始，不可提交！");
            }
            // 需要检查是否有权限在当前比赛进行提交
            contestValidator.validateJudgeAuth(contest, userRolesVo.getUid());

            // 需要校验当前比赛是否为保护或私有比赛，同时是否开启账号规则限制，如果有，需要对当前用户的用户名进行验证
            if (contest.getOpenAccountLimit()
                    && !contestValidator.validateAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
                throw new StatusForbiddenException("对不起！本次比赛只允许符合特定账号规则的用户参赛！");
            }
        }

        // 查询获取对应的pid和cpid
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid).eq("display_id", displayId);
        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper, false);
        judge.setCpid(contestProblem.getId())
                .setPid(contestProblem.getPid())
                .setGid(contest.getGid());

        Problem problem = problemEntityService.getById(contestProblem.getPid());

        if (problem == null){
            throw new StatusForbiddenException("错误！当前题目已不存在，不可提交！");
        }

        if (problem.getAuth() == 2) {
            throw new StatusForbiddenException("错误！当前题目已被隐藏，不可提交！");
        }

        if (problem.getIsGroup()){
            judge.setGid(problem.getGid());
        }

        judge.setDisplayPid(problem.getProblemId());
        // 将新提交数据插入数据库
        judgeEntityService.save(judge);

        // 同时初始化写入contest_record表
        ContestRecord contestRecord = new ContestRecord();
        contestRecord.setDisplayId(displayId)
                .setCpid(contestProblem.getId())
                .setSubmitId(judge.getSubmitId())
                .setPid(judge.getPid())
                .setUsername(userRolesVo.getUsername())
                .setRealname(userRolesVo.getRealname())
                .setUid(userRolesVo.getUid())
                .setCid(judge.getCid())
                .setSubmitTime(judge.getSubmitTime());

        if (contest.getStatus().intValue() == Constants.Contest.STATUS_SCHEDULED.getCode()) {
            contestRecord.setTime(0L);
        } else {
            // 设置比赛开始时间到提交时间之间的秒数
            contestRecord.setTime(DateUtil.between(contest.getStartTime(), judge.getSubmitTime(), DateUnit.SECOND));
        }
        contestRecordEntityService.save(contestRecord);
    }


    @Transactional(rollbackFor = Exception.class)
    public void initTrainingSubmission(Long tid, String displayId, AccountProfile userRolesVo, Judge judge) throws StatusForbiddenException, StatusFailException, StatusAccessDeniedException {

        Training training = trainingEntityService.getById(tid);
        if (training == null || !training.getStatus()) {
            throw new StatusFailException("该训练不存在或不允许显示！");
        }

        trainingValidator.validateTrainingAuth(training, userRolesVo);

        // 查询获取对应的pid和cpid
        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        trainingProblemQueryWrapper.eq("tid", tid)
                .eq("display_id", displayId);
        TrainingProblem trainingProblem = trainingProblemEntityService.getOne(trainingProblemQueryWrapper);
        judge.setPid(trainingProblem.getPid());

        Problem problem = problemEntityService.getById(trainingProblem.getPid());

        if (problem == null){
            throw new StatusForbiddenException("错误！当前题目已不存在，不可提交！");
        }

        if (problem.getAuth() == 2) {
            throw new StatusForbiddenException("错误！当前题目不可提交！");
        }

        if (problem.getIsGroup()){
            judge.setGid(problem.getGid());
        }

        judge.setDisplayPid(problem.getProblemId())
                .setGid(training.getGid());

        // 将新提交数据插入数据库
        judgeEntityService.save(judge);

        // 非私有训练不记录
        if (!training.getAuth().equals(Constants.Training.AUTH_PRIVATE.getValue())) {
            return;
        }

        TrainingRecord trainingRecord = new TrainingRecord();
        trainingRecord.setPid(problem.getId())
                .setTid(tid)
                .setTpid(trainingProblem.getId())
                .setSubmitId(judge.getSubmitId())
                .setUid(userRolesVo.getUid());
        trainingRecordEntityService.save(trainingRecord);
    }


}
package top.hcode.hoj.manager.oj;

import org.springframework.beans.factory.annotation.Value;
import top.hcode.hoj.validator.GroupValidator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.hcode.hoj.common.exception.StatusAccessDeniedException;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.judge.remote.RemoteJudgeDispatcher;
import top.hcode.hoj.judge.self.JudgeDispatcher;
import top.hcode.hoj.pojo.dto.SubmitIdListDto;
import top.hcode.hoj.pojo.dto.ToJudgeDto;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.user.UserAcproblem;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.SubmissionInfoVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.contest.ContestRecordEntityService;
import top.hcode.hoj.dao.judge.JudgeCaseEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.user.UserAcproblemEntityService;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.IpUtils;
import top.hcode.hoj.utils.RedisUtils;
import top.hcode.hoj.validator.ContestValidator;
import top.hcode.hoj.validator.JudgeValidator;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 11:12
 * @Description:
 */
@Component
public class JudgeManager {
    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private JudgeCaseEntityService judgeCaseEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private ContestRecordEntityService contestRecordEntityService;

    @Autowired
    private UserAcproblemEntityService userAcproblemEntityService;

    @Autowired
    private JudgeDispatcher judgeDispatcher;

    @Autowired
    private RemoteJudgeDispatcher remoteJudgeDispatcher;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JudgeValidator judgeValidator;

    @Autowired
    private ContestValidator contestValidator;

    @Autowired
    private BeforeDispatchInitManager beforeDispatchInitManager;

    @Autowired
    private GroupValidator groupValidator;

    @Value("${hoj.web-config.default-user-limit.submit.interval}")
    private Integer defaultSubmitInterval;

    /**
     * @MethodName submitProblemJudge
     * @Description 核心方法 判题通过openfeign调用判题系统服务
     * @Since 2020/10/30
     */
    @Transactional(rollbackFor = Exception.class)
    public Judge submitProblemJudge(ToJudgeDto judgeDto) throws StatusForbiddenException, StatusFailException, StatusNotFoundException, StatusAccessDeniedException {

        judgeValidator.validateSubmissionInfo(judgeDto);

        // 需要获取一下该token对应用户的数据
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isContestSubmission = judgeDto.getCid() != 0;

        boolean isTrainingSubmission = judgeDto.getTid() != null && judgeDto.getTid() != 0;

        if (!isContestSubmission) { // 非比赛提交限制8秒提交一次
            String lockKey = Constants.Account.SUBMIT_NON_CONTEST_LOCK.getCode() + userRolesVo.getUid();
            long count = redisUtils.incr(lockKey, 1);
            if (count > 1) {
                throw new StatusForbiddenException("对不起，您的提交频率过快，请稍后再尝试！");
            }
            redisUtils.expire(lockKey, defaultSubmitInterval);
        }

        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        // 将提交先写入数据库，准备调用判题服务器
        Judge judge = new Judge();
        judge.setShare(false) // 默认设置代码为单独自己可见
                .setCode(judgeDto.getCode())
                .setCid(judgeDto.getCid())
                .setGid(judgeDto.getGid())
                .setLanguage(judgeDto.getLanguage())
                .setLength(judgeDto.getCode().length())
                .setUid(userRolesVo.getUid())
                .setUsername(userRolesVo.getUsername())
                .setStatus(Constants.Judge.STATUS_PENDING.getStatus()) // 开始进入判题队列
                .setSubmitTime(new Date())
                .setVersion(0)
                .setIp(IpUtils.getUserIpAddr(request));

        // 如果比赛id不等于0，则说明为比赛提交
        if (isContestSubmission) {
            beforeDispatchInitManager.initContestSubmission(judgeDto.getCid(), judgeDto.getPid(), userRolesVo, judge);
        } else if (isTrainingSubmission) {
            beforeDispatchInitManager.initTrainingSubmission(judgeDto.getTid(), judgeDto.getPid(), userRolesVo, judge);
        } else { // 如果不是比赛提交和训练提交
            beforeDispatchInitManager.initCommonSubmission(judgeDto.getPid(), judge);
        }

        // 将提交加入任务队列
        if (judgeDto.getIsRemote()) { // 如果是远程oj判题
            remoteJudgeDispatcher.sendTask(judge, judge.getDisplayPid(), isContestSubmission, false);
        } else {
            judgeDispatcher.sendTask(judge, isContestSubmission);
        }

        return judge;
    }


    /**
     * @MethodName resubmit
     * @Description 调用判题服务器提交失败超过60s后，用户点击按钮重新提交判题进入的方法
     * @Since 2021/2/12
     */
    @Transactional(rollbackFor = Exception.class)
    public Judge resubmit(Long submitId) throws StatusNotFoundException {

        Judge judge = judgeEntityService.getById(submitId);
        if (judge == null) {
            throw new StatusNotFoundException("此提交数据不存在！");
        }

        Problem problem = problemEntityService.getById(judge.getPid());

        // 如果是非比赛题目
        if (judge.getCid() == 0) {
            // 重判前，需要将该题目对应记录表一并更新
            // 如果该题已经是AC通过状态，更新该题目的用户ac做题表 user_acproblem
            if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus().intValue()) {
                QueryWrapper<UserAcproblem> userAcproblemQueryWrapper = new QueryWrapper<>();
                userAcproblemQueryWrapper.eq("submit_id", judge.getSubmitId());
                userAcproblemEntityService.remove(userAcproblemQueryWrapper);
            }
        } else {
            if (problem.getIsRemote()) {
                // 将对应比赛记录设置成默认值
                UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("submit_id", submitId).setSql("status=null,score=null");
                contestRecordEntityService.update(updateWrapper);
            } else {
                throw new StatusNotFoundException("错误！非vJudge题目在比赛过程无权限重新提交");
            }
        }

        boolean isHasSubmitIdRemoteRejudge = false;
        if (Objects.nonNull(judge.getVjudgeSubmitId()) &&
                (judge.getStatus().intValue() == Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus()
                        || judge.getStatus().intValue() == Constants.Judge.STATUS_PENDING.getStatus()
                        || judge.getStatus().intValue() == Constants.Judge.STATUS_JUDGING.getStatus()
                        || judge.getStatus().intValue() == Constants.Judge.STATUS_COMPILING.getStatus()
                        || judge.getStatus().intValue() == Constants.Judge.STATUS_SYSTEM_ERROR.getStatus())) {
            isHasSubmitIdRemoteRejudge = true;
        }

        // 重新进入等待队列
        judge.setStatus(Constants.Judge.STATUS_PENDING.getStatus());
        judge.setVersion(judge.getVersion() + 1);
        judge.setErrorMessage(null)
                .setOiRankScore(null)
                .setScore(null)
                .setTime(null)
                .setJudger("")
                .setMemory(null);
        judgeEntityService.updateById(judge);


        // 将提交加入任务队列
        if (problem.getIsRemote()) { // 如果是远程oj判题

            remoteJudgeDispatcher.sendTask(judge, problem.getProblemId(),
                    judge.getCid() != 0, isHasSubmitIdRemoteRejudge);
        } else {
            judgeDispatcher.sendTask(judge, judge.getCid() != 0);
        }
        return judge;
    }


    /**
     * @MethodName getSubmission
     * @Description 获取单个提交记录的详情
     * @Since 2021/1/2
     */
    public SubmissionInfoVo getSubmission(Long submitId) throws StatusNotFoundException, StatusAccessDeniedException {

        Judge judge = judgeEntityService.getById(submitId);
        if (judge == null) {
            throw new StatusNotFoundException("此提交数据不存在！");
        }

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员

        // 清空vj信息
        judge.setVjudgeUsername(null);
        judge.setVjudgeSubmitId(null);
        judge.setVjudgePassword(null);

        // 超级管理员与题目管理员有权限查看代码
        // 如果不是本人或者并未分享代码，则不可查看
        // 当此次提交代码不共享
        // 比赛提交只有比赛创建者和root账号可看代码

        SubmissionInfoVo submissionInfoVo = new SubmissionInfoVo();

        if (judge.getCid() != 0) {
            if (userRolesVo == null) {
                throw new StatusAccessDeniedException("请先登录！");
            }
            Contest contest = contestEntityService.getById(judge.getCid());
            if (!isRoot && !userRolesVo.getUid().equals(contest.getUid())
                    && !(judge.getGid() != null && groupValidator.isGroupRoot(userRolesVo.getUid(), judge.getGid()))) {
                // 如果是比赛,那么还需要判断是否为封榜,比赛管理员和超级管理员可以有权限查看(ACM题目除外)
                if (contest.getType().intValue() == Constants.Contest.TYPE_OI.getCode()
                        && contestValidator.isSealRank(userRolesVo.getUid(), contest, true, false)) {
                    submissionInfoVo.setSubmission(new Judge().setStatus(Constants.Judge.STATUS_SUBMITTED_UNKNOWN_RESULT.getStatus()));
                    return submissionInfoVo;
                }
                // 不是本人的话不能查看代码、时间，空间，长度
                if (!userRolesVo.getUid().equals(judge.getUid())) {
                    judge.setCode(null);
                    // 如果还在比赛时间，不是本人不能查看时间，空间，长度，错误提示信息
                    if (contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()) {
                        judge.setTime(null);
                        judge.setMemory(null);
                        judge.setLength(null);
                        judge.setErrorMessage("The contest is in progress. You are not allowed to view other people's error information.");
                    }
                }
            }
        } else {
            boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");// 是否为题目管理员
            if (!judge.getShare() && !isRoot && !isProblemAdmin
                    && !(judge.getGid() != null && groupValidator.isGroupRoot(userRolesVo.getUid(), judge.getGid()))) {
                if (userRolesVo != null) { // 当前是登陆状态
                    // 需要判断是否为当前登陆用户自己的提交代码
                    if (!judge.getUid().equals(userRolesVo.getUid())) {
                        judge.setCode(null);
                    }
                } else { // 不是登陆状态，就直接无权限查看代码
                    judge.setCode(null);
                }
            }
        }

        Problem problem = problemEntityService.getById(judge.getPid());

        // 只允许用户查看ce错误,sf错误，se错误信息提示
        if (judge.getStatus().intValue() != Constants.Judge.STATUS_COMPILE_ERROR.getStatus() &&
                judge.getStatus().intValue() != Constants.Judge.STATUS_SYSTEM_ERROR.getStatus() &&
                judge.getStatus().intValue() != Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus()) {
            judge.setErrorMessage("The error message does not support viewing.");
        }
        submissionInfoVo.setSubmission(judge);
        submissionInfoVo.setCodeShare(problem.getCodeShare());

        return submissionInfoVo;

    }

    /**
     * @MethodName updateSubmission
     * @Description 修改单个提交详情的分享权限
     * @Since 2021/1/2
     */
    public void updateSubmission(Judge judge) throws StatusForbiddenException, StatusFailException {

        // 需要获取一下该token对应用户的数据
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        if (!userRolesVo.getUid().equals(judge.getUid())) { // 判断该提交是否为当前用户的
            throw new StatusForbiddenException("对不起，您不能修改他人的代码分享权限！");
        }

        Judge judgeInfo = judgeEntityService.getById(judge.getSubmitId());
        if (judgeInfo.getCid() != 0) { // 如果是比赛提交，不可分享！
            throw new StatusForbiddenException("对不起，您不能分享比赛题目的提交代码！");
        }
        judgeInfo.setShare(judge.getShare());
        boolean isOk = judgeEntityService.updateById(judgeInfo);
        if (!isOk) {
            throw new StatusFailException("修改代码权限失败！");
        }
    }

    /**
     * @MethodName getJudgeList
     * @Description 通用查询判题记录列表
     * @Since 2020/10/29
     */
    public IPage<JudgeVo> getJudgeList(Integer limit,
                                       Integer currentPage,
                                       Boolean onlyMine,
                                       String searchPid,
                                       Integer searchStatus,
                                       String searchUsername,
                                       Boolean completeProblemID,
                                       Long gid) throws StatusAccessDeniedException {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        String uid = null;
        // 只查看当前用户的提交
        if (onlyMine) {
            // 需要获取一下该token对应用户的数据（有token便能获取到）
            Session session = SecurityUtils.getSubject().getSession();
            UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

            if (userRolesVo == null) {
                throw new StatusAccessDeniedException("当前用户数据为空，请您重新登陆！");
            }
            uid = userRolesVo.getUid();
        }
        if (searchPid != null) {
            searchPid = searchPid.trim();
        }
        if (searchUsername != null) {
            searchUsername = searchUsername.trim();
        }

        return judgeEntityService.getCommonJudgeList(limit,
                currentPage,
                searchPid,
                searchStatus,
                searchUsername,
                uid,
                completeProblemID,
                gid);
    }


    /**
     * @MethodName checkJudgeResult
     * @Description 对提交列表状态为Pending和Judging的提交进行更新检查
     * @Since 2021/1/3
     */
    public HashMap<Long, Object> checkCommonJudgeResult(SubmitIdListDto submitIdListDto) {

        List<Long> submitIds = submitIdListDto.getSubmitIds();

        if (CollectionUtils.isEmpty(submitIds)) {
            return new HashMap<>();
        }

        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        // lambada表达式过滤掉code
        queryWrapper.select(Judge.class, info -> !info.getColumn().equals("code")).in("submit_id", submitIds);
        List<Judge> judgeList = judgeEntityService.list(queryWrapper);
        HashMap<Long, Object> result = new HashMap<>();
        for (Judge judge : judgeList) {
            judge.setCode(null);
            judge.setErrorMessage(null);
            judge.setVjudgeUsername(null);
            judge.setVjudgeSubmitId(null);
            judge.setVjudgePassword(null);
            result.put(judge.getSubmitId(), judge);
        }
        return result;
    }

    /**
     * @MethodName checkContestJudgeResult
     * @Description 需要检查是否为封榜，是否可以查询结果，避免有人恶意查询
     * @Since 2021/6/11
     */
    public HashMap<Long, Object> checkContestJudgeResult(SubmitIdListDto submitIdListDto) throws StatusNotFoundException {

        if (submitIdListDto.getCid() == null) {
            throw new StatusNotFoundException("查询比赛id不能为空");
        }

        if (CollectionUtils.isEmpty(submitIdListDto.getSubmitIds())) {
            return new HashMap<>();
        }

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        boolean isRoot = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员

        Contest contest = contestEntityService.getById(submitIdListDto.getCid());

        boolean isContestAdmin = isRoot
                || userRolesVo.getUid().equals(contest.getUid())
                || (contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()));
        // 如果是封榜时间且不是比赛管理员和超级管理员
        boolean isSealRank = contestValidator.isSealRank(userRolesVo.getUid(), contest, true, isRoot);

        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        // lambada表达式过滤掉code
        queryWrapper.select(Judge.class, info -> !info.getColumn().equals("code"))
                .in("submit_id", submitIdListDto.getSubmitIds())
                .eq("cid", submitIdListDto.getCid())
                .between(isSealRank, "submit_time", contest.getStartTime(), contest.getSealRankTime());
        List<Judge> judgeList = judgeEntityService.list(queryWrapper);
        HashMap<Long, Object> result = new HashMap<>();
        for (Judge judge : judgeList) {
            judge.setCode(null);
            judge.setDisplayPid(null);
            judge.setErrorMessage(null);
            judge.setVjudgeUsername(null);
            judge.setVjudgeSubmitId(null);
            judge.setVjudgePassword(null);
            if (!judge.getUid().equals(userRolesVo.getUid()) && !isContestAdmin) {
                judge.setTime(null);
                judge.setMemory(null);
                judge.setLength(null);
            }
            result.put(judge.getSubmitId(), judge);
        }
        return result;
    }


    /**
     * @MethodName getJudgeCase
     * @Description 获得指定提交id的测试样例结果，暂不支持查看测试数据，只可看测试点结果，时间，空间，或者IO得分
     * @Since 2020/10/29
     */
    @GetMapping("/get-all-case-result")
    public List<JudgeCase> getALLCaseResult(Long submitId) throws StatusNotFoundException, StatusForbiddenException {

        Judge judge = judgeEntityService.getById(submitId);

        if (judge == null) {
            throw new StatusNotFoundException("此提交数据不存在！");
        }

        Problem problem = problemEntityService.getById(judge.getPid());

        // 如果该题不支持开放测试点结果查看
        if (!problem.getOpenCaseResult()) {
            return null;
        }

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        boolean isRoot = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员

        if (judge.getCid() != 0 && userRolesVo != null && !isRoot) {
            Contest contest = contestEntityService.getById(judge.getCid());
            // 如果不是比赛管理员 比赛封榜不能看
            if (!contest.getUid().equals(userRolesVo.getUid()) ||
                    (contest.getIsGroup() && !groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()))) {
                // 当前是比赛期间 同时处于封榜时间
                if (contest.getSealRank() && contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()
                        && contest.getSealRankTime().before(new Date())) {
                    throw new StatusForbiddenException("对不起，该题测试样例详情不能查看！");
                }

                // 若是比赛题目，只支持OI查看测试点情况，ACM强制禁止查看,比赛管理员除外
                if (problem.getType().intValue() == Constants.Contest.TYPE_ACM.getCode()) {
                    throw new StatusForbiddenException("对不起，该题测试样例详情不能查看！");
                }
            }
        }


        QueryWrapper<JudgeCase> wrapper = new QueryWrapper<>();


        if (userRolesVo == null || (!isRoot
                && !SecurityUtils.getSubject().hasRole("admin")
                && !SecurityUtils.getSubject().hasRole("problem_admin"))) {
            wrapper.select("time", "memory", "score", "status", "user_output");
        }
        wrapper.eq("submit_id", submitId)
                .last("order by length(input_data) asc,input_data asc");

        // 当前所有测试点只支持 空间 时间 状态码 IO得分 和错误信息提示查看而已
        return judgeCaseEntityService.list(wrapper);
    }
}
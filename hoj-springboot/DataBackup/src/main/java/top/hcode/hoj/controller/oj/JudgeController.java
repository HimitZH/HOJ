package top.hcode.hoj.controller.oj;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.JudgeMapper;
import top.hcode.hoj.judge.remote.RemoteJudgeDispatcher;
import top.hcode.hoj.judge.self.JudgeDispatcher;
import top.hcode.hoj.pojo.dto.SubmitIdListDto;
import top.hcode.hoj.pojo.dto.ToJudgeDto;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;

import top.hcode.hoj.service.ProblemService;
import top.hcode.hoj.service.impl.*;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.IpUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 20:52
 * @Description: 处理代码评判相关业务
 */
@RestController
@RequestMapping("/api")
@RefreshScope
public class JudgeController {


    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private JudgeMapper judgeMapper;

    @Autowired
    private JudgeCaseServiceImpl judgeCaseService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private ContestProblemServiceImpl contestProblemService;

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ContestRecordServiceImpl contestRecordService;

    @Autowired
    private UserAcproblemServiceImpl userAcproblemService;

    @Value("${hoj.judge.token}")
    private String judgeToken;

    @Autowired
    private JudgeDispatcher judgeDispatcher;

    @Autowired
    private RemoteJudgeDispatcher remoteJudgeDispatcher;


    /**
     * @MethodName submitProblemJudge
     * @Params * @param null
     * @Description 核心方法 判题通过openfeign调用判题系统服务
     * @Return CommonResult
     * @Since 2020/10/30
     */
    @RequiresAuthentication
    @RequiresPermissions("submit")
    @RequestMapping(value = "/submit-problem-judge", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public CommonResult submitProblemJudge(@RequestBody ToJudgeDto judgeDto, HttpServletRequest request) {

        // 需要获取一下该token对应用户的数据
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 将提交先写入数据库，准备调用判题服务器
        Judge judge = new Judge();
        judge.setShare(false) // 默认设置代码为单独自己可见
                .setCode(judgeDto.getCode())
                .setCid(judgeDto.getCid())
                .setLanguage(judgeDto.getLanguage())
                .setLength(judgeDto.getCode().length())
                .setUid(userRolesVo.getUid())
                .setUsername(userRolesVo.getUsername())
                .setStatus(Constants.Judge.STATUS_PENDING.getStatus()) // 开始进入判题队列
                .setSubmitTime(new Date())
                .setVersion(0)
                .setIp(IpUtils.getUserIpAddr(request));
        boolean updateContestRecord = true;
        int result = 0;
        // 如果比赛id不等于0，则说明为比赛提交，需要查询对应的contest_problem的主键
        if (judgeDto.getCid() != 0) {

            // 首先判断一下比赛的状态是否是正在进行，结束状态都不能提交，比赛前比赛管理员可以提交
            Contest contest = contestService.getById(judgeDto.getCid());

            if (contest == null) {
                return CommonResult.errorResponse("对不起，该比赛不存在！");
            }

            if (contest.getStatus().intValue() == Constants.Contest.STATUS_ENDED.getCode()) {
                return CommonResult.errorResponse("比赛已结束，不可再提交！");
            }
            // 是否为超级管理员或者该比赛的创建者，则为比赛管理者
            boolean root = SecurityUtils.getSubject().hasRole("root");
            if (!root && !contest.getUid().equals(userRolesVo.getUid())) {
                if (contest.getStatus().intValue() == Constants.Contest.STATUS_SCHEDULED.getCode()) {
                    return CommonResult.errorResponse("比赛未开始，不可提交！");
                }
                // 需要检查是否有权限在当前比赛进行提交
                CommonResult checkResult = contestService.checkJudgeAuth(contest, userRolesVo.getUid());
                if (checkResult != null) {
                    return checkResult;
                }
            }

            // 查询获取对应的pid和cpid
            QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
            contestProblemQueryWrapper.eq("cid", judgeDto.getCid()).eq("display_id", judgeDto.getPid());
            ContestProblem contestProblem = contestProblemService.getOne(contestProblemQueryWrapper);
            judge.setCpid(contestProblem.getId())
                    .setPid(contestProblem.getPid());

            Problem problem = problemService.getById(contestProblem.getPid());
            if (problem.getAuth() == 2) {
                return CommonResult.errorResponse("错误！当前题目不可提交！", CommonResult.STATUS_FORBIDDEN);
            }
            judge.setDisplayPid(problem.getProblemId());

            // 将新提交数据插入数据库
            result = judgeMapper.insert(judge);

            // 管理员比赛前的提交不纳入记录
            if (contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()) {
//               方法废弃，为了考虑比赛重测问题，不使用字段记录是否为first AC，在排行榜计算自有排序解决first AC的判断
//                // 先查询是否为首次可能AC提交
//                QueryWrapper<ContestRecord> queryWrapper = new QueryWrapper<>();
//                queryWrapper.eq("cid", judge.getCid())
//                        .eq("status", Constants.Contest.RECORD_AC.getCode())
//                        .lt("submit_time", judge.getSubmitTime())
//                        .eq("pid", judge.getPid());
//
//                boolean FirstACAttempt = contestRecordService.count(queryWrapper) == 0;

                // 同时初始化写入contest_record表
                ContestRecord contestRecord = new ContestRecord();
                contestRecord.setDisplayId(judgeDto.getPid())
                        .setCpid(contestProblem.getId())
                        .setSubmitId(judge.getSubmitId())
                        .setPid(judge.getPid())
                        .setUsername(userRolesVo.getUsername())
                        .setRealname(userRolesVo.getRealname())
                        .setUid(userRolesVo.getUid())
                        .setCid(judge.getCid())
                        .setSubmitTime(judge.getSubmitTime())
                        // 设置比赛开始时间到提交时间之间的秒数
                        .setTime(DateUtil.between(contest.getStartTime(), judge.getSubmitTime(), DateUnit.SECOND));
//                if (FirstACAttempt) {
//                    contestRecord.setFirstBlood(true);
//                } else {
//                    contestRecord.setFirstBlood(false);
//                }
                updateContestRecord = contestRecordService.saveOrUpdate(contestRecord);
            }

        } else { // 如果不是比赛提交

            QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
            problemQueryWrapper.eq("problem_id", judgeDto.getPid());
            Problem problem = problemService.getOne(problemQueryWrapper);

            if (problem.getAuth() == 2) {
                return CommonResult.errorResponse("错误！当前题目不可提交！", CommonResult.STATUS_FORBIDDEN);
            }

            judge.setCpid(0L).setPid(problem.getId()).setDisplayPid(problem.getProblemId());

            // 将新提交数据插入数据库
            result = judgeMapper.insert(judge);
        }

        if (result != 1 || !updateContestRecord) {
            return CommonResult.errorResponse("代码提交失败！", CommonResult.STATUS_ERROR);
        }

        // 将提交加入任务队列
        if (judgeDto.getIsRemote()) { // 如果是远程oj判题
            remoteJudgeDispatcher.sendTask(judge, judgeToken, judge.getDisplayPid(), judge.getCid() != 0, 1);

        } else {
            judgeDispatcher.sendTask(judge, judgeToken, judge.getCid() == 0, 1);
        }

        return CommonResult.successResponse(judge, "代码提交成功！");
    }


    /**
     * @MethodName resubmit
     * @Params * @param null
     * @Description 调用判题服务器提交失败超过60s后，用户点击按钮重新提交判题进入的方法
     * @Return
     * @Since 2021/2/12
     */
    @RequiresAuthentication
    @GetMapping(value = "/resubmit")
    @Transactional
    public CommonResult resubmit(@RequestParam("submitId") Long submitId,
                                 HttpServletRequest request) {

        Judge judge = judgeService.getById(submitId);
        if (judge == null) {
            return CommonResult.errorResponse("此提交数据不存在！");
        }
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        if (!judge.getUid().equals(userRolesVo.getUid())) { // 不是本人无法重新提交
            return CommonResult.errorResponse("对不起！您并非提交数据的本人，无法重新提交！");
        }

        Problem problem = problemService.getById(judge.getPid());

        // 如果是非比赛题目
        if (judge.getCid() == 0) {
            // 重判前，需要将该题目对应记录表一并更新
            // 如果该题已经是AC通过状态，更新该题目的用户ac做题表 user_acproblem
            if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus().intValue()) {
                QueryWrapper<UserAcproblem> userAcproblemQueryWrapper = new QueryWrapper<>();
                userAcproblemQueryWrapper.eq("submit_id", judge.getSubmitId());
                userAcproblemService.remove(userAcproblemQueryWrapper);
            }
        } else {
            if (problem.getIsRemote()) {
                // 将对应比赛记录设置成默认值
                UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("submit_id", submitId).setSql("status=null,score=null");
                contestRecordService.update(updateWrapper);
            } else {
                return CommonResult.errorResponse("错误！非vJudge题目在比赛过程无权限重新提交");
            }
        }

        // 重新进入等待队列
        judge.setStatus(Constants.Judge.STATUS_PENDING.getStatus());
        judge.setVersion(judge.getVersion() + 1);
        judge.setErrorMessage(null);
        judgeService.updateById(judge);
        // 将提交加入任务队列
        if (problem.getIsRemote()) { // 如果是远程oj判题
            remoteJudgeDispatcher.sendTask(judge, judgeToken, problem.getProblemId(),
                    judge.getCid() != 0, 1);
        } else {
            judgeDispatcher.sendTask(judge, judgeToken, judge.getCid() != 0, 1);
        }
        return CommonResult.successResponse(judge, "重新提交成功！");
    }


    /**
     * @MethodName getSubmission
     * @Params * @param null
     * @Description 获取单个提交记录的详情
     * @Return CommonResult
     * @Since 2021/1/2
     */
    @GetMapping("/submission")
    public CommonResult getSubmission(@RequestParam(value = "submitId", required = true) Long submitId, HttpServletRequest request) {
        Judge judge = judgeService.getById(submitId);
        if (judge == null) {
            return CommonResult.errorResponse("此提交数据不存在！");
        }

        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");


        boolean root = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员

        HashMap<String, Object> result = new HashMap<>();
        if (judge.getCid() != 0 && !root) {
            Contest contest = contestService.getById(judge.getCid());
            if (!userRolesVo.getUid().equals(contest.getUid()) && contest.getSealRank()
                    && contest.getType().intValue() == Constants.Contest.TYPE_OI.getCode()
                    && contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()
                    && contest.getSealRankTime().before(new Date())) {
                result.put("submission", new Judge().setStatus(Constants.Judge.STATUS_SUBMITTED_UNKNOWN_RESULT.getStatus()));
                return CommonResult.successResponse(result, "获取提交数据成功！");
            }
        }

        // 超级管理员与管理员有权限查看代码
        // 如果不是本人或者并未分享代码，则不可查看
        // 当此次提交代码不共享
        // 比赛提交只有比赛创建者和root账号可看代码
        if (judge.getCid() != 0){

            Contest contest = contestService.getById(judge.getCid());
            if (!root&&!userRolesVo.getUid().equals(contest.getUid())) {
                // 如果是比赛,那么还需要判断是否为封榜,比赛管理员和超级管理员可以有权限查看(ACM题目除外)
                if(contest.getSealRank()
                        && contest.getType().intValue() == Constants.Contest.TYPE_OI.getCode()
                        && contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()
                        && contest.getSealRankTime().before(new Date())) {
                    result.put("submission", new Judge().setStatus(Constants.Judge.STATUS_SUBMITTED_UNKNOWN_RESULT.getStatus()));
                    return CommonResult.successResponse(result, "获取提交数据成功！");
                }
                judge.setCode(null);
            }
        }else {
            boolean admin = SecurityUtils.getSubject().hasRole("admin")
                    || SecurityUtils.getSubject().hasRole("problem_admin");// 是否为管理员
            if (!judge.getShare() && !root && !admin) {
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

        Problem problem = problemService.getById(judge.getPid());

        // 只允许用户查看ce错误,sf错误，se错误信息提示
        if (judge.getStatus().intValue() != Constants.Judge.STATUS_COMPILE_ERROR.getStatus() &&
                judge.getStatus().intValue() != Constants.Judge.STATUS_SYSTEM_ERROR.getStatus() &&
                judge.getStatus().intValue() != Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus()) {
            judge.setErrorMessage("The error message does not support viewing.");
        }
        result.put("submission", judge);
        result.put("codeShare", problem.getCodeShare());

        return CommonResult.successResponse(result, "获取提交数据成功！");

    }


    /**
     * @MethodName updateSubmission
     * @Params * @param null
     * @Description 修改单个提交详情的分享权限
     * @Return CommonResult
     * @Since 2021/1/2
     */
    @PutMapping("/submission")
    @RequiresAuthentication
    public CommonResult updateSubmission(@RequestBody Judge judge, HttpServletRequest request) {
        // 需要获取一下该token对应用户的数据
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        if (!userRolesVo.getUid().equals(judge.getUid())) { // 判断该提交是否为当前用户的
            return CommonResult.errorResponse("对不起，您不能修改他人的代码分享权限！");
        }
        Judge judgeInfo = judgeService.getById(judge.getSubmitId());
        if (judgeInfo.getCid() != 0) { // 如果是比赛提交，不可分享！
            return CommonResult.errorResponse("对不起，您不能分享比赛题目的提交代码！");
        }
        judgeInfo.setShare(judge.getShare());
        boolean result = judgeService.updateById(judgeInfo);
        if (result) {
            if (judge.getShare()) {
                return CommonResult.successResponse(null, "设置代码公开成功！");
            } else {
                return CommonResult.successResponse(null, "设置代码隐藏成功！");
            }
        } else {
            return CommonResult.errorResponse("修改代码权限失败！");
        }
    }

    /**
     * @MethodName getJudgeList
     * @Params * @param null
     * @Description 通用查询判题记录列表
     * @Return CommonResult
     * @Since 2020/10/29
     */
    @RequestMapping(value = "/submissions", method = RequestMethod.GET)
    public CommonResult getJudgeList(@RequestParam(value = "limit", required = false) Integer limit,
                                     @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                     @RequestParam(value = "onlyMine", required = false) Boolean onlyMine,
                                     @RequestParam(value = "problemID", required = false) String searchPid,
                                     @RequestParam(value = "status", required = false) Integer searchStatus,
                                     @RequestParam(value = "username", required = false) String searchUsername,
                                     HttpServletRequest request) {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        String uid = null;
        // 只查看当前用户的提交
        if (onlyMine) {
            // 需要获取一下该token对应用户的数据（有token便能获取到）
            HttpSession session = request.getSession();
            UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

            if (userRolesVo == null) {
                return CommonResult.errorResponse("当前用户数据为空，请您重新登陆！", CommonResult.STATUS_ACCESS_DENIED);
            }
            uid = userRolesVo.getUid();
        }
        if (searchPid != null) {
            searchPid = searchPid.trim();
        }
        if (searchUsername != null) {
            searchUsername = searchUsername.trim();
        }

        IPage<JudgeVo> commonJudgeList = judgeService.getCommonJudgeList(limit, currentPage, searchPid,
                searchStatus, searchUsername, uid);

        if (commonJudgeList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(null, "暂无数据");
        } else {
            return CommonResult.successResponse(commonJudgeList, "获取成功");
        }
    }


    /**
     * @MethodName checkJudgeResult
     * @Params * @param null
     * @Description 对提交列表状态为Pending和Judging的提交进行更新检查
     * @Return
     * @Since 2021/1/3
     */
    @RequestMapping(value = "/check-submissions-status", method = RequestMethod.POST)
    public CommonResult checkCommonJudgeResult(@RequestBody SubmitIdListDto submitIdListDto) {
        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        // lambada表达式过滤掉code
        queryWrapper.select(Judge.class, info -> !info.getColumn().equals("code")).in("submit_id", submitIdListDto.getSubmitIds());
        List<Judge> judgeList = judgeService.list(queryWrapper);
        HashMap<Long, Object> result = new HashMap<>();
        for (Judge judge : judgeList) {
            result.put(judge.getSubmitId(), judge);
        }
        return CommonResult.successResponse(result, "获取最新判题数据成功！");
    }

    /**
     * @MethodName checkContestJudgeResult
     * @Params * @param submitIdListDto
     * @Description 需要检查是否为封榜，是否可以查询结果，避免有人恶意查询
     * @Return
     * @Since 2021/6/11
     */
    @RequestMapping(value = "/check-contest-submissions-status", method = RequestMethod.POST)
    @RequiresAuthentication
    public CommonResult checkContestJudgeResult(@RequestBody SubmitIdListDto submitIdListDto, HttpServletRequest request) {
        if (submitIdListDto.getCid() == null) {
            return CommonResult.errorResponse("查询比赛ID不能为空");
        }

        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        boolean root = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员

        Contest contest = contestService.getById(submitIdListDto.getCid());

        boolean isSealRank = false;
        // 如果是封榜时间且不是比赛管理员和超级管理员
        if (contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode() &&
                contest.getSealRank() && contest.getSealRankTime().before(new Date()) &&
                !root && !userRolesVo.getUid().equals(contest.getUid())) {
            isSealRank = true;
        }

        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        // lambada表达式过滤掉code
        queryWrapper.select(Judge.class, info -> !info.getColumn().equals("code"))
                .in("submit_id", submitIdListDto.getSubmitIds())
                .eq("cid", submitIdListDto.getCid())
                .between(isSealRank, "submit_time", contest.getStartTime(), contest.getSealRankTime());
        List<Judge> judgeList = judgeService.list(queryWrapper);
        HashMap<Long, Object> result = new HashMap<>();
        for (Judge judge : judgeList) {
            result.put(judge.getSubmitId(), judge);
        }
        return CommonResult.successResponse(result, "获取最新判题数据成功！");
    }


    /**
     * @MethodName getJudgeCase
     * @Params * @param null
     * @Description 获得指定提交id的测试样例结果，暂不支持查看测试数据，只可看测试点结果，时间，空间，或者IO得分
     * @Return
     * @Since 2020/10/29
     */
    @GetMapping("/get-all-case-result")
    public CommonResult getALLCaseResult(@RequestParam(value = "submitId", required = true) Long submitId, HttpServletRequest request) {

        Judge judge = judgeService.getById(submitId);

        if (judge == null) {
            return CommonResult.errorResponse("此提交数据不存在！");
        }

        Problem problem = problemService.getById(judge.getPid());

        // 如果该题不支持开放测试点结果查看
        if (!problem.getOpenCaseResult()) {
            return CommonResult.successResponse(null, "对不起，该题测试样例详情不支持开放！");
        }

        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        boolean root = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员

        if (judge.getCid() != 0 && userRolesVo != null && !root) {
            Contest contest = contestService.getById(judge.getCid());
            // 如果不是比赛管理员 比赛封榜不能看
            if (!contest.getUid().equals(userRolesVo.getUid())) {
                // 当前是比赛期间 同时处于封榜时间
                if (contest.getSealRank() && contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()
                        && contest.getSealRankTime().before(new Date())) {
                    return CommonResult.successResponse(null, "对不起，该题测试样例详情不能查看！");
                }

                // 若是比赛题目，只支持OI查看测试点情况，ACM强制禁止查看,比赛管理员除外
                if (problem.getType().intValue() == Constants.Contest.TYPE_ACM.getCode()) {
                    return CommonResult.successResponse(null, "对不起，该题测试样例详情不能查看！");
                }
            }
        }


        QueryWrapper<JudgeCase> wrapper = new QueryWrapper<JudgeCase>().eq("submit_id", submitId).orderByAsc("case_id");

        // 当前所有测试点并未记录输出输出数据，暂时只支持 空间 时间 状态码 IO得分查看而已
        List<JudgeCase> judgeCaseList = judgeCaseService.list(wrapper);

        if (judgeCaseList.isEmpty()) { // 未查询到一条数据
            return CommonResult.successResponse(null, "暂无数据");
        } else {
            return CommonResult.successResponse(judgeCaseList, "获取成功");
        }
    }
}
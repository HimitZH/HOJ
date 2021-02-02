package top.hcode.hoj.controller.oj;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.JudgeCaseMapper;
import top.hcode.hoj.dao.JudgeMapper;
import top.hcode.hoj.pojo.dto.SubmitIdListDto;
import top.hcode.hoj.pojo.dto.ToJudgeDto;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.JudgeService;
import top.hcode.hoj.service.ProblemService;
import top.hcode.hoj.service.ToJudgeService;
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
public class JudgeController {


    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private JudgeMapper judgeMapper;

    @Autowired
    private JudgeCaseMapper judgeCaseMapper;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private ToJudgeService toJudgeService;

    @Autowired
    private UserRecordServiceImpl userRecordService;

    @Autowired
    private ContestProblemServiceImpl contestProblemService;

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ContestRecordServiceImpl contestRecordService;

//    @Autowired
//    private RestTemplate restTemplate;

//    @Value("${service-url.hoj-judge-server}") // restTemplate风格调用不使用
//    private String REST_URL_PREFIX;

//    @GetMapping("/submit-problem-judge")
//    public String list(){
//        return restTemplate.getForObject(REST_URL_PREFIX+"/hoj-judge-server/submit-problem-judge", String.class);
//    }

    /**
     * @MethodName submitProblemJudge
     * @Params * @param null
     * @Description 核心方法 判题通过openfeign调用判题系统服务
     * @Return CommonResult
     * @Since 2020/10/30
     */
    @RequiresAuthentication
    @RequestMapping(value = "/submit-problem-judge", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
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

        boolean updateUserRecord = true;
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
                return CommonResult.errorResponse("比赛未开始，不可提交！");
            }

            // 查询获取对应的pid和cpid
            QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
            contestProblemQueryWrapper.eq("cid", judgeDto.getCid()).eq("display_id", judgeDto.getPid());
            ContestProblem contestProblem = contestProblemService.getOne(contestProblemQueryWrapper);
            judge.setCpid(contestProblem.getId()).setPid(contestProblem.getPid());

            // 将新提交数据插入数据库
            result = judgeMapper.insert(judge);

            // 管理员比赛前的提交不纳入记录
            if (contest.getStatus().intValue() == Constants.Contest.STATUS_SCHEDULED.getCode()) {
                // 先查询是否为首次可能AC提交
                QueryWrapper<ContestRecord> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("cid", judge.getCid())
                        .eq("status", Constants.Contest.RECORD_AC.getCode())
                        .eq("uid", judge.getUid())
                        .eq("pid", judge.getPid())
                        .eq("cpid", judge.getCpid());

                boolean FirstACAttempt = contestRecordService.count(queryWrapper) == 0;

                // 同时初始化写入contest_record表
                ContestRecord contestRecord = new ContestRecord();
                contestRecord.setDisplayId(judgeDto.getPid())
                        .setCpid(contestProblem.getPid())
                        .setSubmitId(judge.getSubmitId())
                        .setPid(judge.getPid())
                        .setUsername(userRolesVo.getUsername())
                        .setRealname(userRolesVo.getRealname())
                        .setUid(userRolesVo.getUid())
                        .setCid(judge.getCid())
                        .setSubmitTime(judge.getSubmitTime())
                        // 设置比赛开始时间到提交时间之间的秒数
                        .setTime(DateUtil.between(contest.getStartTime(), judge.getSubmitTime(), DateUnit.SECOND));
                if (FirstACAttempt) {
                    contestRecord.setFirstBlood(true);
                } else {
                    contestRecord.setFirstBlood(false);
                }
                updateContestRecord = contestRecordService.save(contestRecord);
            }

        } else { // 如果不是比赛提交，需要将题号转为long类型
            if (NumberUtil.isNumber(judgeDto.getPid())) {
                judge.setCpid(0L).setPid(Long.valueOf(judgeDto.getPid()));
            } else {
                return CommonResult.errorResponse("参数错误！提交评测失败！", CommonResult.STATUS_ERROR);
            }

            // 更新一下user_record表
            UpdateWrapper<UserRecord> userRecordUpdateWrapper = new UpdateWrapper<>();
            userRecordUpdateWrapper.setSql("submissions=submissions+1").eq("uid", judge.getUid());
            updateUserRecord = userRecordService.update(userRecordUpdateWrapper);
            // 将新提交数据插入数据库
            result = judgeMapper.insert(judge);
        }

        if (result != 1 || !updateContestRecord || !updateUserRecord) {
            return CommonResult.errorResponse("数据提交失败", CommonResult.STATUS_ERROR);
        }
        // 异步调用判题机
        toJudgeService.submitProblemJudge(judge);

        return CommonResult.successResponse(judge, "数据提交成功！");
    }


    /**
     * @MethodName getSubmission
     * @Params * @param null
     * @Description 获取单个提交记录的详情
     * @Return CommonResult
     * @Since 2021/1/2
     */
    @GetMapping("/submission")
    @RequiresAuthentication
    public CommonResult getSubmission(@RequestParam(value = "submitId", required = true) Long submitId, HttpServletRequest request) {
        Judge judge = judgeService.getById(submitId);
        if (judge == null) {
            return CommonResult.errorResponse("获取提交数据失败！");
        }

        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        // 超级管理员与管理员有权限查看代码
        // 如果不是本人或者并未分享代码，则不可查看
        // 当此次提交代码不共享
        boolean root = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员
        boolean admin = SecurityUtils.getSubject().hasRole("admin");// 是否为管理员

        if (!judge.getShare() && !root && !admin) {
            if (userRolesVo != null) { // 当前是登陆状态
                // 需要判断是否为当前登陆用户自己的提交代码
                if (!judge.getUid().equals(userRolesVo.getUid())) {
                    return CommonResult.errorResponse("对不起！该提交并未分享，您无权查看！");
                }
            } else { // 不是登陆状态，就直接无权限
                return CommonResult.errorResponse("对不起！该提交并未分享，您无权查看！");
            }
        }

        Problem problem = problemService.getById(judge.getPid());
        HashMap<String, Object> result = new HashMap<>();
        result.put("submission", judge);
        result.put("codeShare", problem.getCodeShare());

        return CommonResult.successResponse(result, "获取提交数据成功！");

    }

    @PutMapping("/submission")
    @RequiresAuthentication
    public CommonResult updateSubmission(@RequestBody Judge judge, HttpServletRequest request) {
        // 需要获取一下该token对应用户的数据
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        if (!userRolesVo.getUid().equals(judge.getUid())) { // 判断该提交是否为当前用户的
            return CommonResult.errorResponse("对不起，您不能修改他人的代码分享权限！");
        }
        if (judge.getCid() == 0) { // 如果是比赛提交，不可分享！
            return CommonResult.errorResponse("对不起，您不能分享比赛题目的提交代码！");
        }
        boolean result = judgeService.updateById(judge);
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
                                     @RequestParam(value = "problemID", required = false) Long searchPid,
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
    public CommonResult checkJudgeResult(@RequestBody SubmitIdListDto submitIdListDto) {
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
     * @MethodName getJudgeCase
     * @Params * @param null
     * @Description 获得指定提交id的测试样例，前提是不能为比赛期间的题目！
     * @Return
     * @Since 2020/10/29
     */
    @GetMapping("/get-judge-case")
    public CommonResult getJudgeCase(@RequestParam(value = "submitId", required = true) Long submitId,
                                     @RequestParam(value = "pid", required = true) Long pid) {


        // 如果该题目是还属于比赛期间的题目，则禁止查看测试样例！
        Problem problem = problemService.getById(pid);

        if (problem.getAuth() == 3) { // 3为比赛期间的题目
            return CommonResult.errorResponse("对不起，该题测试样例不能查看！", CommonResult.STATUS_ACCESS_DENIED);
        }


        QueryWrapper<JudgeCase> wrapper = new QueryWrapper<JudgeCase>().eq("submit_id", submitId).orderByAsc("case_id");

        List<JudgeCase> judgeCaseList = judgeCaseMapper.selectList(wrapper);

        if (judgeCaseList.size() == 0) { // 未查询到一条数据
            return CommonResult.successResponse("暂无数据");
        } else {
            return CommonResult.successResponse(judgeCaseList, "获取成功");
        }
    }
}
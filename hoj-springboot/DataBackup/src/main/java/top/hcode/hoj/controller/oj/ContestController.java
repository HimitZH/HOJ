package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ContestPrintDto;
import top.hcode.hoj.pojo.dto.UserReadContestAnnouncementDto;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.entity.problem.*;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.pojo.entity.contest.*;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.service.common.impl.AnnouncementServiceImpl;
import top.hcode.hoj.service.contest.ContestRecordService;
import top.hcode.hoj.service.contest.impl.*;
import top.hcode.hoj.service.judge.impl.JudgeServiceImpl;
import top.hcode.hoj.service.problem.impl.*;
import top.hcode.hoj.utils.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 21:40
 * @Description: 处理比赛模块的相关数据请求
 */
@RestController
@RequestMapping("/api")
public class ContestController {

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ContestRecordService contestRecordService;

    @Autowired
    private ContestProblemServiceImpl contestProblemService;

    @Autowired
    private ContestAnnouncementServiceImpl contestAnnouncementService;

    @Autowired
    private AnnouncementServiceImpl announcementService;

    @Autowired
    private ContestRegisterServiceImpl contestRegisterService;

    @Autowired
    private ProblemServiceImpl problemService;

    @Autowired
    private ProblemTagServiceImpl problemTagService;

    @Autowired
    private TagServiceImpl tagService;

    @Autowired
    private LanguageServiceImpl languageService;

    @Autowired
    private ProblemLanguageServiceImpl problemLanguageService;


    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private CodeTemplateServiceImpl codeTemplateService;

    @Autowired
    private ContestPrintServiceImpl contestPrintService;

    /**
     * @MethodName getContestList
     * @Params * @param null
     * @Description 获取比赛列表分页数据
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @GetMapping("/get-contest-list")
    public CommonResult getContestList(@RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                       @RequestParam(value = "status", required = false) Integer status,
                                       @RequestParam(value = "type", required = false) Integer type,
                                       @RequestParam(value = "keyword", required = false) String keyword) {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        Page<ContestVo> contestList = contestService.getContestList(limit, currentPage, type, status, keyword);

        if (contestList.getTotal() == 0) {
            return CommonResult.successResponse(contestList, "暂无数据");
        } else {
            return CommonResult.successResponse(contestList, "获取成功");
        }
    }

    /**
     * @MethodName getContestInfo
     * @Params * @param null
     * @Description 获得指定比赛的详细信息
     * @Return
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-info")
    @RequiresAuthentication
    public CommonResult getContestInfo(@RequestParam(value = "cid", required = true) Long cid) {

        ContestVo contestInfo = contestService.getContestInfoById(cid);
        if (contestInfo == null) {
            return CommonResult.errorResponse("对不起，该比赛不存在!");
        }
        // 设置当前服务器系统时间
        contestInfo.setNow(new Date());

        return CommonResult.successResponse(contestInfo, "获取成功");
    }

    /**
     * @MethodName toRegisterContest
     * @Params * @param null
     * @Description 注册比赛
     * @Return
     * @Since 2020/10/28
     */
    @PostMapping("/register-contest")
    @RequiresAuthentication
    public CommonResult toRegisterContest(@RequestBody HashMap<String, Object> params, HttpServletRequest request) {

        String cidStr = (String) params.get("cid");
        String password = (String) params.get("password");
        if (StringUtils.isEmpty(cidStr) || StringUtils.isEmpty(password)) {
            return CommonResult.errorResponse("请求参数不能为空！");
        }

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        Long cid = Long.valueOf(cidStr);
        Contest contest = contestService.getById(cid);

        if (contest == null || !contest.getVisible()) {
            return CommonResult.errorResponse("对不起，该比赛不存在!");
        }

        if (!contest.getPwd().equals(password)) { // 密码不对
            return CommonResult.errorResponse("比赛密码错误，请重新输入！");
        }

        /**
         *
         *  需要校验当前比赛是否开启账号规则限制，如果有，需要对当前用户的用户名进行验证
         *
         */

        if (contest.getOpenAccountLimit()
                && !contestService.checkAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
            return CommonResult.errorResponse("对不起！本次比赛只允许特定账号规则的用户参赛！", CommonResult.STATUS_ACCESS_DENIED);
        }


        QueryWrapper<ContestRegister> wrapper = new QueryWrapper<ContestRegister>().eq("cid", cid)
                .eq("uid", userRolesVo.getUid());
        if (contestRegisterService.getOne(wrapper, false) != null) {
            return CommonResult.errorResponse("您已注册过该比赛，请勿重复注册！");
        }

        boolean result = contestRegisterService.saveOrUpdate(new ContestRegister()
                .setCid(cid)
                .setUid(userRolesVo.getUid()));

        if (!result) {
            return CommonResult.errorResponse("校验比赛密码失败，请稍后再试", CommonResult.STATUS_FAIL);
        } else {
            return CommonResult.successResponse(null, "进入比赛成功！");
        }
    }

    /**
     * @MethodName getContestAccess
     * @Params * @param null
     * @Description 获得指定私有比赛的访问权限或保护比赛的提交权限
     * @Return
     * @Since 2020/10/28
     */
    @RequiresAuthentication
    @GetMapping("/get-contest-access")
    public CommonResult getContestAccess(@RequestParam(value = "cid") Long cid, HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        QueryWrapper<ContestRegister> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid).eq("uid", userRolesVo.getUid());
        ContestRegister contestRegister = contestRegisterService.getOne(queryWrapper, false);

        boolean access = false;
        if (contestRegister != null) {
            access = true;
            Contest contest = contestService.getById(cid);
            if (contest == null || !contest.getVisible()) {
                return CommonResult.errorResponse("对不起，该比赛不存在!");
            }
            if (contest.getOpenAccountLimit()
                    && !contestService.checkAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
                access = false;
                contestRecordService.removeById(contestRegister.getId());
            }
        }

        HashMap<String, Object> result = new HashMap<>();
        result.put("access", access);
        return CommonResult.successResponse(result);
    }


    /**
     * @MethodName getContestProblem
     * @Params * @param null
     * @Description 获得指定比赛的题目列表
     * @Return
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-problem")
    @RequiresAuthentication
    public CommonResult getContestProblem(@RequestParam(value = "cid", required = true) Long cid, HttpServletRequest request) {

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        // 获取本场比赛的状态
        Contest contest = contestService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        /**
         *  需要对该比赛做判断，是否处于开始或结束状态才可以获取题目列表，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
         */
        CommonResult commonResult = contestService.checkContestAuth(contest, userRolesVo, isRoot);

        if (commonResult != null) {
            return commonResult;
        }
        List<ContestProblemVo> contestProblemList;
        boolean isAdmin = isRoot || contest.getAuthor().equals(userRolesVo.getUsername());
        // 如果比赛开启封榜
        if (contest.getSealRank() && contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode() &&
                contest.getSealRankTime().before(new Date())) {
            contestProblemList = contestProblemService.getContestProblemList(cid, contest.getStartTime(), contest.getEndTime(),
                    contest.getSealRankTime(), isAdmin, contest.getAuthor());
        } else {
            contestProblemList = contestProblemService.getContestProblemList(cid, contest.getStartTime(), contest.getEndTime(),
                    null, isAdmin, contest.getAuthor());
        }

        if (contestProblemList.size() == 0) {
            return CommonResult.successResponse(null, "暂无数据");
        } else {
            return CommonResult.successResponse(contestProblemList, "获取成功");
        }
    }

    @GetMapping("/get-contest-problem-details")
    @RequiresAuthentication
    public CommonResult getContestProblemDetails(@RequestParam(value = "cid", required = true) Long cid,
                                                 @RequestParam(value = "displayId", required = true) String displayId,
                                                 HttpServletRequest request) {

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        // 获取本场比赛的状态
        Contest contest = contestService.getById(cid);

        // 是否为超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        /**
         *  需要对该比赛做判断，是否处于开始或结束状态才可以获取题目，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
         */
        CommonResult commonResult = contestService.checkContestAuth(contest, userRolesVo, isRoot);

        if (commonResult != null) {
            return commonResult;
        }

        // 根据cid和displayId获取pid
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid).eq("display_id", displayId);
        ContestProblem contestProblem = contestProblemService.getOne(contestProblemQueryWrapper);

        if (contestProblem == null) {
            return CommonResult.errorResponse("该题目不存在", CommonResult.STATUS_NOT_FOUND);
        }

        //查询题目详情，题目标签，题目语言，题目做题情况

        Problem problem = problemService.getById(contestProblem.getPid());

        if (problem.getAuth() == 2) {
            return CommonResult.errorResponse("该比赛题目当前不可访问！", CommonResult.STATUS_FORBIDDEN);
        }

        // 设置比赛题目的标题为设置展示标题
        problem.setTitle(contestProblem.getDisplayTitle());

        List<Tag> tags = new LinkedList<>();

        // 比赛结束后才开放标签和source、出题人、难度
        if (contest.getStatus().intValue() != Constants.Contest.STATUS_ENDED.getCode()) {
            problem.setSource(null);
            problem.setAuthor(null);
            problem.setDifficulty(null);
            QueryWrapper<ProblemTag> problemTagQueryWrapper = new QueryWrapper<>();
            problemTagQueryWrapper.eq("pid", contestProblem.getPid());
            // 获取该题号对应的标签id
            List<Long> tidList = new LinkedList<>();
            problemTagService.list(problemTagQueryWrapper).forEach(problemTag -> {
                tidList.add(problemTag.getTid());
            });
            if (tidList.size() != 0) {
                tags = (List<Tag>) tagService.listByIds(tidList);
            }
        }
        // 记录 languageId对应的name
        HashMap<Long, String> tmpMap = new HashMap<>();

        // 获取题目提交的代码支持的语言
        List<String> languagesStr = new LinkedList<>();
        QueryWrapper<ProblemLanguage> problemLanguageQueryWrapper = new QueryWrapper<>();
        problemLanguageQueryWrapper.eq("pid", contestProblem.getPid()).select("lid");
        List<Long> lidList = problemLanguageService.list(problemLanguageQueryWrapper)
                .stream().map(ProblemLanguage::getLid).collect(Collectors.toList());
        languageService.listByIds(lidList).forEach(language -> {
            languagesStr.add(language.getName());
            tmpMap.put(language.getId(), language.getName());
        });

        Date now = new Date();
        Date sealRankTime = null;
        //封榜时间除超级管理员和比赛管理员外 其它人不可看到最新数据
        if (contest.getSealRank() &&
                !isRoot &&
                !userRolesVo.getUid().equals(contest.getUid()) &&
                contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode() &&
                contest.getSealRankTime().before(now)) {
            sealRankTime = contest.getSealRankTime();
        }

        // 筛去 比赛管理员和超级管理员的提交
        List<UserInfo> superAdminList = contestRecordService.getSuperAdminList();
        List<String> superAdminUidList = superAdminList.stream().map(UserInfo::getUuid).collect(Collectors.toList());
        superAdminUidList.add(contest.getUid());

        // 获取题目的提交记录
        ProblemCountVo problemCount = judgeService.getContestProblemCount(contestProblem.getPid(), contestProblem.getId(),
                contestProblem.getCid(), contest.getStartTime(), sealRankTime, superAdminUidList);

        // 获取题目的代码模板
        QueryWrapper<CodeTemplate> codeTemplateQueryWrapper = new QueryWrapper<>();
        codeTemplateQueryWrapper.eq("pid", problem.getId()).eq("status", true);
        List<CodeTemplate> codeTemplates = codeTemplateService.list(codeTemplateQueryWrapper);
        HashMap<String, String> LangNameAndCode = new HashMap<>();
        if (codeTemplates.size() > 0) {
            for (CodeTemplate codeTemplate : codeTemplates) {
                LangNameAndCode.put(tmpMap.get(codeTemplate.getLid()), codeTemplate.getCode());
            }
        }
        // 将数据统一写入到一个Vo返回数据实体类中
        ProblemInfoVo problemInfoVo = new ProblemInfoVo(problem, tags, languagesStr, problemCount, LangNameAndCode);
        return CommonResult.successResponse(problemInfoVo, "获取成功");
    }


    @GetMapping("/contest-submissions")
    @RequiresAuthentication
    public CommonResult getContestSubmissions(@RequestParam(value = "limit", required = false) Integer limit,
                                              @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                              @RequestParam(value = "onlyMine", required = false) Boolean onlyMine,
                                              @RequestParam(value = "problemID", required = false) String displayId,
                                              @RequestParam(value = "status", required = false) Integer searchStatus,
                                              @RequestParam(value = "username", required = false) String searchUsername,
                                              @RequestParam(value = "contestID", required = true) Long searchCid,
                                              @RequestParam(value = "beforeContestSubmit", required = true) Boolean beforeContestSubmit,
                                              @RequestParam(value = "completeProblemID", defaultValue = "false") Boolean completeProblemID,
                                              HttpServletRequest request) {

        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 获取本场比赛的状态
        Contest contest = contestService.getById(searchCid);

        // 是否为超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        /**
         *  需要对该比赛做判断，是否处于开始或结束状态才可以获取题目，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
         */
        CommonResult commonResult = contestService.checkContestAuth(contest, userRolesVo, isRoot);

        if (commonResult != null) {
            return commonResult;
        }

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        String uid = null;
        // 只查看当前用户的提交
        if (onlyMine) {
            // 需要获取一下该token对应用户的数据（有token便能获取到）
            uid = userRolesVo.getUid();
        }

        String rule;
        if (contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode()) {
            rule = Constants.Contest.TYPE_ACM.getName();
        } else {
            rule = Constants.Contest.TYPE_OI.getName();
        }
        Date sealRankTime = null;

        // 不是比赛管理员和超级管理，又有开启封榜，需要判断是否处于封榜期间
        if (contest.getSealRank() && !isRoot && !userRolesVo.getUid().equals(contest.getUid())) {
            // 当前是比赛期间 同时处于封榜时间
            if (contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()
                    && contest.getSealRankTime().before(new Date())) {
                sealRankTime = contest.getSealRankTime();
            }
        }
        // OI比赛封榜期间不更新，ACM比赛封榜期间可看到自己的提交，但是其它人的不更新
        IPage<JudgeVo> commonJudgeList = judgeService.getContestJudgeList(limit, currentPage, displayId, searchCid,
                searchStatus, searchUsername, uid, beforeContestSubmit, rule, contest.getStartTime(),
                sealRankTime, userRolesVo.getUid(), completeProblemID);

        if (commonJudgeList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(commonJudgeList, "暂无数据");
        } else {
            // 比赛还是进行阶段，同时不是超级管理员与比赛管理员，需要将除自己之外的提交的时间、空间、长度隐藏
            if (contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()
                    && !isRoot && !userRolesVo.getUid().equals(contest.getUid())) {
                commonJudgeList.getRecords().forEach(judgeVo -> {
                    if (!judgeVo.getUid().equals(userRolesVo.getUid())) {
                        judgeVo.setTime(null);
                        judgeVo.setMemory(null);
                        judgeVo.setLength(null);
                    }
                });
            }

            return CommonResult.successResponse(commonJudgeList, "获取成功");
        }
    }


    /**
     * @MethodName getContestRank
     * @Params * @param null
     * @Description 获得比赛做题记录以用来排名
     * @Return
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-rank")
    @RequiresAuthentication
    public CommonResult getContestRank(@RequestParam(value = "cid", required = true) Long cid,
                                       @RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                       @RequestParam(value = "forceRefresh") Boolean forceRefresh,
                                       @RequestParam(value = "removeStar", defaultValue = "0") Boolean removeStar,
                                       HttpServletRequest request) {

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        // 获取本场比赛的状态
        Contest contest = contestService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        /**
         *  需要对该比赛做判断，是否处于开始或结束状态才可以获取，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
         */
        CommonResult commonResult = contestService.checkContestAuth(contest, userRolesVo, isRoot);

        if (commonResult != null) {
            return commonResult;
        }

        // 校验该比赛是否开启了封榜模式，超级管理员和比赛创建者可以直接看到实际榜单
        boolean isOpenSealRank = contestService.isSealRank(userRolesVo.getUid(), contest, forceRefresh, isRoot);


        IPage resultList;
        if (contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode()) { // ACM比赛

            // 进行排行榜计算以及排名分页
            resultList = contestRecordService.getContestACMRank(isOpenSealRank, removeStar, contest, currentPage, limit);

        } else { //OI比赛：以最后一次提交得分作为该题得分

            resultList = contestRecordService.getContestOIRank(isOpenSealRank, removeStar, contest, currentPage, limit);
        }

        if (resultList == null || resultList.getSize() == 0) {
            return CommonResult.successResponse(resultList, "暂无数据");
        } else {
            return CommonResult.successResponse(resultList, "获取成功");
        }
    }


    /**
     * @MethodName getContestAnnouncement
     * @Params * @param null
     * @Description 获得比赛的通知列表
     * @Return CommonResult
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-announcement")
    @RequiresAuthentication
    public CommonResult getContestAnnouncement(@RequestParam(value = "cid", required = true) Long cid,
                                               @RequestParam(value = "limit", required = false) Integer limit,
                                               @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                               HttpServletRequest request) {

        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 获取本场比赛的状态
        Contest contest = contestService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        /**
         *  需要对该比赛做判断，是否处于开始或结束状态才可以获取公告，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
         */
        CommonResult commonResult = contestService.checkContestAuth(contest, userRolesVo, isRoot);

        if (commonResult != null) {
            return commonResult;
        }


        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        IPage<AnnouncementVo> contestAnnouncementList = announcementService.getContestAnnouncement(cid, true, limit, currentPage);
        if (contestAnnouncementList.getTotal() == 0) {
            return CommonResult.successResponse(contestAnnouncementList, "暂无数据");
        } else {
            return CommonResult.successResponse(contestAnnouncementList, "获取成功");
        }
    }


    /**
     * @param userReadContestAnnouncementDto
     * @MethodName getContestUserNotReadAnnouncement
     * @Description 根据前端传过来的比赛id以及已阅读的公告提示id列表，排除后获取未阅读的公告
     * @Return
     * @Since 2021/7/17
     */
    @PostMapping("/get-contest-not-read-announcement")
    @RequiresAuthentication
    public CommonResult getContestUserNotReadAnnouncement(@RequestBody UserReadContestAnnouncementDto userReadContestAnnouncementDto) {

        Long cid = userReadContestAnnouncementDto.getCid();
        List<Long> readAnnouncementList = userReadContestAnnouncementDto.getReadAnnouncementList();

        QueryWrapper<ContestAnnouncement> contestAnnouncementQueryWrapper = new QueryWrapper<>();
        contestAnnouncementQueryWrapper.eq("cid", cid);
        if (readAnnouncementList != null && readAnnouncementList.size() > 0) {
            contestAnnouncementQueryWrapper.notIn("aid", readAnnouncementList);
        }
        List<ContestAnnouncement> announcementList = contestAnnouncementService.list(contestAnnouncementQueryWrapper);

        List<Long> aidList = announcementList.stream().map(ContestAnnouncement::getAid).collect(Collectors.toList());

        if (aidList.size() > 0) {
            QueryWrapper<Announcement> announcementQueryWrapper = new QueryWrapper<>();
            announcementQueryWrapper.in("id", aidList).orderByDesc("gmt_create");
            List<Announcement> announcements = announcementService.list(announcementQueryWrapper);
            return CommonResult.successResponse(announcements, "获取成功");
        } else {
            return CommonResult.successResponse(new LinkedList<>(), "获取成功");
        }

    }


    /**
     * @param contestPrintDto
     * @param request
     * @MethodName submitPrintText
     * @Description 提交比赛文本打印内容
     * @Return
     * @Since 2021/9/20
     */
    @PostMapping("/submit-print-text")
    @RequiresAuthentication
    public CommonResult submitPrintText(@RequestBody ContestPrintDto contestPrintDto,
                                        HttpServletRequest request) {
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 获取本场比赛的状态
        Contest contest = contestService.getById(contestPrintDto.getCid());

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        /**
         *  需要对该比赛做判断，是否处于开始或结束状态才可以提交打印内容，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
         */
        CommonResult commonResult = contestService.checkContestAuth(contest, userRolesVo, isRoot);

        if (commonResult != null) {
            return commonResult;
        }
        boolean result = contestPrintService.saveOrUpdate(new ContestPrint().setCid(contestPrintDto.getCid())
                .setContent(contestPrintDto.getContent())
                .setUsername(userRolesVo.getUsername())
                .setRealname(userRolesVo.getRealname()));

        if (result) {
            return CommonResult.successResponse(null, "提交成功，请等待工作人员打印！");
        } else {
            return CommonResult.errorResponse("提交失败！");
        }

    }


    /**
     * @MethodName getContestOutsideInfo
     * @param cid 比赛id
     * @Description 提供比赛外榜所需的比赛信息和题目信息
     * @Return
     * @Since 2021/12/8
     */
    @GetMapping("/get-contest-outsize-info")
    public CommonResult getContestOutsideInfo(@RequestParam(value = "cid", required = true) Long cid) {
        ContestVo contestInfo = contestService.getContestInfoById(cid);

        if (contestInfo == null) {
            return CommonResult.errorResponse("访问错误：该比赛不存在！");
        }

        if (!contestInfo.getOpenRank()) {
            return CommonResult.errorResponse("本场比赛未开启外榜，禁止访问外榜！", CommonResult.STATUS_ACCESS_DENIED);
        }

        // 获取本场比赛的状态
        if (contestInfo.getStatus().equals(Constants.Contest.STATUS_SCHEDULED.getCode())) {
            return CommonResult.errorResponse("本场比赛正在筹备中，禁止访问外榜！", CommonResult.STATUS_ACCESS_DENIED);
        }

        contestInfo.setNow(new Date());
        ContestOutsideInfo contestOutsideInfo = new ContestOutsideInfo();
        contestOutsideInfo.setContest(contestInfo);

        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid);
        List<ContestProblem> contestProblemList = contestProblemService.list(contestProblemQueryWrapper);
        contestOutsideInfo.setProblemList(contestProblemList);

        return CommonResult.successResponse(contestOutsideInfo, "success");
    }

    /**
     * @param request
     * @param cid          比赛id
     * @param removeStar   是否移除打星队伍
     * @param forceRefresh 是否强制实时榜单
     * @MethodName getContestScoreBoard
     * @Description 提供比赛外榜排名数据
     * @Return
     * @Since 2021/12/07
     */
    @GetMapping("/get-contest-outside-scoreboard")
    public CommonResult getContestOutsideScoreboard(@RequestParam(value = "cid", required = true) Long cid,
                                                    @RequestParam(value = "removeStar", defaultValue = "0") Boolean removeStar,
                                                    @RequestParam(value = "forceRefresh") Boolean forceRefresh,
                                                    HttpServletRequest request) {

        // 获取本场比赛的状态
        Contest contest = contestService.getById(cid);

        if (contest == null) {
            return CommonResult.errorResponse("访问错误：该比赛不存在！");
        }

        if (!contest.getOpenRank()) {
            return CommonResult.errorResponse("本场比赛未开启外榜，禁止访问外榜！", CommonResult.STATUS_ACCESS_DENIED);
        }

        if (contest.getStatus().equals(Constants.Contest.STATUS_SCHEDULED.getCode())) {
            return CommonResult.errorResponse("本场比赛正在筹备中，禁止访问外榜！", CommonResult.STATUS_ACCESS_DENIED);
        }

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = false;
        String currentUid = null;

        if (userRolesVo != null) {
            currentUid = userRolesVo.getUid();
            isRoot = SecurityUtils.getSubject().hasRole("root");
            // 不是比赛创建者或者超管无权限开启强制实时榜单
            if (!isRoot && !contest.getUid().equals(currentUid)) {
                forceRefresh = false;
            }
        }

        // 校验该比赛是否开启了封榜模式，超级管理员和比赛创建者可以直接看到实际榜单
        boolean isOpenSealRank = contestService.isSealRank(currentUid, contest, forceRefresh, isRoot);

        if (contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode()) { // ACM比赛

            // 获取排行榜
            List<ACMContestRankVo> acmContestScoreboard = contestRecordService.getACMContestScoreboard(isOpenSealRank, removeStar, contest);
            return CommonResult.successResponse(acmContestScoreboard, "success");

        } else { //OI比赛：以最后一次提交得分作为该题得分
            // 获取排行榜
            List<OIContestRankVo> oiContestScoreboard = contestRecordService.getOIContestScoreboard(isOpenSealRank, removeStar, contest);
            return CommonResult.successResponse(oiContestScoreboard, "success");
        }
    }


}
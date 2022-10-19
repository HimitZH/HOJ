package top.hcode.hoj.manager.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.common.AnnouncementEntityService;
import top.hcode.hoj.dao.contest.*;
import top.hcode.hoj.dao.group.GroupMemberEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.*;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.pojo.dto.ContestPrintDTO;
import top.hcode.hoj.pojo.dto.ContestRankDTO;
import top.hcode.hoj.pojo.dto.RegisterContestDTO;
import top.hcode.hoj.pojo.dto.UserReadContestAnnouncementDTO;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.entity.contest.*;
import top.hcode.hoj.pojo.entity.problem.*;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;
import top.hcode.hoj.validator.ContestValidator;
import top.hcode.hoj.validator.GroupValidator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 22:26
 * @Description:
 */
@Component
public class ContestManager {

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private ContestRecordEntityService contestRecordEntityService;

    @Autowired
    private ContestProblemEntityService contestProblemEntityService;

    @Autowired
    private ContestAnnouncementEntityService contestAnnouncementEntityService;

    @Autowired
    private AnnouncementEntityService announcementEntityService;

    @Autowired
    private ContestRegisterEntityService contestRegisterEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private ProblemTagEntityService problemTagEntityService;

    @Autowired
    private TagEntityService tagEntityService;

    @Autowired
    private LanguageEntityService languageEntityService;

    @Autowired
    private ProblemLanguageEntityService problemLanguageEntityService;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private CodeTemplateEntityService codeTemplateEntityService;

    @Autowired
    private ContestPrintEntityService contestPrintEntityService;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private ContestValidator contestValidator;

    @Autowired
    private ContestRankManager contestRankManager;

    @Autowired
    private GroupMemberEntityService groupMemberEntityService;

    @Autowired
    private GroupValidator groupValidator;

    public IPage<ContestVO> getContestList(Integer limit, Integer currentPage, Integer status, Integer type, String keyword) {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        return contestEntityService.getContestList(limit, currentPage, type, status, keyword);
    }


    public ContestVO getContestInfo(Long cid) throws StatusFailException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        ContestVO contestInfo = contestEntityService.getContestInfoById(cid);
        if (contestInfo == null) {
            throw new StatusFailException("对不起，该比赛不存在!");
        }

        Contest contest = contestEntityService.getById(cid);

        if (contest.getIsGroup()) {
            if (!groupValidator.isGroupMember(userRolesVo.getUid(), contest.getGid()) && !isRoot) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }
        }

        // 设置当前服务器系统时间
        contestInfo.setNow(new Date());

        return contestInfo;
    }


    public void toRegisterContest(RegisterContestDTO registerContestDto) throws StatusFailException, StatusForbiddenException {

        Long cid = registerContestDto.getCid();
        String password = registerContestDto.getPassword();
        if (cid == null || StringUtils.isEmpty(password)) {
            throw new StatusFailException("cid或者password不能为空！");
        }

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Contest contest = contestEntityService.getById(cid);

        if (contest == null || !contest.getVisible()) {
            throw new StatusFailException("对不起，该比赛不存在!");
        }

        if (contest.getIsGroup()) {
            if (!groupValidator.isGroupMember(userRolesVo.getUid(), contest.getGid()) && !isRoot) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }
        }

        if (!contest.getPwd().equals(password)) { // 密码不对
            throw new StatusFailException("比赛密码错误，请重新输入！");
        }

        // 需要校验当前比赛是否开启账号规则限制，如果有，需要对当前用户的用户名进行验证
        if (contest.getOpenAccountLimit()
                && !contestValidator.validateAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
            throw new StatusFailException("对不起！本次比赛只允许特定账号规则的用户参赛！");
        }


        QueryWrapper<ContestRegister> wrapper = new QueryWrapper<ContestRegister>().eq("cid", cid)
                .eq("uid", userRolesVo.getUid());
        if (contestRegisterEntityService.getOne(wrapper, false) != null) {
            throw new StatusFailException("您已注册过该比赛，请勿重复注册！");
        }

        boolean isOk = contestRegisterEntityService.saveOrUpdate(new ContestRegister()
                .setCid(cid)
                .setUid(userRolesVo.getUid()));

        if (!isOk) {
            throw new StatusFailException("校验比赛密码失败，请稍后再试");
        }
    }

    public AccessVO getContestAccess(Long cid) throws StatusFailException {
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        QueryWrapper<ContestRegister> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid).eq("uid", userRolesVo.getUid());
        ContestRegister contestRegister = contestRegisterEntityService.getOne(queryWrapper, false);

        boolean access = false;
        if (contestRegister != null) {
            access = true;
            Contest contest = contestEntityService.getById(cid);
            if (contest == null || !contest.getVisible()) {
                throw new StatusFailException("对不起，该比赛不存在!");
            }
            if (contest.getOpenAccountLimit()
                    && !contestValidator.validateAccountRule(contest.getAccountLimitRule(), userRolesVo.getUsername())) {
                access = false;
                contestRecordEntityService.removeById(contestRegister.getId());
            }
        }

        AccessVO accessVo = new AccessVO();
        accessVo.setAccess(access);
        return accessVo;
    }


    public List<ContestProblemVO> getContestProblem(Long cid) throws StatusFailException, StatusForbiddenException {

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        // 需要对该比赛做判断，是否处于开始或结束状态才可以获取题目列表，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
        contestValidator.validateContestAuth(contest, userRolesVo, isRoot);

        List<ContestProblemVO> contestProblemList;
        boolean isAdmin = isRoot
                || contest.getAuthor().equals(userRolesVo.getUsername())
                || (contest.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), contest.getGid()));

        List<String> groupRootUidList = null;
        if (contest.getIsGroup() && contest.getGid() != null) {
            groupRootUidList = groupMemberEntityService.getGroupRootUidList(contest.getGid());
        }

        // 如果比赛开启封榜
        if (contestValidator.isSealRank(userRolesVo.getUid(), contest, true, isRoot)) {
            contestProblemList = contestProblemEntityService.getContestProblemList(cid,
                    contest.getStartTime(),
                    contest.getEndTime(),
                    contest.getSealRankTime(),
                    isAdmin,
                    contest.getAuthor(),
                    groupRootUidList);
        } else {
            contestProblemList = contestProblemEntityService.getContestProblemList(cid,
                    contest.getStartTime(),
                    contest.getEndTime(),
                    null,
                    isAdmin,
                    contest.getAuthor(),
                    groupRootUidList);
        }

        return contestProblemList;
    }

    public ProblemInfoVO getContestProblemDetails(Long cid, String displayId) throws StatusFailException, StatusForbiddenException, StatusNotFoundException {

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        // 是否为超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        // 需要对该比赛做判断，是否处于开始或结束状态才可以获取题目，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
        contestValidator.validateContestAuth(contest, userRolesVo, isRoot);

        // 根据cid和displayId获取pid
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid).eq("display_id", displayId);
        ContestProblem contestProblem = contestProblemEntityService.getOne(contestProblemQueryWrapper);

        if (contestProblem == null) {
            throw new StatusNotFoundException("该比赛题目不存在");
        }

        //查询题目详情，题目标签，题目语言，题目做题情况
        Problem problem = problemEntityService.getById(contestProblem.getPid());

        if (problem.getAuth() == 2) {
            throw new StatusForbiddenException("该比赛题目当前不可访问！");
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
            problemTagEntityService.list(problemTagQueryWrapper).forEach(problemTag -> {
                tidList.add(problemTag.getTid());
            });
            if (tidList.size() != 0) {
                tags = (List<Tag>) tagEntityService.listByIds(tidList);
            }
        }
        // 记录 languageId对应的name
        HashMap<Long, String> tmpMap = new HashMap<>();

        // 获取题目提交的代码支持的语言
        List<String> languagesStr = new LinkedList<>();
        QueryWrapper<ProblemLanguage> problemLanguageQueryWrapper = new QueryWrapper<>();
        problemLanguageQueryWrapper.eq("pid", contestProblem.getPid()).select("lid");
        List<Long> lidList = problemLanguageEntityService.list(problemLanguageQueryWrapper)
                .stream().map(ProblemLanguage::getLid).collect(Collectors.toList());
        languageEntityService.listByIds(lidList).forEach(language -> {
            languagesStr.add(language.getName());
            tmpMap.put(language.getId(), language.getName());
        });

        Date sealRankTime = null;
        //封榜时间除超级管理员和比赛管理员外 其它人不可看到最新数据
        if (contestValidator.isSealRank(userRolesVo.getUid(), contest, true, isRoot)) {
            sealRankTime = contest.getSealRankTime();
        }

        // 筛去 比赛管理员和超级管理员的提交
        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();
        superAdminUidList.add(contest.getUid());

        // 获取题目的提交记录
        ProblemCountVO problemCount = judgeEntityService.getContestProblemCount(contestProblem.getPid(), contestProblem.getId(),
                contestProblem.getCid(), contest.getStartTime(), sealRankTime, superAdminUidList);

        // 获取题目的代码模板
        QueryWrapper<CodeTemplate> codeTemplateQueryWrapper = new QueryWrapper<>();
        codeTemplateQueryWrapper.eq("pid", problem.getId()).eq("status", true);
        List<CodeTemplate> codeTemplates = codeTemplateEntityService.list(codeTemplateQueryWrapper);
        HashMap<String, String> LangNameAndCode = new HashMap<>();
        if (codeTemplates.size() > 0) {
            for (CodeTemplate codeTemplate : codeTemplates) {
                LangNameAndCode.put(tmpMap.get(codeTemplate.getLid()), codeTemplate.getCode());
            }
        }
        // 将数据统一写入到一个Vo返回数据实体类中
        return new ProblemInfoVO(problem, tags, languagesStr, problemCount, LangNameAndCode);
    }


    public IPage<JudgeVO> getContestSubmissionList(Integer limit,
                                                   Integer currentPage,
                                                   boolean onlyMine,
                                                   String displayId,
                                                   Integer searchStatus,
                                                   String searchUsername,
                                                   Long searchCid,
                                                   boolean beforeContestSubmit,
                                                   boolean completeProblemID) throws StatusFailException, StatusForbiddenException {

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(searchCid);

        // 是否为超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        // 需要对该比赛做判断，是否处于开始或结束状态才可以获取题目，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
        contestValidator.validateContestAuth(contest, userRolesVo, isRoot);

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

        // 需要判断是否需要封榜
        if (contestValidator.isSealRank(userRolesVo.getUid(), contest, true, isRoot)) {
            sealRankTime = contest.getSealRankTime();
        }
        // OI比赛封榜期间不更新，ACM比赛封榜期间可看到自己的提交，但是其它人的不可见
        IPage<JudgeVO> contestJudgeList = judgeEntityService.getContestJudgeList(limit,
                currentPage,
                displayId,
                searchCid,
                searchStatus,
                searchUsername,
                uid,
                beforeContestSubmit,
                rule,
                contest.getStartTime(),
                sealRankTime,
                userRolesVo.getUid(),
                completeProblemID);

        if (contestJudgeList.getTotal() == 0) { // 未查询到一条数据
            return contestJudgeList;
        } else {
            // 比赛还是进行阶段，同时不是超级管理员与比赛管理员，需要将除自己之外的提交的时间、空间、长度隐藏
            if (contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()
                    && !isRoot && !userRolesVo.getUid().equals(contest.getUid())) {
                contestJudgeList.getRecords().forEach(judgeVo -> {
                    if (!judgeVo.getUid().equals(userRolesVo.getUid())) {
                        judgeVo.setTime(null);
                        judgeVo.setMemory(null);
                        judgeVo.setLength(null);
                    }
                });
            }
            return contestJudgeList;
        }
    }


    public IPage getContestRank(ContestRankDTO contestRankDto) throws StatusFailException, StatusForbiddenException {

        Long cid = contestRankDto.getCid();
        List<String> concernedList = contestRankDto.getConcernedList();
        Integer currentPage = contestRankDto.getCurrentPage();
        Integer limit = contestRankDto.getLimit();
        Boolean removeStar = contestRankDto.getRemoveStar();
        Boolean forceRefresh = contestRankDto.getForceRefresh();

        if (cid == null) {
            throw new StatusFailException("错误：cid不能为空");
        }
        if (removeStar == null) {
            removeStar = false;
        }
        if (forceRefresh == null) {
            forceRefresh = false;
        }
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 50;

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(contestRankDto.getCid());

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        // 需要对该比赛做判断，是否处于开始或结束状态才可以获取题目，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
        contestValidator.validateContestAuth(contest, userRolesVo, isRoot);

        // 校验该比赛是否开启了封榜模式，超级管理员和比赛创建者可以直接看到实际榜单
        boolean isOpenSealRank = contestValidator.isSealRank(userRolesVo.getUid(), contest, forceRefresh, isRoot);

        IPage resultList;
        if (contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode()) {
            // ACM比赛
            // 进行排行榜计算以及排名分页
            resultList = contestRankManager.getContestACMRankPage(isOpenSealRank,
                    removeStar,
                    userRolesVo.getUid(),
                    concernedList,
                    contestRankDto.getExternalCidList(),
                    contest,
                    currentPage,
                    limit,
                    contestRankDto.getKeyword());

        } else {
            // OI比赛
            resultList = contestRankManager.getContestOIRankPage(isOpenSealRank,
                    removeStar,
                    userRolesVo.getUid(),
                    concernedList,
                    contestRankDto.getExternalCidList(),
                    contest,
                    currentPage,
                    limit,
                    contestRankDto.getKeyword());
        }
        return resultList;
    }


    public IPage<AnnouncementVO> getContestAnnouncement(Long cid, Integer limit, Integer currentPage) throws StatusFailException, StatusForbiddenException {

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");
        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(cid);

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        // 需要对该比赛做判断，是否处于开始或结束状态才可以获取题目，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
        contestValidator.validateContestAuth(contest, userRolesVo, isRoot);

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        return announcementEntityService.getContestAnnouncement(cid, true, limit, currentPage);
    }


    public List<Announcement> getContestUserNotReadAnnouncement(UserReadContestAnnouncementDTO userReadContestAnnouncementDto) {

        Long cid = userReadContestAnnouncementDto.getCid();
        List<Long> readAnnouncementList = userReadContestAnnouncementDto.getReadAnnouncementList();

        QueryWrapper<ContestAnnouncement> contestAnnouncementQueryWrapper = new QueryWrapper<>();
        contestAnnouncementQueryWrapper.eq("cid", cid);
        if (readAnnouncementList != null && readAnnouncementList.size() > 0) {
            contestAnnouncementQueryWrapper.notIn("aid", readAnnouncementList);
        }
        List<ContestAnnouncement> announcementList = contestAnnouncementEntityService.list(contestAnnouncementQueryWrapper);

        List<Long> aidList = announcementList
                .stream()
                .map(ContestAnnouncement::getAid)
                .collect(Collectors.toList());

        if (aidList.size() > 0) {
            QueryWrapper<Announcement> announcementQueryWrapper = new QueryWrapper<>();
            announcementQueryWrapper.in("id", aidList).orderByDesc("gmt_create");
            return announcementEntityService.list(announcementQueryWrapper);
        } else {
            return new ArrayList<>();
        }

    }


    public void submitPrintText(ContestPrintDTO contestPrintDto) throws StatusFailException, StatusForbiddenException {

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        // 获取本场比赛的状态
        Contest contest = contestEntityService.getById(contestPrintDto.getCid());

        // 超级管理员或者该比赛的创建者，则为比赛管理者
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        // 需要对该比赛做判断，是否处于开始或结束状态才可以获取题目，同时若是私有赛需要判断是否已注册（比赛管理员包括超级管理员可以直接获取）
        contestValidator.validateContestAuth(contest, userRolesVo, isRoot);

        String lockKey = Constants.Account.CONTEST_ADD_PRINT_LOCK.getCode() + userRolesVo.getUid();
        if (redisUtils.hasKey(lockKey)) {
            long expire = redisUtils.getExpire(lockKey);
            throw new StatusForbiddenException("提交打印功能限制，请在" + expire + "秒后再进行提交！");
        } else {
            redisUtils.set(lockKey, 1, 30);
        }

        boolean isOk = contestPrintEntityService.saveOrUpdate(new ContestPrint().setCid(contestPrintDto.getCid())
                .setContent(contestPrintDto.getContent())
                .setUsername(userRolesVo.getUsername())
                .setRealname(userRolesVo.getRealname()));

        if (!isOk) {
            throw new StatusFailException("提交失败");
        }

    }

}
package top.hcode.hoj.manager.oj;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.hcode.hoj.annotation.HOJAccessEnum;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.problem.*;
import top.hcode.hoj.exception.AccessException;
import top.hcode.hoj.pojo.dto.LastAcceptedCodeVO;
import top.hcode.hoj.pojo.dto.PidListDTO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.*;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.validator.AccessValidator;
import top.hcode.hoj.validator.ContestValidator;
import top.hcode.hoj.validator.GroupValidator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 10:37
 * @Description:
 */
@Component
public class ProblemManager {
    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private ProblemTagEntityService problemTagEntityService;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private TagEntityService tagEntityService;

    @Autowired
    private LanguageEntityService languageEntityService;

    @Autowired
    private ContestEntityService contestEntityService;

    @Autowired
    private ProblemLanguageEntityService problemLanguageEntityService;

    @Autowired
    private CodeTemplateEntityService codeTemplateEntityService;

    @Autowired
    private ContestValidator contestValidator;

    @Autowired
    private GroupValidator groupValidator;

    @Autowired
    private AccessValidator accessValidator;

    /**
     * @MethodName getProblemList
     * @Params * @param null
     * @Description 获取题目列表分页
     * @Since 2020/10/27
     */
    public Page<ProblemVO> getProblemList(Integer limit, Integer currentPage,
                                          String keyword, List<Long> tagId, Integer difficulty, String oj) {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        // 关键词查询不为空
        if (!StringUtils.isEmpty(keyword)) {
            keyword = keyword.trim();
        }
        if (oj != null && !Constants.RemoteOJ.isRemoteOJ(oj)) {
            oj = "Mine";
        }
        return problemEntityService.getProblemList(limit, currentPage, null, keyword,
                difficulty, tagId, oj);
    }

    /**
     * @MethodName getRandomProblem
     * @Description 随机选取一道题目
     * @Since 2020/10/27
     */
    public RandomProblemVO getRandomProblem() throws StatusFailException {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        // 必须是公开题目
        queryWrapper.select("problem_id").eq("auth", 1)
                .eq("is_group", false);
        List<Problem> list = problemEntityService.list(queryWrapper);
        if (list.size() == 0) {
            throw new StatusFailException("获取随机题目失败，题库暂无公开题目！");
        }
        Random random = new Random();
        int index = random.nextInt(list.size());
        RandomProblemVO randomProblemVo = new RandomProblemVO();
        randomProblemVo.setProblemId(list.get(index).getProblemId());
        return randomProblemVo;
    }

    /**
     * @MethodName getUserProblemStatus
     * @Description 获取用户对应该题目列表中各个题目的做题情况
     * @Since 2020/12/29
     */
    public HashMap<Long, Object> getUserProblemStatus(PidListDTO pidListDto) throws StatusNotFoundException {

        // 需要获取一下该token对应用户的数据
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");
        HashMap<Long, Object> result = new HashMap<>();
        // 先查询判断该用户对于这些题是否已经通过，若已通过，则无论后续再提交结果如何，该题都标记为通过
        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("distinct pid,status,submit_time,score")
                .in("pid", pidListDto.getPidList())
                .eq("uid", userRolesVo.getUid())
                .orderByDesc("submit_time");

        if (pidListDto.getIsContestProblemList()) {
            // 如果是比赛的提交记录需要判断cid
            queryWrapper.eq("cid", pidListDto.getCid());
        } else {
            queryWrapper.eq("cid", 0);
            if (pidListDto.getGid() != null) {
                queryWrapper.eq("gid", pidListDto.getGid());
            } else {
                queryWrapper.isNull("gid");
            }
        }
        List<Judge> judges = judgeEntityService.list(queryWrapper);

        boolean isACMContest = true;
        Contest contest = null;
        if (pidListDto.getIsContestProblemList()) {
            contest = contestEntityService.getById(pidListDto.getCid());
            if (contest == null) {
                throw new StatusNotFoundException("错误：该比赛不存在！");
            }
            isACMContest = contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode();
        }

        for (Judge judge : judges) {
            // 如果是比赛的题目列表状态
            HashMap<String, Object> temp = new HashMap<>();
            if (pidListDto.getIsContestProblemList()) {
                if (!isACMContest) {
                    if (!result.containsKey(judge.getPid())) { // IO比赛的，如果还未写入，则使用最新一次提交的结果
                        // 判断该提交是否为封榜之后的提交,OI赛制封榜后的提交看不到提交结果，
                        // 只有比赛结束可以看到,比赛管理员与超级管理员的提交除外
                        if (contestValidator.isSealRank(userRolesVo.getUid(), contest, false,
                                SecurityUtils.getSubject().hasRole("root"))) {
                            temp.put("status", Constants.Judge.STATUS_SUBMITTED_UNKNOWN_RESULT.getStatus());
                            temp.put("score", null);
                        } else {
                            temp.put("status", judge.getStatus());
                            temp.put("score", judge.getScore());
                        }
                        result.put(judge.getPid(), temp);
                    }
                } else {
                    // 如果该题目已通过，且同时是为不封榜前提交的，则强制写为通过（0）
                    if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                        temp.put("status", Constants.Judge.STATUS_ACCEPTED.getStatus());
                        temp.put("score", null);
                        result.put(judge.getPid(), temp);
                    } else if (!result.containsKey(judge.getPid())) { // 还未写入，则使用最新一次提交的结果
                        temp.put("status", judge.getStatus());
                        temp.put("score", null);
                        result.put(judge.getPid(), temp);
                    }
                }

            } else { // 不是比赛题目
                if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) { // 如果该题目已通过，则强制写为通过（0）
                    temp.put("status", Constants.Judge.STATUS_ACCEPTED.getStatus());
                    result.put(judge.getPid(), temp);
                } else if (!result.containsKey(judge.getPid())) { // 还未写入，则使用最新一次提交的结果
                    temp.put("status", judge.getStatus());
                    result.put(judge.getPid(), temp);
                }
            }
        }

        // 再次检查，应该可能从未提交过该题，则状态写为-10
        for (Long pid : pidListDto.getPidList()) {

            // 如果是比赛的题目列表状态
            if (pidListDto.getIsContestProblemList()) {
                if (!result.containsKey(pid)) {
                    HashMap<String, Object> temp = new HashMap<>();
                    temp.put("score", null);
                    temp.put("status", Constants.Judge.STATUS_NOT_SUBMITTED.getStatus());
                    result.put(pid, temp);
                }
            } else {
                if (!result.containsKey(pid)) {
                    HashMap<String, Object> temp = new HashMap<>();
                    temp.put("status", Constants.Judge.STATUS_NOT_SUBMITTED.getStatus());
                    result.put(pid, temp);
                }
            }
        }
        return result;

    }

    /**
     * @MethodName getProblemInfo
     * @Description 获取指定题目的详情信息，标签，所支持语言，做题情况（只能查询公开题目 也就是auth为1）
     * @Since 2020/10/27
     */
    public ProblemInfoVO getProblemInfo(String problemId, Long gid) throws StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        QueryWrapper<Problem> wrapper = new QueryWrapper<Problem>().eq("problem_id", problemId);
        //查询题目详情，题目标签，题目语言，题目做题情况
        Problem problem = problemEntityService.getOne(wrapper, false);
        if (problem == null) {
            throw new StatusNotFoundException("该题号对应的题目不存在");
        }
        if (problem.getAuth() != 1) {
            throw new StatusForbiddenException("该题号对应题目并非公开题目，不支持访问！");
        }

        if (problem.getIsGroup()) {
            if (!isRoot && !groupValidator.isGroupMember(userRolesVo.getUid(), problem.getGid())) {
                throw new StatusForbiddenException("该题号对应题目并非公开题目，不支持访问！");
            }
        }

        QueryWrapper<ProblemTag> problemTagQueryWrapper = new QueryWrapper<>();
        problemTagQueryWrapper.eq("pid", problem.getId());

        // 获取该题号对应的标签id
        List<Long> tidList = new LinkedList<>();
        problemTagEntityService.list(problemTagQueryWrapper).forEach(problemTag -> {
            tidList.add(problemTag.getTid());
        });
        List<Tag> tags = new ArrayList<>();
        if (tidList.size() > 0) {
            tags = (List<Tag>) tagEntityService.listByIds(tidList);
        }

        // 记录 languageId对应的name
        HashMap<Long, String> tmpMap = new HashMap<>();
        // 获取题目提交的代码支持的语言
        List<String> languagesStr = new LinkedList<>();
        QueryWrapper<ProblemLanguage> problemLanguageQueryWrapper = new QueryWrapper<>();
        problemLanguageQueryWrapper.eq("pid", problem.getId()).select("lid");
        List<Long> lidList = problemLanguageEntityService.list(problemLanguageQueryWrapper)
                .stream().map(ProblemLanguage::getLid).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(lidList)) {
            languageEntityService.listByIds(lidList).forEach(language -> {
                languagesStr.add(language.getName());
                tmpMap.put(language.getId(), language.getName());
            });
        }
        // 获取题目的提交记录
        ProblemCountVO problemCount = judgeEntityService.getProblemCount(problem.getId(), gid);

        // 获取题目的代码模板
        QueryWrapper<CodeTemplate> codeTemplateQueryWrapper = new QueryWrapper<>();
        codeTemplateQueryWrapper.eq("pid", problem.getId()).eq("status", true);
        List<CodeTemplate> codeTemplates = codeTemplateEntityService.list(codeTemplateQueryWrapper);
        HashMap<String, String> LangNameAndCode = new HashMap<>();
        if (CollectionUtil.isNotEmpty(codeTemplates)) {
            for (CodeTemplate codeTemplate : codeTemplates) {
                LangNameAndCode.put(tmpMap.get(codeTemplate.getLid()), codeTemplate.getCode());
            }
        }
        // 屏蔽一些题目参数
        problem.setJudgeExtraFile(null)
                .setSpjCode(null)
                .setSpjLanguage(null);

        // 将数据统一写入到一个Vo返回数据实体类中
        return new ProblemInfoVO(problem, tags, languagesStr, problemCount, LangNameAndCode);
    }

    public LastAcceptedCodeVO getUserLastAcceptedCode(Long pid, Long cid) {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");
        if (cid == null) {
            cid = 0L;
        }
        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.select("submit_id", "cid", "code", "username", "submit_time", "language")
                .eq("uid", userRolesVo.getUid())
                .eq("pid", pid)
                .eq("cid", cid)
                .eq("status", 0)
                .orderByDesc("submit_id")
                .last("limit 1");
        List<Judge> judgeList = judgeEntityService.list(judgeQueryWrapper);
        LastAcceptedCodeVO lastAcceptedCodeVO = new LastAcceptedCodeVO();
        if (CollectionUtil.isNotEmpty(judgeList)) {
            Judge judge = judgeList.get(0);
            lastAcceptedCodeVO.setSubmitId(judge.getSubmitId());
            lastAcceptedCodeVO.setLanguage(judge.getLanguage());
            lastAcceptedCodeVO.setCode(buildCode(judge));
        } else {
            lastAcceptedCodeVO.setCode("");
        }
        return lastAcceptedCodeVO;
    }

    private String buildCode(Judge judge) {
        if (judge.getCid() == 0) {
            // 比赛外的提交代码 如果不是超管或题目管理员，需要检查网站是否开启隐藏代码功能
            boolean isRoot = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员
            boolean isProblemAdmin = SecurityUtils.getSubject().hasRole("problem_admin");// 是否为题目管理员
            if (!isRoot && !isProblemAdmin) {
                try {
                    accessValidator.validateAccess(HOJAccessEnum.HIDE_NON_CONTEST_SUBMISSION_CODE);
                } catch (AccessException e) {
                    return "Because the super administrator has enabled " +
                            "the function of not viewing the submitted code outside the contest of master station, \n" +
                            "the code of this submission details has been hidden.";
                }
            }
        }
        if (!judge.getLanguage().toLowerCase().contains("py")) {
            return judge.getCode() + "\n\n" +
                    "/**\n" +
                    "* @runId: " + judge.getSubmitId() + "\n" +
                    "* @language: " + judge.getLanguage() + "\n" +
                    "* @author: " + judge.getUsername() + "\n" +
                    "* @submitTime: " + DateUtil.format(judge.getSubmitTime(), "yyyy-MM-dd HH:mm:ss") + "\n" +
                    "*/";
        } else {
            return judge.getCode() + "\n\n" +
                    "'''\n" +
                    "    @runId: " + judge.getSubmitId() + "\n" +
                    "    @language: " + judge.getLanguage() + "\n" +
                    "    @author: " + judge.getUsername() + "\n" +
                    "    @submitTime: " + DateUtil.format(judge.getSubmitTime(), "yyyy-MM-dd HH:mm:ss") + "\n" +
                    "'''";
        }
    }
}
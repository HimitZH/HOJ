package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.PidListDto;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.ProblemInfoVo;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.impl.*;
import top.hcode.hoj.utils.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 13:24
 * @Description: 问题数据控制类，处理题目列表请求，题目内容请求。
 */
@RestController
@RequestMapping("/api")
public class ProblemController {

    @Autowired
    private ProblemServiceImpl problemService;

    @Autowired
    private ProblemTagServiceImpl problemTagService;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private TagServiceImpl tagService;

    @Autowired
    private LanguageServiceImpl languageService;

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ProblemLanguageServiceImpl problemLanguageService;

    @Autowired
    private ProblemCountServiceImpl problemCountService;

    /**
     * @MethodName getProblemList
     * @Params * @param null
     * @Description 获取题目列表分页
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @RequestMapping(value = "/get-problem-list", method = RequestMethod.GET)
    public CommonResult getProblemList(@RequestParam(value = "limit", required = false) Integer limit,
                                       @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                       @RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "tagId", required = false) Long tagId,
                                       @RequestParam(value = "difficulty", required = false) Integer difficulty,
                                       @RequestParam(value = "oj", required = false) String oj) {
        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        // 关键词查询不为空
        Long pid = null;
        if (!StringUtils.isEmpty(keyword)) {
            keyword = keyword.trim();
            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher isNum = pattern.matcher(keyword);
            if (isNum.matches()) { // 利用正则表达式判断keyword是否为纯数字
                pid = Long.valueOf(keyword);
            }
        }

        Page<ProblemVo> problemList = problemService.getProblemList(limit, currentPage, pid, keyword,
                difficulty, tagId, oj);
        if (problemList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(problemList, "暂无数据");
        } else {
            return CommonResult.successResponse(problemList, "获取成功");
        }
    }

    /**
     * @MethodName getRandomProblem
     * @Params * @param null
     * @Description 随机选取一道题目
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @GetMapping("/get-random-problem")
    public CommonResult getRandomProblem() {
        QueryWrapper<Problem> queryWrapper = new QueryWrapper<>();
        // 必须是公开题目
        queryWrapper.select("problem_id").eq("auth", 1);
        List<Problem> list = problemService.list(queryWrapper);
        if (list.size() == 0) {
            return CommonResult.errorResponse("获取随机题目失败，本站暂无公开题目！");
        }
        Random random = new Random();
        int index = random.nextInt(list.size());
        Map<String, Object> result = new HashMap<>();
        result.put("problemId", list.get(index).getProblemId());
        return CommonResult.successResponse(result);
    }

    /**
     * @MethodName getUserProblemStatus
     * @Params * @param UidAndPidListDto
     * @Description 获取用户对应该题目列表中各个题目的做题情况
     * @Return CommonResult
     * @Since 2020/12/29
     */
    @RequiresAuthentication
    @PostMapping("/get-user-problem-status")
    public CommonResult getUserProblemStatus(@Validated @RequestBody PidListDto pidListDto, HttpServletRequest request) {

        // 需要获取一下该token对应用户的数据
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        HashMap<Long, Object> result = new HashMap<>();
        // 先查询判断该用户对于这些题是否已经通过，若已通过，则无论后续再提交结果如何，该题都标记为通过
        QueryWrapper<Judge> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("distinct pid,status,submit_time,score").in("pid", pidListDto.getPidList())
                // 如果是比赛的提交记录需要判断cid
                .eq(pidListDto.getIsContestProblemList(), "cid", pidListDto.getCid())
                .eq("uid", userRolesVo.getUid()).orderByDesc("submit_time");
        List<Judge> judges = judgeService.list(queryWrapper);

        boolean isACMContest = true;
        if (pidListDto.getIsContestProblemList()) {
            Contest contest = contestService.getById(pidListDto.getCid());
            isACMContest = contest.getType().intValue() == Constants.Contest.TYPE_ACM.getCode();
        }

        for (Judge judge : judges) {

            // 如果是比赛的题目列表状态
            if (pidListDto.getIsContestProblemList()) {
                HashMap<String, Object> temp = new HashMap<>();
                if (!isACMContest) {
                    if (!result.containsKey(judge.getPid())) { // IO比赛的，如果还未写入，则使用最新一次提交的结果
                        temp.put("status", judge.getStatus());
                        temp.put("score", judge.getScore());
                        result.put(judge.getPid(), temp);
                    }
                } else {
                    if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) { // 如果该题目已通过，则强制写为通过（0）
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
                    result.put(judge.getPid(), Constants.Judge.STATUS_ACCEPTED.getStatus());
                } else if (!result.containsKey(judge.getPid())) { // 还未写入，则使用最新一次提交的结果
                    result.put(judge.getPid(), judge.getStatus());
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
                    temp.put("status", -10);
                    result.put(pid, temp);
                }
            } else {
                if (!result.containsKey(pid)) {
                    result.put(pid, -10);
                }
            }
        }
        return CommonResult.successResponse(result, "查询成功");

    }

    /**
     * @MethodName getProblemInfo
     * @Params * @param null
     * @Description 获取指定题目的详情信息，标签，所支持语言，做题情况（只能查询公开题目 也就是auth为1）
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @RequestMapping(value = "/get-problem", method = RequestMethod.GET)
    public CommonResult getProblemInfo(@RequestParam(value = "problemId", required = true) String problemId) {

        QueryWrapper<Problem> wrapper = new QueryWrapper<Problem>().eq("problem_id", problemId);

        //查询题目详情，题目标签，题目语言，题目做题情况
        Problem problem = problemService.getOne(wrapper);
        if (problem == null) {
            return CommonResult.errorResponse("该题号对应的题目不存在", CommonResult.STATUS_NOT_FOUND);
        }
        if (problem.getAuth() != 1) {
            return CommonResult.errorResponse("该题号对应题目并非公开题目，不支持访问！", CommonResult.STATUS_FORBIDDEN);
        }

        QueryWrapper<ProblemTag> problemTagQueryWrapper = new QueryWrapper<>();
        problemTagQueryWrapper.eq("pid", problem.getId());
        // 获取该题号对应的标签id
        List<Long> tidList = new LinkedList<>();
        problemTagService.list(problemTagQueryWrapper).forEach(problemTag -> {
            tidList.add(problemTag.getTid());
        });
        List<String> tagsStr = new LinkedList<>();
        if (tidList.size() != 0) {
            tagService.listByIds(tidList).forEach(tag -> {
                tagsStr.add(tag.getName());
            });
        }
        // 获取题目提交的代码支持的语言
        List<String> languagesStr = new LinkedList<>();
        QueryWrapper<ProblemLanguage> problemLanguageQueryWrapper = new QueryWrapper<>();
        problemLanguageQueryWrapper.eq("pid", problem.getId()).select("lid");
        List<Long> lidList = problemLanguageService.list(problemLanguageQueryWrapper)
                .stream().map(ProblemLanguage::getLid).collect(Collectors.toList());
        languageService.listByIds(lidList).forEach(language -> {
            languagesStr.add(language.getName());
        });

        // 获取题目的提交记录
        QueryWrapper<ProblemCount> problemCountQueryWrapper = new QueryWrapper<>();
        problemCountQueryWrapper.eq("pid", problem.getId());
        ProblemCount problemCount = problemCountService.getOne(problemCountQueryWrapper);


        // 将数据统一写入到一个Vo返回数据实体类中
        ProblemInfoVo problemInfoVo = new ProblemInfoVo(problem, tagsStr, languagesStr, problemCount);
        return CommonResult.successResponse(problemInfoVo, "获取成功");
    }


}
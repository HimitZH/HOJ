package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.AnnouncementMapper;
import top.hcode.hoj.dao.ContestRegisterMapper;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.dao.ContestProblemMapper;
import top.hcode.hoj.service.AnnouncementService;
import top.hcode.hoj.service.ContestRecordService;
import top.hcode.hoj.service.ContestService;
import top.hcode.hoj.service.impl.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    private ProblemCountServiceImpl problemCountService;


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

        Contest contest = contestService.getById(Long.valueOf(cidStr));

        if (!contest.getPwd().equals(password)) { // 密码不对
            return CommonResult.errorResponse("比赛密码错误！");
        }

        QueryWrapper<ContestRegister> wrapper = new QueryWrapper<ContestRegister>().eq("cid", Long.valueOf(cidStr))
                .eq("uid", userRolesVo.getUid());
        if (contestRegisterService.getOne(wrapper) != null) {
            return CommonResult.errorResponse("您已注册过该比赛，请勿重复注册！");
        }

        boolean result = contestRegisterService.saveOrUpdate(new ContestRegister()
                .setCid(Long.valueOf(cidStr))
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
    public CommonResult getContestAccess(@RequestParam(value = "cid", required = true) Long cid, HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        QueryWrapper<ContestRegister> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cid", cid).eq("uid", userRolesVo.getUid());
        ContestRegister contestRegister = contestRegisterService.getOne(queryWrapper);
        HashMap<String, Object> result = new HashMap<>();
        if (contestRegister != null) {
            result.put("access", true);
        } else {
            result.put("access", false);
        }
        return CommonResult.successResponse(request);
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
    public CommonResult getContestProblem(@RequestParam(value = "cid", required = true) Long cid) {

        /**
         *  待续....需要对该比赛做判断，是否处于开始或结束状态才可以获取题目，以及比赛管理员包括超级管理员可以直接获取。
         */

        List<ContestProblemVo> contestProblemList = contestProblemService.getContestProblemList(cid);

        if (contestProblemList.size() == 0) {
            return CommonResult.successResponse(null, "暂无数据");
        } else {
            return CommonResult.successResponse(contestProblemList, "获取成功");
        }
    }

    @GetMapping("/get-contest-problem-details")
    @RequiresAuthentication
    public CommonResult getContestProblemDetails(@RequestParam(value = "cid", required = true) Long cid,
                                                 @RequestParam(value = "displayId", required = true) String displayId) {
        // 根据cid和displayId获取pid
        QueryWrapper<ContestProblem> contestProblemQueryWrapper = new QueryWrapper<>();
        contestProblemQueryWrapper.eq("cid", cid).eq("display_id", displayId);
        ContestProblem contestProblem = contestProblemService.getOne(contestProblemQueryWrapper);

        if (contestProblem == null) {
            return CommonResult.errorResponse("该题目不存在", CommonResult.STATUS_NOT_FOUND);
        }

        //查询题目详情，题目标签，题目语言，题目做题情况
        Problem problem = problemService.getById(contestProblem.getPid());

        QueryWrapper<ProblemTag> problemTagQueryWrapper = new QueryWrapper<>();
        problemTagQueryWrapper.eq("pid", contestProblem.getPid());
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
        problemLanguageQueryWrapper.eq("pid", contestProblem.getPid()).select("lid");
        List<Long> lidList = problemLanguageService.list(problemLanguageQueryWrapper)
                .stream().map(ProblemLanguage::getLid).collect(Collectors.toList());
        languageService.listByIds(lidList).forEach(language -> {
            languagesStr.add(language.getName());
        });

        // 获取题目的提交记录
        ProblemCount problemCount = problemCountService.getContestProblemCount(contestProblem.getPid(), contestProblem.getId(), contestProblem.getCid());


        // 将数据统一写入到一个Vo返回数据实体类中
        ProblemInfoVo problemInfoVo = new ProblemInfoVo(problem, tagsStr, languagesStr, problemCount);
        return CommonResult.successResponse(problemInfoVo, "获取成功");
    }


    /**
     * @MethodName getContestRank
     * @Params * @param null
     * @Description 获得比赛做题记录以用来排名
     * @Return
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-rank")
    public CommonResult getContestRank(@RequestParam(value = "cid", required = true) Long cid) {

        QueryWrapper<ContestProblem> wrapper = new QueryWrapper<ContestProblem>().eq("cid", cid).groupBy();

        List<ContestRecordVo> contestRecordList = contestRecordService.getContestRecord(cid);
        if (contestRecordList.size() == 0) {
            return CommonResult.errorResponse("暂无数据", CommonResult.STATUS_NOT_FOUND);
        } else {

            return CommonResult.successResponse(contestRecordList, "获取成功");
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
    public CommonResult getContestAnnouncement(@RequestParam(value = "cid", required = true) Long cid,
                                               @RequestParam(value = "limit", required = false) Integer limit,
                                               @RequestParam(value = "currentPage", required = false) Integer currentPage) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        IPage<AnnouncementVo> contestAnnouncementList = announcementService.getContestAnnouncement(cid, true, limit, currentPage);
        if (contestAnnouncementList.getTotal() == 0) {
            return CommonResult.errorResponse("暂无数据", CommonResult.STATUS_NOT_FOUND);
        } else {
            return CommonResult.successResponse(contestAnnouncementList, "获取成功");
        }
    }

}
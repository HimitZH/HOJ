package top.hcode.hoj.controller.oj;


import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ContestPrintDTO;
import top.hcode.hoj.pojo.dto.ContestRankDTO;
import top.hcode.hoj.pojo.dto.RegisterContestDTO;
import top.hcode.hoj.pojo.dto.UserReadContestAnnouncementDTO;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.service.oj.ContestService;

import java.util.*;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 21:40
 * @Description: 处理比赛模块的相关数据请求
 */
@RestController
@RequestMapping("/api")
public class ContestController {

    @Autowired
    private ContestService contestService;


    /**
     * @MethodName getContestList
     * @Params * @param null
     * @Description 获取比赛列表分页数据
     * @Return CommonResult
     * @Since 2020/10/27
     */
    @GetMapping("/get-contest-list")
    public CommonResult<IPage<ContestVO>> getContestList(@RequestParam(value = "limit", required = false) Integer limit,
                                                         @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                         @RequestParam(value = "status", required = false) Integer status,
                                                         @RequestParam(value = "type", required = false) Integer type,
                                                         @RequestParam(value = "keyword", required = false) String keyword) {
        return contestService.getContestList(limit, currentPage, status, type, keyword);
    }

    /**
     * @MethodName getContestInfo
     * @Description 获得指定比赛的详细信息
     * @Return
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-info")
    @RequiresAuthentication
    public CommonResult<ContestVO> getContestInfo(@RequestParam(value = "cid", required = true) Long cid) {

        return contestService.getContestInfo(cid);
    }

    /**
     * @MethodName toRegisterContest
     * @Description 注册比赛
     * @Return
     * @Since 2020/10/28
     */
    @PostMapping("/register-contest")
    @RequiresAuthentication
    public CommonResult<Void> toRegisterContest(@RequestBody RegisterContestDTO registerContestDto) {
        return contestService.toRegisterContest(registerContestDto);
    }

    /**
     * @MethodName getContestAccess
     * @Description 获得指定私有比赛的访问权限或保护比赛的提交权限
     * @Return
     * @Since 2020/10/28
     */
    @RequiresAuthentication
    @GetMapping("/get-contest-access")
    public CommonResult<AccessVO> getContestAccess(@RequestParam(value = "cid") Long cid) {

        return contestService.getContestAccess(cid);
    }


    /**
     * @MethodName getContestProblem
     * @Description 获得指定比赛的题目列表
     * @Return
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-problem")
    @RequiresAuthentication
    public CommonResult<List<ContestProblemVO>> getContestProblem(@RequestParam(value = "cid", required = true) Long cid) {

        return contestService.getContestProblem(cid);
    }

    @GetMapping("/get-contest-problem-details")
    @RequiresAuthentication
    public CommonResult<ProblemInfoVO> getContestProblemDetails(@RequestParam(value = "cid", required = true) Long cid,
                                                                @RequestParam(value = "displayId", required = true) String displayId) {

        return contestService.getContestProblemDetails(cid, displayId);
    }


    @GetMapping("/contest-submissions")
    @RequiresAuthentication
    public CommonResult<IPage<JudgeVO>> getContestSubmissionList(@RequestParam(value = "limit", required = false) Integer limit,
                                                                 @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                                 @RequestParam(value = "onlyMine", required = false) Boolean onlyMine,
                                                                 @RequestParam(value = "problemID", required = false) String displayId,
                                                                 @RequestParam(value = "status", required = false) Integer searchStatus,
                                                                 @RequestParam(value = "username", required = false) String searchUsername,
                                                                 @RequestParam(value = "contestID", required = true) Long searchCid,
                                                                 @RequestParam(value = "beforeContestSubmit", required = true) Boolean beforeContestSubmit,
                                                                 @RequestParam(value = "completeProblemID", defaultValue = "false") Boolean completeProblemID) {

        return contestService.getContestSubmissionList(limit,
                currentPage,
                onlyMine,
                displayId,
                searchStatus,
                searchUsername,
                searchCid,
                beforeContestSubmit,
                completeProblemID);
    }


    /**
     * @MethodName getContestRank
     * @Description 获得比赛做题记录以用来排名
     * @Return
     * @Since 2020/10/28
     */
    @PostMapping("/get-contest-rank")
    @RequiresAuthentication
    public CommonResult<IPage> getContestRank(@RequestBody ContestRankDTO contestRankDto) {

        return contestService.getContestRank(contestRankDto);
    }


    /**
     * @MethodName getContestAnnouncement
     * @Description 获得比赛的通知列表
     * @Return CommonResult
     * @Since 2020/10/28
     */
    @GetMapping("/get-contest-announcement")
    @RequiresAuthentication
    public CommonResult<IPage<AnnouncementVO>> getContestAnnouncement(@RequestParam(value = "cid", required = true) Long cid,
                                                                      @RequestParam(value = "limit", required = false) Integer limit,
                                                                      @RequestParam(value = "currentPage", required = false) Integer currentPage) {

        return contestService.getContestAnnouncement(cid, limit, currentPage);
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
    public CommonResult<List<Announcement>> getContestUserNotReadAnnouncement(@RequestBody UserReadContestAnnouncementDTO userReadContestAnnouncementDto) {
        return contestService.getContestUserNotReadAnnouncement(userReadContestAnnouncementDto);
    }


    /**
     * @param contestPrintDto
     * @MethodName submitPrintText
     * @Description 提交比赛文本打印内容
     * @Return
     * @Since 2021/9/20
     */
    @PostMapping("/submit-print-text")
    @RequiresAuthentication
    public CommonResult<Void> submitPrintText(@RequestBody ContestPrintDTO contestPrintDto) {

        return contestService.submitPrintText(contestPrintDto);
    }


}
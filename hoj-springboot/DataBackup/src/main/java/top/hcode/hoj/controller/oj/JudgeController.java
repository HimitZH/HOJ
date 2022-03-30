package top.hcode.hoj.controller.oj;


import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.SubmitIdListDto;
import top.hcode.hoj.pojo.dto.ToJudgeDto;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.SubmissionInfoVo;
import top.hcode.hoj.service.oj.JudgeService;

import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 20:52
 * @Description: 处理代码评判相关业务
 */
@RestController
@RequestMapping("/api")
public class JudgeController {


    @Autowired
    private JudgeService judgeService;

    /**
     * @MethodName submitProblemJudge
     * @Description 核心方法 判题通过openfeign调用判题系统服务
     * @Return CommonResult
     * @Since 2020/10/30
     */

    /**
     * @param limit
     * @param currentPage
     * @param onlyMine
     * @param searchPid
     * @param searchStatus
     * @param searchUsername
     * @param completeProblemID
     * @MethodName getJudgeList
     * @Description 通用查询判题记录列表
     * @Return CommonResult
     * @Since 2020/10/29
     */
    @GetMapping("/get-submission-list")
    public CommonResult<IPage<JudgeVo>> getJudgeList(@RequestParam(value = "limit", required = false) Integer limit,
                                                     @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                                     @RequestParam(value = "onlyMine", required = false) Boolean onlyMine,
                                                     @RequestParam(value = "problemID", required = false) String searchPid,
                                                     @RequestParam(value = "status", required = false) Integer searchStatus,
                                                     @RequestParam(value = "username", required = false) String searchUsername,
                                                     @RequestParam(value = "completeProblemID", defaultValue = "false") Boolean completeProblemID,
                                                     @RequestParam(value = "gid", required = false) Long gid) {

        return judgeService.getJudgeList(limit, currentPage, onlyMine, searchPid, searchStatus, searchUsername, completeProblemID, gid);
    }

    /**
     * @MethodName getSubmission
     * @Description 获取单个提交记录的详情
     * @Return CommonResult
     * @Since 2021/1/2
     */
    @GetMapping("/get-submission-detail")
    public CommonResult<SubmissionInfoVo> getSubmission(@RequestParam(value = "submitId", required = true) Long submitId) {
        return judgeService.getSubmission(submitId);
    }

    @RequiresAuthentication
    @RequiresPermissions("submit")
    @RequestMapping(value = "/submit-problem-judge", method = RequestMethod.POST)
    public CommonResult<Judge> submitProblemJudge(@RequestBody ToJudgeDto judgeDto) {
        return judgeService.submitProblemJudge(judgeDto);
    }


    /**
     * @MethodName resubmit
     * @Description 调用判题服务器提交失败超过60s后，用户点击按钮重新提交判题进入的方法
     * @Return
     * @Since 2021/2/12
     */
    @RequiresAuthentication
    @GetMapping(value = "/resubmit")
    public CommonResult<Judge> resubmit(@RequestParam("submitId") Long submitId) {
        return judgeService.resubmit(submitId);
    }

    /**
     * @MethodName updateSubmission
     * @Description 修改单个提交详情的分享权限
     * @Return CommonResult
     * @Since 2021/1/2
     */
    @PutMapping("/submission")
    @RequiresAuthentication
    public CommonResult<Void> updateSubmission(@RequestBody Judge judge) {
        return judgeService.updateSubmission(judge);
    }

    /**
     * @MethodName checkJudgeResult
     * @Description 对提交列表状态为Pending和Judging的提交进行更新检查
     * @Return
     * @Since 2021/1/3
     */
    @RequestMapping(value = "/check-submissions-status", method = RequestMethod.POST)
    public CommonResult<HashMap<Long, Object>> checkCommonJudgeResult(@RequestBody SubmitIdListDto submitIdListDto) {
        return judgeService.checkCommonJudgeResult(submitIdListDto);
    }

    /**
     * @param submitIdListDto
     * @MethodName checkContestJudgeResult
     * @Description 需要检查是否为封榜，是否可以查询结果，避免有人恶意查询
     * @Return
     * @Since 2021/6/11
     */
    @RequestMapping(value = "/check-contest-submissions-status", method = RequestMethod.POST)
    @RequiresAuthentication
    public CommonResult<HashMap<Long, Object>> checkContestJudgeResult(@RequestBody SubmitIdListDto submitIdListDto) {
        return judgeService.checkContestJudgeResult(submitIdListDto);
    }


    /**
     * @param submitId
     * @MethodName getJudgeCase
     * @Description 获得指定提交id的测试样例结果，暂不支持查看测试数据，只可看测试点结果，时间，空间，或者IO得分
     * @Return
     * @Since 2020/10/29
     */
    @GetMapping("/get-all-case-result")
    public CommonResult<List<JudgeCase>> getALLCaseResult(@RequestParam(value = "submitId", required = true) Long submitId) {
        return judgeService.getALLCaseResult(submitId);
    }
}
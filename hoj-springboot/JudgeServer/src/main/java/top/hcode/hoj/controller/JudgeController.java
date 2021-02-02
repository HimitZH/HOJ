package top.hcode.hoj.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.CommonResult;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.judger.JudgeStrategy;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.service.impl.*;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.IpUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/29 22:22
 * @Description: 处理代码提交
 */
@RestController
@Slf4j
public class JudgeController {

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private ProblemServiceImpl problemService;

    @Autowired
    private ProblemCountServiceImpl problemCountService;

    @Autowired
    private UserAcproblemServiceImpl userAcproblemService;

    @Autowired
    private UserRecordServiceImpl userRecordService;

    @Autowired
    private ContestRecordServiceImpl contestRecordService;

    @Autowired
    private ContestServiceImpl contestService;

    @Autowired
    private ProblemCaseServiceImpl problemCaseService;

    @Autowired
    private JudgeStrategy judgeStrategy;

    @PostMapping(value = "/judge") // 待定，到时再添加服务熔断兜底方法
    public CommonResult submitProblemJudge(@RequestBody Judge judge) {

        if (judge == null || judge.getSubmitId() == null || judge.getUid() == null || judge.getPid() == null) {
            return CommonResult.errorResponse("参数错误！");
        }

        judge.setStatus(Constants.Judge.STATUS_COMPILING.getStatus()); // 标志该判题过程进入编译阶段
        judge.setJudger(IpUtils.getServiceIp()); // 获取当前判题系统所在ip写入数据库
        boolean updateResult = judgeService.saveOrUpdate(judge);
        if (!updateResult) { // 出错并不影响主要业务逻辑，可以稍微记录一下即可。
            log.error("修改Judge表失效----->{}", "修改提交评判为评测队列出错");
        }
        // 进行判题操作
        Problem problem = problemService.getById(judge.getPid());

        Judge finalJudge = judgeService.Judge(problem, judge);

        // 更新该次提交
        judgeService.updateById(finalJudge);

        if (finalJudge.getCid() == 0) { // 非比赛提交

            // 如果是AC,就更新user_acproblem表,
            if (finalJudge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                userAcproblemService.saveOrUpdate(new UserAcproblem()
                        .setPid(finalJudge.getPid())
                        .setUid(finalJudge.getUid())
                        .setSubmitId(finalJudge.getSubmitId())
                );
            }
            // 比赛的提交不纳入，更新该提交对应题目的数据
            problemCountService.updateCount(finalJudge.getStatus(), finalJudge);


            // 如果是非比赛提交，且为OI题目的提交，需要判断是否更新用户得分
            if (finalJudge.getScore() != null) {
                userRecordService.updateRecord(finalJudge);
            }

        } else { //如果是比赛提交

            Contest contest = contestService.getById(finalJudge.getCid());
            if (contest == null) {
                log.error("判题机出错----------->{}", "该比赛不存在");
                return CommonResult.errorResponse("该比赛不存在");
            }
            // 每个提交都得记录到contest_record里面,同时需要判断是否为比赛前的提交
            if (contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()) {
                contestRecordService.UpdateContestRecord(finalJudge);
            }

        }
        return CommonResult.successResponse(finalJudge, "判题机评测完成！");
    }

    @GetMapping("/init-test-case")
    public CommonResult initTestCase(@RequestParam("pid") Long pid, @RequestParam("isSpj") Boolean isSpj) throws SystemError {

        QueryWrapper<ProblemCase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
        List<ProblemCase> problemCases = problemCaseService.list(queryWrapper);

        List<HashMap<String, Object>> testCases = new LinkedList<>();
        for (ProblemCase problemCase : problemCases) {
            HashMap<String, Object> tmp = new HashMap<>();
            tmp.put("input", problemCase.getInput());
            tmp.put("output", problemCase.getOutput());
            testCases.add(tmp);
        }
        judgeStrategy.initTestCase(testCases, pid, isSpj);
        return CommonResult.successResponse(null, "初始化评测数据到判题机成功！");
    }

}
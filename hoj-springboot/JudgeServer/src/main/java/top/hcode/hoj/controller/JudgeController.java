package top.hcode.hoj.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.CommonResult;
import top.hcode.hoj.pojo.entity.Contest;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.UserAcproblem;
import top.hcode.hoj.service.impl.*;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.IpUtils;

import java.util.concurrent.TimeUnit;

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
    private ProblemCountServiceImpl problemCountService;

    @Autowired
    private UserAcproblemServiceImpl userAcproblemService;

    @Autowired
    private UserRecordServiceImpl userRecordService;

    @Autowired
    private ContestRecordServiceImpl contestRecordService;

    @Autowired
    private ContestServiceImpl contestService;

    @PostMapping(value = "/judge") // 待定，到时再添加服务熔断兜底方法
    public CommonResult submitProblemJudge(@RequestBody Judge judge) {

        if (judge == null || judge.getSubmitId() == null || judge.getUid() == null || judge.getPid() == null) {
            return CommonResult.errorResponse("参数错误！");
        }

        judge.setStatus(Constants.Judge.STATUS_JUDGING.getStatus()); // 标志该判题过程进入评测阶段
        judge.setJudger(IpUtils.getServiceIp()); // 获取当前判题系统所在ip写入数据库
        boolean updateResult = judgeService.saveOrUpdate(judge);
        if (!updateResult) { // 出错并不影响主要业务逻辑，可以稍微记录一下即可。
            log.error("修改Judge表失效----->{}", "修改提交评判为评测队列出错");
        }
        /*
         * 先空着，此部分调用判题机进行测评,所以先返回成功~
         */
        // 线程沉睡，模仿判题过程消耗时间
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 直接默认为通过,且说明该题已被评测过
        judge.setStatus(Constants.Judge.STATUS_ACCEPTED.getStatus())
                .setTime(1)
                .setMemory(1);
        // 更新该次提交
        judgeService.updateById(judge);

        if (judge.getCid() == 0) { // 非比赛提交

            // 如果是AC,就更新user_acproblem表,
            if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                userAcproblemService.saveOrUpdate(new UserAcproblem()
                        .setPid(judge.getPid())
                        .setUid(judge.getUid())
                        .setSubmitId(judge.getSubmitId())
                );
            }
            // 比赛的提交不纳入，更新该提交对应题目的数据
            problemCountService.updateCount(Constants.Judge.STATUS_ACCEPTED.getStatus(), judge);


            // 如果是非比赛提交，且为OI题目的提交，需要判断是否更新用户得分
            if (judge.getScore() != null) {
                userRecordService.updateRecord(judge);
            }

        } else { //如果是比赛提交

            Contest contest = contestService.getById(judge.getCid());
            if (contest == null) {
                log.error("判题机出错----------->{}", "该比赛不存在");
                return CommonResult.errorResponse("该比赛不存在");
            }
            // 每个提交都得记录到contest_record里面,同时需要判断是否为比赛前的提交
            if (contest.getStatus().intValue() == Constants.Contest.STATUS_RUNNING.getCode()) {
                contestRecordService.UpdateContestRecord(judge);
            }

        }
        return CommonResult.successResponse(judge, "判题机评测完成！");
    }


}
package top.hcode.hoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.CommonResult;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.ProblemCount;
import top.hcode.hoj.pojo.entity.UserAcproblem;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.service.impl.ProblemCountServiceImpl;
import top.hcode.hoj.service.impl.UserAcproblemServiceImpl;
import top.hcode.hoj.service.impl.UserRecordServiceImpl;
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

    @PostMapping(value = "/judge") // 待定，到时再添加服务熔断兜底方法
    public CommonResult submitProblemJudge(@RequestBody Judge judge) {

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
        // 如果是AC,就更新user_acproblem表,比赛提交不纳入
        if(judge.getStatus().intValue()==Constants.Judge.STATUS_ACCEPTED.getStatus()&&judge.getCid()==0){
                userAcproblemService.saveOrUpdate(new UserAcproblem()
                        .setPid(judge.getPid())
                        .setUid(judge.getUid())
                        .setSubmitId(judge.getSubmitId())
                );
        }

        // 比赛的提交不纳入，更新该提交对应题目的数据
        if (judge.getCid()==0) {
            problemCountService.updateCount(Constants.Judge.STATUS_ACCEPTED.getStatus(), judge);
        }

        // 如果是非比赛提交，且为OI题目的提交，需要判断是否更新用户得分
        if (judge.getCid()==0 && judge.getScore()!=null) {
            userRecordService.updateRecord(judge);
        }
        return CommonResult.successResponse(judge,"判题机评测完成！");
    }


}
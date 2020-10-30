package top.hcode.hoj.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.CommonResult;
import top.hcode.hoj.dao.JudgeMapper;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.util.IpUtils;



/**
 * @Author: Himit_ZH
 * @Date: 2020/10/29 22:22
 * @Description: 处理代码提交
 */
@RestController
@Slf4j
public class JudgeController {
    @Autowired
    private JudgeMapper judgeMapper;

    @PostMapping(value = "/judge") // 待定，到时再添加服务熔断兜底方法
    public CommonResult submitProblemJudge(@RequestBody Judge judge) {
        judge.setStatus(-5); // 标志该判题过程进入评测阶段
        judge.setJudger(IpUtils.getServiceIp()); // 获取当前判题系统所在ip写入数据库
        int update = judgeMapper.updateById(judge);
        if (update != 1) { // 出错并不影响主要业务逻辑，可以稍微记录一下即可。
            log.error("修改Judge表失效----->{}", "修改提交评判为评测队列出错");
        }
        /*
        * 先空着，此部分调用判题机进行测评,所以先返回成功~
         */
        judge.setStatus(0); // 直接默认为通过
        judgeMapper.updateById(judge);
        return CommonResult.successResponse(judge,"评测成功");
    }
}
package top.hcode.hoj.common.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.judge.JudgeDispatcher;
import top.hcode.hoj.pojo.entity.CompileSpj;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.ToJudge;
import top.hcode.hoj.service.ToJudgeService;


import java.util.concurrent.TimeUnit;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/30 10:21
 * @Description:
 */
@Component
@RefreshScope
public class CloudHandler implements ToJudgeService {

    @Autowired
    private JudgeDispatcher judgeDispatcher;

    @Value("${hoj.judge.token}")
    private String judgeToken;

    // 调度判题服务器失败，可能是判题服务器有故障，或者全部达到判题最大数，那么将该提交重新进入等待队列
    @Override
    public CommonResult submitProblemJudge(ToJudge toJudge) {

        // 线程沉睡两秒，再将任务重新发布，避免过快问题，同时判题服务过多，导致的失败
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Judge judge = toJudge.getJudge();
        judgeDispatcher.sendTask(judge.getSubmitId(), judge.getPid(), judgeToken, judge.getPid() == 0);
        return CommonResult.errorResponse("判题机系统出错，提交进入重判队列，请等待管理员处理！", CommonResult.STATUS_ERROR);
    }

    @Override
    public CommonResult compileSpj(CompileSpj compileSpj) {
        return CommonResult.errorResponse("没有可用的判题服务，请重新尝试！");
    }

}
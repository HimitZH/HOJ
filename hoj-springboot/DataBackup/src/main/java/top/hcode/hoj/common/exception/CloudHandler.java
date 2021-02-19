package top.hcode.hoj.common.exception;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.judge.remote.RemoteJudgeDispatcher;
import top.hcode.hoj.judge.self.JudgeDispatcher;
import top.hcode.hoj.pojo.entity.CompileSpj;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.ToJudge;
import top.hcode.hoj.service.ToJudgeService;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;


import java.util.concurrent.TimeUnit;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/30 10:21
 * @Description: 调用判题服务器的方法的容错机制，调用失败会走到以下方法进行执行
 */
@Component
public class CloudHandler implements ToJudgeService {

    @Autowired
    private JudgeDispatcher judgeDispatcher;

    @Autowired
    private RemoteJudgeDispatcher remoteJudgeDispatcher;

    @Autowired
    private RedisUtils redisUtils;


    // 调度判题服务器失败，可能是判题服务器有故障，或者全部达到判题最大数，那么将该提交重新进入等待队列
    @Override
    public CommonResult submitProblemJudge(ToJudge toJudge) {

        // 线程沉睡1秒，再将任务重新发布，避免过快问题，同时判题服务过多，导致的失败
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Judge judge = toJudge.getJudge();
        judgeDispatcher.sendTask(judge.getSubmitId(), judge.getPid(), toJudge.getToken(), judge.getCid() == 0);
        return CommonResult.errorResponse("判题服务器繁忙或出错，提交进入重判队列，请等待管理员处理！", CommonResult.STATUS_ERROR);
    }

    @Override
    public CommonResult compileSpj(CompileSpj compileSpj) {
        return CommonResult.errorResponse("没有可用的判题服务，请重新尝试！");
    }

    @Override
    public CommonResult remoteJudge(ToJudge toJudge) {

        // 将使用的账号放回对应列表
        JSONObject account = new JSONObject();
        account.set("username", toJudge.getUsername());
        account.set("password", toJudge.getPassword());
        redisUtils.llPush(Constants.Judge.getListNameByOJName(toJudge.getRemoteJudge().split("-")[0]), JSONUtil.toJsonStr(account));

        // 线程沉睡一秒，再将任务重新发布，避免过快问题，同时判题服务过多，导致的失败
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Judge judge = toJudge.getJudge();
        remoteJudgeDispatcher.sendTask(judge.getSubmitId(), judge.getPid(), toJudge.getToken(),
                toJudge.getRemoteJudge(), judge.getCid() == 0);

        return CommonResult.errorResponse("判题服务器繁忙或出错，提交进入重判队列，请等待管理员处理！", CommonResult.STATUS_ERROR);
    }

}
package top.hcode.hoj.judge.self;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.ToJudge;
import top.hcode.hoj.service.ToJudgeService;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/5 16:43
 * @Description: 1. 判题信息的接受者，调用判题服务，对提交代码进行判断，
 * 2. 若无空闲判题服务器，会自动进入熔断机制，重新将该判题信息发布到频道内，
 * 3. 再次接受到信息，再次查询是否有空闲判题服务器，若有则进行判题，否则回到2
 */
@Component
@Async
public class JudgeReceiver {

    @Autowired
    private ToJudgeService toJudgeService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private JudgeDispatcher judgeDispatcher;

    public void processWaitingTask() {
        // 如果队列中还有任务，则继续处理
        if (redisUtils.lGetListSize(Constants.Judge.STATUS_JUDGE_WAITING.getName()) > 0) {
            String taskJsonStr = (String) redisUtils.lrPop(Constants.Judge.STATUS_JUDGE_WAITING.getName());
            // 再次检查
            if (taskJsonStr != null) {
                handleJudgeMsg(taskJsonStr);
            }
        }
    }

    private void handleJudgeMsg(String taskJsonStr) {

        JSONObject task = JSONUtil.parseObj(taskJsonStr);
        Long submitId = task.getLong("submitId");
        String token = task.getStr("token");
        Integer tryAgainNum = task.getInt("tryAgainNum");
        Judge judge = judgeService.getById(submitId);
        // 调用判题服务
        toJudgeService.submitProblemJudge(new ToJudge()
                .setJudge(judge)
                .setToken(token)
                .setRemoteJudge(null)
                .setTryAgainNum(tryAgainNum));

        // 接着处理任务
        processWaitingTask();

    }

}
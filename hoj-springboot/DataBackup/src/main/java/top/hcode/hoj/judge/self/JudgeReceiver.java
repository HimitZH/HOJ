package top.hcode.hoj.judge.self;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.hcode.hoj.judge.AbstractReceiver;
import top.hcode.hoj.judge.Dispatcher;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.ToJudge;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/5 16:43
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class JudgeReceiver extends AbstractReceiver {

    @Autowired
    private Dispatcher dispatcher;

    @Autowired
    private RedisUtils redisUtils;

    @Async("judgeTaskAsyncPool")
    public void processWaitingTask() {
        // 优先处理比赛的提交
        // 其次处理普通提交的提交
        handleWaitingTask(Constants.Queue.CONTEST_JUDGE_WAITING.getName(),
                Constants.Queue.GENERAL_JUDGE_WAITING.getName());
    }


    @Override
    public String getTaskByRedis(String queue) {
        long size = redisUtils.lGetListSize(queue);
        if (size > 0) {
            return (String) redisUtils.lrPop(queue);
        } else {
            return null;
        }
    }

    @Override
    public void handleJudgeMsg(String taskJsonStr) {
        JSONObject task = JSONUtil.parseObj(taskJsonStr);
        Judge judge = task.get("judge", Judge.class);
        String token = task.getStr("token");
        // 调用判题服务
        dispatcher.dispatcher("judge", "/judge", new ToJudge()
                .setJudge(judge)
                .setToken(token)
                .setRemoteJudgeProblem(null));
        // 接着处理任务
        processWaitingTask();
    }

}
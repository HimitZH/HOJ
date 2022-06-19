package top.hcode.hoj.judge.self;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.judge.AbstractReceiver;
import top.hcode.hoj.judge.Dispatcher;
import top.hcode.hoj.pojo.dto.TestJudgeReq;
import top.hcode.hoj.pojo.dto.ToJudgeDTO;
import top.hcode.hoj.pojo.entity.judge.Judge;
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

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Async("judgeTaskAsyncPool")
    public void processWaitingTask() {
        // 优先处理比赛的提交任务
        // 其次处理普通提交的提交任务
        // 最后处理在线调试的任务
        handleWaitingTask(Constants.Queue.CONTEST_JUDGE_WAITING.getName(),
                Constants.Queue.GENERAL_JUDGE_WAITING.getName(),
                Constants.Queue.TEST_JUDGE_WAITING.getName());
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
    public void handleJudgeMsg(String taskStr, String queueName) {
        if (Constants.Queue.TEST_JUDGE_WAITING.getName().equals(queueName)) {
            TestJudgeReq testJudgeReq = JSONUtil.toBean(taskStr, TestJudgeReq.class);
            dispatcher.dispatcherTestJudge(testJudgeReq, "/test-judge");
        } else {
            JSONObject task = JSONUtil.parseObj(taskStr);
            Long judgeId = task.getLong("judgeId");
            Judge judge = judgeEntityService.getById(judgeId);
            if (judge != null) {
                String token = task.getStr("token");
                // 调用判题服务
                dispatcher.dispatcherJudge("judge", "/judge", new ToJudgeDTO()
                        .setJudge(judge)
                        .setToken(token)
                        .setRemoteJudgeProblem(null));
            }

        }
        // 接着处理任务
        processWaitingTask();
    }

}
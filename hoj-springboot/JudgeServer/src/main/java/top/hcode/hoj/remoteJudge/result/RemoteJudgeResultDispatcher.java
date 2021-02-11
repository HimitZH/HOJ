package top.hcode.hoj.remoteJudge.result;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.RedisUtils;

@Component
@Slf4j
public class RemoteJudgeResultDispatcher {
    @Autowired
    private RedisUtils redisUtils;

    public void sendTask(String remoteJudge, Long submitId) throws Exception {
        JSONObject task = new JSONObject();
        task.set("submitId", submitId);
        task.set("remoteJudge", remoteJudge);
        // 提醒判题机有待查询结果题目
        redisUtils.sendMessage(Constants.RemoteJudge.JUDGE_RESULT_HANDLER.getName(), "New Result Query Added");
        // 队列中插入待判ID和题库名称
        redisUtils.lrPush(Constants.RemoteJudge.JUDGE_WAITING_RESULT_QUEUE.getName(), JSONUtil.toJsonStr(task));
    }
}

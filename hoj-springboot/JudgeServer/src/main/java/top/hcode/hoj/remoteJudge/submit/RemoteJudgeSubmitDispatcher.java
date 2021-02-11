package top.hcode.hoj.remoteJudge.submit;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.RedisUtils;

@Component
@Slf4j
public class RemoteJudgeSubmitDispatcher {
    @Autowired
    private RedisUtils redisUtils;


    public void sendTask(String remoteJudge, Long pid, String language, String userCode) throws Exception {
        JSONObject task = new JSONObject();
        task.set("pid", pid);
        task.set("remoteJudge", remoteJudge);
        task.set("userCode", userCode);
        task.set("language", language);
        redisUtils.sendMessage(Constants.RemoteJudge.JUDGE_SUBMIT_HANDLER.getName(), "New Problem Added");
        redisUtils.lrPush(Constants.RemoteJudge.JUDGE_WAITING_SUBMIT_QUEUE.getName(), JSONUtil.toJsonStr(task));
    }
}

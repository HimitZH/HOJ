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

    public void sendTask(Long submitId, Long pid, String remoteJudge, Boolean isContest) {
        JSONObject task = new JSONObject();
        task.set("submitId", submitId);
        task.set("pid", pid);
        task.set("remoteJudge", remoteJudge);
        task.set("isContest", isContest);
        try {
            redisUtils.sendMessage(Constants.RemoteJudge.STATUS_JUDGE_WAITING_RESULT.getName(), JSONUtil.toJsonStr(task));
        } catch (Exception e) {

        }
    }
}

package top.hcode.hoj.remotejudge;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

@Component
@Slf4j
public class RemoteJudgeSubmitDispatcher {
    @Autowired
    private RedisUtils redisUtils;

    public void sendTask(Long submitId, Long pid, String remoteJudge, Boolean isContest) {
        JSONObject task = new JSONObject();
        task.set("submitId", submitId);
        task.set("remoteJudge", remoteJudge);
        task.set("isContest", isContest);
        try {
            redisUtils.sendMessage(Constants.RemoteJudge.STATUS_JUDGE_WAITING_SUBMIT.getName(), JSONUtil.toJsonStr(task));
        } catch (Exception e) {

        }
    }
}

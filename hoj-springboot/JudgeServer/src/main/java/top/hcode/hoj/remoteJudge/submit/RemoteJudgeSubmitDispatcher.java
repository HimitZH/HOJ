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


    public void sendTask(String username, String password, String remoteJudge, String remotePid, Long submitId,
                         String uid, Long cid, Long pid, String language, String userCode) throws Exception {
        JSONObject task = new JSONObject();
        task.set("submitId", submitId);
        task.set("uid", uid);
        task.set("cid", cid);
        task.set("pid", pid);
        task.set("remoteJudge", remoteJudge);
        task.set("remotePid", remotePid);
        task.set("userCode", userCode);
        task.set("language", language);
        task.set("username", username);
        task.set("password", password);
        redisUtils.sendMessage(Constants.RemoteJudge.JUDGE_SUBMIT_HANDLER.getName(), "New Problem Added");
        redisUtils.lrPush(Constants.RemoteJudge.JUDGE_WAITING_SUBMIT_QUEUE.getName(), JSONUtil.toJsonStr(task));
    }
}

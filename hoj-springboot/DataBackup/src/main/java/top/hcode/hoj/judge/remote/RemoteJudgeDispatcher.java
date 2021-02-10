package top.hcode.hoj.judge.remote;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

@Component
@Slf4j
public class RemoteJudgeDispatcher {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JudgeServiceImpl judgeService;

    public void sendTask(Long submitId, Long pid, String token, String remoteJudge, Boolean isContest) {
        JSONObject task = new JSONObject();
        task.set("submitId", submitId);
        task.set("pid", pid);
        task.set("remoteJudge", remoteJudge);
        task.set("token", token);
        task.set("isContest", isContest);
        try {
            redisUtils.sendMessage(Constants.Judge.STATUS_REMOTE_JUDGE_WAITING_HANDLE.getName(), JSONUtil.toJsonStr(task));
        } catch (Exception e) {
            log.error("调用redis消息发布异常,此次判题任务判为系统错误--------------->{}", e.getMessage());
            judgeService.failToUseRedisPublishJudge(submitId, pid, isContest);
        }
    }
}

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

    public void sendTask(Long submitId, Long pid, String token, String remoteJudge, Boolean isContest, Integer tryAgainNum) {
        JSONObject task = new JSONObject();
        task.set("submitId", submitId);
        task.set("pid", pid);
        task.set("remoteJudge", remoteJudge);
        task.set("token", token);
        task.set("isContest", isContest);
        task.set("tryAgainNum", tryAgainNum);
        try {
            // 对应列表右边取出账号
            String account = (String) redisUtils.lrPop(Constants.Judge.getListNameByOJName(remoteJudge.split("-")[0]));
            if (account != null) {
                JSONObject accountJson = JSONUtil.parseObj(account);
                task.set("username", accountJson.getStr("username", null));
                task.set("password", accountJson.getStr("password", null));
            } else {
                task.set("username", null);
                task.set("password", null);
            }

            redisUtils.sendMessage(Constants.Judge.STATUS_REMOTE_JUDGE_WAITING_HANDLE.getName(), JSONUtil.toJsonStr(task));
        } catch (Exception e) {
            log.error("调用redis消息发布异常,此次判题任务判为系统错误--------------->{}", e.getMessage());
            judgeService.failToUseRedisPublishJudge(submitId, pid, isContest);
        }
    }
}

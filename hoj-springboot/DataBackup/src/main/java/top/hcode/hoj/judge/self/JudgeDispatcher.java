package top.hcode.hoj.judge.self;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/5 16:44
 * @Description: 判题信息的发布者，通过主题发布到特定频道内
 */
@Component
@Slf4j
public class JudgeDispatcher {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JudgeServiceImpl judgeService;

    public void sendTask(Long submitId, Long pid, String token, Boolean isContest) {
        JSONObject task = new JSONObject();
        task.set("submitId", submitId);
        task.set("pid", pid);
        task.set("token", token);
        task.set("isContest", isContest);

        try {
            redisUtils.sendMessage(Constants.Judge.STATUS_JUDGE_WAITING.getName(), JSONUtil.toJsonStr(task));
        } catch (Exception e) {
            log.error("调用redis消息发布异常,此次判题任务判为系统错误--------------->{}", e.getMessage());
            judgeService.failToUseRedisPublishJudge(submitId, pid, isContest);
        }
    }
}
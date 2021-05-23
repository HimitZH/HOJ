package top.hcode.hoj.judge.self;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/5 16:44
 * @Description: 判题信息的发布者，通过主题发布到特定频道内
 */
@Component
@Slf4j(topic = "hoj")
public class JudgeDispatcher {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private JudgeReceiver judgeReceiver;

    public void sendTask(Judge judge, String token, Boolean isContest, Integer tryAgainNum) {
        JSONObject task = new JSONObject();
        task.set("judge", judge);
        task.set("token", token);
        task.set("isContest", isContest);
        task.set("tryAgainNum", tryAgainNum);
        try {
            boolean isOk = redisUtils.llPush(Constants.Judge.STATUS_JUDGE_WAITING.getName(), JSONUtil.toJsonStr(task));
            if (!isOk) {
                judgeService.updateById(new Judge()
                        .setSubmitId(judge.getSubmitId())
                        .setStatus(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                        .setErrorMessage("Please try to submit again!")
                );
            } else {
                // 调用判题任务处理
                judgeReceiver.processWaitingTask();
            }
        } catch (Exception e) {
            log.error("调用redis将判题纳入判题等待队列异常,此次判题任务判为系统错误--------------->{}", e.getMessage());
            judgeService.failToUseRedisPublishJudge(judge.getSubmitId(), judge.getPid(), isContest);
        }
    }
}
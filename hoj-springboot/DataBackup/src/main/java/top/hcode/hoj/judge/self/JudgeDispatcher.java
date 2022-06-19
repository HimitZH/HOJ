package top.hcode.hoj.judge.self;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.pojo.dto.TestJudgeReq;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/5 16:44
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
@RefreshScope
public class JudgeDispatcher {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private JudgeReceiver judgeReceiver;

    @Value("${hoj.judge.token}")
    private String judgeToken;

    public void sendTask(Long judgeId, Long pid, Boolean isContest) {
        JSONObject task = new JSONObject();
        task.set("judgeId", judgeId);
        task.set("token", judgeToken);
        task.set("isContest", isContest);
        try {
            boolean isOk;
            if (isContest) {
                isOk = redisUtils.llPush(Constants.Queue.CONTEST_JUDGE_WAITING.getName(), JSONUtil.toJsonStr(task));
            } else {
                isOk = redisUtils.llPush(Constants.Queue.GENERAL_JUDGE_WAITING.getName(), JSONUtil.toJsonStr(task));
            }
            if (!isOk) {
                judgeEntityService.updateById(new Judge()
                        .setSubmitId(judgeId)
                        .setStatus(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                        .setErrorMessage("Call Redis to push task error. Please try to submit again!")
                );
            }
            // 调用判题任务处理
            judgeReceiver.processWaitingTask();
        } catch (Exception e) {
            log.error("调用redis将判题纳入判题等待队列异常--------------->{}", e.getMessage());
            judgeEntityService.failToUseRedisPublishJudge(judgeId, pid, isContest);
        }
    }

    public void sendTestJudgeTask(TestJudgeReq testJudgeReq) throws StatusSystemErrorException {
        testJudgeReq.setToken(judgeToken);
        try {
            boolean isOk = redisUtils.llPush(Constants.Queue.TEST_JUDGE_WAITING.getName(), JSONUtil.toJsonStr(testJudgeReq));
            if (!isOk) {
                throw new StatusSystemErrorException("系统错误：当前评测任务进入等待队列失败！");
            }
            // 调用判题任务处理
            judgeReceiver.processWaitingTask();
        } catch (Exception e) {
            log.error("调用redis将判题纳入判题等待队列异常--------------->{}", e.getMessage());
            throw new StatusSystemErrorException("系统错误：当前评测任务进入等待队列失败！");
        }
    }
}
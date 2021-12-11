package top.hcode.hoj.judge.remote;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.service.judge.impl.JudgeServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;


@Component
@Slf4j(topic = "hoj")
public class RemoteJudgeDispatcher {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private RemoteJudgeReceiver remoteJudgeReceiver;

    public void sendTask(Judge judge, String token, String remoteJudgeProblem, Boolean isContest,
                         Integer tryAgainNum, Boolean isHasSubmitIdRemoteReJudge) {
        JSONObject task = new JSONObject();
        task.set("judge", judge);
        task.set("remoteJudgeProblem", remoteJudgeProblem);
        task.set("token", token);
        task.set("isContest", isContest);
        task.set("tryAgainNum", tryAgainNum);
        task.set("isHasSubmitIdRemoteReJudge", isHasSubmitIdRemoteReJudge);
        try {
            boolean isOk = redisUtils.llPush(Constants.Judge.STATUS_REMOTE_JUDGE_WAITING_HANDLE.getName(), JSONUtil.toJsonStr(task));
            if (!isOk) {
                judgeService.updateById(new Judge()
                        .setSubmitId(judge.getSubmitId())
                        .setStatus(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                        .setErrorMessage("Please try to submit again!")
                );
            } else {
                remoteJudgeReceiver.processWaitingTask();
            }
        } catch (Exception e) {
            log.error("调用redis将判题纳入判题等待队列异常,此次判题任务判为系统错误--------------->", e);
            judgeService.failToUseRedisPublishJudge(judge.getSubmitId(), judge.getPid(), isContest);
        }
    }
}

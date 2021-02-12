package top.hcode.hoj.remoteJudge.submit;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import top.hcode.hoj.remoteJudge.result.RemoteJudgeResultDispatcher;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeFactory;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.RedisUtils;

@Component
@Slf4j
public class RemoteJudgeSubmitReceiver implements MessageListener {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RemoteJudgeResultDispatcher remoteJudgeResultDispatcher;

    @Override
    public void onMessage(Message message, byte[] bytes) {
//        log.debug("RemoteJudgeSubmitReceiver获取到消息{}", Arrays.toString(message.getBody()));
        String source = (String) redisUtils.lrPop(Constants.RemoteJudge.JUDGE_WAITING_SUBMIT_QUEUE.getName());
        // 如果竞争不到提交队列，结束
        if (source == null) {
            return;
        }
        JSONObject task = JSONUtil.parseObj(source);
        Long pid = task.getLong("pid");
        String remoteJudge = task.getStr("remoteJudge");
        String language = task.getStr("language");
        String userCode = task.getStr("userCode");
        RemoteJudgeStrategy remoteJudgeStrategy = RemoteJudgeFactory.selectJudge(remoteJudge);
        // 获取不到对应的题库或者题库写错了
        if (remoteJudgeStrategy == null) {
            log.error("暂不支持该{}题库---------------->请求失败", remoteJudge);
            return;
        }
        Long submitId = -1L;
        try {
            submitId = remoteJudgeStrategy.submit(pid, language, userCode);
        } catch (Exception e) {
            log.error("网络错误，提交失败");
        }
        // TODO 提交失败 前端手动按按钮再次提交 修改状态 STATUS_NOT_SUBMITTED
        if (submitId < 0) {
            log.error("网络错误---------------->获取不到提交ID");
            return;
        }
        try {
            remoteJudgeResultDispatcher.sendTask(remoteJudge, submitId);
        } catch (Exception e) {
            log.error("调用redis消息发布异常,此次远程查询结果任务判为系统错误--------------->{}", e.getMessage());
        }

    }
}

package top.hcode.hoj.remoteJudge.result;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeFactory;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.RedisUtils;

@Slf4j
@Component
public class RemoteJudgeResultReceiver implements MessageListener {
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String source = (String) redisUtils.lrPop(Constants.RemoteJudge.JUDGE_WAITING_SUBMIT_QUEUE.getName());
        // 如果竞争不到查询队列，结束
        if (source == null) {
            return;
        }
        JSONObject task = JSONUtil.parseObj(source);
        Long submitId = task.getLong("submitId");
        String remoteJudge = task.getStr("remoteJudge");
        RemoteJudgeStrategy remoteJudgeStrategy = RemoteJudgeFactory.selectJudge(remoteJudge);
        // 获取不到对应的题库或者题库写错了
        if (remoteJudgeStrategy == null) {
            log.error("暂不支持该{}题库---------------->请求失败", remoteJudge);
            return;
        }

        String result = remoteJudgeStrategy.result(submitId);
        // TODO 获取对应的result ID，修改到数据库

    }
}

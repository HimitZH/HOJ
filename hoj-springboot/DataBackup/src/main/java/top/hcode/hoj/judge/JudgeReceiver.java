package top.hcode.hoj.judge;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.ToJudge;
import top.hcode.hoj.service.ToJudgeService;
import top.hcode.hoj.service.impl.JudgeServiceImpl;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/5 16:43
 * @Description:
 */
@Component
public class JudgeReceiver implements MessageListener {

    @Autowired
    private ToJudgeService toJudgeService;

    @Autowired
    private JudgeServiceImpl judgeService;


    @Override
    public void onMessage(Message message, byte[] bytes) {

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//        //接收的topic
//        String channel = stringRedisSerializer.deserialize(message.getChannel());
        // 进行反序列化获取信息
        String taskJson = (String)jackson2JsonRedisSerializer.deserialize(message.getBody());
        JSONObject task = JSONUtil.parseObj(taskJson);
        Long submitId = task.getLong("submitId");
        String token = task.getStr("token");
        Judge judge = judgeService.getById(submitId);
        // 调用判题服务
        toJudgeService.submitProblemJudge(new ToJudge().setJudge(judge).setToken(token));
    }
}
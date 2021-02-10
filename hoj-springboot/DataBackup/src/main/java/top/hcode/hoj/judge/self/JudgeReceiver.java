package top.hcode.hoj.judge.self;

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
 * 1. 判题信息的接受者，调用判题服务，对提交代码进行判断，
 * 2. 若无空闲判题服务器，会自动进入熔断机制，重新将该判题信息发布到频道内，
 * 3. 再次接受到信息，再次查询是否有空闲判题服务器，若有则进行判题，否则回到2
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
        toJudgeService.submitProblemJudge(new ToJudge().setJudge(judge).setToken(token).setRemoteJudge(null));
    }
}
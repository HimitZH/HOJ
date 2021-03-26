package top.hcode.hoj.judge.remote;

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
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

@Component
public class RemoteJudgeReceiver implements MessageListener {

    @Autowired
    private ToJudgeService toJudgeService;

    @Autowired
    private RemoteJudgeDispatcher remoteJudgeDispatcher;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        String taskJson = (String) jackson2JsonRedisSerializer.deserialize(message.getBody());
        JSONObject task = JSONUtil.parseObj(taskJson);
        Long submitId = task.getLong("submitId");
        String token = task.getStr("token");
        Long pid = task.getLong("pid");
        String remoteJudge = task.getStr("remoteJudge");
        Boolean isContest = task.getBool("isContest");
        String username = task.getStr("username");
        String password = task.getStr("password");
        Integer tryAgainNum = task.getInt("tryAgainNum");

        System.out.println(username);
        System.out.println(password);

        if (username == null || password == null) {
            remoteJudgeDispatcher.sendTask(submitId, pid, token, remoteJudge, isContest, tryAgainNum);
            return;
        }

        Judge judge = judgeService.getById(submitId);

        // 调用判题服务
        toJudgeService.remoteJudge(new ToJudge()
                .setJudge(judge)
                .setToken(token)
                .setRemoteJudge(remoteJudge)
                .setUsername(username)
                .setPassword(password)
                .setTryAgainNum(tryAgainNum));
    }
}

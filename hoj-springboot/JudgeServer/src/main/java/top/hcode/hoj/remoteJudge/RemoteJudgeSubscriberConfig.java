package top.hcode.hoj.remoteJudge;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import top.hcode.hoj.remoteJudge.result.RemoteJudgeResultReceiver;
import top.hcode.hoj.remoteJudge.submit.RemoteJudgeSubmitReceiver;
import top.hcode.hoj.util.Constants;


@Configuration
@AutoConfigureAfter({RemoteJudgeSubmitReceiver.class, RemoteJudgeResultReceiver.class})
public class RemoteJudgeSubscriberConfig {


    /**
     * 消息监听适配器，注入接受消息方法，处理提交代码到其它oj的消息
     *
     * @param receiver
     * @return
     */
    @Bean
    public MessageListenerAdapter toSubmitMessageListenerAdapter(RemoteJudgeSubmitReceiver receiver) {
        return new MessageListenerAdapter(receiver); //当没有继承MessageListener时需要写方法名字
    }


    /**
     * 消息监听适配器，注入接受消息方法，处理获取提交结果的消息
     *
     * @param receiver
     * @return
     */
    @Bean
    public MessageListenerAdapter getResultMessageListenerAdapter(RemoteJudgeResultReceiver receiver) {
        return new MessageListenerAdapter(receiver); //当没有继承MessageListener时需要写方法名字
    }

    /**
     * 创建消息监听容器
     *
     * @param redisConnectionFactory
     * @param toSubmitMessageListenerAdapter
     * @param getResultMessageListenerAdapter
     * @return
     */
    @Bean
    public RedisMessageListenerContainer getRedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                          MessageListenerAdapter toSubmitMessageListenerAdapter,
                                                                          MessageListenerAdapter getResultMessageListenerAdapter) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        // 添加频道名字
        redisMessageListenerContainer.addMessageListener(toSubmitMessageListenerAdapter, new PatternTopic(Constants.RemoteJudge.STATUS_JUDGE_WAITING_SUBMIT.getName()));
        redisMessageListenerContainer.addMessageListener(getResultMessageListenerAdapter, new PatternTopic(Constants.RemoteJudge.STATUS_JUDGE_WAITING_RESULT.getName()));
        return redisMessageListenerContainer;
    }

}
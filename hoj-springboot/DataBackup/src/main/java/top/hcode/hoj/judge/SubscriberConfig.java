package top.hcode.hoj.judge;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import top.hcode.hoj.utils.Constants;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/5 16:50
 * @Description:
 */

@Configuration
@AutoConfigureAfter({JudgeReceiver.class})
public class SubscriberConfig {


    /**
     * 消息监听适配器，注入接受消息方法，输入方法名字 反射方法
     *
     * @param receiver
     * @return
     */
    @Bean
    public MessageListenerAdapter getMessageListenerAdapter(JudgeReceiver receiver) {
        return new MessageListenerAdapter(receiver); //当没有继承MessageListener时需要写方法名字
    }

    /**
     * 创建消息监听容器
     *
     * @param redisConnectionFactory
     * @param messageListenerAdapter
     * @return
     */
    @Bean
    public RedisMessageListenerContainer getRedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory, MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        // 添加频道名字
        redisMessageListenerContainer.addMessageListener(messageListenerAdapter, new PatternTopic(Constants.Judge.STATUS_JUDGE_WAITING.getName()));
        return redisMessageListenerContainer;
    }

}
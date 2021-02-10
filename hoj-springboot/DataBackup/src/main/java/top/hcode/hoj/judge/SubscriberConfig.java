package top.hcode.hoj.judge;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import top.hcode.hoj.judge.remote.RemoteJudgeReceiver;
import top.hcode.hoj.judge.self.JudgeReceiver;
import top.hcode.hoj.utils.Constants;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/5 16:50
 * @Description: redis的发布订阅者模式的消息频道与订阅者的注册绑定
 */

@Configuration
@AutoConfigureAfter({JudgeReceiver.class, RemoteJudgeReceiver.class})
public class SubscriberConfig {


    /**
     * 消息监听适配器，注入接受消息方法，注入自己的判题服务的消息接收者
     *
     * @param receiver
     * @return
     */
    @Bean
    public MessageListenerAdapter selfJudgeMessageListenerAdapter(JudgeReceiver receiver) {
        return new MessageListenerAdapter(receiver); //当没有继承MessageListener时需要写方法名字
    }


    /**
     * 消息监听适配器，注入接受消息方法，注入远程判题服务的消息接收者
     *
     * @param receiver
     * @return
     */
    @Bean
    public MessageListenerAdapter remoteJudgeMessageListenerAdapter(RemoteJudgeReceiver receiver) {
        return new MessageListenerAdapter(receiver); //当没有继承MessageListener时需要写方法名字
    }


    /**
     * 创建消息监听容器
     *
     * @param redisConnectionFactory
     * @param selfJudgeMessageListenerAdapter
     * @param remoteJudgeMessageListenerAdapter
     * @return
     */
    @Bean
    public RedisMessageListenerContainer getRedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                          MessageListenerAdapter selfJudgeMessageListenerAdapter,
                                                                          MessageListenerAdapter remoteJudgeMessageListenerAdapter) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        // 注册信息接收者绑定自己判题服务的频道
        redisMessageListenerContainer.addMessageListener(selfJudgeMessageListenerAdapter, new PatternTopic(Constants.Judge.STATUS_JUDGE_WAITING.getName()));
        // 注册信息接收者绑定远程虚拟判题的频道
        redisMessageListenerContainer.addMessageListener(remoteJudgeMessageListenerAdapter, new PatternTopic(Constants.Judge.STATUS_REMOTE_JUDGE_WAITING_HANDLE.getName()));
        return redisMessageListenerContainer;
    }

}
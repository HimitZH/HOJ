package top.hcode.hoj.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Optional;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/21 15:53
 * @Description:
 */
@Configuration
public class RedisAutoConfig {

    @Value("${spring.redis.jedis.pool.max-active:200}")
    private Integer maxActive;
    @Value("${spring.redis.jedis.pool.max-idle:50}")
    private Integer maxIdle;
    @Value("${spring.redis.jedis.pool.max-wait:-1}")
    private Long maxWait;
    @Value("${spring.redis.jedis.pool.min-idle:10}")
    private Integer minIdle;

    @Value("${redis-host}")
    private String redisHost;
    @Value("${redis-port}")
    private Integer redisPort;
    @Value("${redis-password}")
    private String redisPassword;

    @Autowired
    private JedisPoolConfigure jedisPoolConfigure;


    @Bean
    public RedisConnectionFactory redisConnectionFactory(JedisPoolConfig jedisPool,
                                                         RedisStandaloneConfiguration jedisConfig) {
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(jedisConfig);
        connectionFactory.setPoolConfig(jedisPool);
        return connectionFactory;
    }

    @Bean
    public JedisPoolConfig jedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWait);
        jedisPoolConfig.setMaxTotal(maxActive);
        jedisPoolConfig.setMinIdle(minIdle);
        return jedisPoolConfig;
    }

    @Bean
    @RefreshScope
    public RedisStandaloneConfiguration jedisConfig() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(Optional.ofNullable(jedisPoolConfigure.getHost()).orElseGet(() -> redisHost));
        config.setPort(Optional.ofNullable(jedisPoolConfigure.getPort()).orElseGet(() -> redisPort));
        config.setPassword(RedisPassword.of(Optional.ofNullable(jedisPoolConfigure.getPassword()).orElseGet(() -> redisPassword)));
        return config;
    }

}

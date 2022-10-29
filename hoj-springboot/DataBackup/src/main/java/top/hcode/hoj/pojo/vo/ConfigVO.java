package top.hcode.hoj.pojo.vo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;


/**
 * @Author: Himit_ZH
 * @Date: 2020/12/2 21:30
 * @Description:
 */
@RefreshScope
@Data
@Component
public class ConfigVO {
    // 数据库配置
    @Value("${hoj.db.username}")
    private String mysqlUsername;

    @Value("${hoj.db.password}")
    private String mysqlPassword;

    @Value("${hoj.db.name}")
    private String mysqlDBName;

    @Value("${hoj.db.host}")
    private String mysqlHost;

    @Value("${hoj.db.public-host:172.20.0.3}")
    private String mysqlPublicHost;

    @Value("${hoj.db.port}")
    private Integer mysqlPort;

    @Value("${hoj.db.public-port:3306}")
    private Integer mysqlPublicPort;

    // 判题服务token
    @Value("${hoj.judge.token}")
    private String judgeToken;

    // 缓存配置
    @Value("${hoj.redis.host}")
    private String redisHost;

    @Value("${hoj.redis.port}")
    private Integer redisPort;

    @Value("${hoj.redis.password}")
    private String redisPassword;

    // jwt配置
    @Value("${hoj.jwt.secret}")
    private String tokenSecret;

    @Value("${hoj.jwt.expire}")
    private String tokenExpire;

    @Value("${hoj.jwt.checkRefreshExpire}")
    private String checkRefreshExpire;
}

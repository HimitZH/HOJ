package top.hcode.hoj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @Author Himit_ZH
 * @Date 2022/6/15
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "hoj.redis")
@Data
public class JedisPoolConfigure {
    private String host;

    private Integer port;

    private String password;
}

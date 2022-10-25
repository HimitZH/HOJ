package top.hcode.hoj.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @Author Himit_ZH
 * @Date 2022/6/15
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "hoj.db")
@Data
public class DataSourceConfigure {

    private String username;

    private String password;

    @Value("${hoj.db.public-host}")
    private String host;

    @Value("${hoj.db.public-port}")
    private Integer port;

    private String name;
}

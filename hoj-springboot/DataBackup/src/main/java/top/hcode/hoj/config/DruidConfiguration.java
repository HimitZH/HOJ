package top.hcode.hoj.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/21 17:57
 * @Description:
 */
@Configuration
@RefreshScope
@Data
public class DruidConfiguration {

    @Value("${hoj.db.username:${MYSQL_USERNAME:root}}")
    private String username;

    @Value("${hoj.db.password:${MYSQL_ROOT_PASSWORD:hoj123456}}")
    private String password;

    @Value("${hoj.db.host:${MYSQL_HOST:172.20.0.3}}")
    private String host;

    @Value("${hoj.db.port:${MYSQL_PORT:3306}}")
    private Integer port;

    @Value("${hoj.db.name:${MYSQL_DATABASE_NAME:hoj}}")
    private String name;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.initial-size}")
    private Integer initialSize;

    @Value("${spring.datasource.min-idle}")
    private Integer minIdle;

    @Value("${spring.datasource.maxActive}")
    private Integer maxActive;

    @Value("${spring.datasource.maxWait}")
    private Integer maxWait;

    @Bean(name = "datasource")
    @RefreshScope
    public DruidDataSource dataSource() {
        DruidDataSource datasource = new DruidDataSource();
        String url = "jdbc:mysql://" + host + ":" + port + "/" + name + "?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true&rewriteBatchedStatements=true";
        datasource.setUrl(url);
        datasource.setUsername(username);
        datasource.setPassword(password);
        datasource.setDriverClassName(driverClassName);
        datasource.setMaxActive(maxActive);
        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxWait(maxWait);
        return datasource;
    }
}
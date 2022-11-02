package top.hcode.hoj.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/21 17:57
 * @Description:
 */
@Configuration
@Slf4j(topic = "hoj")
@RefreshScope
@Data
public class DruidConfig {

    @Value("${hoj.db.username:root}")
    private String username;

    @Value("${hoj.db.password:hoj123456}")
    private String password;

    @Value("${hoj.db.host:172.20.0.3}")
    private String host;

    @Value("${hoj.db.port:3306}")
    private Integer port;

    @Value("${hoj.db.public-host:172.20.0.3}")
    private String publicHost;

    @Value("${hoj.db.public-port:3306}")
    private Integer publicPort;

    @Value("${hoj.db.name:hoj}")
    private String name;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.type}")
    private String type;

    @Value("${spring.datasource.initial-size:10}")
    private Integer initialSize;

    @Value("${spring.datasource.poolPreparedStatements:true}")
    private Boolean poolPreparedStatements;

    @Value("${spring.datasource.maxPoolPreparedStatementPerConnectionSize:20}")
    private Integer maxPoolPreparedStatementPerConnectionSize;

    @Value("${spring.datasource.timeBetweenEvictionRunsMillis:60000}")
    private Integer timeBetweenEvictionRunsMillis;

    @Value("${spring.datasource.minEvictableIdleTimeMillis:300000}")
    private Integer minEvictableIdleTimeMillis;

    @Value("${spring.datasource.validationQuery}")
    private String validationQuery;

    @Value("${spring.datasource.testWhileIdle:true}")
    private Boolean testWhileIdle;

    @Value("${spring.datasource.testOnBorrow:false}")
    private Boolean testOnBorrow;

    @Value("${spring.datasource.testOnReturn:false}")
    private Boolean testOnReturn;

    @Value("${spring.datasource.connectionErrorRetryAttempts:3}")
    private Integer connectionErrorRetryAttempts;

    @Value("${spring.datasource.breakAfterAcquireFailure:true}")
    private Boolean breakAfterAcquireFailure;

    @Value("${spring.datasource.timeBetweenConnectErrorMillis:300000}")
    private Integer timeBetweenConnectErrorMillis;

    @Value("${spring.datasource.min-idle:20}")
    private Integer minIdle;

    @Value("${spring.datasource.maxActive:40}")
    private Integer maxActive;

    @Value("${spring.datasource.maxWait:60000}")
    private Integer maxWait;

    @Bean(name = "datasource")
    @RefreshScope
    public DruidDataSource dataSource() {

        String mysqlHost = publicHost;
        Integer mysqlPort = publicPort;
        if (Objects.equals(publicHost, host)) {
            // 如果judgeServer访问的mysql的host与backend访问的一致，则用backend的端口号
            mysqlPort = port;
        }
        String mysqlName = name;
        String mysqlUsername = username;
        String mysqlUserPassword = password;

        log.warn("[MySQL] [Config Init] name:[{}], host:[{}], port:[{}], username:[{}], password:[{}]",
                mysqlName, mysqlHost, mysqlPort, mysqlUsername, mysqlUserPassword);

        DruidDataSource datasource = new DruidDataSource();
        String url = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + mysqlName + "?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true&rewriteBatchedStatements=true";
        datasource.setUrl(url);
        datasource.setUsername(mysqlUsername);
        datasource.setPassword(mysqlUserPassword);
        datasource.setDriverClassName(driverClassName);
        datasource.setDbType(type);
        datasource.setMaxActive(maxActive);
        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxWait(maxWait);
        datasource.setPoolPreparedStatements(poolPreparedStatements);
        datasource.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        datasource.setValidationQuery(validationQuery);
        datasource.setTestWhileIdle(testWhileIdle);
        datasource.setTestOnReturn(testOnReturn);
        datasource.setTestOnBorrow(testOnBorrow);
        datasource.setConnectionErrorRetryAttempts(connectionErrorRetryAttempts);
        datasource.setBreakAfterAcquireFailure(breakAfterAcquireFailure);
        datasource.setTimeBetweenConnectErrorMillis(timeBetweenConnectErrorMillis);
        return datasource;
    }
}
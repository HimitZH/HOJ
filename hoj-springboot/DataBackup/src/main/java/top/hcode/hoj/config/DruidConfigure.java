package top.hcode.hoj.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/21 17:57
 * @Description:
 */
@Configuration
@RefreshScope
@Data
@Slf4j(topic = "hoj")
public class DruidConfigure {

    @Value("${mysql-username}")
    private String username;

    @Value("${mysql-password}")
    private String password;

    @Value("${mysql-host}")
    private String host;

    @Value("${mysql-port}")
    private Integer port;

    @Value("${mysql-name}")
    private String name;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.type}")
    private String type;

    @Value("${spring.datasource.initial-size}")
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

    @Value("${spring.datasource.min-idle}")
    private Integer minIdle;

    @Value("${spring.datasource.maxActive}")
    private Integer maxActive;

    @Value("${spring.datasource.maxWait}")
    private Integer maxWait;

    @Autowired
    private DataSourceConfigure dataSourceConfigure;

    @Bean(name = "datasource")
    @RefreshScope
    public DruidDataSource dataSource(){

        String mysqlHost = Optional.ofNullable(dataSourceConfigure.getHost()).orElseGet(() -> host);
        Integer mysqlPort = Optional.ofNullable(dataSourceConfigure.getPort()).orElseGet(() -> port);
        String mysqlName = Optional.ofNullable(dataSourceConfigure.getName()).orElseGet(() -> name);
        String mysqlUsername = Optional.ofNullable(dataSourceConfigure.getUsername()).orElseGet(() -> username);
        String mysqlUserPassword = Optional.ofNullable(dataSourceConfigure.getPassword()).orElseGet(() -> password);

        log.warn("[MySQL] [Config Change] name:[{}], host:[{}], port:[{}], username:[{}], password:[{}]", mysqlName, mysqlHost, mysqlPort, mysqlUsername, mysqlUserPassword);

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
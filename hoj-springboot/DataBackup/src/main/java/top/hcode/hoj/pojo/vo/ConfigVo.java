package top.hcode.hoj.pojo.vo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @Author: Himit_ZH
 * @Date: 2020/12/2 21:30
 * @Description:
 */
@RefreshScope
@Data
@Component
public class ConfigVo {
    // 数据库配置
    @Value("${hoj.db.username}")
    private String mysqlUsername;

    @Value("${hoj.db.password}")
    private String mysqlPassword;

    @Value("${hoj.db.name}")
    private String mysqlDBName;

    @Value("${hoj.db.host}")
    private String mysqlHost;

    @Value("${hoj.db.public-host}")
    private String mysqlPublicHost;

    @Value("${hoj.db.port}")
    private Integer mysqlPort;

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

    // 邮箱配置
    @Value("${hoj.mail.username}")
    private String emailUsername;

    @Value("${hoj.mail.password}")
    private String emailPassword;

    @Value("${hoj.mail.host}")
    private String emailHost;

    @Value("${hoj.mail.port}")
    private Integer emailPort;

    @Value("${hoj.mail.ssl}")
    private Boolean emailSsl;

    @Value("${hoj.mail.background-img}")
    private String emailBGImg;

    // 网站前端显示配置
    @Value("${hoj.web-config.base-url}")
    private String baseUrl;

    @Value("${hoj.web-config.name}")
    private String name;

    @Value("${hoj.web-config.short-name}")
    private String shortName;

    @Value("${hoj.web-config.description}")
    private String description;

    @Value("${hoj.web-config.register}")
    private Boolean register;

    @Value("${hoj.web-config.footer.record.name}")
    private String recordName;

    @Value("${hoj.web-config.footer.record.url}")
    private String recordUrl;

    @Value("${hoj.web-config.footer.project.name}")
    private String projectName;

    @Value("${hoj.web-config.footer.project.url}")
    private String projectUrl;

    @Value("${hoj.hdu.account.username:}")
    private List<String> hduUsernameList;

    @Value("${hoj.hdu.account.password:}")
    private List<String> hduPasswordList;

    @Value("${hoj.cf.account.username:}")
    private List<String> cfUsernameList;

    @Value("${hoj.cf.account.password:}")
    private List<String> cfPasswordList;

    @Value("${hoj.poj.account.username:}")
    private List<String> pojUsernameList;

    @Value("${hoj.poj.account.password:}")
    private List<String> pojPasswordList;

    @Value("${hoj.atcoder.account.username:}")
    private List<String> atcoderUsernameList;

    @Value("${hoj.atcoder.account.password:}")
    private List<String> atcoderPasswordList;

    @Value("${hoj.spoj.account.username:}")
    private List<String> spojUsernameList;

    @Value("${hoj.spoj.account.password:}")
    private List<String> spojPasswordList;

}

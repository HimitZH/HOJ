package top.hcode.hoj.config;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.entity.RemoteJudgeAccount;
import top.hcode.hoj.pojo.vo.ConfigVo;
import top.hcode.hoj.service.impl.ConfigServiceImpl;
import top.hcode.hoj.service.impl.RemoteJudgeAccountServiceImpl;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/19 22:11
 * @Description:项目启动后，初始化运行该run方法
 */
@Component
@Slf4j(topic = "hoj")
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private ConfigVo configVo;

    @Autowired
    private ConfigServiceImpl configService;

    @Autowired
    private RemoteJudgeAccountServiceImpl remoteJudgeAccountService;

    @Value("${OPEN_REMOTE_JUDGE:true}")
    private String openRemoteJudge;

    // jwt配置
    @Value("${JWT_TOKEN_SECRET:default}")
    private String tokenSecret;

    @Value("${JWT_TOKEN_EXPIRE:86400}")
    private String tokenExpire;

    @Value("${JWT_TOKEN_FRESH_EXPIRE:43200}")
    private String checkRefreshExpire;

    // 数据库配置
    @Value("${MYSQL_USERNAME:root}")
    private String mysqlUsername;

    @Value("${MYSQL_ROOT_PASSWORD:hoj123456}")
    private String mysqlPassword;

    @Value("${MYSQL_DATABASE_NAME:hoj}")
    private String mysqlDBName;

    @Value("${MYSQL_HOST:172.20.0.3}")
    private String mysqlHost;

    @Value("${MYSQL_PUBLIC_HOST:172.20.0.3}")
    private String mysqlPublicHost;

    @Value("${MYSQL_PORT:3306}")
    private Integer mysqlPort;

    // 缓存配置
    @Value("${REDIS_HOST:172.20.0.2}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private Integer redisPort;

    @Value("${REDIS_PASSWORD:hoj123456}")
    private String redisPassword;
    // 判题服务token
    @Value("${JUDGE_TOKEN:default}")
    private String judgeToken;

    // 邮箱配置
    @Value("${EMAIL_USERNAME:your_email_username}")
    private String emailUsername;

    @Value("${EMAIL_PASSWORD:your_email_password}")
    private String emailPassword;

    @Value("${EMAIL_SERVER_HOST:your_email_host}")
    private String emailHost;

    @Value("${EMAIL_SERVER_PORT:465}")
    private Integer emailPort;

    @Value("${HDU_ACCOUNT_USERNAME_LIST:}")
    private List<String> hduUsernameList;

    @Value("${HDU_ACCOUNT_PASSWORD_LIST:}")
    private List<String> hduPasswordList;

    @Value("${CF_ACCOUNT_USERNAME_LIST:}")
    private List<String> cfUsernameList;

    @Value("${CF_ACCOUNT_PASSWORD_LIST:}")
    private List<String> cfPasswordList;

    @Override
    public void run(String... args) throws Exception {
        if (openRemoteJudge.equals("true")) {
            addRemoteJudgeAccountToRedis();
        }
        // 动态修改nacos上的配置文件
        if (judgeToken.equals("default")) {
            configVo.setJudgeToken(IdUtil.fastSimpleUUID());
        } else {
            configVo.setJudgeToken(judgeToken);
        }

        if (tokenSecret.equals("default")) {
            configVo.setTokenSecret(IdUtil.fastSimpleUUID());
        } else {
            configVo.setTokenSecret(tokenSecret);
        }
        configVo.setTokenExpire(tokenExpire);
        configVo.setCheckRefreshExpire(checkRefreshExpire);

        configVo.setMysqlUsername(mysqlUsername);
        configVo.setMysqlPassword(mysqlPassword);
        configVo.setMysqlHost(mysqlHost);
        configVo.setMysqlPublicHost(mysqlPublicHost);
        configVo.setMysqlPort(mysqlPort);
        configVo.setMysqlDBName(mysqlDBName);

        configVo.setRedisHost(redisHost);
        configVo.setRedisPort(redisPort);
        configVo.setRedisPassword(redisPassword);

        configVo.setEmailHost(emailHost);
        configVo.setEmailPort(emailPort);
        configVo.setEmailUsername(emailUsername);
        configVo.setEmailPassword(emailPassword);

        configVo.setHduUsernameList(hduUsernameList);
        configVo.setHduPasswordList(hduPasswordList);

        configVo.setCfUsernameList(cfUsernameList);
        configVo.setCfPasswordList(cfPasswordList);
        configService.sendNewConfigToNacos();
    }

    /**
     * @MethodName addRemoteJudgeAccountToRedis
     * @Params * @param null
     * @Description 将传入的对应oj账号写入到mysql
     * @Return
     * @Since 2021/5/18
     */
    private void addRemoteJudgeAccountToRedis() {

        // 初始化清空表
        remoteJudgeAccountService.remove(new QueryWrapper<>());

        List<RemoteJudgeAccount> hduRemoteAccountList = new LinkedList<>();
        for (int i = 0; i < hduUsernameList.size(); i++) {

            hduRemoteAccountList.add(new RemoteJudgeAccount()
                    .setUsername(hduUsernameList.get(i))
                    .setPassword(hduPasswordList.get(i))
                    .setStatus(true)
                    .setVersion(0L)
                    .setOj("HDU"));

        }
        if (hduRemoteAccountList.size()>0) {
            boolean addHduOk = remoteJudgeAccountService.saveOrUpdateBatch(hduRemoteAccountList);
            if (!addHduOk) {
                log.error("HDU账号添加失败------------>{}", "请检查配置文件，然后重新启动！");
            }
        }

        List<RemoteJudgeAccount> cfRemoteAccountList = new LinkedList<>();
        for (int i = 0; i < cfUsernameList.size(); i++) {
            cfRemoteAccountList.add(new RemoteJudgeAccount()
                    .setUsername(cfUsernameList.get(i))
                    .setPassword(cfPasswordList.get(i))
                    .setStatus(true)
                    .setVersion(0L)
                    .setOj("CF"));
        }
        if (cfRemoteAccountList.size()>0) {
            boolean addCFOk = remoteJudgeAccountService.saveOrUpdateBatch(cfRemoteAccountList);
            if (!addCFOk) {
                log.error("Codeforces账号添加失败------------>{}", "请检查配置文件，然后重新启动！");
            }
        }
    }

}
package top.hcode.hoj.config;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.crawler.language.LanguageContext;
import top.hcode.hoj.crawler.language.SPOJLanguageStrategy;
import top.hcode.hoj.pojo.entity.judge.RemoteJudgeAccount;
import top.hcode.hoj.pojo.entity.problem.Language;
import top.hcode.hoj.pojo.vo.ConfigVo;
import top.hcode.hoj.service.common.impl.ConfigServiceImpl;
import top.hcode.hoj.service.judge.impl.RemoteJudgeAccountServiceImpl;
import top.hcode.hoj.service.problem.impl.LanguageServiceImpl;
import top.hcode.hoj.utils.Constants;

import java.util.Arrays;
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

    @Autowired
    private LanguageServiceImpl languageService;

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

    @Value("${POJ_ACCOUNT_USERNAME_LIST:}")
    private List<String> pojUsernameList;

    @Value("${POJ_ACCOUNT_PASSWORD_LIST:}")
    private List<String> pojPasswordList;

    @Value("${ATCODER_ACCOUNT_USERNAME_LIST:}")
    private List<String> atcoderUsernameList;

    @Value("${ATCODER_ACCOUNT_PASSWORD_LIST:}")
    private List<String> atcoderPasswordList;

    @Value("${SPOJ_ACCOUNT_USERNAME_LIST:}")
    private List<String> spojUsernameList;

    @Value("${SPOJ_ACCOUNT_PASSWORD_LIST:}")
    private List<String> spojPasswordList;

    @Value("${spring.profiles.active}")
    private String profile;


    @Override
    public void run(String... args) throws Exception {

        if (profile.equals("dev")) {
            return;
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

        configVo.setPojUsernameList(pojUsernameList);
        configVo.setPojPasswordList(pojPasswordList);

        configVo.setAtcoderUsernameList(atcoderUsernameList);
        configVo.setAtcoderPasswordList(atcoderUsernameList);

        configVo.setSpojUsernameList(spojUsernameList);
        configVo.setSpojPasswordList(spojPasswordList);

        configService.sendNewConfigToNacos();

        upsertHOJLanguage("PHP", "PyPy2", "PyPy3", "JavaScript Node", "JavaScript V8");

        checkAllLanguageUpdate();

        if (openRemoteJudge.equals("true")) {
            // 初始化清空表
            remoteJudgeAccountService.remove(new QueryWrapper<>());
            addRemoteJudgeAccountToMySQL(Constants.RemoteOJ.HDU.getName(), hduUsernameList, hduPasswordList);
            addRemoteJudgeAccountToMySQL(Constants.RemoteOJ.POJ.getName(), pojUsernameList, pojPasswordList);
            addRemoteJudgeAccountToMySQL(Constants.RemoteOJ.CODEFORCES.getName(), cfUsernameList, cfPasswordList);
            addRemoteJudgeAccountToMySQL(Constants.RemoteOJ.SPOJ.getName(), spojUsernameList, spojPasswordList);
            addRemoteJudgeAccountToMySQL(Constants.RemoteOJ.ATCODER.getName(), atcoderUsernameList, atcoderPasswordList);

            checkRemoteOJLanguage(Constants.RemoteOJ.SPOJ, Constants.RemoteOJ.ATCODER);
        }

    }

    /**
     * @param oj
     * @param usernameList
     * @param passwordList
     * @MethodName addRemoteJudgeAccountToRedis
     * @Description 将传入的对应oj账号写入到mysql
     * @Return
     * @Since 2021/5/18
     */
    private void addRemoteJudgeAccountToMySQL(String oj, List<String> usernameList, List<String> passwordList) {


        if (CollectionUtils.isEmpty(usernameList) || CollectionUtils.isEmpty(passwordList) || usernameList.size() != passwordList.size()) {
            log.error("[{}]: There is no account or password configured for remote judge, " +
                            "username list:{}, password list:{}", oj, Arrays.toString(usernameList.toArray()),
                    Arrays.toString(passwordList.toArray()));
        }

        List<RemoteJudgeAccount> remoteAccountList = new LinkedList<>();
        for (int i = 0; i < usernameList.size(); i++) {

            remoteAccountList.add(new RemoteJudgeAccount()
                    .setUsername(usernameList.get(i))
                    .setPassword(passwordList.get(i))
                    .setStatus(true)
                    .setVersion(0L)
                    .setOj(oj));

        }

        if (remoteAccountList.size() > 0) {
            boolean addOk = remoteJudgeAccountService.saveOrUpdateBatch(remoteAccountList);
            if (!addOk) {
                log.error("Remote judge initialization failed. Failed to add account for: [{}]. Please check the configuration file and restart!", oj);
            }
        }
    }


    private void upsertHOJLanguage(String... languageList) {
        /**
         * 2022.02.25 新增js、pypy、php语言
         */
        for (String language : languageList) {
            QueryWrapper<Language> languageQueryWrapper = new QueryWrapper<>();
            languageQueryWrapper.eq("oj", "ME")
                    .eq("name", language);
            int count = languageService.count(languageQueryWrapper);
            if (count == 0) {
                Language newLanguage = buildHOJLanguage(language);
                boolean isOk = languageService.save(newLanguage);
                if (!isOk) {
                    log.error("[HOJ] Failed to add new language [{}]! Please check whether the language table corresponding to the database has the language!", language);
                }
            }
        }
    }

    private void checkAllLanguageUpdate() {

        /**
         * 2022.02.25 更新原有的python3.6.9为python3.7.5
         */
        UpdateWrapper<Language> languageUpdateWrapper = new UpdateWrapper<>();
        languageUpdateWrapper.eq("oj", "ME")
                .eq("name", "Python3")
                .set("description", "Python 3.7.5");
        languageService.update(languageUpdateWrapper);

        /**
         * 2022.02.25 删除cf的Microsoft Visual C++ 2010
         */
        UpdateWrapper<Language> deleteWrapper = new UpdateWrapper<>();
        deleteWrapper.eq("name", "Microsoft Visual C++ 2010")
                .eq("oj", "CF");
        languageService.remove(deleteWrapper);
    }

    private void checkRemoteOJLanguage(Constants.RemoteOJ... remoteOJList) {
        for (Constants.RemoteOJ remoteOJ : remoteOJList) {
            QueryWrapper<Language> languageQueryWrapper = new QueryWrapper<>();
            languageQueryWrapper.eq("oj", remoteOJ.getName());
            int count = languageService.count(languageQueryWrapper);
            if (count == 0) {
                List<Language> languageList = new LanguageContext(remoteOJ).buildLanguageList();
                boolean isOk = languageService.saveBatch(languageList);
                if (!isOk) {
                    log.error("[{}] Failed to initialize language list! Please check whether the language table corresponding to the database has the OJ language!", remoteOJ.getName());
                }
            }
        }
    }


    private Language buildHOJLanguage(String lang) {
        Language language = new Language();
        switch (lang) {
            case "PHP":
                language.setName("PHP")
                        .setCompileCommand("/usr/bin/php {src_path}")
                        .setContentType("text/x-php")
                        .setDescription("PHP 7.3.33")
                        .setTemplate("<?=array_sum(fscanf(STDIN, \"%d %d\"));")
                        .setIsSpj(false)
                        .setOj("ME");
                return language;
            case "JavaScript Node":
                language.setName("JavaScript Node")
                        .setCompileCommand("/usr/bin/node {src_path}")
                        .setContentType("text/javascript")
                        .setDescription("Node.js 14.19.0")
                        .setTemplate("var readline = require('readline');\n" +
                                "const rl = readline.createInterface({\n" +
                                "        input: process.stdin,\n" +
                                "        output: process.stdout\n" +
                                "});\n" +
                                "rl.on('line', function(line){\n" +
                                "   var tokens = line.split(' ');\n" +
                                "    console.log(parseInt(tokens[0]) + parseInt(tokens[1]));\n" +
                                "});")
                        .setIsSpj(false)
                        .setOj("ME");
                return language;
            case "JavaScript V8":
                language.setName("JavaScript V8")
                        .setCompileCommand("/usr/bin/jsv8/d8 {src_path}")
                        .setContentType("text/javascript")
                        .setDescription("JavaScript V8 8.4.109")
                        .setTemplate("const [a, b] = readline().split(' ').map(n => parseInt(n, 10));\n" +
                                "print((a + b).toString());")
                        .setIsSpj(false)
                        .setOj("ME");
                return language;
            case "PyPy2":
                language.setName("PyPy2")
                        .setContentType("text/x-python")
                        .setCompileCommand("/usr/bin/pypy -m py_compile {src_path}")
                        .setDescription("PyPy 2.7.18 (7.3.8)")
                        .setTemplate("print sum(int(x) for x in raw_input().split(' '))")
                        .setCodeTemplate("//PREPEND BEGIN\n" +
                                "//PREPEND END\n" +
                                "\n" +
                                "//TEMPLATE BEGIN\n" +
                                "def add(a, b):\n" +
                                "    return a + b\n" +
                                "//TEMPLATE END\n" +
                                "\n" +
                                "\n" +
                                "if __name__ == '__main__':  \n" +
                                "    //APPEND BEGIN\n" +
                                "    a, b = 1, 1\n" +
                                "    print add(a, b)\n" +
                                "    //APPEND END")
                        .setIsSpj(false)
                        .setOj("ME");
                return language;
            case "PyPy3":
                language.setName("PyPy3")
                        .setContentType("text/x-python")
                        .setDescription("PyPy 3.8.12 (7.3.8)")
                        .setCompileCommand("/usr/bin/pypy3 -m py_compile {src_path}")
                        .setTemplate("print(sum(int(x) for x in input().split(' ')))")
                        .setCodeTemplate("//PREPEND BEGIN\n" +
                                "//PREPEND END\n" +
                                "\n" +
                                "//TEMPLATE BEGIN\n" +
                                "def add(a, b):\n" +
                                "    return a + b\n" +
                                "//TEMPLATE END\n" +
                                "\n" +
                                "\n" +
                                "if __name__ == '__main__':  \n" +
                                "    //APPEND BEGIN\n" +
                                "    a, b = 1, 1\n" +
                                "    print(add(a, b))\n" +
                                "    //APPEND END")
                        .setIsSpj(false)
                        .setOj("ME");
                return language;
        }
        return null;
    }

}


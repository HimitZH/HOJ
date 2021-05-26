package top.hcode.hoj.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.vo.ConfigVo;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/2 23:17
 * @Description:
 */
@Component
public class ConfigUtils {
    @Autowired
    private ConfigVo configVo;

    public String getConfigContent() {

        return buildYamlStr(configVo);
    }

    public String buildYamlStr(ConfigVo configVo) {
        return "hoj:\n" +
                "  jwt:\n" +
                "    # 加密秘钥\n" +
                "    secret: " + configVo.getTokenSecret() + "\n" +
                "    # token有效时长，1天，单位秒\n" +
                "    expire: " + configVo.getTokenExpire() + "\n" +
                "    checkRefreshExpire: " + configVo.getCheckRefreshExpire() + "\n" +
                "    header: token\n" +
                "  judge:\n" +
                "    # 调用判题服务器的token\n" +
                "    token: " + configVo.getJudgeToken() + "\n" +
                "  db:\n" +
                "    host: " + configVo.getMysqlHost() + "\n" +
                "    public-host: " + configVo.getMysqlPublicHost() + "\n" +
                "    port: " + configVo.getMysqlPort() + "\n" +
                "    name: " + configVo.getMysqlDBName() + "\n" +
                "    username: " + configVo.getMysqlUsername() + "\n" +
                "    password: " + configVo.getMysqlPassword() + "\n" +
                "  mail:\n" +
                "    ssl: " + configVo.getEmailSsl() + "\n" +
                "    username: " + configVo.getEmailUsername() + "\n" +
                "    password: " + configVo.getEmailPassword() + "\n" +
                "    host: " + configVo.getEmailHost() + "\n" +
                "    port: " + configVo.getEmailPort() + "\n" +
                "    background-img: " + configVo.getEmailBGImg() + "\n" +
                "  redis:\n" +
                "    host: " + configVo.getRedisHost() + "\n" +
                "    port: " + configVo.getRedisPort() + "\n" +
                "    password: " + configVo.getRedisPassword() + "\n" +
                "  web-config:\n" +
                "    base-url: " + configVo.getBaseUrl() + "\n" +
                "    name: " + configVo.getName() + "\n" +
                "    short-name: " + configVo.getShortName() + "\n" +
                "    register: " + configVo.getRegister() + "\n" +
                "    footer:\n" +
                "      record:\n" +
                "        name: " + configVo.getRecordName() + "\n" +
                "        url: " + configVo.getRecordUrl() + "\n" +
                "      project:\n" +
                "        name: " + configVo.getProjectName() + "\n" +
                "        url: " + configVo.getProjectUrl() + "\n" +
                "  hdu:\n" +
                "    account:\n" +
                "      username: " + listToStr(configVo.getHduUsernameList()) + "\n" +
                "      password: " + listToStr(configVo.getHduPasswordList()) + "\n" +
                "  cf:\n" +
                "    account:\n" +
                "      username: " + listToStr(configVo.getCfUsernameList()) + "\n" +
                "      password: " + listToStr(configVo.getCfPasswordList());
    }

    private String listToStr(List<String> list) {
        StringBuilder listStr = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i != list.size() - 1) {
                listStr.append(list.get(i)).append(",");
            } else {
                listStr.append(list.get(i));
            }
        }
        return listStr.toString();
    }

}
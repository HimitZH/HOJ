package top.hcode.hoj.utils;

import cn.hutool.core.text.UnicodeUtil;
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
                "    base-url: \"" + UnicodeUtil.toUnicode(configVo.getBaseUrl(), false) + "\"\n" +
                "    name: \"" + UnicodeUtil.toUnicode(configVo.getName(), false) + "\"\n" +
                "    short-name: \"" + UnicodeUtil.toUnicode(configVo.getShortName(), false) + "\"\n" +
                "    description: \"" + UnicodeUtil.toUnicode(configVo.getDescription(), false) + "\"\n" +
                "    register: " + configVo.getRegister() + "\n" +
                "    footer:\n" +
                "      record:\n" +
                "        name: \"" + UnicodeUtil.toUnicode(configVo.getRecordName(), false) + "\"\n" +
                "        url: \"" + UnicodeUtil.toUnicode(configVo.getRecordUrl(), false) + "\"\n" +
                "      project:\n" +
                "        name: \"" + UnicodeUtil.toUnicode(configVo.getProjectName(), false) + "\"\n" +
                "        url: \"" + UnicodeUtil.toUnicode(configVo.getProjectUrl(), false) + "\"\n" +
                "  hdu:\n" +
                "    account:\n" +
                "      username: " + listToStr(configVo.getHduUsernameList()) + "\n" +
                "      password: " + listToStr(configVo.getHduPasswordList()) + "\n" +
                "  cf:\n" +
                "    account:\n" +
                "      username: " + listToStr(configVo.getCfUsernameList()) + "\n" +
                "      password: " + listToStr(configVo.getCfPasswordList()) + "\n" +
                "  poj:\n" +
                "    account:\n" +
                "      username: " + listToStr(configVo.getPojUsernameList()) + "\n" +
                "      password: " + listToStr(configVo.getPojPasswordList()) + "\n" +
                "  atcoder:\n" +
                "    account:\n" +
                "      username: " + listToStr(configVo.getAtcoderUsernameList()) + "\n" +
                "      password: " + listToStr(configVo.getAtcoderPasswordList()) + "\n" +
                "  spoj:\n" +
                "    account:\n" +
                "      username: " + listToStr(configVo.getSpojUsernameList()) + "\n" +
                "      password: " + listToStr(configVo.getSpojPasswordList());
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
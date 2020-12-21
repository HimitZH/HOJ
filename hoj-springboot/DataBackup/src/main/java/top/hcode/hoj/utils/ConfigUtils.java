package top.hcode.hoj.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.vo.ConfigVo;

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
        return "hoj:\n" +
                "  jwt:\n" +
                "    # 加密秘钥\n" +
                "    secret: "+configVo.getTokenSecret()+"\n" +
                "    # token有效时长，1小时，单位秒\n" +
                "    expire: "+configVo.getTokenExpire()+"\n" +
                "    checkRefreshExpire: "+configVo.getCheckRefreshExpire()+"\n"+
                "    header: token\n" +
                "  db:\n" +
                "    host: "+configVo.getMysqlHost()+"\n" +
                "    port: "+configVo.getMysqlPort()+"\n" +
                "    name: "+configVo.getMysqlDBName()+"\n" +
                "    username: "+configVo.getMysqlUsername()+"\n" +
                "    password: "+configVo.getMysqlPassword()+"\n" +
                "  mail:\n" +
                "    ssl: "+configVo.getEmailSsl()+"\n" +
                "    username: "+configVo.getEmailUsername()+"\n" +
                "    password: "+configVo.getEmailPassword()+"\n" +
                "    host: "+configVo.getEmailHost()+"\n" +
                "  redis:\n" +
                "    host: "+configVo.getRedisHost()+"\n" +
                "    port: "+configVo.getRedisPort()+"\n" +
                "  web-config:\n" +
                "    base-url: "+configVo.getBaseUrl()+"\n" +
                "    name: "+configVo.getName()+"\n" +
                "    short-name: "+configVo.getShortName()+"\n" +
                "    register: "+configVo.getRegister()+"\n" +
                "    footer:\n" +
                "      record:\n" +
                "        name: "+configVo.getRecordName()+"\n" +
                "        url: "+configVo.getRecordUrl()+"\n" +
                "      project:\n" +
                "        name: "+configVo.getProjectName()+"\n" +
                "        url: "+configVo.getProjectUrl();
    }

}
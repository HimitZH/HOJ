package top.hcode.hoj.utils;

import cn.hutool.core.text.UnicodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.pojo.vo.ConfigVO;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/2 23:17
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class ConfigUtils {

    @Autowired
    private ConfigVO configVo;

    public String getConfigContent() {
        return buildYamlStr(configVo);
    }

    public String buildYamlStr(ConfigVO configVo) {
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
                "    port: " + configVo.getMysqlPort() + "\n" +
                "    public-host: " + configVo.getMysqlPublicHost() + "\n" +
                "    public-port: " + configVo.getMysqlPublicPort() + "\n" +
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
                "      password: " + listToStr(configVo.getSpojPasswordList()) + "\n" +
                "  switch:\n" +
                "    judge:\n" +
                "      public: " + configVo.getOpenPublicJudge() + "\n" +
                "      group: " + configVo.getOpenGroupJudge() + "\n" +
                "      contest: " + configVo.getOpenContestJudge() + "\n" +
                "      hide-non-contest-code: " + configVo.getHideNonContestSubmissionCode() + "\n" +
                "      submit-interval: " + configVo.getDefaultSubmitInterval() + "\n" +
                "    discussion:\n" +
                "      public: " + configVo.getOpenPublicDiscussion() + "\n" +
                "      group: " + configVo.getOpenGroupDiscussion() + "\n" +
                "      ac-initial-value: " + configVo.getDefaultCreateDiscussionACInitValue() + "\n" +
                "      create-daily: " + configVo.getDefaultCreateDiscussionDailyLimit() + "\n" +
                "    comment:\n" +
                "      contest: " + configVo.getOpenContestComment() + "\n" +
                "      ac-initial-value: " + configVo.getDefaultCreateCommentACInitValue() + "\n" +
                "    group:\n" +
                "      ac-initial-value: " + configVo.getDefaultCreateGroupACInitValue() + "\n" +
                "      create-daily: " + configVo.getDefaultCreateGroupDailyLimit() + "\n" +
                "      create-total: " + configVo.getDefaultCreateGroupLimit();
    }

    private String listToStr(List<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
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
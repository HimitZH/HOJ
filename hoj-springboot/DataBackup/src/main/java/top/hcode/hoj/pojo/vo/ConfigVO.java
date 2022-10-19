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
public class ConfigVO {
    // 数据库配置
    @Value("${hoj.db.username}")
    private String mysqlUsername;

    @Value("${hoj.db.password}")
    private String mysqlPassword;

    @Value("${hoj.db.name}")
    private String mysqlDBName;

    @Value("${hoj.db.host}")
    private String mysqlHost;

    @Value("${hoj.db.public-host:172.20.0.3}")
    private String mysqlPublicHost;

    @Value("${hoj.db.port}")
    private Integer mysqlPort;

    @Value("${hoj.db.public-port:3306}")
    private Integer mysqlPublicPort;

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

    /**
     * 是否开启公开评论区
     */
    @Value("${hoj.switch.discussion.public:true}")
    private Boolean openPublicDiscussion;

    /**
     * 是否开启团队评论区
     */
    @Value("${hoj.switch.discussion.group:true}")
    private Boolean openGroupDiscussion;

    /**
     * 是否开启比赛讨论区
     */
    @Value("${hoj.switch.comment.contest:true}")
    private Boolean openContestComment;

    /**
     * 是否开启公开评测
     */
    @Value("${hoj.switch.judge.public:true}")
    private Boolean openPublicJudge;

    /**
     * 是否开启团队评测
     */
    @Value("${hoj.switch.judge.group:true}")
    private Boolean openGroupJudge;

    /**
     * 是否开启比赛评测
     */
    @Value("${hoj.switch.judge.contest:true}")
    private Boolean openContestJudge;

    /**
     * 是否隐藏非比赛提交详情的代码（超管不受限制）
     */
    @Value("${hoj.switch.judge.hide-non-contest-code:false}")
    private Boolean hideNonContestSubmissionCode;

    /**
     * 非比赛的提交间隔秒数
     */
    @Value("${hoj.switch.judge.submit-interval:8}")
    private Integer defaultSubmitInterval;

    /**
     * 每天可以创建的团队数量
     */
    @Value("${hoj.switch.group.create-daily:2}")
    private Integer defaultCreateGroupDailyLimit;

    /**
     * 总共可以拥有的团队数量
     */
    @Value("${hoj.switch.group.create-total:5}")
    private Integer defaultCreateGroupLimit;

    /**
     * 创建团队的前提：20道题目通过
     */
    @Value("${hoj.switch.group.ac-initial-value:20}")
    private Integer defaultCreateGroupACInitValue;

    /**
     * 每天可以创建的帖子数量
     */
    @Value("${hoj.switch.discussion.create-daily:5}")
    private Integer defaultCreateDiscussionDailyLimit;

    /**
     * 创建讨论帖子的前提：10道题目通过
     */
    @Value("${hoj.switch.discussion.ac-initial-value:10}")
    private Integer defaultCreateDiscussionACInitValue;

    /**
     * 评论和回复的前提：10道题目通过
     */
    @Value("${hoj.switch.comment.ac-initial-value:10}")
    private Integer defaultCreateCommentACInitValue;
}

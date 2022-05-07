package top.hcode.hoj.remoteJudge.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import top.hcode.hoj.util.CodeForcesUtils;
import top.hcode.hoj.util.Constants;

import java.io.Serializable;
import java.net.HttpCookie;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/29 00:18
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@ToString
public class RemoteJudgeDTO implements Serializable {
    private static final long serialVersionUID = 888L;

    /**
     * 远程评测的oj
     */
    private String oj;

    /**
     * 远程评测的账号
     */
    private String username;

    /**
     * 远程评测的密码
     */
    private String password;

    /**
     * 远程评测的cookies
     */
    private List<HttpCookie> cookies;

    public RemoteJudgeDTO setCookies(List<HttpCookie> cookies) {
        if (cookies != null
                && (Constants.RemoteJudge.CF_JUDGE.getName().equals(this.oj)
                || Constants.RemoteJudge.GYM_JUDGE.getName().equals(this.oj))) {
            HttpCookie rcpc = new HttpCookie("RCPC", CodeForcesUtils.getRCPC());
            rcpc.setVersion(0);
            cookies.add(rcpc);
        }
        this.cookies = cookies;
        return this;
    }

    /**
     * 远程测评的csrfToken
     */
    private String csrfToken;

    /**
     * 远程评测的完整题目id  like CF:110C AtCoder: abc111_a
     */
    private String completeProblemId;

    /**
     * 远程评测的原题比赛id
     */
    private String contestId;

    /**
     * 远程测评的原题题号展示序号
     */
    private String problemNum;

    /**
     * 远程测评的语言
     */
    private String language;

    /**
     * 远程评测的用户代码
     */
    private String userCode;

    /**
     * 远程评测的题目id
     */
    private Long pid;

    /**
     * 远程测评的用户id
     */
    private String uid;

    /**
     * 远程测评的比赛id
     */
    private Long cid;

    /**
     * 远程测评的团队id
     */
    private Long gid;

    /**
     * 远程测评的judge_id;
     */
    private Long judgeId;

    /**
     * 远程测评的提交id（远程oj的提交id）
     */
    private Long submitId;

    /**
     * 远程测评的测试数据的序号(codeforces)
     */
    private Integer testcaseNum;

    /**
     * 远程测评的当前已获得测试数据点最大运行时间ms
     */
    private Integer maxTime;

    /**
     * 远程测评的当前已获得测试数据点最大运行空间kb
     */
    private Integer maxMemory;

    /**
     * 远程测评的登录状态码
     */
    private Integer loginStatus;

    /**
     * 远程测评的提交状态码
     */
    private Integer submitStatus;

    /**
     * 当前评测的服务器ip
     */
    private String serverIp;

    /**
     * 当前评测的服务器端口号
     */
    private Integer serverPort;
}
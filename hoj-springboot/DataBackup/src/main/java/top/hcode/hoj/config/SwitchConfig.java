package top.hcode.hoj.config;

import cn.hutool.core.text.UnicodeUtil;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2022/10/26
 */
@Data
public class SwitchConfig {

    private List<String> hduUsernameList = new ArrayList<>();

    private List<String> hduPasswordList = new ArrayList<>();

    private List<String> cfUsernameList = new ArrayList<>();

    private List<String> cfPasswordList = new ArrayList<>();

    private List<String> pojUsernameList = new ArrayList<>();

    private List<String> pojPasswordList = new ArrayList<>();

    private List<String> atcoderUsernameList = new ArrayList<>();

    private List<String> atcoderPasswordList = new ArrayList<>();

    private List<String> spojUsernameList = new ArrayList<>();

    private List<String> spojPasswordList = new ArrayList<>();

    /**
     * 是否开启公开评论区
     */
    private Boolean openPublicDiscussion = true;

    /**
     * 是否开启团队评论区
     */
    private Boolean openGroupDiscussion = true;

    /**
     * 是否开启比赛讨论区
     */
    private Boolean openContestComment = true;

    /**
     * 是否开启公开评测
     */
    private Boolean openPublicJudge = true;

    /**
     * 是否开启团队评测
     */
    private Boolean openGroupJudge = true;

    /**
     * 是否开启比赛评测
     */
    private Boolean openContestJudge = true;

    /**
     * 是否隐藏非比赛提交详情的代码（超管不受限制）
     */
    private Boolean hideNonContestSubmissionCode = false;

    /**
     * 非比赛的提交间隔秒数
     */
    private Integer defaultSubmitInterval = 8;

    /**
     * 每天可以创建的团队数量
     */
    private Integer defaultCreateGroupDailyLimit = 2;

    /**
     * 总共可以拥有的团队数量
     */
    private Integer defaultCreateGroupLimit = 5;

    /**
     * 创建团队的前提：20道题目通过
     */
    private Integer defaultCreateGroupACInitValue = 20;

    /**
     * 每天可以创建的帖子数量
     */
    private Integer defaultCreateDiscussionDailyLimit = 5;

    /**
     * 创建讨论帖子的前提：10道题目通过
     */
    private Integer defaultCreateDiscussionACInitValue = 10;

    /**
     * 评论和回复的前提：10道题目通过
     */
    private Integer defaultCreateCommentACInitValue = 10;

    public void formatStrRemoteAccount2Unicode(){
        this.setHduUsernameList(format2Unicode(this.hduUsernameList));
        this.setHduPasswordList(format2Unicode(this.hduPasswordList));
        this.setPojUsernameList(format2Unicode(this.pojUsernameList));
        this.setPojPasswordList(format2Unicode(this.pojPasswordList));
        this.setCfUsernameList(format2Unicode(this.cfUsernameList));
        this.setCfPasswordList(format2Unicode(this.cfPasswordList));
        this.setAtcoderUsernameList(format2Unicode(this.atcoderUsernameList));
        this.setAtcoderPasswordList(format2Unicode(this.atcoderPasswordList));
        this.setSpojUsernameList(format2Unicode(this.spojUsernameList));
        this.setSpojPasswordList(format2Unicode(this.spojPasswordList));
    }

    public void convertUnicodeRemoteAccount2Str(){
        this.setHduUsernameList(convertUnicode2Str(this.hduUsernameList));
        this.setHduPasswordList(convertUnicode2Str(this.hduPasswordList));
        this.setPojUsernameList(convertUnicode2Str(this.pojUsernameList));
        this.setPojPasswordList(convertUnicode2Str(this.pojPasswordList));
        this.setCfUsernameList(convertUnicode2Str(this.cfUsernameList));
        this.setCfPasswordList(convertUnicode2Str(this.cfPasswordList));
        this.setAtcoderUsernameList(convertUnicode2Str(this.atcoderUsernameList));
        this.setAtcoderPasswordList(convertUnicode2Str(this.atcoderPasswordList));
        this.setSpojUsernameList(convertUnicode2Str(this.spojUsernameList));
        this.setSpojPasswordList(convertUnicode2Str(this.spojPasswordList));
    }


    private List<String> format2Unicode(List<String> strList) {
        if (CollectionUtils.isEmpty(strList)) {
            return Collections.emptyList();
        }
        List<String> unicodeList = new ArrayList<>();
        for (String str : strList) {
            unicodeList.add(UnicodeUtil.toUnicode(str, true));
        }
        return unicodeList;
    }

    private List<String> convertUnicode2Str(List<String> unicodeList) {
        if (CollectionUtils.isEmpty(unicodeList)) {
            return Collections.emptyList();
        }
        List<String> strList = new ArrayList<>();
        for (String str : unicodeList) {
            strList.add(UnicodeUtil.toString(str));
        }
        return strList;
    }
}

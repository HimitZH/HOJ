package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/18 18:16
 * @Description:
 */

@Data
@Accessors(chain = true)
public class OIContestRankVO {

    @ApiModelProperty(value = "排名,排名为-1则为打星队伍")
    private Integer rank;

    @ApiModelProperty(value = "是否得奖")
    private Boolean isWinAward;

    @ApiModelProperty(value = "排名奖项名称")
    private String awardName;

    @ApiModelProperty(value = "排名背景颜色")
    private String awardBackground;

    @ApiModelProperty(value = "排名文本颜色")
    private String awardColor;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户真实姓名")
    private String realname;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "提交总得分")
    private Integer totalScore;

    @ApiModelProperty(value = "提交总耗时，只有满分的提交才会统计")
    private Integer totalTime;

    @ApiModelProperty(value = "OI的题对应提交得分")
    private HashMap<String, Integer> submissionInfo;

    @ApiModelProperty(value = "OI的题得满分后对应提交最优耗时")
    private HashMap<String, Integer> timeInfo;
}
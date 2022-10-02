package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/12/7 19:45
 * @Description:
 */
@ApiModel(value="管理比赛的回传实体", description="")
@Data
public class AdminContestVo {

    @ApiModelProperty(value = "比赛id")
    private Long id;

    @ApiModelProperty(value = "比赛创建者id")
    private String uid;

    @ApiModelProperty(value = "比赛创建者的用户名")
    private String author;

    @ApiModelProperty(value = "比赛标题")
    private String title;

    @ApiModelProperty(value = "0为acm赛制，1为比分赛制")
    private Integer type;

    @ApiModelProperty(value = "比赛说明")
    private String description;

    @ApiModelProperty(value = "比赛来源，原创为0，克隆赛为比赛id")
    private Integer source;

    @ApiModelProperty(value = "0为公开赛，1为私有赛（访问有密码），2为保护赛（提交有密码）")
    private Integer auth;

    @ApiModelProperty(value = "比赛密码")
    private String pwd;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "比赛时长（s）")
    private Long duration;

    @ApiModelProperty(value = "是否开启封榜")
    private Boolean sealRank;

    @ApiModelProperty(value = "封榜起始时间，一直到比赛结束，不刷新榜单")
    private Date sealRankTime;

    @ApiModelProperty(value = "比赛结束是否自动解除封榜,自动转换成真实榜单")
    private Boolean autoRealRank;

    @ApiModelProperty(value = "-1为未开始，0为进行中，1为已结束")
    private Integer status;

    @ApiModelProperty(value = "是否可见")
    private Boolean visible;

    @ApiModelProperty(value = "是否打开打印功能")
    private Boolean openPrint;

    @ApiModelProperty(value = "是否打开账号限制")
    private Boolean openAccountLimit;

    @ApiModelProperty(value = "账号限制规则 <prefix>**</prefix><suffix>**</suffix><start>**</start><end>**</end><extra>**</extra>")
    private String accountLimitRule;

    @ApiModelProperty(value = "排行榜显示（username、nickname、realname）")
    private String rankShowName;

    @ApiModelProperty(value = "打星用户列表")
    private List<String> starAccount;

    @ApiModelProperty(value = "是否开放比赛榜单")
    private Boolean openRank;

    @ApiModelProperty(value = "oi排行榜得分方式，Recent、Highest（最近一次提交、最高得分提交）")
    private String oiRankScoreType;

    @ApiModelProperty(value = "团队id")
    private Long gid;

    @ApiModelProperty(value = "奖项类型：0(不设置),1(设置占比),2(设置人数)")
    private Integer awardType;

    @ApiModelProperty(value = "奖项配置")
    private List<ContestAwardConfigVo> awardConfigList;

    private Date gmtCreate;

    private Date gmtModified;

}
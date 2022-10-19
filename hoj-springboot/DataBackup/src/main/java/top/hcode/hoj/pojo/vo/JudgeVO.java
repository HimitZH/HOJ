package top.hcode.hoj.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/29 13:08
 * @Description:
 */
@Data
@ApiModel(value="返回的判题信息", description="")
public class JudgeVO {

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "提交id")
    @TableId(value = "submit_id", type = IdType.AUTO)
    private Long submitId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "题目id")
    private Long pid;

    @ApiModelProperty(value = "题目展示id")
    private String displayPid;

    @ApiModelProperty(value = "题目标题")
    private String title;

    @ApiModelProperty(value = "比赛display_id")
    private String displayId;

    @ApiModelProperty(value = "结果码具体参考文档")
    private Date submitTime;

    @ApiModelProperty(value = "结果码具体参考文档")
    private Integer status;

    @ApiModelProperty(value = "0为代码全部人可见，1为仅自己可见。")
    private Boolean share;

    @ApiModelProperty(value = "运行时间(ms)")
    private Integer time;

    @ApiModelProperty(value = "运行内存（b）")
    private Integer memory;

    @ApiModelProperty(value = "题目得分，ACM题目默认为null")
    private Integer score;

    @ApiModelProperty(value = "该题在OI排行榜的分数")
    private Integer oiRankScore;

    @ApiModelProperty(value = "代码长度")
    private Integer length;

    @ApiModelProperty(value = "代码语言")
    private String language;

    @ApiModelProperty(value = "比赛id，非比赛题目默认为0")
    private Long cid;

    @ApiModelProperty(value = "比赛中题目排序id，非比赛题目默认为0")
    private Long cpid;

    @ApiModelProperty(value = "题目来源")
    private String source;

    @ApiModelProperty(value = "判题机ip")
    private String judger;

    @ApiModelProperty(value = "提交者所在ip")
    private String ip;

    @ApiModelProperty(value = "是否人工评测")
    private Boolean isManual;
}
package top.hcode.hoj.pojo.entity.judge;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="Judge对象", description="")
public class Judge implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "submit_id", type = IdType.AUTO)
    private Long submitId;

    @ApiModelProperty(value = "题目id")
    private Long pid;

    @ApiModelProperty(value = "题目展示id")
    private String displayPid;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "提交的时间")
    private Date submitTime;

    @ApiModelProperty(value = "结果码具体参考文档")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer status;

    @ApiModelProperty(value = "0为仅自己可见，1为全部人可见。")
    private Boolean share;

    @ApiModelProperty(value = "错误提醒（编译错误，或者vj提醒）")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String errorMessage;

    @ApiModelProperty(value = "运行时间(ms)")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer time;

    @ApiModelProperty(value = "运行内存(kb)")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer memory;

    @ApiModelProperty(value = "IO判题不为空")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer score;

    @ApiModelProperty(value = "代码长度")
    private Integer length;

    @ApiModelProperty(value = "代码")
    private String code;

    @ApiModelProperty(value = "代码语言")
    private String language;

    @ApiModelProperty(value = "比赛id，非比赛提交默认为0")
    private Long cid;

    @ApiModelProperty(value = "比赛中题目排序id，非比赛提交默认为0")
    private Long cpid;

    @ApiModelProperty(value = "团队id，非团队内题目提交为null")
    private Long gid;

    @ApiModelProperty(value = "判题机名称")
    private String judger;

    @ApiModelProperty(value = "提交者所在ip")
    private String ip;

    @ApiModelProperty(value = "废弃")
    private Integer version;

    @ApiModelProperty(value = "该题在OI排行榜的分数")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer oiRankScore;

    @ApiModelProperty(value = "vjudge判题在其它oj的提交id")
    private Long vjudgeSubmitId;

    @ApiModelProperty(value = "vjudge判题在其它oj的提交用户名")
    private String vjudgeUsername;

    @ApiModelProperty(value = "vjudge判题在其它oj的提交账号密码")
    private String vjudgePassword;

    @ApiModelProperty(value = "是否人工评测")
    private Boolean isManual;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;


}

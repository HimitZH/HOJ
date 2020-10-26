package top.hcode.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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

    private Long pid;

    private String uid;

    private Date submitTime;

    @ApiModelProperty(value = "结果码具体参考文档")
    private Integer status;

    @ApiModelProperty(value = "0为代码全部人可见，1为仅自己可见。")
    private Integer auth;

    @ApiModelProperty(value = "错误提醒（编译错误，或者vj提醒）")
    private String errorMessage;

    @ApiModelProperty(value = "运行时间")
    private Integer time;

    @ApiModelProperty(value = "运行内存")
    private Integer memory;

    @ApiModelProperty(value = "代码长度")
    private Integer length;

    @ApiModelProperty(value = "代码")
    private String code;

    @ApiModelProperty(value = "代码语言")
    private String language;

    @ApiModelProperty(value = "比赛id，非比赛题目默认为0")
    private Long cid;

    @ApiModelProperty(value = "比赛中题目排序id，非比赛题目默认为0")
    private Long cpid;

    @ApiModelProperty(value = "判题机ip")
    private String judger;

    @ApiModelProperty(value = "提交者所在ip")
    private String ip;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;


}

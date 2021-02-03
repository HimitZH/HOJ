package top.hcode.hoj.pojo.entity;

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
@ApiModel(value="Problem对象", description="")
public class Problem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "题目")
    private String title;

    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "0为ACM,1为OI")
    private Integer type;

    @ApiModelProperty(value = "单位ms")
    private Integer timeLimit;

    @ApiModelProperty(value = "单位mb")
    private Integer memoryLimit;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "输入描述")
    private String input;

    @ApiModelProperty(value = "输出描述")
    private String output;

    @ApiModelProperty(value = "题面样例")
    private String examples;

    @ApiModelProperty(value = "题目来源")
    private String source;

    @ApiModelProperty(value = "题目难度")
    private Integer difficulty;

    @ApiModelProperty(value = "备注,提醒")
    private String hint;

    @ApiModelProperty(value = "默认为1公开，2为私有，3为比赛中")
    private Integer auth;

    @ApiModelProperty(value = "当该题目为io题目时的分数")
    private Integer ioScore;

    @ApiModelProperty(value = "该题目对应的相关提交代码，用户是否可用分享")
    private Boolean codeShare;

    @ApiModelProperty(value = "特判程序的代码 空代表无特判")
    private String spjCode;

    @ApiModelProperty(value = "特判程序的语言")
    private String spjLanguage;

    @ApiModelProperty(value = "是否默认去除用户代码的文末空格")
    private Boolean isRemoveEndBlank;

    @ApiModelProperty(value = "是否默认开启该题目的测试样例结果查看")
    private Boolean openCaseResult;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;


}

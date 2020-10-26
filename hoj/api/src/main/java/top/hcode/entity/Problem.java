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
@ApiModel(value="Problem对象", description="")
public class Problem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String author;

    @ApiModelProperty(value = "单位ms")
    private Integer timeLimit;

    @ApiModelProperty(value = "单位kb")
    private Integer memoryLimit;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "输入描述")
    private String input;

    @ApiModelProperty(value = "输出描述")
    private String output;

    @ApiModelProperty(value = "多样例用(#)隔开")
    private String inputSample;

    @ApiModelProperty(value = "多样例用(#)隔开")
    private String outputSample;

    @ApiModelProperty(value = "题目来源")
    private String source;

    @ApiModelProperty(value = "备注")
    private String comment;

    @ApiModelProperty(value = "默认为1公开，2为私有，3为比赛中")
    private Integer auth;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;


}

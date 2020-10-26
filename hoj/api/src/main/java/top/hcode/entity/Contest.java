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
@ApiModel(value="Contest对象", description="")
public class Contest implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "比赛创建者id")
    private String uid;

    @ApiModelProperty(value = "比赛标题")
    private String title;

    @ApiModelProperty(value = "0为acm赛制，1为比分赛制")
    private Integer type;

    @ApiModelProperty(value = "比赛说明")
    private String explain;

    @ApiModelProperty(value = "比赛来源，原创为0，克隆赛为比赛id")
    private Integer source;

    @ApiModelProperty(value = "0为公开赛，1为私有赛（有密码），2为报名赛")
    private Integer auth;

    @ApiModelProperty(value = "比赛密码")
    private String pwd;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "比赛时长（分）")
    private Integer duration;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;


}

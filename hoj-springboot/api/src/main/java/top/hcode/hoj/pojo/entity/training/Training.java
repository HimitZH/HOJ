package top.hcode.hoj.pojo.entity.training;

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
 * @Author: Himit_ZH
 * @Date: 2021/11/19 21:37
 * @Description:
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "Training对象", description = "训练题单实体")
public class Training implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "训练题单标题")
    private String title;

    @ApiModelProperty(value = "训练题单简介")
    private String description;

    @ApiModelProperty(value = "训练题单创建者用户名")
    private String author;

    @ApiModelProperty(value = "训练题单权限类型：Public、Private")
    private String auth;

    @ApiModelProperty(value = "训练题单权限为Private时的密码")
    private String privatePwd;

    @ApiModelProperty(value = "是否可用")
    private Boolean status;

    @ApiModelProperty(value = "编号，升序排序")
    @TableField("`rank`")
    private Integer rank;

    @ApiModelProperty(value = "是否为团队内的训练")
    private Boolean isGroup;

    @ApiModelProperty(value = "团队ID")
    private Long gid;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
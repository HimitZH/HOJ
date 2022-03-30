package top.hcode.hoj.pojo.entity.group;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "Group对象", description = "")
@TableName("`group`")
public class Group implements Serializable  {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value = "团队id")
    private Long id;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "团队名称")
    private String name;

    @ApiModelProperty(value = "团队简称")
    private String shortName;

    @ApiModelProperty(value = "团队简介")
    private String brief;

    @ApiModelProperty(value = "团队介绍")
    private String description;

    @ApiModelProperty(value = "团队拥有者的用户名")
    private String owner;

    @ApiModelProperty(value = "团队拥有者的uuid")
    private String uid;

    @ApiModelProperty(value = "1为公开团队，2为保护团队，3为私有团队")
    private Integer auth;

    @ApiModelProperty(value = "团队是否可见")
    private Boolean visible;

    @ApiModelProperty(value = "是否封禁或删除 0正常，1无效")
    private Integer status;

    @ApiModelProperty(value = "团队邀请码")
    private String code;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;
}

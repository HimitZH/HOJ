package top.hcode.hoj.pojo.entity.user;

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
@ApiModel(value="UserInfo对象", description="")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "uuid",type =IdType.UUID)
    private String uuid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "昵称")
    private String nickname;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "学校")
    private String school;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "专业")
    private String course;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "学号")
    private String number;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "性别")
    private String gender;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "cf的username")
    private String cfUsername;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "github地址")
    private String github;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "博客地址")
    private String blog;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "个性介绍")
    private String signature;

    @ApiModelProperty(value = "头衔、称号")
    private String titleName;

    @ApiModelProperty(value = "头衔、称号的颜色")
    private String titleColor;

    @ApiModelProperty(value = "0可用，-1不可用")
    private int status;

//    @ApiModelProperty(value = "是否为比赛账号")
//    private Boolean isContest;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @ApiModelProperty(value = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;


}

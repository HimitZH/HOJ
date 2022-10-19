package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.hcode.hoj.pojo.entity.user.Role;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/5 13:30
 * @Description:
 */
@ApiModel(value="用户信息以及其对应的角色", description="")
@Data
public class UserRolesVO implements Serializable {

    private static final long serialVersionUID = 10000L;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "专业")
    private String course;

    @ApiModelProperty(value = "学号")
    private String number;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @ApiModelProperty(value = "cf的username")
    private String cfUsername;

    @ApiModelProperty(value = "github地址")
    private String github;

    @ApiModelProperty(value = "博客地址")
    private String blog;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "头衔名称")
    private String titleName;

    @ApiModelProperty(value = "头衔背景颜色")
    private String titleColor;

    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "0可用，1不可用")
    private int status;

    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;

    @ApiModelProperty(value = "修改时间")
    private Date gmtModified;

    @ApiModelProperty(value = "角色列表")
    private List<Role> roles;
}
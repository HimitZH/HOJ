package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */

@ApiModel(value="团队查询对象GroupVo", description="")
@Data
public class GroupVO {
    @ApiModelProperty(value = "团队id")
    private Long id;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "团队拥有者的用户名")
    private String owner;

    @ApiModelProperty(value = "团队名称")
    private String name;

    @ApiModelProperty(value = "团队简称")
    private String shortName;

    @ApiModelProperty(value = "团队简介")
    private String brief;

    @ApiModelProperty(value = "团队介绍")
    private String description;

    @ApiModelProperty(value = "0为直接加入，1为输入密码加入，2为申请加入")
    private Integer auth;

    @ApiModelProperty(value = "是否隐藏")
    private Boolean visible;

    @ApiModelProperty(value = "该团队的总人数")
    private Integer memberCount;

    @ApiModelProperty(value = "团队创建时间")
    private Date gmtCreate;
}

package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/16 15:24
 * @Description:
 */
@Data
public class ReplyVO {

    private Integer id;

    @ApiModelProperty(value = "评论id")
    private Integer commentId;

    @ApiModelProperty(value = "回复评论者id")
    private String fromUid;

    @ApiModelProperty(value = "回复评论者用户名")
    private String fromName;

    @ApiModelProperty(value = "回复评论者头像地址")
    private String fromAvatar;

    @ApiModelProperty(value = "回复评论者角色")
    private String fromRole;

    @ApiModelProperty(value = "回复评论者头衔名称")
    private String fromTitleName;

    @ApiModelProperty(value = "回复评论者头衔背景颜色")
    private String fromTitleColor;

    @ApiModelProperty(value = "被回复的用户id")
    private String toUid;

    @ApiModelProperty(value = "被回复的用户名")
    private String toName;

    @ApiModelProperty(value = "被回复的用户头像地址")
    private String toAvatar;

    @ApiModelProperty(value = "回复的内容")
    private String content;

    @ApiModelProperty(value = "是否封禁或删除 0正常，1封禁")
    private Integer status;

    private Date gmtCreate;

    private Date gmtModified;
}
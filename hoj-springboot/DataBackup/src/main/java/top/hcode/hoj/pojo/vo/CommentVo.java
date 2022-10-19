package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/5 22:30
 * @Description:
 */
@ApiModel(value = "评论数据列表VO", description = "")
@Data
public class CommentVo {

    @ApiModelProperty(value = "评论id")
    private Integer id;

    @ApiModelProperty(value = "评论内容")
    private String content;

    @ApiModelProperty(value = "评论者id")
    private String fromUid;

    @ApiModelProperty(value = "评论者用户名")
    private String fromName;

    @ApiModelProperty(value = "评论组头像地址")
    private String fromAvatar;

    @ApiModelProperty(value = "评论者角色")
    private String fromRole;

    @ApiModelProperty(value = "评论者头衔名称")
    private String fromTitleName;

    @ApiModelProperty(value = "评论者头衔背景颜色")
    private String fromTitleColor;

    @ApiModelProperty(value = "点赞数量")
    private Integer likeNum;

    @ApiModelProperty(value = "该评论的总回复数")
    private Integer totalReplyNum;

    private Date gmtCreate;

    @ApiModelProperty(value = "该评论回复列表")
    private List<ReplyVO> replyList;
}
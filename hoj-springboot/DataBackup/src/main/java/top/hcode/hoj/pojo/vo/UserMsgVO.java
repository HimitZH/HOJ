package top.hcode.hoj.pojo.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/2 20:50
 * @Description:
 */
@ApiModel(value="用户的讨论贴被评论的、被点赞、评论被回复的消息Vo", description="")
@Data
public class UserMsgVO {

    private Long id;

    @ApiModelProperty(value = "动作类型，如点赞讨论帖Like_Post、点赞评论Like_Discuss、评论Discuss、回复Reply等")
    private String action;

    @ApiModelProperty(value = "消息来源id，讨论id或比赛id")
    private Integer sourceId;

    @ApiModelProperty(value = "事件源类型：'Discussion'、'Contest'等")
    private String sourceType;

    @ApiModelProperty(value = "事件源的标题，讨论帖子的标题或者比赛的标题")
    private String sourceTitle;

    @ApiModelProperty(value = "事件源的内容，比如回复的内容，回复的评论等等,不超过250字符，超过使用...")
    private String sourceContent;

    @ApiModelProperty(value = "事件引用上一级评论或回复id")
    private Integer quoteId;

    @ApiModelProperty(value = "事件引用上一级的类型：Comment、Reply")
    private String quoteType;

    @ApiModelProperty(value = "事件引用上一级的内容，例如回复你的源评论内容")
    private String quoteContent;

    @ApiModelProperty(value = "事件所发生的地点链接 url")
    private String url;

    @ApiModelProperty(value = "是否已读")
    private Boolean state;

    @ApiModelProperty(value = "动作发出者的uid")
    private String senderId;

    @ApiModelProperty(value = "动作发出者的用户名")
    private String senderUsername;

    @ApiModelProperty(value = "动作发出者的头像")
    private String senderAvatar;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;
}
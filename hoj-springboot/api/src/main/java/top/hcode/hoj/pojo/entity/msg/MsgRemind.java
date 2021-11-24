package top.hcode.hoj.pojo.entity.msg;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:21
 * @Description:
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="MsgRemind", description="")
public class MsgRemind {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "动作类型，如点赞讨论帖Like_Post、点赞评论Like_Discuss、评论Discuss、回复Reply等")
    private String action;

    @ApiModelProperty(value = "消息来源id，讨论id或比赛id")
    private Integer sourceId;

    @ApiModelProperty(value = "事件源类型：'Discussion'、'Contest'等")
    private String sourceType;

    @ApiModelProperty(value = "事件源的内容，比如回复的内容，回复的评论等等,不超过250字符，超过使用...")
    private String sourceContent;

    @ApiModelProperty(value = "事件引用上一级评论或回复id")
    private Integer quoteId;

    @ApiModelProperty(value = "事件引用上一级的类型：Comment、Reply")
    private String quoteType;

    @ApiModelProperty(value = "事件所发生的地点链接 url")
    private String url;

    @ApiModelProperty(value = "接受通知的用户的id")
    private String recipientId;

    @ApiModelProperty(value = "动作执行者的id")
    private String senderId;

    @ApiModelProperty(value = "是否已读")
    private Boolean state;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}
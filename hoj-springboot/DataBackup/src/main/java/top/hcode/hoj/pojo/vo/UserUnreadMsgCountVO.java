package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:59
 * @Description:
 */
@ApiModel(value="用户未读消息统计", description="")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUnreadMsgCountVO {

    @ApiModelProperty(value = "未读评论")
    private Integer comment;

    @ApiModelProperty(value = "未读回复")
    private Integer reply;

    @ApiModelProperty(value = "未读点赞")
    private Integer like;

    @ApiModelProperty(value = "未读系统通知")
    private Integer sys;

    @ApiModelProperty(value = "未读我的消息")
    private Integer mine;
}
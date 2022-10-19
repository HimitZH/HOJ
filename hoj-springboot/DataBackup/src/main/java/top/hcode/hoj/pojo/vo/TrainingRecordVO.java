package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/21 14:31
 * @Description:
 */
@Data
@ApiModel(value="用户在训练的记录", description="")
public class TrainingRecordVO {

    private Long id;

    @ApiModelProperty(value = "训练id")
    private Long tid;

    @ApiModelProperty(value = "训练题目id")
    private Long tpid;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "题目id")
    private Long pid;

    @ApiModelProperty(value = "提交id")
    private Long submitId;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "提交结果状态码")
    private Integer status;

    @ApiModelProperty(value = "OI得分")
    private Integer score;

    @ApiModelProperty(value = "提交耗时")
    private Integer useTime;
}
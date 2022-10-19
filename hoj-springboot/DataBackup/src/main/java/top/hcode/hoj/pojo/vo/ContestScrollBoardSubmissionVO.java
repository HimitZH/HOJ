package top.hcode.hoj.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author Himit_ZH
 * @Date 2022/10/3
 */
@ApiModel(value = "比赛滚榜页面所需提交信息")
@Data
public class ContestScrollBoardSubmissionVO {

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "真实姓名")
    private String realname;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "提交id")
    private Long submitId;

    @ApiModelProperty(value = "提交结果状态")
    private Integer status;

    @ApiModelProperty(value = "提交时间")
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;

    @ApiModelProperty(value = "比赛题目id")
    private String displayId;
}

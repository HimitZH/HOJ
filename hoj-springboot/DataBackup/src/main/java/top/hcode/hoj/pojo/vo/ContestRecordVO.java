package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/28 12:05
 * @Description:
 */
@ApiModel(value="用户在比赛的记录", description="")
@Data
public class ContestRecordVO implements Serializable {

    private Long id;

    @ApiModelProperty(value = "比赛id")
    private Long cid;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "题目id")
    private Long pid;

    @ApiModelProperty(value = "比赛中的题目id")
    private Long cpid;

    @ApiModelProperty(value = "比赛中展示的id")
    private String displayId;

    @ApiModelProperty(value = "提交id，用于可重判")
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

    @ApiModelProperty(value = "提交结果，0表示未AC通过不罚时，1表示AC通过，-1为未AC通过算罚时")
    private Integer status;

    @ApiModelProperty(value = "具体提交时间")
    private Date submitTime;

    @ApiModelProperty(value = "提交时间，为提交时间减去比赛时间")
    private Long time;

    @ApiModelProperty(value = "OI比赛的得分")
    private Integer score;

    @ApiModelProperty(value = "提交耗时")
    private Integer useTime;

    @ApiModelProperty(value = "AC是否已校验")
    private Boolean checked;

    private Date gmtCreate;

    private Date gmtModified;

}
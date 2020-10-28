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
public class ContestRecordVo implements Serializable {
    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "比赛id")
    private Long cid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "题目id")
    private Long pid;

    @ApiModelProperty(value = "比赛中的题目顺序id")
    private Long cpid;

    @ApiModelProperty(value = "提交id，用于可重判")
    private Long submitId;

    @ApiModelProperty(value = "提交结果，0表示未AC通过，1表示AC通过")
    private Integer status;

    @ApiModelProperty(value = "提交时间，为提交时间减去比赛时间")
    private long time;

}
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
@ApiModel(value="GroupMemberVO", description="")
@Data
public class GroupMemberVO {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "团队id")
    private Long gid;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "当前状态")
    private Integer auth;

    @ApiModelProperty(value = "申请理由")
    private String reason;

    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;

    @ApiModelProperty(value = "修改时间")
    private Date gmtModify;
}

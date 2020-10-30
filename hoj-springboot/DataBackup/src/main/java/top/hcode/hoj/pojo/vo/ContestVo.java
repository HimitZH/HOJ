package top.hcode.hoj.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 21:53
 * @Description:
 */
@ApiModel(value="比赛信息", description="")
@Data
public class ContestVo implements Serializable {

    @TableId(value = "比赛id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "比赛创建者id")
    private String uid;

    @ApiModelProperty(value = "创建者用户名")
    private String username;

    @ApiModelProperty(value = "创建者昵称")
    private String nickname;

    @ApiModelProperty(value = "比赛标题")
    private String title;

    @ApiModelProperty(value = "比赛说明")
    private String explain;

    @ApiModelProperty(value = "0为acm赛制，1为比分赛制")
    private Integer type;

    @ApiModelProperty(value = "比赛来源，原创为0，克隆赛为比赛id")
    private Integer source;

    @ApiModelProperty(value = "0为公开赛，1为私有赛（有密码），2为报名赛")
    private Integer auth;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "比赛时长（分）")
    private Integer duration;
}
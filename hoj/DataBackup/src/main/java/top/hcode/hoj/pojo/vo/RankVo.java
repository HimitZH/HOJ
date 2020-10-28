package top.hcode.hoj.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 21:09
 * @Description:
 */
@ApiModel(value="排行榜数据类RankVo", description="")
@Data
public class RankVo implements Serializable {

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "学号")
    private String number;

    @ApiModelProperty(value = "总做题数")
    private Integer total;

    @ApiModelProperty(value = "总提交数")
    private Integer submissions;

    @ApiModelProperty(value = "总通过数")
    private Integer ac;

    @ApiModelProperty(value = "cf得分")
    @TableField("Rating")
    private Integer Rating;

    @ApiModelProperty(value = "io制比赛得分")
    private Integer score;
}
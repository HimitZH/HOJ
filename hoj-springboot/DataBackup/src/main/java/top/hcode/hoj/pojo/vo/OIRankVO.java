package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/7 14:56
 * @Description:
 */
@ApiModel(value="OI排行榜数据类OIRankVo", description="")
@Data
public class OIRankVO implements Serializable {
    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "头衔、称号")
    private String titleName;

    @ApiModelProperty(value = "头衔、称号的颜色")
    private String titleColor;

    @ApiModelProperty(value = "OI得分列表")
    private Integer score;

    @ApiModelProperty(value = "总提交数")
    private Integer total;

    @ApiModelProperty(value = "总通过数")
    private Integer ac;

}
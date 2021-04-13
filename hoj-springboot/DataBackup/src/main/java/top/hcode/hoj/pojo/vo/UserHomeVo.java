package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/7 22:27
 * @Description:用户主页的数据格式
 */
@ApiModel(value="用户主页的数据格式类UserHomeVo", description="")
@Data
public class UserHomeVo {

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "个性签名")
    private String signature;

    @ApiModelProperty(value = "github地址")
    private String github;

    @ApiModelProperty(value = "博客地址")
    private String blog;

    @ApiModelProperty(value = "头像地址")
    private String avatar;

    @ApiModelProperty(value = "总提交数")
    private Integer total;

    @ApiModelProperty(value = "cf得分")
    private Integer rating;

    @ApiModelProperty(value = "OI得分")
    private Integer score;

    @ApiModelProperty(value = "已解决题目列表")
    private List<String> solvedList;

}
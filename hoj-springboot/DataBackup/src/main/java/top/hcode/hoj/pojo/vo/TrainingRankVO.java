package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/22 19:58
 * @Description:
 */
@Data
@Accessors(chain = true)
public class TrainingRankVO {

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "用户真实姓名")
    private String realname;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "学校")
    private String school;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "ac题目数")
    private Integer ac;

    @ApiModelProperty(value = "总运行时间ms")
    private Integer totalRunTime;

    @ApiModelProperty(value = "有提交的题的提交详情")
    private HashMap<String, HashMap<String,Object>> submissionInfo;
}
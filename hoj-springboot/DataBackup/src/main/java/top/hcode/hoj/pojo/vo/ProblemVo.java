package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.hcode.hoj.pojo.entity.problem.Tag;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 14:14
 * @Description:
 */
@ApiModel(value="题目列表查询对象ProblemVo", description="")
@Data
public class ProblemVo implements Serializable {

    @ApiModelProperty(value = "题目id")
    private Long pid;

    @ApiModelProperty(value = "题目展示id")
    private String problemId;

    @ApiModelProperty(value = "题目标题")
    private String title;

    @ApiModelProperty(value = "题目难度")
    private Integer difficulty;

    @ApiModelProperty(value = "题目类型")
    private Integer type;

    @ApiModelProperty(value = "题目标签")
    private List<Tag> tags;

    // 以下为题目做题情况

    @ApiModelProperty(value = "该题总提交数")
    private Integer total;

    @ApiModelProperty(value = "通过提交数")
    private Integer ac;

    @ApiModelProperty(value = "空间超限提交数")
    private Integer mle;

    @ApiModelProperty(value = "时间超限提交数")
    private Integer tle;

    @ApiModelProperty(value = "运行错误提交数")
    private Integer re;

    @ApiModelProperty(value = "格式错误提交数")
    private Integer pe;

    @ApiModelProperty(value = "编译错误提交数")
    private Integer ce;

    @ApiModelProperty(value = "答案错误提交数")
    private Integer wa;

    @ApiModelProperty(value = "系统错误提交数")
    private Integer se;

    @ApiModelProperty(value = "该IO题目分数总和")
    private Integer pa;

    @ApiModelProperty(value = "IO题目总分数")
    private Integer score;

}
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
@ApiModel(value = "题目列表查询对象ProblemVO", description = "")
@Data
public class ProblemVO implements Serializable {

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
    private Integer total = 0;

    @ApiModelProperty(value = "通过提交数")
    private Integer ac = 0;

    @ApiModelProperty(value = "空间超限提交数")
    private Integer mle = 0;

    @ApiModelProperty(value = "时间超限提交数")
    private Integer tle = 0;

    @ApiModelProperty(value = "运行错误提交数")
    private Integer re = 0;

    @ApiModelProperty(value = "格式错误提交数")
    private Integer pe = 0;

    @ApiModelProperty(value = "编译错误提交数")
    private Integer ce = 0;

    @ApiModelProperty(value = "答案错误提交数")
    private Integer wa = 0;

    @ApiModelProperty(value = "系统错误提交数")
    private Integer se = 0;

    @ApiModelProperty(value = "该IO题目分数总和")
    private Integer pa = 0;

    @ApiModelProperty(value = "IO题目总分数")
    private Integer score;

    public void setProblemCountVo(ProblemCountVO problemCountVo) {
        this.total = problemCountVo.getTotal() == null ? 0 : problemCountVo.getTotal();
        this.ac = problemCountVo.getAc() == null ? 0 : problemCountVo.getAc();
        this.mle = problemCountVo.getMle() == null ? 0 : problemCountVo.getMle();
        this.tle = problemCountVo.getTle() == null ? 0 : problemCountVo.getTle();
        this.re = problemCountVo.getRe() == null ? 0 : problemCountVo.getRe();
        this.pe = problemCountVo.getPe() == null ? 0 : problemCountVo.getPe();
        this.ce = problemCountVo.getCe() == null ? 0 : problemCountVo.getCe();
        this.wa = problemCountVo.getWa() == null ? 0 : problemCountVo.getWa();
        this.se = problemCountVo.getSe() == null ? 0 : problemCountVo.getSe();
        this.pa = problemCountVo.getPa() == null ? 0 : problemCountVo.getPa();
    }

}
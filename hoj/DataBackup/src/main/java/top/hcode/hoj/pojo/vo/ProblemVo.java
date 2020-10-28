package top.hcode.hoj.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/27 14:14
 * @Description:
 */
@ApiModel(value="题目列表查询对象ProblemVo", description="")
@Data
public class ProblemVo implements Serializable {

    @ApiModelProperty(value = "题目id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "题目")
    private String title;

    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "题目来源")
    private String source;

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

    @ApiModelProperty(value = "题目分数，默认为100")
    private Integer score;

}
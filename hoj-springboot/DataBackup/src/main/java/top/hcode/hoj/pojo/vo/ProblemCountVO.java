package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Data
@Accessors(chain = true)
public class ProblemCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long pid;

    private Integer total;

    private Integer ac;

    @ApiModelProperty(value = "空间超限")
    private Integer mle;

    @ApiModelProperty(value = "时间超限")
    private Integer tle;

    @ApiModelProperty(value = "运行错误")
    private Integer re;

    @ApiModelProperty(value = "格式错误")
    private Integer pe;

    @ApiModelProperty(value = "编译错误")
    private Integer ce;

    @ApiModelProperty(value = "答案错误")
    private Integer wa;

    @ApiModelProperty(value = "系统错误")
    private Integer se;

    @ApiModelProperty(value = "部分通过，OI题目")
    private Integer pa;

}

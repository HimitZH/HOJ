package top.hcode.hoj.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="ProblemCount对象", description="")
public class ProblemCount implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "pid", type = IdType.ID_WORKER)
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

    @ApiModelProperty(value = "题目分数，默认为100")
    private Integer score;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;


}

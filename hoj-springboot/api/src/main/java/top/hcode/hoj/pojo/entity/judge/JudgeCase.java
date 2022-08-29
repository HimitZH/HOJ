package top.hcode.hoj.pojo.entity.judge;

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
@ApiModel(value="JudgeCase对象", description="")
public class JudgeCase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "题目id")
    private Long pid;

    @ApiModelProperty(value = "判题id")
    private Long submitId;

    @ApiModelProperty(value = "用户id")
    private String uid;

    @ApiModelProperty(value = "测试样例id")
    private Long caseId;

    @ApiModelProperty(value = "测试该样例所用时间ms")
    private Integer time;

    @ApiModelProperty(value = "测试该样例所用空间KB")
    private Integer memory;

    @ApiModelProperty(value = "IO得分")
    private Integer score;

    @ApiModelProperty(value = "测试该样例结果状态码")
    private Integer status;

    @ApiModelProperty(value = "样例输入，输入文件名")
    private String inputData;

    @ApiModelProperty(value = "样例输出，输出文件名")
    private String outputData;

    @ApiModelProperty(value = "用户样例输出，暂不使用，当前用于记录对单个测试点的输出或信息提示")
    private String userOutput;

    @ApiModelProperty(value = "subtask分组的组号")
    private Integer groupNum;

    @ApiModelProperty(value = "排序")
    private Integer seq;

    @ApiModelProperty(value = "default,subtask_lowest,subtask_average")
    private String mode;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}

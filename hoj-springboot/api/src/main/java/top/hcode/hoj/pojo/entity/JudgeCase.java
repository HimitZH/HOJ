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
import java.sql.Blob;
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

    @ApiModelProperty(value = "样例输入，比赛不可看,暂不记录")
    private String inputData;

    @ApiModelProperty(value = "样例输出，比赛不可看，暂不记录")
    private String outputData;

    @ApiModelProperty(value = "用户样例输出，比赛不可看，暂不记录")
    private String userOutput;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}

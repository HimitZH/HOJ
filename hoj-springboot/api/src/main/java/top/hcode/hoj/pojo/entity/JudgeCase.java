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

    @TableId(value = "submit_id", type = IdType.ID_WORKER)
    private Long submitId;

    private String uid;

    private Long pid;

    @ApiModelProperty(value = "具体看结果码")
    private Integer status;

    @ApiModelProperty(value = "运行时间")
    private Integer time;

    @ApiModelProperty(value = "运行内存")
    private Integer memory;

    @ApiModelProperty(value = "测试样例id")
    private Long caseId;

    @ApiModelProperty(value = "样例输入，比赛不可看")
    private String inputData;

    @ApiModelProperty(value = "样例输出，比赛不可看")
    private String outputData;

    @ApiModelProperty(value = "用户样例输出，比赛不可看")
    private Blob userOutput;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;


}

package top.hcode.hoj.pojo.entity.problem;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/12 23:14
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="Language对象", description="")
@AllArgsConstructor
@NoArgsConstructor
public class Language {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "语言类型")
    private String contentType;

    @ApiModelProperty(value = "语言描述")
    private String description;

    @ApiModelProperty(value = "语言名字")
    private String name;

    @ApiModelProperty(value = "编译指令")
    private String compileCommand;

    @ApiModelProperty(value = "A+B模板")
    private String template;

    @ApiModelProperty(value = "语言默认代码模板")
    private String codeTemplate;

    @ApiModelProperty(value = "是否可作为特殊判题的一种语言")
    private Boolean isSpj;

    @ApiModelProperty(value = "该语言属于哪个oj，自身oj用ME")
    private String oj;

    @ApiModelProperty(value = "语言顺序")
    private Integer seq;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;
}
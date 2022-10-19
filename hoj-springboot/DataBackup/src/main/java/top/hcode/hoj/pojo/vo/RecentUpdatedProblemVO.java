package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @Author Himit_ZH
 * @Date 2022/10/19
 */
@Data
@Builder
public class RecentUpdatedProblemVO {

    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "题目的自定义ID 例如（HOJ-1000）")
    private String problemId;

    @ApiModelProperty(value = "题目")
    private String title;

    @ApiModelProperty(value = "0为ACM,1为OI")
    private Integer type;

    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;

    @ApiModelProperty(value = "最近更新时间")
    private Date gmtModified;
}

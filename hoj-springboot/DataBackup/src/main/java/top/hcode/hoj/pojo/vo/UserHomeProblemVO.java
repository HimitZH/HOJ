package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @Author Himit_ZH
 * @Date 2022/10/19
 */
@Data
@Builder
public class UserHomeProblemVO {

    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "题目的自定义ID 例如（HOJ-1000）")
    private String problemId;

    @ApiModelProperty(value = "题目难度")
    private Integer difficulty;
}

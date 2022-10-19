package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Himit_ZH
 * @Date 2022/10/2
 */
@ApiModel(value="比赛奖项配置", description="")
@Data
public class ContestAwardConfigVO {

    @ApiModelProperty(value = "优先级")
    private Integer priority;

    @ApiModelProperty(value = "奖项名称")
    private String name;

    @ApiModelProperty(value = "背景颜色")
    private String background;

    @ApiModelProperty(value = "文本颜色")
    private String color;

    @ApiModelProperty(value = "数量")
    private Integer num;

}

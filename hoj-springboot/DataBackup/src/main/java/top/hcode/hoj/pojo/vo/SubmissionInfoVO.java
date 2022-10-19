package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import top.hcode.hoj.pojo.entity.judge.Judge;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 11:38
 * @Description:
 */
@Data
public class SubmissionInfoVO {

    @ApiModelProperty(value = "提交详情")
    private Judge submission;

    @ApiModelProperty(value = "提交者是否可以分享该代码")
    private Boolean codeShare;
}
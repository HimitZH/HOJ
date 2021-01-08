package top.hcode.hoj.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/30 11:03
 * @Description: 用户代码提交类
 */
@Data
@Accessors(chain = true)
public class ToJudgeDto {
    @NotBlank(message = "题目id不能为空,由于可能是比赛的展示id，所以使用string")
    private String pid;

    @NotBlank(message = "代码语言选择不能为空")
    private String language;

    @NotBlank(message = "提交的代码不能为空")
    private String code;

    @NotBlank(message = "提交的比赛id所属不能为空，若并非比赛提交，请设置为0")
    private Long cid;

}
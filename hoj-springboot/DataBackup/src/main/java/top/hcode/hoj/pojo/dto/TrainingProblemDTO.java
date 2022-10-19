package top.hcode.hoj.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 11:10
 * @Description:
 */
@Data
public class TrainingProblemDTO {

    @NotBlank(message = "题目id不能为空")
    private Long pid;

    @NotBlank(message = "训练id不能为空")
    private Long tid;

    @NotBlank(message = "题目在训练中的展示id不能为空")
    private String displayId;
}
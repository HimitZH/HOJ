package top.hcode.hoj.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 17:55
 * @Description:
 */
@Data
public class RegisterTrainingDTO {

    @NotBlank(message = "tid不能为空")
    private Long tid;

    @NotBlank(message = "password不能为空")
    private String password;
}
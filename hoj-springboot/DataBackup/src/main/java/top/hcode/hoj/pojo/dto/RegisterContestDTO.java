package top.hcode.hoj.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/12 14:37
 * @Description:
 */
@Data
public class RegisterContestDTO {

    @NotBlank(message = "cid不能为空")
    private Long cid;

    @NotBlank(message = "password不能为空")
    private String password;
}
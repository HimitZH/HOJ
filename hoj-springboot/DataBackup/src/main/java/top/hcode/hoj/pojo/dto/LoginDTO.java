package top.hcode.hoj.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Author: Himit_ZH
 * @Date: 2020/7/20 00:23
 * @Description: 登录数据实体类
 */
@Data
public class LoginDTO implements Serializable {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
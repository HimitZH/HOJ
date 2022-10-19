package top.hcode.hoj.pojo.dto;

import lombok.Data;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 18:06
 * @Description:
 */
@Data
public class ChangePasswordDTO {

    private String oldPassword;

    private String newPassword;
}
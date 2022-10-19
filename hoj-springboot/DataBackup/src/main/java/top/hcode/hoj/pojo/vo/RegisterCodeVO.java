package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 17:08
 * @Description:
 */
@Data
public class RegisterCodeVO {

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "注册邮件有效时间，单位秒")
    private Integer expire;
}
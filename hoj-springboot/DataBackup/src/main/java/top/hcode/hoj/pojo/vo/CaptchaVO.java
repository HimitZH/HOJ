package top.hcode.hoj.pojo.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 16:37
 * @Description:
 */
@Data
public class CaptchaVO {

    @ApiModelProperty(value = "验证码图片的base64")
    private String img;

    @ApiModelProperty(value = "验证码key")
    private String captchaKey;
}
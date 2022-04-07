package top.hcode.hoj.pojo.dto;

import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author sgpublic
 * @Date 2022/4/2 19:40
 * @Description
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@ApiModel(value="EmailConfig", description="")
public class EmailConfigDto {

    /**
     * SMTP 主机
     */
    private String emailHost;

    /**
     * SMTP 密码/授权码
     */
    private String emailPassword;

    /**
     * SMTP 端口
     */
    private Integer emailPort;

    /**
     * SMTP 邮箱
     */
    private String emailUsername;

    /**
     * 邮件背景图片
     */
    private String emailBGImg;

    /**
     * SMTP 使用 SSL
     */
    private Boolean emailSsl;
}

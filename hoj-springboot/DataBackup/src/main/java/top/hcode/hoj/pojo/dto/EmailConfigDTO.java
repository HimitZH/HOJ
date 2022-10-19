package top.hcode.hoj.pojo.dto;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * @Author sgpublic
 * @Date 2022/4/2 19:40
 * @Description
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailConfigDTO {

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

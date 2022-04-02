package top.hcode.hoj.pojo.entity.admin;

import io.swagger.annotations.ApiModel;
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
@ApiModel(value="EmailConfig", description="")
public class EmailConfig {

    /**
     * SMTP 主机
     */
    private String emailHost = null;

    /**
     * SMTP 密码/授权码
     */
    private String emailPassword = null;

    /**
     * SMTP 端口
     */
    private Integer emailPort = null;

    /**
     * SMTP 邮箱
     */
    private String emailUsername = null;

    /**
     * 邮件背景图片
     */
    private String emailBGImg = null;

    /**
     * SMTP 使用 SSL
     */
    private Boolean emailSsl = null;
}

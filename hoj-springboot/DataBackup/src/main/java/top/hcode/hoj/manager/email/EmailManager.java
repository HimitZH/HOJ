package top.hcode.hoj.manager.email;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.UnicodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import top.hcode.hoj.config.NacosSwitchConfig;
import top.hcode.hoj.config.WebConfig;
import top.hcode.hoj.utils.Constants;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 22:01
 * @Description:
 */

@Component
@RefreshScope
@Slf4j(topic = "hoj")
public class EmailManager {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private NacosSwitchConfig nacosSwitchConfig;

    /**
     * @MethodName getMailSender
     * @Params * @param
     * @Description 获取邮件系统配置
     * @Return
     * @Since 2021/5/21
     */
    private JavaMailSenderImpl getMailSender() {
        WebConfig webConfig = nacosSwitchConfig.getWebConfig();
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(webConfig.getEmailHost());
        sender.setPort(webConfig.getEmailPort());
        sender.setDefaultEncoding("UTF-8");
        sender.setUsername(webConfig.getEmailUsername());
        sender.setPassword(webConfig.getEmailPassword());

        Properties p = new Properties();
        p.setProperty("mail.smtp.ssl.enable", webConfig.getEmailSsl().toString());
        p.setProperty("mail.smtp.auth", "true");
        p.setProperty("mail.smtp.starttls.enable", webConfig.getEmailSsl().toString());
        sender.setJavaMailProperties(p);
        return sender;
    }

    /**
     * @MethodName isOk
     * @Params * @param null
     * @Description 验证当前邮箱系统是否已配置。
     * @Return
     * @Since 2021/6/12
     */
    public boolean isOk() {
        WebConfig webConfig = nacosSwitchConfig.getWebConfig();
        return webConfig.getEmailUsername() != null
                && webConfig.getEmailPassword() != null
                && !webConfig.getEmailUsername().equals("your_email_username")
                && !webConfig.getEmailPassword().equals("your_email_password")
                && Validator.isEmail(webConfig.getEmailUsername());
    }

    /**
     * @param email 用户邮箱
     * @param code  生成的六位随机数字验证码
     * @MethodName sendRegisterCode
     * @Description 为正在注册的用户发送一份注册验证码。
     * @Return
     * @Since 2021/1/14
     */
    @Async
    public void sendRegisterCode(String email, String code) {
        DateTime expireTime = DateUtil.offsetMinute(new Date(), 10);
        JavaMailSenderImpl mailSender = getMailSender();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            // 设置渲染到html页面对应的值
            Context context = new Context();
            WebConfig webConfig = nacosSwitchConfig.getWebConfig();
            context.setVariable(Constants.Email.OJ_NAME.name(), UnicodeUtil.toString(webConfig.getName()));
            context.setVariable(Constants.Email.OJ_SHORT_NAME.name(), UnicodeUtil.toString(webConfig.getShortName()));
            context.setVariable(Constants.Email.OJ_URL.name(), webConfig.getBaseUrl());
            context.setVariable(Constants.Email.EMAIL_BACKGROUND_IMG.name(), webConfig.getEmailBGImg());
            context.setVariable("CODE", code);
            context.setVariable("EXPIRE_TIME", expireTime.toString());

            //利用模板引擎加载html文件进行渲染并生成对应的字符串
            String emailContent = templateEngine.process("emailTemplate_registerCode", context);

            // 设置邮件标题
            mimeMessageHelper.setSubject(UnicodeUtil.toString(webConfig.getShortName()) + "的注册邮件");
            mimeMessageHelper.setText(emailContent, true);
            // 收件人
            mimeMessageHelper.setTo(email);
            // 发送人
            mimeMessageHelper.setFrom(webConfig.getEmailUsername());

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("用户注册的邮件任务发生异常------------>{}", e.getMessage());
        }
    }


    /**
     * @param username 需要重置密码的用户名
     * @param email    用户邮箱
     * @param code     随机生成20位数字与字母的组合
     * @MethodName sendResetPassword
     * @Description 给指定的邮箱的用户发送重置密码链接的邮件。
     * @Return
     * @Since 2021/1/14
     */
    @Async
    public void sendResetPassword(String username, String code, String email) {
        DateTime expireTime = DateUtil.offsetMinute(new Date(), 10);
        JavaMailSenderImpl mailSender = getMailSender();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                    true);
            // 设置渲染到html页面对应的值
            Context context = new Context();
            WebConfig webConfig = nacosSwitchConfig.getWebConfig();
            context.setVariable(Constants.Email.OJ_NAME.name(), UnicodeUtil.toString(webConfig.getName()));
            context.setVariable(Constants.Email.OJ_SHORT_NAME.name(), UnicodeUtil.toString(webConfig.getShortName()));
            context.setVariable(Constants.Email.OJ_URL.name(), webConfig.getBaseUrl());
            context.setVariable(Constants.Email.EMAIL_BACKGROUND_IMG.name(), webConfig.getEmailBGImg());

            String resetUrl;
            if (webConfig.getBaseUrl().endsWith("/")) {
                resetUrl = webConfig.getBaseUrl() + "reset-password?username=" + username + "&code=" + code;
            } else {
                resetUrl = webConfig.getBaseUrl() + "/reset-password?username=" + username + "&code=" + code;
            }

            context.setVariable("RESET_URL", resetUrl);
            context.setVariable("EXPIRE_TIME", expireTime.toString());
            context.setVariable("USERNAME", username);

            //利用模板引擎加载html文件进行渲染并生成对应的字符串
            String emailContent = templateEngine.process("emailTemplate_resetPassword", context);

            mimeMessageHelper.setSubject(UnicodeUtil.toString(webConfig.getShortName()) + "的重置密码邮件");

            mimeMessageHelper.setText(emailContent, true);
            // 收件人
            mimeMessageHelper.setTo(email);
            // 发送人
            mimeMessageHelper.setFrom(webConfig.getEmailUsername());
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("用户重置密码的邮件任务发生异常------------>{}", e.getMessage());
        }
    }


    /**
     * @param email 用户邮箱
     * @MethodName testEmail
     * @Description 超级管理员后台修改邮件系统配置后发送的测试邮箱可用性的测试邮件。
     * @Return
     * @Since 2021/1/14
     */
    @Async
    public void testEmail(String email) {
        JavaMailSenderImpl mailSender = getMailSender();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                    true);
            // 设置渲染到html页面对应的值
            Context context = new Context();
            WebConfig webConfig = nacosSwitchConfig.getWebConfig();
            context.setVariable(Constants.Email.OJ_NAME.name(), UnicodeUtil.toString(webConfig.getName()));
            context.setVariable(Constants.Email.OJ_SHORT_NAME.name(), UnicodeUtil.toString(webConfig.getShortName()));
            context.setVariable(Constants.Email.OJ_URL.name(), webConfig.getBaseUrl());
            context.setVariable(Constants.Email.EMAIL_BACKGROUND_IMG.name(), webConfig.getBaseUrl());
            //利用模板引擎加载html文件进行渲染并生成对应的字符串
            String emailContent = templateEngine.process("emailTemplate_testEmail", context);

            mimeMessageHelper.setSubject(UnicodeUtil.toString(webConfig.getShortName()) + "的测试邮件");

            mimeMessageHelper.setText(emailContent, true);
            // 收件人
            mimeMessageHelper.setTo(email);
            // 发送人
            mimeMessageHelper.setFrom(webConfig.getEmailUsername());
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("超级管理员重置邮件系统配置的测试邮箱可用性的任务发生异常------------>{}", e.getMessage());
        }
    }


    /**
     * @param email 用户邮箱
     * @param code  生成的六位随机数字验证码
     * @MethodName sendChangeEmailCode
     * @Description 为正在修改邮箱的用户的新邮箱发送验证码
     * @Return
     * @Since 2021/1/14
     */
    public void sendChangeEmailCode(String email, String username, String code) {
        DateTime expireTime = DateUtil.offsetMinute(new Date(), 10);
        JavaMailSenderImpl mailSender = getMailSender();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            // 设置渲染到html页面对应的值
            Context context = new Context();
            WebConfig webConfig = nacosSwitchConfig.getWebConfig();
            context.setVariable(Constants.Email.OJ_NAME.name(), UnicodeUtil.toString(webConfig.getName()));
            context.setVariable(Constants.Email.OJ_SHORT_NAME.name(), UnicodeUtil.toString(webConfig.getShortName()));
            context.setVariable(Constants.Email.OJ_URL.name(), webConfig.getBaseUrl());
            context.setVariable(Constants.Email.EMAIL_BACKGROUND_IMG.name(), webConfig.getEmailBGImg());
            context.setVariable("CODE", code);
            context.setVariable("USERNAME", username);
            context.setVariable("EXPIRE_TIME", expireTime.toString());

            //利用模板引擎加载html文件进行渲染并生成对应的字符串
            String emailContent = templateEngine.process("emailTemplate_changeEmailCode", context);

            // 设置邮件标题
            mimeMessageHelper.setSubject(UnicodeUtil.toString(webConfig.getShortName()) + "的修改邮箱邮件");
            mimeMessageHelper.setText(emailContent, true);
            // 收件人
            mimeMessageHelper.setTo(email);
            // 发送人
            mimeMessageHelper.setFrom(webConfig.getEmailUsername());

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("用户修改邮箱的邮件任务发生异常------------>{}", e.getMessage());
        }
    }

}
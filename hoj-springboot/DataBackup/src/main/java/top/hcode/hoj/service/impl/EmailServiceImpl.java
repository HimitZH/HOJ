package top.hcode.hoj.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import top.hcode.hoj.service.EmailService;
import top.hcode.hoj.utils.Constants;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/24 13:21
 * @Description: 异步发送邮件的任务
 */
@Service
@Async
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * @param email 用户邮箱
     * @param code  生成的六位随机数字验证码
     * @MethodName sendCode
     * @Description 为正在注册的用户发送一份注册验证码。
     * @Return
     * @Since 2021/1/14
     */
    @Override
    public void sendCode(String email, String code) {
        DateTime expireTime = DateUtil.offsetMinute(new Date(), 10);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                    true);
            // 设置渲染到html页面对应的值
            Context context = new Context();
            context.setVariable(Constants.Email.OJ_NAME.name(), Constants.Email.OJ_NAME.getValue());
            context.setVariable(Constants.Email.OJ_SHORT_NAME.name(), Constants.Email.OJ_SHORT_NAME.getValue());
            context.setVariable(Constants.Email.OJ_URL.name(), Constants.Email.OJ_URL.getValue());
            context.setVariable(Constants.Email.EMAIL_BACKGROUND_IMG.name(), Constants.Email.EMAIL_BACKGROUND_IMG.getValue());
            context.setVariable("CODE", code);
            context.setVariable("EXPIRE_TIME", expireTime.toString());

            //利用模板引擎加载html文件进行渲染并生成对应的字符串
            String emailContent = templateEngine.process("emailTemplate_registerCode", context);
            // 设置邮件标题
            mimeMessageHelper.setSubject("HOJ的注册邮件");
            mimeMessageHelper.setText(emailContent, true);
            // 收件人
            mimeMessageHelper.setTo(email);
            // 发送人
            mimeMessageHelper.setFrom(Constants.Email.EMAIL_FROM.getValue());
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("用户注册的邮件任务发生异常------------>{}", e.getMessage());
        }
    }


    /**
     * @param username 需要重置密码的用户名
     * @param email 用户邮箱
     * @param code  随机生成20位数字与字母的组合
     * @MethodName sendResetPassword
     * @Description 给指定的邮箱的用户发送重置密码链接的邮件。
     * @Return
     * @Since 2021/1/14
     */
    @Override
    public void sendResetPassword(String username, String code, String email) {
        DateTime expireTime = DateUtil.offsetMinute(new Date(), 10);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                    true);
            // 设置渲染到html页面对应的值
            Context context = new Context();
            context.setVariable(Constants.Email.OJ_NAME.name(), Constants.Email.OJ_NAME.getValue());
            context.setVariable(Constants.Email.OJ_SHORT_NAME.name(), Constants.Email.OJ_SHORT_NAME.getValue());
            context.setVariable(Constants.Email.OJ_URL.name(), Constants.Email.OJ_URL.getValue());
            context.setVariable(Constants.Email.EMAIL_BACKGROUND_IMG.name(), Constants.Email.EMAIL_BACKGROUND_IMG.getValue());
            context.setVariable("RESET_URL", Constants.Email.OJ_URL.getValue() + "/reset-password?username=" + username + "&code=" + code);
            context.setVariable("EXPIRE_TIME", expireTime.toString());
            context.setVariable("USERNAME", username);

            //利用模板引擎加载html文件进行渲染并生成对应的字符串
            String emailContent = templateEngine.process("emailTemplate_resetPassword", context);

            mimeMessageHelper.setSubject("HOJ的重置密码邮件");

            mimeMessageHelper.setText(emailContent, true);
            // 收件人
            mimeMessageHelper.setTo(email);
            // 发送人
            mimeMessageHelper.setFrom(Constants.Email.EMAIL_FROM.getValue());
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
    @Override
    public void testEmail(String email) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                    true);
            // 设置渲染到html页面对应的值
            Context context = new Context();
            context.setVariable(Constants.Email.OJ_NAME.name(), Constants.Email.OJ_NAME.getValue());
            context.setVariable(Constants.Email.OJ_SHORT_NAME.name(), Constants.Email.OJ_SHORT_NAME.getValue());
            context.setVariable(Constants.Email.OJ_URL.name(), Constants.Email.OJ_URL.getValue());
            context.setVariable(Constants.Email.EMAIL_BACKGROUND_IMG.name(), Constants.Email.EMAIL_BACKGROUND_IMG.getValue());

            //利用模板引擎加载html文件进行渲染并生成对应的字符串
            String emailContent = templateEngine.process("emailTemplate_testEmail", context);

            mimeMessageHelper.setSubject("HOJ的测试邮件");

            mimeMessageHelper.setText(emailContent, true);
            // 收件人
            mimeMessageHelper.setTo(email);
            // 发送人
            mimeMessageHelper.setFrom(Constants.Email.EMAIL_FROM.getValue());
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("超级管理员重置邮件系统配置的测试邮箱可用性的任务发生异常------------>{}", e.getMessage());
        }
    }
}
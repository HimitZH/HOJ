package top.hcode.hoj.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hcode.hoj.service.EmailService;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;


/**
 * @Author: Himit_ZH
 * @Date: 2020/10/24 13:21
 * @Description:
 */
@Service
@Async
public class EmailServiceImpl implements EmailService {

    @Autowired
    JavaMailSender mailSender;

    @Override
    public void sendCode(String email, String code) throws MessagingException {
        DateTime dateTime = DateUtil.offsetMinute(new Date(), 5);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                true);
        mimeMessageHelper.setSubject("HOJ的注册邮件");
        mimeMessageHelper.setText("<div style=\"background: white;\n" +
                "\t\t      width: 100%;\n" +
                "\t\t      max-width: 800px;\n" +
                "\t\t      margin: auto auto;\n" +
                "\t\t      border-radius: 5px;\n" +
                "\t\t      border:#1bc3fb 1px solid;\n" +
                "\t\t      overflow: hidden;\n" +
                "\t\t      -webkit-box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.12);\n" +
                "\t\t      box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.18);\">\n" +
                "\t\t\t\t<header style=\"overflow: hidden;\">\n" +
                "\t\t\t\t\t<center>\n" +
                "\t\t\t\t\t\t<img style=\"width:100%;HEz-index: 666;\" src=\"https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/HCODE.png\">\n" +
                "\t\t\t\t\t</center>\n" +
                "\t\t\t\t</header>\n" +
                "\t\t\t\t<div style=\"padding: 5px 20px;\">\n" +
                "\t\t\t\t\t<p style=\"position: relative;\n" +
                "\t\t        color: white;\n" +
                "\t\t        float: left;\n" +
                "\t\t        z-index: 999;\n" +
                "\t\t        background: #1bc3fb;\n" +
                "\t\t        padding: 5px 30px;\n" +
                "\t\t        margin: -25px auto 0 ;\n" +
                "\t\t        box-shadow: 5px 5px 5px rgba(0, 0, 0, 0.30)\">\n" +
                "\t\t\t\t\t\tDear New HOJer\n" +
                "\t\t\t\t\t</p>\n" +
                "\t\t\t\t\t<br>\n" +
                "\t\t\t\t\t<center>\n" +
                "\t\t\t\t\t\t<h3>\n" +
                "\t\t\t\t\t\t\t来自 <span style=\"text-decoration: none;color: #FF779A; \">HOJ</span> 邮件提醒\n" +
                "\t\t\t\t\t\t</h3>\n" +
                "\t\t\t\t\t<p style=\"text-indent:2em; \">\n" +
                "\t\t\t\t\t\t您收到这封电子邮件是因为您 (也可能是某人冒充您的名义) 在<a style=\"text-decoration: none;color: #1bc3fb \" target=\"_blank\" href=\"${POST_URL}\" rel=\"noopener\">&nbsp;HOJ&nbsp;</a>上进行注册。假如这不是您本人所申请, 请不用理会这封电子邮件, 但是如果您持续收到这类的信件骚扰, 请您尽快联络管理员。\n" +
                "\t\t\t\t\t</p>\n" +
                "\t\t\t\t\t<div style=\"background: #fafafa repeating-linear-gradient(-45deg,#fff,#fff 1.125rem,transparent 1.125rem,transparent 2.25rem);box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);margin:20px 0px;padding:15px;border-radius:5px;font-size:14px;color:#555555;text-align: center;\">\n" +
                "\t\t\t\t\t\t请使用以下验证码完成后续注册流程：<br>\n" +
                "\t\t\t\t\t\t <span style=\"color: #FF779A;font-weight: bolder;font-size: 25px;\">"+code+"</span><br>\n" +
                "\t\t\t\t\t\t 注意:请您在收到邮件5分钟内("+dateTime.toString()+"前)使用，否则该验证码将会失效。\n" +
                "\t\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t&nbsp; &nbsp;\n" +
                "\n" +
                "\t\t\t\t\t<br>\n" +
                "\t\t\t\t\t<div style=\"text-align: center;\">\n" +
                "\t\t\t\t\t\t<a style=\"text-transform: uppercase;\n" +
                "\t\t                      text-decoration: none;\n" +
                "\t\t                      font-size: 14px;\n" +
                "\t\t                      background: #FF779A;\n" +
                "\t\t                      color: #FFFFFF;\n" +
                "\t\t                      padding: 10px;\n" +
                "\t\t                      display: inline-block;\n" +
                "\t\t                      border-radius: 5px;\n" +
                "\t\t                      margin: 10px auto 0; \"\n" +
                "\t\t\t\t\t\t target=\"_blank\" href=\"oj.hcode.top\" rel=\"noopener\">HOJ｜传送门\uD83D\uDEAA</a>\n" +
                "\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t<p style=\"font-size: 12px;text-align: center;color: #999;\">\n" +
                "\t\t\t\t\t\t欢迎常来访问！<br>\n" +
                "\t\t\t\t\t\t© 2020 <a style=\"text-decoration:none; color:#1bc3fb\" href=\"${SITE_URL}\" rel=\"noopener\" target=\"_blank\">\n" +
                "\t\t\t\t\t\t\tHODE-OJ </a>\n" +
                "\t\t\t\t\t</p>\n" +
                "\t\t\t\t\t<p></p>\n" +
                "\t\t\t\t</div>\n" +
                "\t\t\t</div>",true);


        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setFrom("oj.hcode@qq.com");
        mailSender.send(mimeMessage);
    }

    @Override
    public void sendResetPassword(String username, String code, String email) throws MessagingException {
        DateTime dateTime = DateUtil.offsetMinute(new Date(), 10);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,
                true);
        mimeMessageHelper.setSubject("HOJ的重置密码邮件");
        mimeMessageHelper.setText("<div style=\"background: white;\n" +
                "\t\t      width: 100%;\n" +
                "\t\t      max-width: 800px;\n" +
                "\t\t      margin: auto auto;\n" +
                "\t\t      border-radius: 5px;\n" +
                "\t\t      border:#1bc3fb 1px solid;\n" +
                "\t\t      overflow: hidden;\n" +
                "\t\t      -webkit-box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.12);\n" +
                "\t\t      box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.18);\">\n" +
                "\t\t\t\t<header style=\"overflow: hidden;\">\n" +
                "\t\t\t\t\t<center>\n" +
                "\t\t\t\t\t\t<img style=\"width:100%;HEz-index: 666;\" src=\"https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/HCODE.png\">\n" +
                "\t\t\t\t\t</center>\n" +
                "\t\t\t\t</header>\n" +
                "\t\t\t\t<div style=\"padding: 5px 20px;\">\n" +
                "\t\t\t\t\t<p style=\"position: relative;\n" +
                "\t\t        color: white;\n" +
                "\t\t        float: left;\n" +
                "\t\t        z-index: 999;\n" +
                "\t\t        background: #1bc3fb;\n" +
                "\t\t        padding: 5px 30px;\n" +
                "\t\t        margin: -25px auto 0 ;\n" +
                "\t\t        box-shadow: 5px 5px 5px rgba(0, 0, 0, 0.30)\">\n" +
                "\t\t\t\t\t\tDear "+username+"\n" +
                "\t\t\t\t\t</p>\n" +
                "\t\t\t\t\t<br>\n" +
                "\t\t\t\t\t<center>\n" +
                "\t\t\t\t\t\t<h3>\n" +
                "\t\t\t\t\t\t\t来自 <span style=\"text-decoration: none;color: #FF779A; \">HOJ</span> 邮件提醒\n" +
                "\t\t\t\t\t\t</h3>\n" +
                "\t\t\t\t\t<p style=\"text-indent:2em; \">\n" +
                "\t\t\t\t\t\t您收到这封电子邮件是因为您 (也可能是某人冒充您的名义) 在<a style=\"text-decoration: none;color: #1bc3fb \" target=\"_blank\" href=\"${POST_URL}\" rel=\"noopener\">&nbsp;HOJ&nbsp;</a>上进行密码重置操作。假如这不是您本人所申请, 请不用理会这封电子邮件, 但是如果您持续收到这类的信件骚扰, 请您尽快联络管理员。\n" +
                "\t\t\t\t\t</p>\n" +
                "\t\t\t\t\t<div style=\"background: #fafafa repeating-linear-gradient(-45deg,#fff,#fff 1.125rem,transparent 1.125rem,transparent 2.25rem);box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);margin:20px 0px;padding:15px;border-radius:5px;font-size:14px;color:#555555;text-align: center;\">\n" +
                "\t\t\t\t\t\t请点击下面的链接完成后续重置密码的流程：<br>\n" +
                "\t\t\t\t\t\t <a href=\"http://localhost:8080/reset-password?username="+username+"&code="+code+"\"  style=\"color: #FF779A;font-weight: bolder;font-size: 25px;text-decoration: none;\">CLICK HERE</a><br>\n" +
                "\t\t\t\t\t\t 注意:请您在收到邮件10分钟内("+dateTime.toString()+"前)使用，否则该链接将会失效。\n" +
                "\t\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t&nbsp; &nbsp;\n" +
                "\n" +
                "\t\t\t\t\t<br>\n" +
                "\t\t\t\t\t<div style=\"text-align: center;\">\n" +
                "\t\t\t\t\t\t<a style=\"text-transform: uppercase;\n" +
                "\t\t                      text-decoration: none;\n" +
                "\t\t                      font-size: 14px;\n" +
                "\t\t                      background: #FF779A;\n" +
                "\t\t                      color: #FFFFFF;\n" +
                "\t\t                      padding: 10px;\n" +
                "\t\t                      display: inline-block;\n" +
                "\t\t                      border-radius: 5px;\n" +
                "\t\t                      margin: 10px auto 0; \"\n" +
                "\t\t\t\t\t\t target=\"_blank\" href=\"oj.hcode.top\" rel=\"noopener\">HOJ｜传送门\uD83D\uDEAA</a>\n" +
                "\t\t\t\t\t</div>\n" +
                "\t\t\t\t\t<p style=\"font-size: 12px;text-align: center;color: #999;\">\n" +
                "\t\t\t\t\t\t欢迎常来访问！<br>\n" +
                "\t\t\t\t\t\t© 2020 <a style=\"text-decoration:none; color:#1bc3fb\" href=\"${SITE_URL}\" rel=\"noopener\" target=\"_blank\">\n" +
                "\t\t\t\t\t\t\tHODE-OJ </a>\n" +
                "\t\t\t\t\t</p>\n" +
                "\t\t\t\t\t<p></p>\n" +
                "\t\t\t\t</div>\n" +
                "\t\t\t</div>",true);


        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setFrom("oj.hcode@qq.com");
        mailSender.send(mimeMessage);
    }
}
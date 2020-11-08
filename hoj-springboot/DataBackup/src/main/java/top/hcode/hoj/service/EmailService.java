package top.hcode.hoj.service;

import javax.mail.MessagingException;

public interface EmailService {
    public void sendCode(String email,String code) throws MessagingException;
    public void sendResetPassword(String username,String code,String email) throws MessagingException;
}

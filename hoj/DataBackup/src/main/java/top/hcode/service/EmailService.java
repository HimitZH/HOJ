package top.hcode.service;

import javax.mail.MessagingException;

public interface EmailService {
    public void sendCode(String email,String username,String code) throws MessagingException;
}

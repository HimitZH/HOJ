package top.hcode.hoj.service;


public interface EmailService {
    public void sendCode(String email,String code);
    public void sendResetPassword(String username,String code,String email);
    public void testEmail(String email) ;
}

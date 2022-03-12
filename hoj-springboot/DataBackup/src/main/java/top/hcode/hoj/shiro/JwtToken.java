package top.hcode.hoj.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @Author: Himit_ZH
 * @Date: 2020/7/19 22:58
 * @Description:
 */
public class JwtToken implements AuthenticationToken {

    private String token;

    public JwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
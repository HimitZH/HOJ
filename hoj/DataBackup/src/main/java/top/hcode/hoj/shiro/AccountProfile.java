package top.hcode.hoj.shiro;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Himit_ZH
 * @Date: 2020/7/19 22:59
 * @Description:
 */
@Data
public class AccountProfile implements Serializable {
    private String uuid;

    private String username;

    private String password;

    private String nickname;

    private String school;

    private String course;

    private String number;

    private String realname;

    private String cfUsername;

    private String email;

    private String avatar;

    private String signature;

    private int status;

    public String getId(){ //shiro登录用户实体默认主键获取方法要为getId
        return uuid;
    }
}
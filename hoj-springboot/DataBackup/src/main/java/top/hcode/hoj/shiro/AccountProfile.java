package top.hcode.hoj.shiro;

import lombok.Data;
import top.hcode.hoj.pojo.entity.user.Role;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/7/19 22:59
 * @Description:
 */
@Data
public class AccountProfile implements Serializable {
    private String uid;

    private String username;

    private String password;

    private String nickname;

    private String school;

    private String course;

    private String number;

    private String gender;

    private String realname;

    private String cfUsername;

    private String email;

    private String avatar;

    private String signature;

    private int status;

    private Date gmtCreate;


    private Date gmtModified;


    private List<Role> roles;

    public String getId(){ //shiro登录用户实体默认主键获取方法要为getId
        return uid;
    }
}
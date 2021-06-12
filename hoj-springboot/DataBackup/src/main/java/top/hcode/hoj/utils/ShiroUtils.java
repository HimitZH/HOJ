package top.hcode.hoj.utils;

import org.apache.shiro.SecurityUtils;
import top.hcode.hoj.shiro.AccountProfile;


/**
 * @Author: Himit_ZH
 * @Date: 2020/7/20 14:13
 * @Description:
 */
public class ShiroUtils {

    private ShiroUtils() {
    }

    public static AccountProfile getProfile(){
        return (AccountProfile) SecurityUtils.getSubject().getPrincipal();
    }

}
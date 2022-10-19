package top.hcode.hoj.pojo.vo;

import lombok.Data;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 17:58
 * @Description:
 */
@Data
public class ChangeAccountVO {

    private Integer code;

    private String msg;

    private UserInfoVO userInfo;
}
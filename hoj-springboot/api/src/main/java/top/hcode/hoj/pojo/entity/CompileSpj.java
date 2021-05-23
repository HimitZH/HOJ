package top.hcode.hoj.pojo.entity;

import lombok.Data;


/**
 * @Author: Himit_ZH
 * @Date: 2021/2/6 14:42
 * @Description:
 */
@Data
public class CompileSpj {

    private String spjSrc;

    private Long pid;

    private String spjLanguage;

    private String token;
}
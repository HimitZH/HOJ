package top.hcode.hoj.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;


/**
 * @Author: Himit_ZH
 * @Date: 2021/2/6 14:42
 * @Description:
 */
@Data
public class CompileDTO implements Serializable {

    private static final long serialVersionUID = 333L;

    /**
     * 编译的源代码
     */
    private String code;

    /**
     * 编译的源代码相关的题目id
     */
    private Long pid;

    /**
     * 编译的源代码所选语言
     */
    private String language;

    /**
     * 调用判题机的凭证
     */
    private String token;

    /**
     * 编译所需的额外文件，key:文件名,value:文件内容
     */
    private HashMap<String,String> extraFiles;
}
package top.hcode.hoj.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @Author Himit_ZH
 * @Date 2022/5/26
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TestJudgeReq implements Serializable {
    private static final long serialVersionUID = 666L;

    /**
     * 调用评测验证的token
     */
    private String token;

    /**
     * 调用的唯一标识，用于返回结果识别
     */
    private String uniqueKey;

    /**
     * 评测的代码
     */
    private String code;

    /**
     * 评测的语言
     */
    private String language;

    /**
     * 评测时允许调用的额外文件
     */
    private HashMap<String, String> extraFile;

    /**
     * 评测的最大时间限制 ms
     */
    private Integer timeLimit;

    /**
     * 评测的最大空间限制 mb
     */
    private Integer memoryLimit;

    /**
     * 评测的最大栈空间限制 mb
     */
    private Integer stackLimit;

    /**
     * 输入数据
     */
    private String testCaseInput;

    /**
     * 期望输出
     */
    private String expectedOutput;

    /**
     * 是否在对比testcaseOutput和expectedOutput去除每行末尾空白符
     */
    private Boolean isRemoveEndBlank;

    /**
     * 原题的评测模式：default、spj、interactive
     */
    private String problemJudgeMode;

    /**
     * 是否是file io自定义输入输出文件模式
     */
    private Boolean isFileIO;

    /***
     * 题目指定的io file输入文件的名称
     */
    private String ioReadFileName;

    /***
     * 题目指定的io file输出文件的名称
     */
    private String ioWriteFileName;

}

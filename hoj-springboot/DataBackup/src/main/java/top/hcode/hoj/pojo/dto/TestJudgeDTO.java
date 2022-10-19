package top.hcode.hoj.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @Author Himit_ZH
 * @Date 2022/5/27
 */
@Data
@Accessors(chain = true)
public class TestJudgeDTO {

    @NotBlank(message = "题目id")
    private Long pid;

    @NotBlank(message = "评测类型：public、contest、group")
    private String type;

    @NotBlank(message = "代码")
    private String code;

    @NotBlank(message = "编程语言")
    private String language;

    @NotBlank(message = "输入")
    private String userInput;

    /**
     * 预期输出
     */
    private String expectedOutput;

    /**
     * 是否为原创OJ的题目
     */
    private Boolean isRemoteJudge;

    /**
     * text/x-csrc 用于鉴别语言
     */
    private String mode;
}

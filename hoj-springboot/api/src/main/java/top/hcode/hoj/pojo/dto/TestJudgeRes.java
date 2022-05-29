package top.hcode.hoj.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author Himit_ZH
 * @Date 2022/5/26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestJudgeRes implements Serializable {

    private static final long serialVersionUID = 888L;

    /**
     *  评测结果状态码
     */
    private Integer status;

    /**
     *  评测运行时间消耗 ms
     */
    private Long time;

    /**
     *  评测运行空间消耗 kb
     */
    private Long memory;

    /**
     * 输入
     */
    private String Input;

    /**
     * 期望输出
     */
    private String expectedOutput;

    /**
     * 运行标准输出
     */
    private String stdout;

    /**
     * 运行错误输出
     */
    private String stderr;

    /**
     * 原题的评测模式：default、spj、interactive
     */
    private String problemJudgeMode;
}

package top.hcode.hoj.judge.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/3 15:27
 * @Description: 单个测评结果实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class SandBoxRes {
    /**
     * 单个程序的状态码
     */
    private Integer status;

    /**
     * 原沙盒输出的状态字符
     */
    private String originalStatus;

    /**
     * 单个程序的退出码
     */
    private Integer exitCode;

    /**
     * 单个程序的运行所耗空间 kb
     */
    private Long memory;

    /**
     * 单个程序的运行所耗时间 ms
     */
    private Long time;

    /**
     * 单个程序的标准输出
     */
    private String stdout;

    /**
     * 单个程序的错误信息
     */
    private String stderr;

}
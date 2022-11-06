package top.hcode.hoj.pojo.vo;

import lombok.Data;

/**
 * @Author Himit_ZH
 * @Date 2022/11/6
 */
@Data
public class ProblemFullScreenListVO {

    private Long pid;

    private String problemId;

    private String title;

    private Integer status;

    private Integer score;
}

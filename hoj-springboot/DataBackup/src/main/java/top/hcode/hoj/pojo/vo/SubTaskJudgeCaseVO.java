package top.hcode.hoj.pojo.vo;

import lombok.Data;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;

import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2022/8/28
 */
@Data
public class SubTaskJudgeCaseVO {


    /**
     * 该subtask的分组编号
     */
    private Integer groupNum;

    /**
     * 该subtask最终结果所用时间ms
     */
    private Integer time;

    /**
     * 该subtask最终结果所用空间KB
     */
    private Integer memory;

    /**
     * 该subtask最终结果所得分数
     */
    private Integer score;

    /**
     * 该subtask最终结果状态码
     */
    private Integer status;

    /**
     * 该subtask最终AC测试点的个数
     */
    private Integer ac;

    /**
     * 该subtask的测试点总数
     */
    private Integer total;

    /**
     * 该subtask下各个测试点的具体结果
     */
    List<JudgeCase> subtaskDetailList;
}

package top.hcode.hoj.pojo.vo;

import lombok.Data;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;

import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2022/8/28
 */
@Data
public class JudgeCaseVo {

    /**
     * 当judgeCaseMode为default时
     */
    private List<JudgeCase> judgeCaseList;

    /**
     * 当judgeCaseMode为subtask_lowest,subtask_average时
     */
    private List<SubTaskJudgeCaseVo> subTaskJudgeCaseVoList;

    /**
     * default,subtask_lowest,subtask_average
     */
    private String judgeCaseMode;
}

package top.hcode.hoj.validator;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;

/**
 * @Author Himit_ZH
 * @Date 2022/11/4
 */
@Component
public class ProblemValidator {

    @Resource
    private CommonValidator commonValidator;

    public void validateProblem(Problem problem) throws StatusFailException {
        if (problem == null) {
            throw new StatusFailException("题目的配置项不能为空！");
        }

        if (StrUtil.isBlank(problem.getProblemId())) {
            throw new StatusFailException("题目的展示ID不能为空！");
        }

        defaultValidate(problem);

        if (problem.getTimeLimit() <= 0) {
            throw new StatusFailException("题目的时间限制不能小于等于0！");
        }
        if (problem.getMemoryLimit() <= 0) {
            throw new StatusFailException("题目的内存限制不能小于等于0！");
        }
        if (problem.getStackLimit() <= 0) {
            throw new StatusFailException("题目的栈限制不能小于等于0！");
        }
    }

    private void defaultValidate(Problem problem) throws StatusFailException {
        Constants.ProblemType type = Constants.ProblemType.getProblemType(problem.getType());
        if (type == null) {
            throw new StatusFailException("题目的类型必须为ACM(0), OI(1)！");
        }

        Constants.ProblemAuth auth = Constants.ProblemAuth.getProblemAuth(problem.getAuth());
        if (auth == null) {
            throw new StatusFailException("题目的权限必须为公开题目(1), 隐藏题目(2), 比赛题目(3)！");
        }

        Constants.JudgeMode judgeMode = Constants.JudgeMode.getJudgeMode(problem.getJudgeMode());
        if (judgeMode == null) {
            throw new StatusFailException("题目的判题模式必须为普通判题(default), 特殊判题(spj), 交互判题(interactive)！");
        }

        Constants.JudgeCaseMode judgeCaseMode = Constants.JudgeCaseMode.getJudgeCaseMode(problem.getJudgeCaseMode());
        if (judgeCaseMode == null) {
            throw new StatusFailException("题目的用例模式不正确！");
        }
        if (type == Constants.ProblemType.ACM) {
            if (judgeCaseMode != Constants.JudgeCaseMode.DEFAULT
                    && judgeCaseMode != Constants.JudgeCaseMode.ERGODIC_WITHOUT_ERROR) {
                throw new StatusFailException("题目的用例模式错误，ACM类型的题目只能为默认模式(default)、遇错止评(ergodic_without_error)！");
            }
        } else {
            if (judgeCaseMode != Constants.JudgeCaseMode.DEFAULT
                    && judgeCaseMode != Constants.JudgeCaseMode.SUBTASK_AVERAGE
                    && judgeCaseMode != Constants.JudgeCaseMode.SUBTASK_LOWEST) {
                throw new StatusFailException("题目的用例模式错误，OI类型的题目只能为默认模式(default)、" +
                        "子任务（最低得分）(subtask_lowest)、 子任务（平均得分）(subtask_average)！");
            }
        }
    }

    public void validateProblemUpdate(Problem problem) throws StatusFailException {
        if (problem == null) {
            throw new StatusFailException("题目的配置项不能为空！");
        }
        if (problem.getId() == null) {
            throw new StatusFailException("题目的id不能为空！");
        }
        validateProblem(problem);
    }

    public void validateGroupProblem(Problem problem) throws StatusFailException {

        if (problem == null) {
            throw new StatusFailException("题目的配置项不能为空！");
        }

        if (StrUtil.isBlank(problem.getProblemId())) {
            throw new StatusFailException("题目的展示ID不能为空！");
        }

        defaultValidate(problem);

        commonValidator.validateContent(problem.getProblemId(), "题目的展示ID", 50);
        if (problem.getTimeLimit() == null
                || problem.getTimeLimit() <= 0
                || problem.getTimeLimit() > 1000 * 30) {
            throw new StatusFailException("题目的时间限制范围请合理填写！(1~30000ms)");
        }

        if (problem.getMemoryLimit() == null
                || problem.getMemoryLimit() <= 0
                || problem.getMemoryLimit() > 1024) {
            throw new StatusFailException("题目的内存限制范围请合理填写！(1~1024mb)");
        }

        if (problem.getStackLimit() == null
                || problem.getStackLimit() <= 0
                || problem.getStackLimit() > 1024) {
            throw new StatusFailException("题目的栈限制范围请合理填写！(1~1024mb)");
        }

        commonValidator.validateContent(problem.getTitle(), "题目标题", 255);
        commonValidator.validateContentLength(problem.getDescription(), "题目描述", 65535);
        commonValidator.validateContentLength(problem.getInput(), "输入描述", 65535);
        commonValidator.validateContentLength(problem.getOutput(), "输出描述", 65535);
        commonValidator.validateContentLength(problem.getHint(), "题目提示", 65535);
    }

    public void validateGroupProblemUpdate(Problem problem) throws StatusFailException {

        if (problem == null) {
            throw new StatusFailException("题目的配置项不能为空！");
        }

        if (problem.getId() == null) {
            throw new StatusFailException("题目的id不能为空！");
        }
        validateGroupProblem(problem);
    }
}

package top.hcode.hoj.validator;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.entity.problem.Problem;

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
        if (StrUtil.isBlank(problem.getProblemId())) {
            throw new StatusFailException("题目的展示ID不能为空！");
        }
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

    public void validateProblemUpdate(Problem problem) throws StatusFailException {
        if (problem.getId() == null) {
            throw new StatusFailException("题目的id不能为空！");
        }
        validateProblem(problem);
    }

    public void validateGroupProblem(Problem problem) throws StatusFailException {
        if (StrUtil.isBlank(problem.getProblemId())) {
            throw new StatusFailException("题目的展示ID不能为空！");
        }
        commonValidator.validateContent(problem.getProblemId(), "题目的展示ID", 50);
        if (problem.getTimeLimit() == null
                || problem.getTimeLimit() <= 0
                || problem.getTimeLimit() > 1000 * 30) {
            throw new StatusFailException("题目的时间限制范围请合理填写！");
        }

        if (problem.getMemoryLimit() == null
                || problem.getMemoryLimit() <= 0
                || problem.getMemoryLimit() > 1024) {
            throw new StatusFailException("题目的内存限制范围请合理填写！");
        }

        if (problem.getStackLimit() == null
                || problem.getStackLimit() <= 0
                || problem.getStackLimit() > 1024) {
            throw new StatusFailException("题目的栈限制范围请合理填写！");
        }

        if (problem.getType() == null || (problem.getType() != 0 && problem.getType() != 1)) {
            throw new StatusFailException("题目的类型错误，请正确选择！");
        }

        commonValidator.validateContent(problem.getTitle(), "题目标题", 255);
        commonValidator.validateContentLength(problem.getDescription(), "题目描述", 65535);
        commonValidator.validateContentLength(problem.getInput(), "输入描述", 65535);
        commonValidator.validateContentLength(problem.getOutput(), "输出描述", 65535);
        commonValidator.validateContentLength(problem.getHint(), "题目提示", 65535);
    }

    public void validateGroupProblemUpdate(Problem problem) throws StatusFailException {
        if (problem.getId() == null) {
            throw new StatusFailException("题目的id不能为空！");
        }
        validateGroupProblem(problem);
    }
}

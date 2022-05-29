package top.hcode.hoj.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.hcode.hoj.annotation.HOJAccessEnum;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.exception.AccessException;
import top.hcode.hoj.pojo.dto.TestJudgeDto;
import top.hcode.hoj.pojo.dto.ToJudgeDto;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 11:20
 * @Description:
 */
@Component
public class JudgeValidator {

    @Autowired
    private AccessValidator accessValidator;

    private final static List<String> HOJ_LANGUAGE_LIST = Arrays.asList("C++", "C++ With O2",
            "C", "C With O2", "Python3", "Python2", "Java", "Golang", "C#", "PHP", "PyPy2", "PyPy3",
            "JavaScript Node", "JavaScript V8");

    public void validateSubmissionInfo(ToJudgeDto toJudgeDto) throws StatusFailException, AccessException {

        if (toJudgeDto.getGid() != null) { // 团队内的提交
            accessValidator.validateAccess(HOJAccessEnum.GROUP_JUDGE);
        } else if (toJudgeDto.getCid() != null && toJudgeDto.getCid() != 0) {
            accessValidator.validateAccess(HOJAccessEnum.CONTEST_JUDGE);
        } else {
            accessValidator.validateAccess(HOJAccessEnum.PUBLIC_JUDGE);
        }

        if (!toJudgeDto.getIsRemote() && !HOJ_LANGUAGE_LIST.contains(toJudgeDto.getLanguage())) {
            throw new StatusFailException("提交的代码的语言错误！请使用" + HOJ_LANGUAGE_LIST + "中之一的语言！");
        }

        if (toJudgeDto.getCode().length() < 50
                && !toJudgeDto.getLanguage().contains("Py")
                && !toJudgeDto.getLanguage().contains("PHP")
                && !toJudgeDto.getLanguage().contains("JavaScript")) {
            throw new StatusFailException("提交的代码是无效的，代码字符长度请不要低于50！");
        }

        if (toJudgeDto.getCode().length() > 65535) {
            throw new StatusFailException("提交的代码是无效的，代码字符长度请不要超过65535！");
        }
    }

    public void validateTestJudgeInfo(TestJudgeDto testJudgeDto) throws StatusFailException, AccessException {
        String type = testJudgeDto.getType();
        switch (type) {
            case "public":
                accessValidator.validateAccess(HOJAccessEnum.PUBLIC_JUDGE);
                break;
            case "contest":
                accessValidator.validateAccess(HOJAccessEnum.CONTEST_JUDGE);
                break;
            case "group":
                accessValidator.validateAccess(HOJAccessEnum.GROUP_JUDGE);
                break;
            default:
                throw new StatusFailException("请求参数type错误！");
        }

        if (StringUtils.isEmpty(testJudgeDto.getCode())) {
            throw new StatusFailException("在线调试的代码不可为空！");
        }

        if (!HOJ_LANGUAGE_LIST.contains(testJudgeDto.getLanguage())) {
            throw new StatusFailException("提交的代码的语言错误！请使用" + HOJ_LANGUAGE_LIST + "中之一的语言！");
        }

        if (StringUtils.isEmpty(testJudgeDto.getUserInput())) {
            throw new StatusFailException("在线调试的输入数据不可为空！");
        }

        if (testJudgeDto.getUserInput().length() > 1000) {
            throw new StatusFailException("在线调试的输入数据字符长度不能超过1000！");
        }

        if (testJudgeDto.getPid() == null) {
            throw new StatusFailException("在线调试所属题目的id不能为空！");
        }

        if (testJudgeDto.getCode().length() < 50
                && !testJudgeDto.getLanguage().contains("Py")
                && !testJudgeDto.getLanguage().contains("PHP")
                && !testJudgeDto.getLanguage().contains("JavaScript")) {
            throw new StatusFailException("提交的代码是无效的，代码字符长度请不要低于50！");
        }

        if (testJudgeDto.getCode().length() > 65535) {
            throw new StatusFailException("提交的代码是无效的，代码字符长度请不要超过65535！");
        }
    }
}
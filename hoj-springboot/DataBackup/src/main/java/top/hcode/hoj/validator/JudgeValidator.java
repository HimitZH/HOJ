package top.hcode.hoj.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.hcode.hoj.annotation.HOJAccessEnum;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.exception.AccessException;
import top.hcode.hoj.pojo.dto.TestJudgeDTO;
import top.hcode.hoj.pojo.dto.SubmitJudgeDTO;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
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

    private final static List<String> HOJ_LANGUAGE_LIST = Arrays.asList(
            "C++", "C++ With O2", "C++ 17", "C++ 17 With O2","C++ 20", "C++ 20 With O2",
            "C", "C With O2", "Python3", "Python2", "Java", "Golang", "C#", "PHP", "PyPy2", "PyPy3",
            "JavaScript Node", "JavaScript V8", "Ruby", "Rust");

    private static HashMap<String, String> MODE_MAP_LANGUAGE;

    @PostConstruct
    public void init() {
        MODE_MAP_LANGUAGE = new HashMap<>();
        MODE_MAP_LANGUAGE.put("text/x-c++src", "C++ With O2");
        MODE_MAP_LANGUAGE.put("text/x-csrc", "C With O2");
        MODE_MAP_LANGUAGE.put("text/x-java", "Java");
        MODE_MAP_LANGUAGE.put("text/x-go", "Golang");
        MODE_MAP_LANGUAGE.put("text/x-csharp", "C#");
        MODE_MAP_LANGUAGE.put("text/x-php", "PHP");
        MODE_MAP_LANGUAGE.put("text/x-ruby", "Ruby");
        MODE_MAP_LANGUAGE.put("text/x-rustsrc", "Rust");
    }

    public void validateSubmissionInfo(SubmitJudgeDTO submitJudgeDto) throws StatusFailException, AccessException {

        if (submitJudgeDto.getGid() != null) { // 团队内的提交
            accessValidator.validateAccess(HOJAccessEnum.GROUP_JUDGE);
        } else if (submitJudgeDto.getCid() != null && submitJudgeDto.getCid() != 0) {
            accessValidator.validateAccess(HOJAccessEnum.CONTEST_JUDGE);
        } else {
            accessValidator.validateAccess(HOJAccessEnum.PUBLIC_JUDGE);
        }

        if (!submitJudgeDto.getIsRemote() && !HOJ_LANGUAGE_LIST.contains(submitJudgeDto.getLanguage())) {
            throw new StatusFailException("提交的代码的语言错误！请使用" + HOJ_LANGUAGE_LIST + "中之一的语言！");
        }

        if (submitJudgeDto.getCode().length() < 50
                && !submitJudgeDto.getLanguage().contains("Py")
                && !submitJudgeDto.getLanguage().contains("PHP")
                && !submitJudgeDto.getLanguage().contains("Ruby")
                && !submitJudgeDto.getLanguage().contains("JavaScript")) {
            throw new StatusFailException("提交的代码是无效的，代码字符长度请不要低于50！");
        }

        if (submitJudgeDto.getCode().length() > 65535) {
            throw new StatusFailException("提交的代码是无效的，代码字符长度请不要超过65535！");
        }
    }

    public void validateTestJudgeInfo(TestJudgeDTO testJudgeDto) throws StatusFailException, AccessException {
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

        if (StringUtils.isEmpty(testJudgeDto.getLanguage())) {
            throw new StatusFailException("在线调试的编程语言不可为空！");
        }

        // Remote Judge的编程语言需要转换成HOJ的编程语言才能进行自测
        if (testJudgeDto.getIsRemoteJudge() != null && testJudgeDto.getIsRemoteJudge()) {
            String language = MODE_MAP_LANGUAGE.get(testJudgeDto.getMode());
            if (language != null) {
                testJudgeDto.setLanguage(language);
            } else {
                String dtoLanguage = testJudgeDto.getLanguage();
                if (dtoLanguage.contains("PyPy 3") || dtoLanguage.contains("PyPy3")) {
                    testJudgeDto.setLanguage("PyPy3");
                } else if (dtoLanguage.contains("PyPy")) {
                    testJudgeDto.setLanguage("PyPy2");
                } else if (dtoLanguage.contains("Python 3")) {
                    testJudgeDto.setLanguage("Python3");
                } else if (dtoLanguage.contains("Python")) {
                    testJudgeDto.setLanguage("Python2");
                }else if (dtoLanguage.contains("Node")){
                    testJudgeDto.setLanguage("JavaScript Node");
                }else if (dtoLanguage.contains("JavaScript")){
                    testJudgeDto.setLanguage("JavaScript V8");
                }
            }
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
                && !testJudgeDto.getLanguage().contains("Ruby")
                && !testJudgeDto.getLanguage().contains("JavaScript")) {
            throw new StatusFailException("提交的代码是无效的，代码字符长度请不要低于50！");
        }

        if (testJudgeDto.getCode().length() > 65535) {
            throw new StatusFailException("提交的代码是无效的，代码字符长度请不要超过65535！");
        }

    }
}
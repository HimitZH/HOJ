package top.hcode.hoj.judge.task;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.judge.AbstractJudge;
import top.hcode.hoj.judge.SandboxRun;
import top.hcode.hoj.judge.entity.JudgeDTO;
import top.hcode.hoj.judge.entity.JudgeGlobalDTO;
import top.hcode.hoj.judge.entity.SandBoxRes;
import top.hcode.hoj.util.Constants;

import java.util.Objects;

/**
 * @Author Himit_ZH
 * @Date 2022/5/26
 * @Description: 在线自测
 */
@Component
public class TestJudge extends AbstractJudge {
    @Override
    public JSONArray judgeCase(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) throws SystemError {
        Constants.RunConfig runConfig = judgeGlobalDTO.getRunConfig();
        // 调用安全沙箱使用测试点对程序进行测试
        return SandboxRun.testCase(
                parseRunCommand(runConfig.getCommand(), runConfig, null, null, null),
                runConfig.getEnvs(),
                judgeDTO.getTestCaseInputPath(),
                judgeDTO.getTestCaseInputContent(),
                judgeGlobalDTO.getTestTime(),
                judgeGlobalDTO.getMaxMemory(),
                judgeDTO.getMaxOutputSize(),
                judgeGlobalDTO.getMaxStack(),
                runConfig.getExeName(),
                judgeGlobalDTO.getUserFileId(),
                judgeGlobalDTO.getUserFileContent());
    }

    @Override
    public JSONObject checkResult(SandBoxRes sandBoxRes, JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) throws SystemError {
        JSONObject result = new JSONObject();
        StringBuilder errMsg = new StringBuilder();
        // 如果测试跑题无异常
        if (sandBoxRes.getStatus().equals(Constants.Judge.STATUS_ACCEPTED.getStatus())) {
            // 对结果的时间损耗和空间损耗与题目限制做比较，判断是否mle和tle
            if (sandBoxRes.getTime() > judgeGlobalDTO.getMaxTime()) {
                result.set("status", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
            } else if (sandBoxRes.getMemory() > judgeGlobalDTO.getMaxMemory() * 1024) {
                result.set("status", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
            } else {
                if (judgeDTO.getTestCaseOutputContent() != null) {
                    if (judgeGlobalDTO.getRemoveEOLBlank() != null && judgeGlobalDTO.getRemoveEOLBlank()) {
                        String stdOut = rtrim(sandBoxRes.getStdout());
                        String testCaseOutput = rtrim(judgeDTO.getTestCaseOutputContent());
                        if (Objects.equals(stdOut, testCaseOutput)) {
                            result.set("status", Constants.Judge.STATUS_ACCEPTED.getStatus());
                        } else {
                            result.set("status", Constants.Judge.STATUS_WRONG_ANSWER.getStatus());
                        }
                    } else {
                        if (Objects.equals(sandBoxRes.getStdout(), judgeDTO.getTestCaseOutputContent())) {
                            result.set("status", Constants.Judge.STATUS_ACCEPTED.getStatus());
                        } else {
                            result.set("status", Constants.Judge.STATUS_WRONG_ANSWER.getStatus());
                        }
                    }
                } else {
                    result.set("status", Constants.Judge.STATUS_ACCEPTED.getStatus());
                }
            }
        } else if (sandBoxRes.getStatus().equals(Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus())) {
            result.set("status", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        } else if (sandBoxRes.getExitCode() != 0) {
            result.set("status", Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());
            if (sandBoxRes.getExitCode() < 32) {
                errMsg.append(String.format("ExitCode: %s (%s)\n", sandBoxRes.getExitCode(), SandboxRun.signals.get(sandBoxRes.getExitCode())));
            } else {
                errMsg.append(String.format("ExitCode: %s\n", sandBoxRes.getExitCode()));
            }
        } else {
            result.set("status", sandBoxRes.getStatus());
            // 输出超限的特别提示
            if ("Output Limit Exceeded".equals(sandBoxRes.getOriginalStatus())){
                errMsg.append("The output character length of the program exceeds the limit");
            }
        }

        // b
        result.set("memory", sandBoxRes.getMemory());
        // ns->ms
        result.set("time", sandBoxRes.getTime());

        if (!StringUtils.isEmpty(sandBoxRes.getStderr())) {
            errMsg.append(sandBoxRes.getStderr());
        }

        // 记录该测试点的错误信息
        if (!StringUtils.isEmpty(errMsg.toString())) {
            String str = errMsg.toString();
            result.set("errMsg", str.substring(0, Math.min(1024 * 1024, str.length())));
        }

        // 记录该测试点的运行输出
        if (!StringUtils.isEmpty(sandBoxRes.getStdout()) && sandBoxRes.getStdout().length() > 1000) {
            result.set("output", sandBoxRes.getStdout().substring(0, 1000) + "...");
        } else {
            result.set("output", sandBoxRes.getStdout());
        }
        return result;
    }

    @Override
    public JSONObject checkMultipleResult(SandBoxRes userSandBoxRes, SandBoxRes interactiveSandBoxRes, JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) {
        return null;
    }
}

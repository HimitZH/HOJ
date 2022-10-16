package top.hcode.hoj.judge.task;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.judge.AbstractJudge;
import top.hcode.hoj.judge.SandboxRun;
import top.hcode.hoj.judge.entity.JudgeDTO;
import top.hcode.hoj.judge.entity.JudgeGlobalDTO;
import top.hcode.hoj.judge.entity.SandBoxRes;
import top.hcode.hoj.util.Constants;

import java.nio.charset.StandardCharsets;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/2 21:18
 * @Description: 普通评测
 */
@Component
public class DefaultJudge extends AbstractJudge {
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
    public JSONObject checkResult(SandBoxRes sandBoxRes, JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) {

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
                // 与原测试数据输出的md5进行对比 AC或者是WA
                JSONObject testcaseInfo = (JSONObject) ((JSONArray) judgeGlobalDTO.getTestCaseInfo().get("testCases")).get(judgeDTO.getTestCaseNum() - 1);
                result.set("status", compareOutput(sandBoxRes.getStdout(), judgeGlobalDTO.getRemoveEOLBlank(), testcaseInfo));
            }
        } else if (sandBoxRes.getStatus().equals(Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus())) {
            result.set("status", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        } else if (sandBoxRes.getExitCode() != 0) {
            result.set("status", Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());
            if (sandBoxRes.getExitCode() < 32) {
                errMsg.append(String.format("The program return exit status code: %s (%s)\n", sandBoxRes.getExitCode(), SandboxRun.signals.get(sandBoxRes.getExitCode())));
            } else {
                errMsg.append(String.format("The program return exit status code: %s\n", sandBoxRes.getExitCode()));
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

//        if (!StringUtils.isEmpty(sandBoxRes.getStdout())) {
//            // 对于当前测试样例，用户程序的输出对应生成的文件
//            FileWriter stdWriter = new FileWriter(judgeGlobalDTO.getRunDir() + "/" + judgeDTO.getTestCaseId() + ".out");
//            stdWriter.write(sandBoxRes.getStdout());
//        }

        // 记录该测试点的错误信息
        if (!StringUtils.isEmpty(errMsg.toString())) {
            String str = errMsg.toString();
            result.set("errMsg", str.substring(0, Math.min(1024 * 1024, str.length())));
        }

        if (judgeGlobalDTO.getNeedUserOutputFile()) { // 如果需要获取用户对于该题目的输出
            result.set("output", sandBoxRes.getStdout());
        }

        return result;
    }

    @Override
    public JSONObject checkMultipleResult(SandBoxRes userSandBoxRes, SandBoxRes interactiveSandBoxRes, JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) {
        return null;
    }

    // 根据评测结果与用户程序输出的字符串MD5进行对比
    private Integer compareOutput(String userOutput, Boolean isRemoveEOLBlank, JSONObject testcaseInfo) {

        // 如果当前题目选择默认去掉字符串末位空格
        if (isRemoveEOLBlank) {
            String userOutputMd5 = DigestUtils.md5DigestAsHex(rtrim(userOutput).getBytes(StandardCharsets.UTF_8));
            if (userOutputMd5.equals(testcaseInfo.getStr("EOFStrippedOutputMd5"))) {
                return Constants.Judge.STATUS_ACCEPTED.getStatus();
            }else{
                return Constants.Judge.STATUS_WRONG_ANSWER.getStatus();
            }
        } else { // 不选择默认去掉文末空格 与原数据进行对比
            String userOutputMd5 = DigestUtils.md5DigestAsHex(userOutput.getBytes(StandardCharsets.UTF_8));
            if (userOutputMd5.equals(testcaseInfo.getStr("outputMd5"))) {
                return Constants.Judge.STATUS_ACCEPTED.getStatus();
            }
        }
        // 如果不AC,进行PE判断，否则为WA
        String userOutputMd5 = DigestUtils.md5DigestAsHex(userOutput.replaceAll("\\s+", "").getBytes(StandardCharsets.UTF_8));
        if (userOutputMd5.equals(testcaseInfo.getStr("allStrippedOutputMd5"))) {
            return Constants.Judge.STATUS_PRESENTATION_ERROR.getStatus();
        } else {
            return Constants.Judge.STATUS_WRONG_ANSWER.getStatus();
        }
    }


}
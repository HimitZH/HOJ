package top.hcode.hoj.judge.task;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.NumberUtil;
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

import java.io.File;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/2 22:23
 * @Description: 特殊判题 支持testlib
 */

@Component
public class SpecialJudge extends AbstractJudge {
    @Override
    public JSONArray judgeCase(JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) throws SystemError {
        Constants.RunConfig runConfig = judgeGlobalDTO.getRunConfig();
        // 调用安全沙箱使用测试点对程序进行测试
        return SandboxRun.testCase(
                parseRunCommand(runConfig.getCommand(), runConfig, null, null, null),
                runConfig.getEnvs(),
                judgeDTO.getTestCaseInputPath(),
                null,
                judgeGlobalDTO.getTestTime(),
                judgeGlobalDTO.getMaxMemory(),
                judgeDTO.getMaxOutputSize(),
                judgeGlobalDTO.getMaxStack(),
                runConfig.getExeName(),
                judgeGlobalDTO.getUserFileId(),
                judgeGlobalDTO.getUserFileContent());
    }


    @Override
    public JSONObject checkMultipleResult(SandBoxRes userSandBoxRes, SandBoxRes interactiveSandBoxRes, JudgeDTO judgeDTO, JudgeGlobalDTO judgeGlobalDTO) {
        return null;
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

                // 对于当前测试样例，用户程序的输出对应生成的文件
                String userOutputFilePath = judgeGlobalDTO.getRunDir() + File.separator + judgeDTO.getTestCaseNum() + ".out";
                FileWriter stdWriter = new FileWriter(userOutputFilePath);
                stdWriter.write(sandBoxRes.getStdout());

                Constants.RunConfig spjRunConfig = judgeGlobalDTO.getSpjRunConfig();

                // 特判程序的路径
                String spjExeSrc = Constants.JudgeDir.SPJ_WORKPLACE_DIR.getContent() + File.separator
                        + judgeGlobalDTO.getProblemId() + File.separator + spjRunConfig.getExeName();

                String userOutputFileName = judgeGlobalDTO.getProblemId() + "_user_output";
                String testCaseInputFileName = judgeGlobalDTO.getProblemId() + "_input";
                String testCaseOutputFileName = judgeGlobalDTO.getProblemId() + "_output";
                // 进行spj程序运行比对
                JSONObject spjResult = spjRunAndCheckResult(userOutputFilePath,
                        userOutputFileName,
                        judgeDTO.getTestCaseInputPath(),
                        testCaseInputFileName,
                        judgeDTO.getTestCaseOutputPath(),
                        testCaseOutputFileName,
                        spjExeSrc,
                        spjRunConfig);

                // 删除用户输出文件
                FileUtil.del(userOutputFilePath);

                int code = spjResult.getInt("code");
                if (code == SPJ_WA) {
                    result.set("status", Constants.Judge.STATUS_WRONG_ANSWER.getStatus());
                } else if (code == SPJ_AC) {
                    result.set("status", Constants.Judge.STATUS_ACCEPTED.getStatus());
                } else if (code == SPJ_PE) {
                    result.set("status", Constants.Judge.STATUS_PRESENTATION_ERROR.getStatus());
                } else if (code == SPJ_PC) {
                    result.set("status", Constants.Judge.STATUS_PARTIAL_ACCEPTED.getStatus());
                    result.set("percentage", spjResult.getDouble("percentage"));
                } else {
                    result.set("status", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
                }

                String spjErrMsg = spjResult.getStr("errMsg");
                if (!StringUtils.isEmpty(spjErrMsg)) {
                    errMsg.append(spjErrMsg).append(" ");
                }

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

        // 记录该测试点的错误信息
        if (!StringUtils.isEmpty(errMsg.toString())) {
            String str = errMsg.toString();
            result.set("errMsg", str.substring(0, Math.min(1024 * 1024, str.length())));
        }

        return result;
    }


    private JSONObject spjRunAndCheckResult(String userOutputFilePath,
                                            String userOutputFileName,
                                            String testCaseInputFilePath,
                                            String testCaseInputFileName,
                                            String testCaseOutputFilePath,
                                            String testCaseOutputFileName,
                                            String spjExeSrc,
                                            Constants.RunConfig spjRunConfig) throws SystemError {

        // 调用安全沙箱运行spj程序
        JSONArray spjJudgeResultList = SandboxRun.spjCheckResult(
                parseRunCommand(spjRunConfig.getCommand(), spjRunConfig, testCaseInputFileName, userOutputFileName, testCaseOutputFileName),
                spjRunConfig.getEnvs(),
                userOutputFilePath,
                userOutputFileName,
                testCaseInputFilePath,
                testCaseInputFileName,
                testCaseOutputFilePath,
                testCaseOutputFileName,
                spjExeSrc,
                spjRunConfig.getExeName());

        JSONObject result = new JSONObject();

        JSONObject spjJudgeResult = (JSONObject) spjJudgeResultList.get(0);

        // 获取跑题用户输出或错误输出
        String spjErrOut = ((JSONObject) spjJudgeResult.get("files")).getStr("stderr");
        String spjStdOut = ((JSONObject) spjJudgeResult.get("files")).getStr("stdout");

        if (!StringUtils.isEmpty(spjErrOut)) {
            result.set("errMsg", spjErrOut);
        }

        // 退出状态码
        int exitCode = spjJudgeResult.getInt("exitStatus");
        // 如果测试跑题无异常
        if (spjJudgeResult.getInt("status").intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
            if (exitCode == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                result.set("code", SPJ_AC);
            } else {
                result.set("code", exitCode);
            }
        } else if (spjJudgeResult.getInt("status").intValue() == Constants.Judge.STATUS_RUNTIME_ERROR.getStatus()) {
            if (exitCode == SPJ_WA || exitCode == SPJ_ERROR || exitCode == SPJ_AC || exitCode == SPJ_PE) {
                result.set("code", exitCode);
            } else if (exitCode == SPJ_PC) {
                result.set("code", exitCode);
                if (NumberUtil.isNumber(spjStdOut)) {
                    double percentage = 0.0;
                    percentage = Double.parseDouble(spjStdOut) / 100;
                    if (percentage == 1) {
                        result.set("code", SPJ_AC);
                    } else {
                        result.set("percentage", percentage);
                    }
                }
            } else {
                if (!StringUtils.isEmpty(spjErrOut)) {
                    // 适配testlib.h 根据错误信息前缀判断
                    return parseTestLibErr(spjErrOut);
                } else {
                    result.set("code", SPJ_ERROR);
                }
            }
        } else {
            result.set("code", SPJ_ERROR);
        }

        return result;
    }
}
package top.hcode.hoj.judge;

import cn.hutool.core.io.file.FileWriter;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.JudgeUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * @Author: Himit_ZH
 * @Date: 2021/4/16 12:15
 * @Description: 判题流程解耦重构2.0，该类负责输入数据进入程序进行测评
 */
@Slf4j(topic = "hoj")
public class JudgeRun {

    private static final int cpuNum = Runtime.getRuntime().availableProcessors();


    private static final int SPJ_AC = 100;

    private static final int SPJ_PE = 101;

    private static final int SPJ_WA = 102;

    private static final int SPJ_ERROR = 103;


    private Long submitId;

    private Long problemId;

    private String testCasesDir;

    private JSONObject testCasesInfo;

    private Constants.RunConfig runConfig;

    private Constants.RunConfig spjRunConfig;


    public JudgeRun(Long submitId, Long problemId, String testCasesDir, JSONObject testCasesInfo, Constants.RunConfig runConfig, Constants.RunConfig spjRunConfig) {
        this.submitId = submitId;
        this.problemId = problemId;
        this.testCasesDir = testCasesDir;
        this.testCasesInfo = testCasesInfo;
        this.runConfig = runConfig;
        this.spjRunConfig = spjRunConfig;
    }

    public List<JSONObject> judgeAllCase(String userFileId,
                                         Long maxTime,
                                         Long maxMemory,
                                         Integer maxStack,
                                         Boolean getUserOutput,
                                         Boolean isRemoveEOFBlank,
                                         String spjExeName)
            throws SystemError, ExecutionException, InterruptedException {

        if (testCasesInfo == null) {
            throw new SystemError("The evaluation data of the problem does not exist", null, null);
        }

        // 使用线程池开启多线程测试每一测试输入数据
        ExecutorService threadPool = new ThreadPoolExecutor(
                cpuNum, // 核心线程数
                cpuNum * 2, // 最大线程数。最多几个线程并发。
                3,//当非核心线程无任务时，几秒后结束该线程
                TimeUnit.SECONDS,// 结束线程时间单位
                new LinkedBlockingDeque<>(200), //阻塞队列，限制等候线程数
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy());//队列满了，尝试去和最早的竞争，也不会抛出异常！

        List<FutureTask<JSONObject>> futureTasks = new ArrayList<>();
        JSONArray testcaseList = (JSONArray) testCasesInfo.get("testCases");
        Boolean isSpj = testCasesInfo.getBool("isSpj");

        // 默认给题目限制时间+200ms用来测评
        final Long testTime = maxTime + 200;

        // 用户输出的文件夹
        String runDir = Constants.JudgeDir.RUN_WORKPLACE_DIR.getContent() + File.separator + submitId;
        for (int index = 0; index < testcaseList.size(); index++) {

            JSONObject testcase = (JSONObject) testcaseList.get(index);

            // 将每个需要测试的线程任务加入任务列表中
            final int testCaseId = index;
            // 输入文件名
            final String inputFileName = testcase.getStr("inputName");
            // 输出文件名
            final String outputFileName = testcase.getStr("outputName");
            // 测试样例的路径
            final String testCaseInputPath = testCasesDir + File.separator + inputFileName;
            // 数据库表的测试样例id
            final Long caseId = testcase.getLong("caseId", null);
            // 该测试点的满分
            final Integer score = testcase.getInt("score", 0);

            final Long maxOutputSize = Math.max(testcase.getLong("outputSize", 0L) * 2, 16 * 1024 * 1024L);

            if (!isSpj) {
                futureTasks.add(new FutureTask<>(new Callable<JSONObject>() {
                    @Override
                    public JSONObject call() throws SystemError {
                        JSONObject result = judgeOneCase(userFileId,
                                testCaseId,
                                runDir,
                                testCaseInputPath,
                                testTime,// 默认给题目限制时间+200ms用来测评
                                maxTime,
                                maxMemory,
                                maxStack,
                                maxOutputSize,
                                getUserOutput,
                                isRemoveEOFBlank);
                        result.set("caseId", caseId);
                        result.set("score", score);
                        result.set("inputFileName", inputFileName);
                        result.set("outputFileName", outputFileName);
                        return result;
                    }
                }));
            } else {
                final String testCaseOutputPath = testCasesDir + File.separator + outputFileName;
                futureTasks.add(new FutureTask<>(new Callable<JSONObject>() {
                    @Override
                    public JSONObject call() throws SystemError {
                        JSONObject result = spjJudgeOneCase(userFileId,
                                testCaseId,
                                runDir,
                                testCaseInputPath,
                                testCaseOutputPath,
                                testTime,// 默认给题目限制时间+200ms用来测评
                                maxTime,
                                maxMemory,
                                maxOutputSize,
                                maxStack,
                                spjExeName);
                        result.set("caseId", caseId);
                        result.set("score", score);
                        result.set("inputFileName", inputFileName);
                        result.set("outputFileName", outputFileName);
                        return result;
                    }
                }));
            }
        }

        // 提交到线程池进行执行
        for (FutureTask<JSONObject> futureTask : futureTasks) {
            threadPool.submit(futureTask);
        }
        // 所有任务执行完成且等待队列中也无任务关闭线程池
        if (!threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        // 阻塞主线程, 直至线程池关闭
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("判题线程池异常--------------->{}", e.getMessage());
        }
        List<JSONObject> result = new LinkedList<>();

        // 获取线程返回结果
        for (int i = 0; i < futureTasks.size(); i++) {
            JSONObject tmp = futureTasks.get(i).get();
            result.add(tmp);
        }

        return result;
    }


    private JSONObject spjJudgeOneCase(String userFileId,
                                       Integer testCaseId,
                                       String runDir,
                                       String testCaseInputFilePath,
                                       String testCaseOutputFilePath,
                                       Long testTime,
                                       Long maxTime,
                                       Long maxMemory,
                                       Long maxOutputSize,
                                       Integer maxStack,
                                       String spjExeName) throws SystemError {

        // 调用安全沙箱使用测试点对程序进行测试
        JSONArray judgeResultList = SandboxRun.testCase(
                parseRunCommand(runConfig.getCommand(), runConfig, null, null, null),
                runConfig.getEnvs(),
                testCaseInputFilePath,
                testTime,
                maxOutputSize,
                maxStack,
                runConfig.getExeName(),
                userFileId);
        JSONObject result = new JSONObject();

        JSONObject judgeResult = (JSONObject) judgeResultList.get(0);

        // 获取跑题用户输出或错误输出
        String userStdOut = ((JSONObject) judgeResult.get("files")).getStr("stdout");
        String userErrOut = ((JSONObject) judgeResult.get("files")).getStr("stderr");

        StringBuilder errMsg = new StringBuilder();

        // 获取程序运行内存 b-->kb
        long memory = judgeResult.getLong("memory") / 1024;
        // 获取程序运行时间 ns->ms
        long time = judgeResult.getLong("time") / 1000000;
        // 异常退出的状态码
        int exitCode = judgeResult.getInt("exitStatus");
        // 如果测试跑题无异常
        if (judgeResult.getInt("status").intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {

            // 对结果的时间损耗和空间损耗与题目限制做比较，判断是否mle和tle
            if (time >= maxTime) {
                result.set("status", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
            } else if (memory >= maxMemory) {
                result.set("status", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
            } else {

                // 对于当前测试样例，用户程序的输出对应生成的文件
                String userOutputFilePath = runDir + File.separator + (testCaseId + 1) + ".out";
                FileWriter stdWriter = new FileWriter(userOutputFilePath);
                stdWriter.write(userStdOut);

                // 特判程序的路径
                String spjExeSrc = Constants.JudgeDir.SPJ_WORKPLACE_DIR.getContent() + File.separator + problemId + File.separator + spjExeName;

                String userOutputFileName = problemId + "_user_output";
                String testCaseInputFileName = problemId + "_input";
                String testCaseOutputFileName = problemId + "_output";
                // 进行spj程序运行比对
                JSONObject spjResult = spjCheckResult(userOutputFilePath,
                        userOutputFileName,
                        testCaseInputFilePath,
                        testCaseInputFileName,
                        testCaseOutputFilePath,
                        testCaseOutputFileName,
                        spjExeSrc,
                        spjExeName,
                        runDir);
                int code = spjResult.getInt("code");
                if (code == SPJ_WA) {
                    result.set("status", Constants.Judge.STATUS_WRONG_ANSWER.getStatus());
                } else if (code == SPJ_AC) {
                    result.set("status", Constants.Judge.STATUS_ACCEPTED.getStatus());
                } else if (code == SPJ_PE) {
                    result.set("status", Constants.Judge.STATUS_PRESENTATION_ERROR.getStatus());
                } else {
                    result.set("status", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
                }

                String spjErrMsg = spjResult.getStr("errMsg");
                if (!StringUtils.isEmpty(spjErrMsg)) {
                    errMsg.append(spjErrMsg).append(" ");
                }

            }
        } else if (judgeResult.getInt("status").intValue() == Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus()) {
            result.set("status", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        } else if (exitCode != 0) {
            result.set("status", Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());
            if (exitCode < 32) {
                errMsg.append(String.format("ExitCode: %s (%s)\n", exitCode, SandboxRun.signals.get(exitCode)));
            } else {
                errMsg.append(String.format("ExitCode: %s\n", exitCode));
            }
        } else {
            result.set("status", judgeResult.getInt("status"));
        }

        // b
        result.set("memory", memory);
        // ns->ms
        result.set("time", time);

        // 记录该测试点的错误信息
        if (!StringUtils.isEmpty(errMsg.toString())) {
            result.set("errMsg", errMsg.toString());
        }

        if (!StringUtils.isEmpty(userErrOut)) {
            // 同时记录错误信息
            errMsg.append(userErrOut);
            // 对于当前测试样例，用户的错误提示生成对应文件
            FileWriter errWriter = new FileWriter(runDir + File.separator + testCaseId + ".err");
            errWriter.write(userErrOut);
        }

        return result;

    }

    private JSONObject spjCheckResult(String userOutputFilePath,
                                      String userOutputFileName,
                                      String testCaseInputFilePath,
                                      String testCaseInputFileName,
                                      String testCaseOutputFilePath,
                                      String testCaseOutputFileName,
                                      String spjExeSrc,
                                      String spjExeName,
                                      String runDir) throws SystemError {

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
                spjExeName);

        JSONObject result = new JSONObject();

        JSONObject spjJudgeResult = (JSONObject) spjJudgeResultList.get(0);

        // 获取跑题用户输出或错误输出
        String spjErrOut = ((JSONObject) spjJudgeResult.get("files")).getStr("stderr");

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
            } else {
                if (!StringUtils.isEmpty(spjErrOut)) {
                    // 适配testlib.h 根据错误信息前缀判断
                    return parseTestlibErr(spjErrOut);
                } else {
                    result.set("code", SPJ_ERROR);
                }
            }
        } else {
            result.set("code", SPJ_ERROR);
        }

        return result;
    }


    private JSONObject parseTestlibErr(String err) {

        JSONObject res = new JSONObject(2);

        if (err.startsWith("ok ")) {
            res.set("code", SPJ_AC);
            res.set("errMsg", err.split("ok ")[1]);
        } else if (err.startsWith("wrong answer ")) {
            res.set("code", SPJ_WA);
            res.set("errMsg", err.split("wrong answer ")[1]);
        } else if (err.startsWith("wrong output format ")) {
            res.set("code", SPJ_PE);
            res.set("errMsg", err.split("wrong output format")[1]);
        } else if (err.startsWith("FAIL ")) {
            res.set("code", SPJ_ERROR);
            res.set("errMsg", err.split("FAIL ")[1]);
        } else {
            res.set("code", SPJ_ERROR);
            res.set("errMsg", err);
        }
        return res;
    }

    private JSONObject judgeOneCase(String userFileId,
                                    Integer testCaseId,
                                    String runDir,
                                    String testCasePath,
                                    Long testTime,
                                    Long maxTime,
                                    Long maxMemory,
                                    Integer maxStack,
                                    Long maxOutputSize,
                                    Boolean getUserOutput,
                                    Boolean isRemoveEOFBlank) throws SystemError {

        // 调用安全沙箱使用测试点对程序进行测试
        JSONArray judgeResultList = SandboxRun.testCase(parseRunCommand(runConfig.getCommand(), runConfig, null, null, null),
                runConfig.getEnvs(),
                testCasePath,
                testTime,
                maxOutputSize,
                maxStack,
                runConfig.getExeName(),
                userFileId);

        JSONObject result = new JSONObject();

        JSONObject judgeResult = (JSONObject) judgeResultList.get(0);

        // 获取跑题用户输出或错误输出
        String userStdOut = ((JSONObject) judgeResult.get("files")).getStr("stdout");
        String userErrOut = ((JSONObject) judgeResult.get("files")).getStr("stderr");

        StringBuffer errMsg = new StringBuffer();

        // 获取程序运行内存 b-->kb
        long memory = judgeResult.getLong("memory") / 1024;
        // 获取程序运行时间 ns->ms
        long time = judgeResult.getLong("time") / 1000000;
        // 异常退出的状态码
        int exitCode = judgeResult.getInt("exitStatus");
        // 如果测试跑题无异常
        if (judgeResult.getInt("status").intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {

            // 对结果的时间损耗和空间损耗与题目限制做比较，判断是否mle和tle
            if (time >= maxTime) {
                result.set("status", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
            } else if (memory >= maxMemory) {
                result.set("status", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
            } else {
                // 与原测试数据输出的md5进行对比 AC或者是WA
                result.set("status", compareOutput(testCaseId, userStdOut, isRemoveEOFBlank));
            }
        } else if (judgeResult.getInt("status").equals(Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus())) {
            result.set("status", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        } else if (exitCode != 0) {
            result.set("status", Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());
            if (exitCode < 32) {
                errMsg.append(String.format("ExitCode: %s (%s)\n", exitCode, SandboxRun.signals.get(exitCode)));
            } else {
                errMsg.append(String.format("ExitCode: %s\n", exitCode));
            }
        } else {
            result.set("status", judgeResult.getInt("status"));
        }

        // b
        result.set("memory", memory);
        // ns->ms
        result.set("time", time);

        if (!StringUtils.isEmpty(userStdOut)) {
            // 对于当前测试样例，用户程序的输出对应生成的文件
            FileWriter stdWriter = new FileWriter(runDir + "/" + testCaseId + ".out");
            stdWriter.write(userStdOut);
        }

        if (!StringUtils.isEmpty(userErrOut)) {
            // 对于当前测试样例，用户的错误提示生成对应文件
            FileWriter errWriter = new FileWriter(runDir + "/" + testCaseId + ".err");
            errWriter.write(userErrOut);
            // 同时记录错误信息
            errMsg.append(userErrOut);
        }

        // 记录该测试点的错误信息
        if (!StringUtils.isEmpty(errMsg.toString())) {
            result.set("errMsg", errMsg.toString());
        }

        if (getUserOutput) { // 如果需要获取用户对于该题目的输出
            result.set("output", userStdOut);
        }

        return result;

    }


    private static List<String> parseRunCommand(String command,
                                                Constants.RunConfig runConfig,
                                                String testCaseInputName,
                                                String userOutputName,
                                                String testCaseOutputName) {

        command = MessageFormat.format(command, Constants.JudgeDir.TMPFS_DIR.getContent(),
                runConfig.getExeName(), Constants.JudgeDir.TMPFS_DIR.getContent() + File.separator + testCaseInputName,
                Constants.JudgeDir.TMPFS_DIR.getContent() + File.separator + userOutputName,
                Constants.JudgeDir.TMPFS_DIR.getContent() + File.separator + testCaseOutputName);

        return JudgeUtils.translateCommandline(command);
    }


    public JSONObject getTestCasesInfo(int testCaseId) {
        return (JSONObject) ((JSONArray) testCasesInfo.get("testCases")).get(testCaseId);
    }

    // 根据评测结果与用户程序输出的字符串MD5进行对比
    private Integer compareOutput(int testCaseId, String userOutput, Boolean isRemoveEOFBlank) {

        // 如果当前题目选择默认去掉字符串末位空格
        if (isRemoveEOFBlank) {
            String userOutputMd5 = DigestUtils.md5DigestAsHex(rtrim(userOutput).getBytes());
            if (userOutputMd5.equals(getTestCasesInfo(testCaseId).getStr("EOFStrippedOutputMd5"))) {
                return Constants.Judge.STATUS_ACCEPTED.getStatus();
            }
        } else { // 不选择默认去掉文末空格 与原数据进行对比
            String userOutputMd5 = DigestUtils.md5DigestAsHex(userOutput.getBytes());
            if (userOutputMd5.equals(getTestCasesInfo(testCaseId).getStr("outputMd5"))) {
                return Constants.Judge.STATUS_ACCEPTED.getStatus();
            }
        }
        // 如果不AC,进行PE判断，否则为WA
        String userOutputMd5 = DigestUtils.md5DigestAsHex(userOutput.replaceAll("\\s+", "").getBytes());
        if (userOutputMd5.equals(getTestCasesInfo(testCaseId).getStr("allStrippedOutputMd5"))) {
            return Constants.Judge.STATUS_PRESENTATION_ERROR.getStatus();
        } else {
            return Constants.Judge.STATUS_WRONG_ANSWER.getStatus();
        }
    }

    // 去除行末尾空白符
    public static String rtrim(String value) {
        if (value == null) return null;
        StringBuilder sb = new StringBuilder();
        String[] strArr = value.split("\n");
        for (String str : strArr) {
            sb.append(str.replaceAll("\\s+$", "")).append("\n");
        }
        return sb.toString().replaceAll("\\s+$", "");
    }

    /*
        交互题 暂时不启用
     */
    private JSONObject interactOneCase(String userFileId,
                                       Integer testCaseId,
                                       String runDir,
                                       String testCaseInputFilePath,
                                       String testCaseOutputFilePath,
                                       Long maxTime,
                                       Long maxMemory,
                                       Integer maxStack,
                                       String userExeName,
                                       String spjExeName,
                                       Boolean getUserOutput) throws SystemError {

        // 对于当前测试样例，用户程序的输出对应生成的文件（正常就输出数据，错误就是输出错误信息）
        String realUserOutputFile = runDir + File.separator + testCaseId + ".out";

        // 特判程序的路径
        String spjExeSrc = Constants.JudgeDir.SPJ_WORKPLACE_DIR.getContent() + File.separator + problemId + File.separator + spjExeName;
        String testCaseInputFileName = problemId + "_input";
        String testCaseOutputFileName = problemId + "_output";

        JSONArray judgeResultList = SandboxRun.interactTestCase(
                parseRunCommand(runConfig.getCommand(), runConfig, null, null, null),
                runConfig.getEnvs(),
                userExeName,
                userFileId,
                testCaseInputFilePath,
                testCaseInputFileName,
                maxTime,
                maxStack,
                parseRunCommand(spjRunConfig.getCommand(), spjRunConfig, testCaseInputFileName, null, testCaseOutputFileName),
                spjRunConfig.getEnvs(),
                spjExeSrc,
                testCaseOutputFilePath,
                testCaseOutputFileName,
                spjExeName);

        JSONObject result = new JSONObject();

        // 用户程序输出写入文件
        FileWriter fileWriter = new FileWriter(realUserOutputFile);

        JSONObject userJudgeResult = (JSONObject) judgeResultList.get(0);
        JSONObject spjJudgeResult = (JSONObject) judgeResultList.get(1);

        // 特判程序输出或错误输出
        String spjStdOut = ((JSONObject) spjJudgeResult.get("files")).getStr("stdout");
        String spjErrOut = ((JSONObject) spjJudgeResult.get("files")).getStr("stderr");

        // 获取用户程序运行内存 b-->kb
        long memory = userJudgeResult.getLong("memory") / 1024;
        // 获取用户程序运行时间 ns->ms
        long time = userJudgeResult.getLong("time") / 1000000;
        // 用户程序的退出状态码
        int userExitCode = userJudgeResult.getInt("exitStatus");
        // 记录错误信息
        StringBuffer errMsg = new StringBuffer();
        // 如果用户提交的代码运行无误
        if (userJudgeResult.getInt("status").intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
            // 如果运行超过题目限制时间，直接TLE
            if (time >= maxTime) {
                result.set("status", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
            } else if (memory >= maxMemory) { // 如果运行超过题目限制空间，直接MLE
                result.set("status", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
            } else { // 校验特判程序的输出
                // 根据特判程序的退出状态码进行判断
                if (spjJudgeResult.getInt("exitStatus") == SPJ_AC) {
                    result.set("status", Constants.Judge.STATUS_ACCEPTED.getStatus());
                } else if (spjJudgeResult.getInt("exitStatus") == SPJ_WA) {
                    result.set("status", Constants.Judge.STATUS_WRONG_ANSWER.getStatus());
                } else {
                    throw new SystemError(spjErrOut, spjStdOut, spjErrOut);
                }
            }
        } else if (userJudgeResult.getInt("status").intValue() == Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus()) {
            result.set("status", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        } else if (userExitCode != 0) {
            if (userExitCode < 32) {
                result.set("status", Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());
                errMsg.append(String.format("ExitCode: %s (%s)\n", userExitCode, SandboxRun.signals.get(userExitCode)));
            } else {
                errMsg.append(String.format("ExitCode: %s\n", userExitCode));
            }
            String err = ((JSONObject) userJudgeResult.get("files")).getStr("stderr", null);
            errMsg.append(err);
            result.set("errMsg", errMsg.toString());
            fileWriter.write(err);
        } else {
            result.set("status", userJudgeResult.getInt("status"));
        }
        // kb
        result.set("memory", memory);
        // ms
        result.set("time", time);

        if (getUserOutput) { // 如果需要获取用户对于该题目的输出,只提供特判程序输出
            result.set("output", spjStdOut);
        }
        return result;

    }
}
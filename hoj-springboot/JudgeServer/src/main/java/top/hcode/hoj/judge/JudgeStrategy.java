package top.hcode.hoj.judge;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.CompileError;
import top.hcode.hoj.common.exception.SystemError;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.JudgeCase;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.pojo.entity.ProblemCase;
import top.hcode.hoj.service.impl.JudgeCaseServiceImpl;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.service.impl.ProblemCaseServiceImpl;
import top.hcode.hoj.util.Constants;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Component
public class JudgeStrategy {

    private static final int SPJ_WA = 101;

    private static final int SPJ_AC = 100;

    private static final int SPJ_ERROR = 102;

    private static final int cpuNum = Runtime.getRuntime().availableProcessors();

    private JSONObject testCasesInfo;

    private String testCasesDir;

    private Constants.CompileConfig compileConfig;

    private Constants.RunConfig runConfig;

    private Constants.RunConfig spjRunConfig;

    private String code;

    private String Language;

    private Long pid;

    private Problem problem;

    private Judge judge;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private ProblemCaseServiceImpl problemCaseService;

    @Autowired
    private JudgeCaseServiceImpl judgeCaseService;

    public void init(Problem problem, Judge judge) {
        this.testCasesDir = Constants.JudgeDir.TEST_CASE_DIR.getContent() + "/problem_" + problem.getId();
        this.runConfig = Constants.RunConfig.getRunnerByLanguage(judge.getLanguage());
        this.spjRunConfig = Constants.RunConfig.getRunnerByLanguage("SPJ-" + problem.getSpjLanguage());
        this.compileConfig = Constants.CompileConfig.getCompilerByLanguage(judge.getLanguage());
        this.code = judge.getCode();
        this.Language = judge.getLanguage();
        this.problem = problem;
        this.pid = problem.getId();
        this.judge = judge;
    }

    public HashMap<String, Object> judge(Problem problem, Judge judge) {

        // 初始化环境
        init(problem, judge);

        HashMap<String, Object> result = new HashMap<>();
        String userFileId = null;

        try {
            // 对用户源代码进行编译 获取tmpfs中的fileId
            userFileId = compile();
            // 加载测试数据
            this.testCasesInfo = loadTestCaseInfo(problem.getId(), problem.getCaseVersion(), !StringUtils.isEmpty(problem.getSpjCode()));
            // 检查是否为spj，同时是否有spj编译完成的文件，若不存在，就先编译生成该spj文件。
            Boolean hasSpjOrNotSpj = checkOrCompileSpj(problem);
            // 如果该题为spj，但是没有spj程序
            if (!hasSpjOrNotSpj) {
                result.put("code", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
                result.put("errMsg", "The special judge code does not exist.");
                result.put("time", 0L);
                result.put("memory", 0L);
                return result;
            }

            // 更新状态为评测数据中
            judge.setStatus(Constants.Judge.STATUS_JUDGING.getStatus());
            judgeService.saveOrUpdate(judge);
            // spj程序的名字
            String spjExeName = null;
            if (!StringUtils.isEmpty(problem.getSpjCode())) {
                spjExeName = Constants.RunConfig.getRunnerByLanguage("SPJ-" + problem.getSpjLanguage()).getExeName();
            }
            // 开始测试每个测试点
            List<JSONObject> allCaseResultList = judgeAllCase(userFileId, problem.getTimeLimit() * 1L,
                    problem.getMemoryLimit() * 1024L,
                    runConfig.getExeName(), false, problem.getIsRemoveEndBlank(), spjExeName);
            // 对全部测试点结果进行评判,获取最终评判结果
            HashMap<String, Object> judgeInfo = getJudgeInfo(allCaseResultList, problem, judge);

            return judgeInfo;
        } catch (SystemError systemError) {
            result.put("code", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
            result.put("errMsg", "Oops, something has gone wrong with the judgeServer. Please report this to administrator.");
            result.put("time", 0L);
            result.put("memory", 0L);
            log.error("题号为：" + problem.getId() + "的题目，提交id为" + judge.getSubmitId() + "在评测过程中发生系统性的异常------------------->{}",
                    systemError.getMessage() + "\n" + systemError.getStderr());
        } catch (CompileError compileError) {
            result.put("code", Constants.Judge.STATUS_COMPILE_ERROR.getStatus());
            result.put("errMsg", compileError.getStderr());
            result.put("time", 0L);
            result.put("memory", 0L);
        } catch (Exception e) {
            result.put("code", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
            result.put("errMsg", "Oops, something has gone wrong with the judgeServer. Please report this to administrator.");
            result.put("time", 0L);
            result.put("memory", 0L);
            log.error("题号为：" + problem.getId() + "的题目，提交id为" + judge.getSubmitId() + "在评测过程中发生系统性的异常-------------------->{}", e.getMessage());
        } finally {

            // 删除tmpfs内存中的用户代码可执行文件
            if (!StringUtils.isEmpty(userFileId)) {
                SandboxRun.delFile(userFileId);
            }
        }
        return result;
    }

    public static List<String> parseCompileCommand(String command, Constants.CompileConfig compileConfig) {

        command = MessageFormat.format(command, Constants.JudgeDir.TMPFS_DIR.getContent(),
                compileConfig.getSrcName(), compileConfig.getExeName());

        return Arrays.asList(command.split(" "));
    }

    public static List<String> parseRunCommand(String command, Constants.RunConfig runConfig, String testCaseTmpName) {

        command = MessageFormat.format(command, Constants.JudgeDir.TMPFS_DIR.getContent(),
                runConfig.getExeName(), Constants.JudgeDir.TMPFS_DIR.getContent() + File.separator + testCaseTmpName);

        return Arrays.asList(command.split(" "));
    }

    public String compile() throws SystemError, CompileError {

        if (compileConfig == null) {
            throw new CompileError("Unsupported language " + Language, null, null);
        }

        // 调用安全沙箱进行编译
        JSONArray result = SandboxRun.compile(compileConfig.getMaxCpuTime(),
                compileConfig.getMaxRealTime(),
                compileConfig.getMaxMemory(),
                128 * 1024 * 1024L,
                compileConfig.getSrcName(),
                compileConfig.getExeName(),
                parseCompileCommand(compileConfig.getCommand(), compileConfig),
                compileConfig.getEnvs(),
                code,
                true,
                false,
                null
        );
        JSONObject compileResult = (JSONObject) result.get(0);
        if (compileResult.getInt("status") != Constants.Judge.STATUS_ACCEPTED.getStatus()) {
            throw new CompileError("Compile Error.", ((JSONObject) compileResult.get("files")).getStr("stderr"), ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }

        String fileId = ((JSONObject) compileResult.get("fileIds")).getStr(compileConfig.getExeName());
        if (StringUtils.isEmpty(fileId)) {
            throw new CompileError("Executable file not found.", null, null);
        }
        return fileId;
    }

    public Boolean compileSpj(String code, Long pid, String spjLanguage) throws SystemError, CompileError {

        Constants.CompileConfig spjCompiler = Constants.CompileConfig.getCompilerByLanguage("SPJ-" + spjLanguage);
        if (spjCompiler == null) {
            throw new CompileError("Unsupported language " + spjLanguage, null, null);
        }

        Boolean copyOutExe = true;
        if (pid == null) { // 题目id为空，则不进行本地存储，可能为新建题目时测试特判程序是否正常的判断而已
            copyOutExe = false;
        }

        // 调用安全沙箱对特别判题程序进行编译
        JSONArray res = SandboxRun.compile(spjCompiler.getMaxCpuTime(),
                spjCompiler.getMaxRealTime(),
                spjCompiler.getMaxMemory(),
                128 * 1024 * 1024L,
                spjCompiler.getSrcName(),
                spjCompiler.getExeName(),
                parseCompileCommand(spjCompiler.getCommand(), spjCompiler),
                spjCompiler.getEnvs(),
                code,
                false,
                copyOutExe,
                Constants.JudgeDir.SPJ_WORKPLACE_DIR.getContent() + "/" + pid
        );
        JSONObject compileResult = (JSONObject) res.get(0);
        if (compileResult.getInt("status") != Constants.Judge.STATUS_ACCEPTED.getStatus()) {
            throw new SystemError("Special Judge Code Compile Error.", ((JSONObject) compileResult.get("files")).getStr("stderr"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }
        return true;
    }


    public Boolean checkOrCompileSpj(Problem problem) throws CompileError, SystemError {
        // 如果是需要特判的题目，则需要检测特批程序是否已经编译，否则进行编译
        if (!StringUtils.isEmpty(problem.getSpjCode())) {
            Constants.CompileConfig spjCompiler = Constants.CompileConfig.getCompilerByLanguage(problem.getSpjLanguage());
            // 如果不存在该已经编译好的特批程序，则需要再次进行编译
            if (!FileUtil.exist(Constants.JudgeDir.SPJ_WORKPLACE_DIR.getContent() + "/" + problem.getId() + "/" + spjCompiler.getExeName())) {
                return compileSpj(problem.getSpjCode(), problem.getId(), problem.getSpjLanguage());
            }
        }
        return true;
    }


    public JSONObject spjJudgeOneCase(String userFileId, Integer testCaseId, String runDir, String testCaseInputFilePath,
                                      String testCaseOutputFilePath, Long maxTime, Long maxMemory, Long maxOutputSize, String userExeName,
                                      String spjExeName, Boolean getUserOutput) throws SystemError {

        // 对于当前测试样例，用户程序的输出对应生成的文件（正常就输出数据，错误就是输出错误信息）
        String realUserOutputFile = runDir + "/" + testCaseId + ".out";

        // 特判程序的路径
        String spjExeSrc = Constants.JudgeDir.SPJ_WORKPLACE_DIR.getContent() + "/" + pid + "/" + spjExeName;

        JSONArray judgeResultList = SandboxRun.spjTestCase(parseRunCommand(runConfig.getCommand(), runConfig, null),
                runConfig.getEnvs(),
                userExeName,
                userFileId,
                testCaseInputFilePath,
                maxTime,
                maxOutputSize,
                parseRunCommand(spjRunConfig.getCommand(), spjRunConfig, "tmp"),
                spjRunConfig.getEnvs(),
                spjExeSrc,
                testCaseOutputFilePath,
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
        if (userJudgeResult.getInt("status") == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
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
        } else if (userJudgeResult.getInt("status") == Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus()) {
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

    public JSONObject judgeOneCase(String userFileId, Integer testCaseId, String runDir, String testCasePath, Long maxTime,
                                   Long maxMemory, Long maxOutputSize, Boolean getUserOutput, Boolean isRemoveEOFBlank) throws SystemError {

        // 调用安全沙箱使用测试点对程序进行测试
        JSONArray judgeResultList = SandboxRun.testCase(parseRunCommand(runConfig.getCommand(), runConfig, null),
                runConfig.getEnvs(),
                testCasePath,
                maxTime,
                maxOutputSize,
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
        if (judgeResult.getInt("status") == Constants.Judge.STATUS_ACCEPTED.getStatus()) {

            // 对结果的时间损耗和空间损耗与题目限制做比较，判断是否mle和tle
            if (time >= maxTime) {
                result.set("status", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
            } else if (memory >= maxMemory) {
                result.set("status", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
            } else {
                // 与原测试数据输出的md5进行对比 AC或者是WA
                result.set("status", compareOutput(testCaseId, userStdOut, isRemoveEOFBlank));
            }
        } else if (judgeResult.getInt("status") == Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus()) {
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


    public List<JSONObject> judgeAllCase(String userFileId, Long maxTime, Long maxMemory, @Nullable String userExeName,
                                         Boolean getUserOutput, Boolean isRemoveEOFBlank, String spjExeName) throws SystemError, ExecutionException, InterruptedException {

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
        // 默认给1.5倍题目限制时间用来测评
        Double time = maxTime * 1.5;
        final Long testTime = time.longValue();

        // 用户输出的文件夹
        String runDir = Constants.JudgeDir.RUN_WORKPLACE_DIR.getContent() + "/" + judge.getSubmitId();
        for (int index = 0; index < testcaseList.size(); index++) {
            // 将每个需要测试的线程任务加入任务列表中
            final int testCaseId = index;
            // 测试样例的路径
            final String testCaseInputPath = testCasesDir + "/" + ((JSONObject) testcaseList.get(index)).getStr("inputName");
            // 数据库表的测试样例id
            final Long caseId = ((JSONObject) testcaseList.get(index)).getLong("caseId");
            // 该测试点的满分
            final Integer score = ((JSONObject) testcaseList.get(index)).getInt("score", 0);

            final Long maxOutputSize = Math.max(((JSONObject) testcaseList.get(index)).getLong("outputSize", 0L) * 2, 16 * 1024 * 1024L);
            if (!isSpj) {
                futureTasks.add(new FutureTask<>(new Callable<JSONObject>() {
                    @Override
                    public JSONObject call() throws SystemError {
                        JSONObject result = judgeOneCase(userFileId,
                                testCaseId,
                                runDir,
                                testCaseInputPath,
                                testTime,// 默认给1.5倍题目限制时间用来测评
                                maxMemory,
                                maxOutputSize,
                                getUserOutput,
                                isRemoveEOFBlank);
                        result.set("caseId", caseId);
                        result.set("score", score);
                        return result;
                    }
                }));
            } else {
                final String testCaseOutputPath = testCasesDir + "/" + ((JSONObject) testcaseList.get(index)).getStr("outputName");
                futureTasks.add(new FutureTask<>(new Callable<JSONObject>() {
                    @Override
                    public JSONObject call() throws SystemError {
                        JSONObject result = spjJudgeOneCase(userFileId,
                                testCaseId,
                                runDir,
                                testCaseInputPath,
                                testCaseOutputPath,
                                testTime,// 默认给1.5倍题目限制时间用来测评
                                maxMemory,
                                maxOutputSize,
                                userExeName,
                                spjExeName,
                                getUserOutput);
                        result.set("caseId", caseId);
                        result.set("score", score);
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

    // 根据评测结果与用户程序输出的字符串MD5进行对比
    public Integer compareOutput(int testCaseId, String userOutput, Boolean isRemoveEOFBlank) {


        JSONObject result = new JSONObject();
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

    // 获取判题的运行时间，运行空间，OI得分
    public HashMap<String, Object> computeResultInfo(List<JSONObject> testCaseResultList, Boolean isACM, Integer errorCaseNum, Integer totalScore) {
        HashMap<String, Object> result = new HashMap<>();
        // 用时和内存占用保存为多个测试点中最长的
        testCaseResultList.stream().max(Comparator.comparing(t -> t.getLong("time")))
                .ifPresent(t -> result.put("time", t.getLong("time")));

        testCaseResultList.stream().max(Comparator.comparing(t -> t.getLong("memory")))
                .ifPresent(t -> result.put("memory", t.getLong("memory")));

        // OI题目计算得分
        if (!isACM) {
            int score = 0;
            // 全对的直接用总分
            if (errorCaseNum == 0) {
                result.put("score", totalScore);
            } else {
                for (JSONObject testcaseResult : testCaseResultList) {
                    score += testcaseResult.getInt("score", 0);
                }
                result.put("socre", score);
            }
        }
        return result;
    }

    // 进行最终测试结果的判断（除编译失败外的评测状态码和时间，空间,OI题目的得分）
    public HashMap<String, Object> getJudgeInfo(List<JSONObject> testCaseResultList, Problem problem, Judge judge) {

        boolean isACM = problem.getType() == Constants.Contest.TYPE_ACM.getCode();

        List<JSONObject> errorTestCaseList = new LinkedList<>();

        List<JudgeCase> allCaseResList = new LinkedList<>();

        // 记录所有测试点的结果
        testCaseResultList.stream().forEach(jsonObject -> {
            Integer time = jsonObject.getLong("time").intValue();
            Integer memory = jsonObject.getLong("memory").intValue();
            Integer status = jsonObject.getInt("status");
            Long caseId = jsonObject.getLong("caseId");
            JudgeCase judgeCase = new JudgeCase();
            judgeCase.setTime(time).setMemory(memory)
                    .setStatus(status)
                    .setPid(problem.getId())
                    .setUid(judge.getUid())
                    .setCaseId(caseId)
                    .setSubmitId(judge.getSubmitId());
            // 过滤出结果不是AC的测试点结果 同时如果是IO题目 则得分为0
            if (jsonObject.getInt("status").intValue() != Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                errorTestCaseList.add(jsonObject);
                if (!isACM) {
                    judgeCase.setScore(0);
                }
            } else { // 如果是AC同时为IO题目测试点，则获得该点的得分
                if (!isACM) {
                    judgeCase.setScore(jsonObject.getInt("score").intValue());
                }
            }
            allCaseResList.add(judgeCase);
        });

        // 更新到数据库
        boolean addCaseRes = judgeCaseService.saveBatch(allCaseResList);
        if (!addCaseRes) {
            log.error("题号为：" + problem.getId() + "，提交id为：" + judge.getSubmitId() + "的各个测试数据点的结果更新到数据库操作失败");
        }

        // 获取判题的运行时间，运行空间，OI得分
        HashMap<String, Object> result = computeResultInfo(testCaseResultList, isACM, errorTestCaseList.size(), problem.getIoScore());

        // 如果该题为ACM类型的题目，多个测试点全部正确则AC，否则取第一个错误的测试点的状态
        // 如果该题为OI类型的题目, 若多个测试点全部正确则AC，若全部错误则取第一个错误测试点状态，否则为部分正确
        if (errorTestCaseList.size() == 0) { // 全部测试点正确，则为AC
            result.put("code", Constants.Judge.STATUS_ACCEPTED.getStatus());
        } else if (isACM || errorTestCaseList.size() == testCaseResultList.size()) {
            result.put("code", errorTestCaseList.get(0).getInt("status"));
            result.put("errMsg", errorTestCaseList.get(0).getStr("errMsg", ""));
        } else {
            result.put("code", Constants.Judge.STATUS_PARTIAL_ACCEPTED.getStatus());
        }
        return result;
    }


    public JSONObject getTestCasesInfo(int testCaseId) {
        return (JSONObject) ((JSONArray) testCasesInfo.get("testCases")).get(testCaseId);
    }


    // 初始化测试数据，写成json文件
    public JSONObject initTestCase(List<HashMap<String, Object>> testCases, Long problemId, String version, Boolean isSpj) throws SystemError, UnsupportedEncodingException {

        if (testCases == null || testCases.size() == 0) {
            throw new SystemError("题号为：" + problemId + "的评测数据为空！", null, "The test cases does not exist.");
        }

        JSONObject result = new JSONObject();
        result.set("isSpj", isSpj);
        result.set("version", version);
        result.set("testCasesSize", testCases.size());
        result.set("testCases", new JSONArray());

        String testCasesDir = Constants.JudgeDir.TEST_CASE_DIR.getContent() + "/problem_" + problemId;

        // 无论有没有测试数据，一旦执行该函数，一律清空，重新生成该题目对应的测试数据文件

        FileUtil.del(testCasesDir);
        for (int index = 0; index < testCases.size(); index++) {
            JSONObject jsonObject = new JSONObject();
            String inputName = (index + 1) + ".in";
            jsonObject.set("caseId", (long) testCases.get(index).get("caseId"));
            jsonObject.set("score", testCases.get(index).getOrDefault("score", null));
            jsonObject.set("inputName", inputName);
            // 生成对应文件
            FileWriter infileWriter = new FileWriter(testCasesDir + "/" + inputName, CharsetUtil.UTF_8);
            // 将该测试数据的输入写入到文件
            infileWriter.write((String) testCases.get(index).get("input"));

            String outputName = (index + 1) + ".out";
            jsonObject.set("outputName", outputName);
            // 生成对应文件
            String outputData = (String) testCases.get(index).get("output");
            FileWriter outFile = new FileWriter(testCasesDir + "/" + outputName, CharsetUtil.UTF_8);
            outFile.write(outputData);

            // spj是根据特判程序输出判断结果，所以无需初始化测试数据
            if (!isSpj) {
                // 原数据MD5
                jsonObject.set("outputMd5", DigestUtils.md5DigestAsHex(outputData.getBytes()));
                // 原数据大小
                jsonObject.set("outputSize", outputData.getBytes("utf-8").length);
                // 去掉全部空格的MD5，用来判断pe
                jsonObject.set("allStrippedOutputMd5", DigestUtils.md5DigestAsHex(outputData.replaceAll("\\s+", "").getBytes()));
                // 默认去掉文末空格的MD5
                jsonObject.set("EOFStrippedOutputMd5", DigestUtils.md5DigestAsHex(rtrim(outputData).getBytes()));
            }

            ((JSONArray) result.get("testCases")).put(index, jsonObject);
        }

        FileWriter infoFile = new FileWriter(testCasesDir + "/info", CharsetUtil.UTF_8);
        // 写入记录文件
        infoFile.write(JSONUtil.toJsonStr(result));
        return result;
    }

    // 获取指定题目的info数据
    public JSONObject loadTestCaseInfo(Long problemId, String version, Boolean isSpj) throws SystemError, UnsupportedEncodingException {
        String testCasesDir = Constants.JudgeDir.TEST_CASE_DIR.getContent() + "/problem_" + problemId;
        if (FileUtil.exist(testCasesDir + "/info")) {
            FileReader fileReader = new FileReader(testCasesDir + "/info", CharsetUtil.UTF_8);
            String infoStr = fileReader.readString();
            JSONObject testcaseInfo = JSONUtil.parseObj(infoStr);
            // 测试样例被改动需要重新生成
            if (!testcaseInfo.getStr("version", null).equals(version)) {
                return tryInitTestCaseInfo(problemId, version, isSpj);
            }
            return testcaseInfo;
        } else {
            return tryInitTestCaseInfo(problemId, version, isSpj);
        }
    }

    // 若没有测试数据，则尝试从数据库获取并且初始化到本地
    public JSONObject tryInitTestCaseInfo(Long problemId, String version, Boolean isSpj) throws SystemError, UnsupportedEncodingException {
        QueryWrapper<ProblemCase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", problemId);
        List<ProblemCase> problemCases = problemCaseService.list(queryWrapper);
        List<HashMap<String, Object>> testCases = new LinkedList<>();
        for (ProblemCase problemCase : problemCases) {
            HashMap<String, Object> tmp = new HashMap<>();
            tmp.put("input", problemCase.getInput());
            tmp.put("output", problemCase.getOutput());
            tmp.put("caseId", problemCase.getId());
            tmp.put("score", problemCase.getScore());
            testCases.add(tmp);
        }

        return initTestCase(testCases, problemId, version, isSpj);
    }

    public static String rtrim(String value) {
        if (value == null) return null;
        return value.replaceAll("\\s+$", "");
    }

}

package top.hcode.hoj.judger;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.NumberUtil;
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
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.pojo.entity.ProblemCase;
import top.hcode.hoj.service.impl.JudgeCaseServiceImpl;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.service.impl.ProblemCaseServiceImpl;
import top.hcode.hoj.util.Constants;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JudgeStrategy {

    private static final int SPJ_WA = 1;

    private static final int SPJ_AC = 0;

    private static final int SPJ_ERROR = -1;

    private static final int cpuNum = Runtime.getRuntime().availableProcessors();

    private JSONObject testCasesInfo;

    private String testCasesDir;

    private Constants.CompileConfig compileConfig;

    private Constants.RunConfig runConfig;

    private String code;

    private String Language;

    private Long pid;

    private Problem problem;

    private Judge judge;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private ProblemCaseServiceImpl problemCaseService;


    public void init(Problem problem, Judge judge) {
        this.testCasesDir = Constants.JudgeDir.TEST_CASE_DIR.getContent() + "/problem_" + problem.getId();
        this.runConfig = Constants.RunConfig.getRunnerByLanguage(judge.getLanguage());
        this.compileConfig = Constants.CompileConfig.getCompilerByLanguage(judge.getLanguage());
        this.code = judge.getCode();
        this.Language = judge.getLanguage();
        this.problem = problem;
        this.judge = judge;
    }

    public HashMap<String, Object> judge(Problem problem, Judge judge) {

        // 初始化环境
        init(problem, judge);

        HashMap<String, Object> result = new HashMap<>();
        String userFileId = null;
        try {
            // 加载测试数据
            this.testCasesInfo = loadTestCaseInfo(problem.getId(), !StringUtils.isEmpty(problem.getSpjCode()));
            // 对用户源代码进行编译 获取tmpfs中的fileId
            userFileId = compile();
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

            // 开始测试每个测试点
            List<JSONObject> allCaseResultList = judgeAllCase(userFileId, problem.getTimeLimit() * 1L, problem.getMemoryLimit() * 1024L,
                    runConfig.getExeName(), false);

            // 对全部测试点结果进行评判,获取最终评判结果
            HashMap<String, Object> judgeInfo = getJudgeInfo(allCaseResultList, problem.getType() == Constants.Contest.TYPE_ACM.getCode());

            return judgeInfo;
        } catch (SystemError systemError) {
            result.put("code", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
            result.put("errMsg", "stdout:" + systemError.getStdout() + "\nerror:" + systemError.getStderr());
            result.put("time", 0L);
            result.put("memory", 0L);
            log.error("题号为：" + problem.getId() + "的题目，提交id为" + judge.getSubmitId() + "在评测过程中发生系统性的异常------------------->{}",
                    systemError.getMessage());
        } catch (CompileError compileError) {
            result.put("code", Constants.Judge.STATUS_COMPILE_ERROR.getStatus());
            result.put("errMsg", compileError.getStderr());
            result.put("time", 0L);
            result.put("memory", 0L);
        } catch (Exception e) {
            result.put("code", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
            result.put("errMsg", e.getMessage());
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

    public static List<String> parseRunCommand(String command, Constants.RunConfig runConfig, String testCaseOutputFilePath) {

        command = MessageFormat.format(command, Constants.JudgeDir.TMPFS_DIR.getContent(),
                runConfig.getExeName(), testCaseOutputFilePath);

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
        if (SandboxRun.RESULT_MAP_STATUS.get(compileResult.get("status")).intValue() != Constants.Judge.STATUS_ACCEPTED.getStatus()) {
            throw new CompileError("Compile Error.", ((JSONObject) compileResult.get("files")).getStr("stderr"), ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }

        String fileId = ((JSONObject) compileResult.get("fileIds")).getStr(compileConfig.getExeName());
        if (StringUtils.isEmpty(fileId)) {
            throw new CompileError("Executable file not found.", null, null);
        }
        return fileId;
    }

    public Boolean compileSpj(String code, Long pid, String spjLanguage) throws SystemError, CompileError {

        Constants.CompileConfig spjCompiler = Constants.CompileConfig.getCompilerByLanguage(spjLanguage);
        if (spjCompiler == null) {
            throw new CompileError("Unsupported language " + spjLanguage, null, null);
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
                true,
                Constants.JudgeDir.SPJ_WORKPLACE_DIR.getContent() + "/" + pid
        );
        JSONObject compileResult = (JSONObject) res.get(0);
        if (SandboxRun.RESULT_MAP_STATUS.get(compileResult.get("status")).intValue() != Constants.Judge.STATUS_ACCEPTED.getStatus()) {
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
                                      String testCaseOutputFilePath, Long maxTime, Long maxMemory, String userExeName,
                                      String spjExeName, Boolean getUserOutput) throws SystemError {

        // 对于当前测试样例，用户程序的输出对应生成的文件（正常就输出数据，错误就是输出错误信息）
        String realUserOutputFile = runDir + "/" + testCaseId + ".out";

        // 特判程序的路径
        String spjExeSrc = Constants.JudgeDir.SPJ_WORKPLACE_DIR.getContent() + "/" + pid + "/" + spjExeName;

        JSONArray judgeResultList = SandboxRun.spjTestCase(parseRunCommand(runConfig.getCommand(), runConfig, testCaseOutputFilePath),
                runConfig.getEnvs(),
                userExeName,
                userFileId,
                testCaseInputFilePath,
                spjExeSrc,
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

        // 如果用户提交的代码运行无误
        if (SandboxRun.RESULT_MAP_STATUS.get(userJudgeResult.get("status")).intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {

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
        } else { // 其它情况根据用户程序的映射关系进行错误判断
            result.set("status", SandboxRun.RESULT_MAP_STATUS.get(userJudgeResult.get("status")));
            String err = ((JSONObject) userJudgeResult.get("files")).getStr("stderr", null);
            result.set("errMsg", err);
            fileWriter.write(err);
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
                                   Long maxMemory, Boolean getUserOutput) throws SystemError {

        // 调用安全沙箱使用测试点对程序进行测试
        JSONArray judgeResultList = SandboxRun.testCase(parseRunCommand(runConfig.getCommand(), runConfig, null),
                runConfig.getEnvs(),
                testCasePath,
                runConfig.getExeName(),
                userFileId);

        JSONObject result = new JSONObject();

        JSONObject judgeResult = (JSONObject) judgeResultList.get(0);

        // 获取跑题用户输出或错误输出
        String userStdOut = ((JSONObject) judgeResult.get("files")).getStr("stdout");
        String userErrOut = ((JSONObject) judgeResult.get("files")).getStr("stderr");

        // 获取程序运行内存 b-->kb
        long memory = judgeResult.getLong("memory") / 1024;
        // 获取程序运行时间 ns->ms
        long time = judgeResult.getLong("time") / 1000000;
        // 如果测试跑题无异常
        if (SandboxRun.RESULT_MAP_STATUS.get(judgeResult.get("status")).intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {

            // 对结果的时间损耗和空间损耗与题目限制做比较，判断是否mle和tle
            if (time >= maxTime) {
                result.set("status", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
            } else if (memory >= maxMemory) {
                result.set("status", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
            } else {
                // 与原测试数据输出的md5进行对比 AC或者是WA
                result.set("status", compareOutput(testCaseId, userStdOut));
            }
        } else { // 其它情况根据映射关系进行错误判断
            result.set("status", SandboxRun.RESULT_MAP_STATUS.get(judgeResult.get("status")));
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
            result.set("errMsg", userErrOut);
        }

        if (getUserOutput) { // 如果需要获取用户对于该题目的输出
            result.set("output", userStdOut);
        }

        return result;

    }


    public List<JSONObject> judgeAllCase(String userFileId, Long maxTime, Long maxMemory, @Nullable String userExeName,
                                         Boolean getUserOutput) throws SystemError, ExecutionException, InterruptedException {

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
        // 用户输出的文件夹
        String runDir = Constants.JudgeDir.RUN_WORKPLACE_DIR.getContent() + "/" + judge.getSubmitId();
        for (int index = 0; index < testcaseList.size(); index++) {
            // 将每个需要测试的线程任务加入任务列表中
            final int testCaseId = index;
            // 测试样例的路径
            final String testCaseInputPath = testCasesDir + "/" + ((JSONObject) testcaseList.get(index)).getStr("inputName");
            if (!isSpj) {
                futureTasks.add(new FutureTask<>(new Callable<JSONObject>() {
                    @Override
                    public JSONObject call() throws SystemError {
                        return judgeOneCase(userFileId, testCaseId, runDir, testCaseInputPath, maxTime, maxMemory, getUserOutput);
                    }
                }));
            } else {
                final String testCaseOutputPath = testCasesDir + "/" + ((JSONObject) testcaseList.get(index)).getStr("outputName");
                futureTasks.add(new FutureTask<>(new Callable<JSONObject>() {
                    @Override
                    public JSONObject call() throws SystemError {
                        return spjJudgeOneCase(userFileId, testCaseId, runDir, testCaseInputPath, testCaseOutputPath, maxTime,
                                maxMemory, userExeName, runConfig.getExeName(), getUserOutput);
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
    public Integer compareOutput(int testCaseId, String userOutput) {

        String outputMd5 = DigestUtils.md5DigestAsHex(rtrim(userOutput).getBytes());
        JSONObject result = new JSONObject();
        // 如果去掉字符串末位空格的输出完全一样，则该测试点通过
        if (outputMd5.equals(getTestCasesInfo(testCaseId).getStr("strippedOutputMd5"))) {
            return Constants.Judge.STATUS_ACCEPTED.getStatus();
        } else {
            return Constants.Judge.STATUS_WRONG_ANSWER.getStatus();
        }

    }

    // 获取判题的运行时间，运行空间，OI得分
    public HashMap<String, Object> computeResultInfo(List<JSONObject> testCaseResultList, Boolean isACM, Integer errorCaseNum) {
        HashMap<String, Object> result = new HashMap<>();
        // 用时和内存占用保存为多个测试点中最长的
        testCaseResultList.stream().max(Comparator.comparing(t -> t.getLong("time")))
                .ifPresent(t -> result.put("time", t.getLong("time")));

        testCaseResultList.stream().max(Comparator.comparing(t -> t.getLong("memory")))
                .ifPresent(t -> result.put("memory", t.getLong("memory")));

        // OI题目计算得分
        if (!isACM) {
            // 现在默认得分规则为 通过测试点数/总测试点数*100
            int score = 0;
            score = (testCaseResultList.size() - errorCaseNum) / testCaseResultList.size() * 100;
            result.put("score", score);
        }
        return result;
    }

    // 进行最终测试结果的判断（除编译失败外的评测状态码和时间，空间,OI题目的得分）
    public HashMap<String, Object> getJudgeInfo(List<JSONObject> testCaseResultList, Boolean isACM) {


        // 过滤出结果不是AC的测试点结果
        List<JSONObject> errorTestCaseList = testCaseResultList.stream()
                .filter(jsonObject -> jsonObject.getInt("status").intValue() != Constants.Judge.STATUS_ACCEPTED.getStatus())
                .collect(Collectors.toList());

        // 获取判题的运行时间，运行空间，OI得分
        HashMap<String, Object> result = computeResultInfo(testCaseResultList, isACM, errorTestCaseList.size());

        // 如果该题为ACM类型的题目，多个测试点全部正确则AC，否则取第一个错误的测试点的状态
        // 如果该题为OI类型的题目, 若多个测试点全部正确则AC，若全部错误则取第一个错误测试点状态，否则为部分正确
        if (errorTestCaseList.size() == 0) { // 全部测试点正确，则为AC
            result.put("code", Constants.Judge.STATUS_ACCEPTED.getStatus());
        } else if (isACM || errorTestCaseList.size() == testCaseResultList.size()) {
            result.put("code", errorTestCaseList.get(0).getInt("status"));
        } else {
            result.put("code", Constants.Judge.STATUS_PARTIAL_ACCEPTED.getStatus());
        }
        return result;
    }


    public JSONObject getTestCasesInfo(int testCaseId) {
        return (JSONObject) ((JSONArray) testCasesInfo.get("testCases")).get(testCaseId);
    }


    // 初始化测试数据，写成json文件
    public JSONObject initTestCase(List<HashMap<String, Object>> testCases, Long problemId, Boolean isSpj) throws SystemError {

        if (testCases == null || testCases.size() == 0) {
            throw new SystemError("题号为：" + problemId + "的评测数据为空！", null, "The test cases does not exist.");
        }

        JSONObject result = new JSONObject();
        result.set("isSpj", isSpj);
        result.set("testCasesSize", testCases.size());
        result.set("testCases", new JSONArray());

        String testCasesDir = Constants.JudgeDir.TEST_CASE_DIR.getContent() + "/problem_" + problemId;

        // 无论有没有测试数据，一旦执行该函数，一律清空，重新生成该题目对应的测试数据文件
        FileUtil.del(testCasesDir);
        for (int index = 0; index < testCases.size(); index++) {
            JSONObject jsonObject = new JSONObject();
            String inputName = index + ".in";
            jsonObject.set("inputName", inputName);
            // 生成对应文件
            FileWriter fileWriter = new FileWriter(testCasesDir + "/" + inputName, CharsetUtil.UTF_8);
            // 将该测试数据的输入写入到文件
            fileWriter.write((String) testCases.get(index).get("input"));

            if (!isSpj) {
                String outputName = index + ".out";
                jsonObject.set("outputName", outputName);
                String outputData = (String) testCases.get(index).get("output");
                jsonObject.set("outputMd5", DigestUtils.md5DigestAsHex(outputData.getBytes()));
                jsonObject.set("outputSize", outputData.length());
                jsonObject.set("strippedOutputMd5", DigestUtils.md5DigestAsHex(rtrim(outputData).getBytes()));
                // 生成对应文件
                FileWriter outFile = new FileWriter(testCasesDir + "/" + outputName, CharsetUtil.UTF_8);
                outFile.write(outputData);
            }

            ((JSONArray) result.get("testCases")).put(index, jsonObject);
        }

        FileWriter infoFile = new FileWriter(testCasesDir + "/info", CharsetUtil.UTF_8);
        // 写入记录文件
        infoFile.write(JSONUtil.toJsonStr(result));
        return result;
    }

    // 获取指定题目的info数据
    public JSONObject loadTestCaseInfo(Long problemId, Boolean isSpj) throws SystemError {
        String testCasesDir = Constants.JudgeDir.TEST_CASE_DIR.getContent() + "/problem_" + problemId;
        if (FileUtil.exist(testCasesDir + "/info")) {
            FileReader fileReader = new FileReader(testCasesDir + "/info", CharsetUtil.UTF_8);
            String infoStr = fileReader.readString();
            return JSONUtil.parseObj(infoStr);
        } else {
            return tryInitTestCaseInfo(problemId, isSpj);
        }
    }

    // 若没有测试数据，则尝试从数据库获取并且初始化到本地
    public JSONObject tryInitTestCaseInfo(Long problemId, Boolean isSpj) throws SystemError {
        QueryWrapper<ProblemCase> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", problemId);
        List<ProblemCase> problemCases = problemCaseService.list(queryWrapper);
        List<HashMap<String, Object>> testCases = new LinkedList<>();
        for (ProblemCase problemCase : problemCases) {
            HashMap<String, Object> tmp = new HashMap<>();
            tmp.put("input", problemCase.getInput());
            tmp.put("output", problemCase.getOutput());
            testCases.add(tmp);
        }

        return initTestCase(testCases, problemId, isSpj);
    }

    public static String rtrim(String value) {
        if (value == null) return null;
        return value.replaceAll("\\s+$", "");
    }

}

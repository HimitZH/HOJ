package top.hcode.hoj.judger;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.util.Constants;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class JudgeStrategy {

    private static final int SPJ_WA = 1;

    private static final int SPJ_AC = 0;

    private static final int SPJ_ERROR = -1;

    private static final int cpuNum = Runtime.getRuntime().availableProcessors();

    private JSONObject testCasesInfo;

    private String testCasesDir;

    private String submissionDir;

    private String spjExePath;

    private String spjExeName;

    private String spjRunCommand;

    private Long problemId;

    public JudgeStrategy(Problem problem, Long submitId) {
        this.problemId = problem.getId();
        this.testCasesInfo = loadTestCaseInfo(problemId);
        this.testCasesDir = Constants.Compiler.TEST_CASE_DIR.getContent() + "/problem_" + problemId;
        this.submissionDir = Constants.Compiler.WORKPLACE.getContent() + "/" + submitId;
        if (problem.getSpjLanguage().equals("C")) { //
            this.spjRunCommand = Constants.RunConfig.SPJ_C.getCommand();
            String spjExeName = MessageFormat.format(Constants.CompileConfig.SPJ_C.getExeName(), problem.getId());
            this.spjExeName = spjExeName;
            this.spjExePath = Constants.Compiler.SPJ_EXE_DIR.getContent() + "/" + MessageFormat.format(spjExeName, problemId);
            if (!FileUtil.exist(spjExePath)) {
                this.spjExePath = null;
            }
        } else if (problem.getSpjLanguage().equals("C++")) {
            this.spjRunCommand = Constants.RunConfig.SPJ_CPP.getCommand();
            String spjExeName = MessageFormat.format(Constants.CompileConfig.SPJ_CPP.getExeName(), problem.getId());
            this.spjExeName = spjExeName;
            this.spjExePath = Constants.Compiler.SPJ_EXE_DIR.getContent() + "/" + MessageFormat.format(spjExeName, problemId);
            if (!FileUtil.exist(spjExePath)) {
                this.spjExePath = null;
            }
        }
    }

    public HashMap<String, Object> compile(String srcPath, String outputDir, String command, String exePath,
                                           Long maxCpuTime, Long maxRealTime, Long maxMemory, List<String> envs) {

        // 执行的exe指令路径
        command = MessageFormat.format(command, srcPath, outputDir, exePath);

        List<String> commandList = Arrays.asList(command.split(" "));

        // 编译后的重定向到文件
        String outFilePath = outputDir + "/compiler.out";

        // 调用沙盒进行编译
        JSONObject result = SandboxRun.run(maxCpuTime,
                maxRealTime,
                maxMemory,
                128 * 1024 * 1024L,
                20 * 1024 * 1024L,
                Constants.SandBoxStatus.UNLIMITED.getStatus(),
                commandList.get(0),
                srcPath,
                outFilePath,
                outFilePath,
                commandList.subList(1, commandList.size()),
                envs,
                Constants.Compiler.COMPILE_LOG_PATH.getContent(),
                null,
                0,
                0,
                0);

        System.out.println(result+"\n");

        HashMap<String, Object> data = new HashMap<>();
        if (result == null) {
            data.put("result", false);
            data.put("code", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
            data.put("msg", "Error running security sandbox.");
            return data;
        } else if (result.get("result") != Constants.SandBoxStatus.RESULT_SUCCESS.getStatus()) {
            data.put("result", false);
            if (FileUtil.exist(outFilePath)) {
                FileReader fileReader = new FileReader(outFilePath);
                String error = fileReader.readString();
                FileUtil.del(outFilePath);
                if (!StringUtils.isEmpty(error)) {
                    data.put("msg", error);
                    data.put("code", Constants.Judge.STATUS_COMPILE_ERROR.getStatus());
                    return data;
                }
            }
            data.put("msg", MessageFormat.format("Compiler runtime error, info:{0}", result.toString()));
            data.put("code", Constants.Judge.STATUS_COMPILE_ERROR.getStatus());
            return data;
        } else {
            FileUtil.del(outFilePath);
            data.put("result", true);
            data.put("exePath", exePath);
            return data;
        }
    }

    public String checkOrCompileSpj(String spjCode, String spjLanguage) {
        // 如果是需要特判的题目，则需要检测特批程序是否已经编译，否则进行编译
        if (!StringUtils.isEmpty(spjCode)) {
            if (spjLanguage.equals("C")) {
                String srcName = MessageFormat.format(Constants.CompileConfig.SPJ_C.getSrcName(), problemId);
                String command = Constants.CompileConfig.SPJ_C.getCommand();
                Long maxCpuTime = Constants.CompileConfig.SPJ_C.getMaxCpuTime();
                Long maxRealTime = Constants.CompileConfig.SPJ_C.getMaxRealTime();
                Long maxMemory = Constants.CompileConfig.SPJ_C.getMaxMemory();

                // 如果不存在该已经编译好的特批程序，则需要再次进行编译
                if (!FileUtil.exist(Constants.Compiler.SPJ_EXE_DIR.getContent() + "/" + spjExeName)) {
                    String result = compileSpj(spjCode, srcName, spjExeName, command, maxCpuTime, maxRealTime, maxMemory, new LinkedList<String>());
                    if (!result.equals("success")) { // 如果出错，直接在判题结束，报系统错误！
                        return result;
                    }
                }
            } else if (spjLanguage.equals("C++")) {

                String srcName = MessageFormat.format(Constants.CompileConfig.SPJ_CPP.getSrcName(), problemId);
                String command = Constants.CompileConfig.SPJ_CPP.getCommand();
                Long maxCpuTime = Constants.CompileConfig.SPJ_CPP.getMaxCpuTime();
                Long maxRealTime = Constants.CompileConfig.SPJ_CPP.getMaxRealTime();
                Long maxMemory = Constants.CompileConfig.SPJ_CPP.getMaxMemory();

                // 如果不存在该已经编译好的特批程序，则需要再次进行编译
                if (!FileUtil.exist(Constants.Compiler.SPJ_EXE_DIR.getContent() + "/" + spjExeName)) {
                    String result = compileSpj(spjCode, srcName, spjExeName, command, maxCpuTime, maxRealTime, maxMemory, new LinkedList<String>());
                    if (!result.equals("success")) { // 如果出错，直接在判题结束，报系统错误！
                        return result;
                    }
                }
            }
        }
        return "success";
    }


    public String compileSpj(String spjCode, String srcName, String exeName, String command, Long maxCpuTime, Long maxRealTime,
                             Long maxMemory, List<String> envs) {
        String srcPath = Constants.Compiler.SPJ_SRC_DIR.getContent() + "/" + srcName;
        String exePath = Constants.Compiler.SPJ_EXE_DIR.getContent() + "/" + exeName;
        if (!FileUtil.exist(srcPath) && !StringUtils.isEmpty(spjCode)) {
            // 如果不存在，则将特别判题代码写入
            FileWriter fileWriter = new FileWriter(srcPath);
            fileWriter.write(spjCode);
        }
        // 调用安全沙盒进行编译
        HashMap<String, Object> compileResult = compile(srcPath, Constants.Compiler.SPJ_EXE_DIR.getContent(), command, exePath, maxCpuTime, maxRealTime, maxMemory, envs);
        if ((Boolean) compileResult.getOrDefault("result", false)) {
            return "success";
        } else {
            log.error("特殊判题程序的编译异常---------------->{}", (String) compileResult.getOrDefault("msg", "特殊判题程序编译失败！"));
            return (String) compileResult.getOrDefault("msg", "特殊判题程序编译失败！");
        }
    }

    // 初始化测试数据，写成json文件
    public static void initTestCase(List<HashMap<String, Object>> testCases, Long problemId, Boolean isSpj) {

        JSONObject result = new JSONObject();
        result.set("isSpj", isSpj);
        result.set("testCasesSize", testCases.size());
        result.set("testCases", new JSONArray());

        String testCasesDir = Constants.Compiler.TEST_CASE_DIR.getContent() + "/problem_" + problemId;

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
                jsonObject.set("strippedOutputMd5", DigestUtils.md5DigestAsHex(outputData.trim().getBytes()));
                // 生成对应文件
                FileWriter outFile = new FileWriter(testCasesDir + "/" + outputName, CharsetUtil.UTF_8);
                outFile.write(outputData);
            }

            ((JSONArray) result.get("testCases")).put(index, jsonObject);
        }

        FileWriter infoFile = new FileWriter(testCasesDir + "/info", CharsetUtil.UTF_8);
        // 写入记录文件
        infoFile.write(JSONUtil.toJsonStr(result));
    }

    // 获取指定题目的info数据
    public JSONObject loadTestCaseInfo(Long problemId) {
        String testCasesDir = Constants.Compiler.TEST_CASE_DIR.getContent() + "/problem_" + problemId;
        if (FileUtil.exist(testCasesDir + "/info")) {
            FileReader fileReader = new FileReader(testCasesDir + "/info", CharsetUtil.UTF_8);
            String infoStr = fileReader.readString();
            return JSONUtil.parseObj(infoStr);
        } else {
            log.error("加载题目测试数据的info文件异常----------------->{}", problemId);
            return null;
        }
    }

    public JSONObject getTestCasesInfo(int testCaseId) {
        return (JSONObject) ((JSONArray) testCasesInfo.get("testCases")).get(testCaseId);
    }

    public Integer specialJudge(String inFilePath, String userOutFilePath, Long maxCpuTime, Long maxMemory, String spjRule) {

        if (spjRunCommand == null || spjExePath == null) {
            log.error("spj-" + problemId + "---------------->{}", "该题特别判题程序不存在！");
            return null;
        }
        List<String> commandList = Arrays.asList(MessageFormat.format(spjRunCommand, spjExePath, inFilePath, userOutFilePath).split(" "));

        JSONObject runResult = SandboxRun.run(maxCpuTime * 3,
                maxCpuTime * 9,
                maxMemory * 3,
                128 * 1024 * 1024L,
                1024 * 1024 * 1024L,
                Constants.SandBoxStatus.UNLIMITED.getStatus(),
                commandList.get(0),
                inFilePath,
                "/tmp/spj.out",
                "tmp/spj.out",
                commandList.subList(1, commandList.size()),
                null,
                Constants.Compiler.RUN_LOG_PATH.getContent(),
                spjRule,
                0,
                0,
                0);
        if (runResult == null) {
            return SPJ_ERROR;
        } else if (runResult.getInt("result").intValue() == Constants.SandBoxStatus.RESULT_SUCCESS.getStatus() ||
                (runResult.getInt("result").intValue() == Constants.SandBoxStatus.RESULT_RUNTIME_ERROR.getStatus() &&
                        (runResult.getInt("exit_code") == SPJ_WA || runResult.getInt("exit_code") == SPJ_ERROR) &&
                        runResult.getInt("signal") == 0)) {
            return runResult.getInt("exit_code");
        } else {
            return SPJ_ERROR;
        }
    }

    public JSONObject judgeOneCase(int testCaseId, Boolean getUserOutput, Boolean isSpj, String exePath, String exeDir,
                                   Long maxCpuTime, Long maxRealTime, Long maxMemory, String command, List<String> envs,
                                   String seccompRule, String spjRule, Integer memoryLimitCheckOnly) {
        if (testCasesInfo != null) {
            JSONObject testCaseInfo = getTestCasesInfo(testCaseId);
            String inFile = testCasesDir + "/" + testCaseInfo.get("inputName");
            // 对于当前测试样例，用户程序的控制台输出对应生成的文件
            String realUserOutputFile = submissionDir + "/" + testCaseId + ".out";

            List<String> commandList = Arrays.asList(MessageFormat.format(command, exePath, exeDir, maxMemory/1024).split(" "));
            // 调用沙盒环境进行程序运行测试样例
            JSONObject result = SandboxRun.run(maxCpuTime,
                    maxRealTime,
                    maxMemory,
                    128 * 1024 * 1024L,
                    Math.max(testCasesInfo.getLong("outputSize", 0L) * 2, 1024 * 1024 * 16L),
                    Constants.SandBoxStatus.UNLIMITED.getStatus(),
                    commandList.get(0),
                    inFile,
                    realUserOutputFile,
                    realUserOutputFile,
                    commandList.subList(1, commandList.size()),
                    envs,
                    Constants.Compiler.RUN_LOG_PATH.getContent(),
                    seccompRule,
                    0,
                    0,
                    memoryLimitCheckOnly);

            if (result == null) {
                result = new JSONObject();
                result.set("msg", "Error running security sandbox.");
                result.set("cpu_time", 0L);
                result.set("memory", 0L);
                result.set("result", Constants.SandBoxStatus.RESULT_SYSTEM_ERROR.getStatus());
                return result;
            }

            result.set("testCaseId", testCaseId);
            result.set("outputMd5", null);
            result.set("output", null);
            if (result.getInt("result").intValue() == Constants.SandBoxStatus.RESULT_SUCCESS.getStatus()) {
                if (!FileUtil.exist(realUserOutputFile)) {
                    result.set("result", Constants.SandBoxStatus.RESULT_WRONG_ANSWER.getStatus());
                } else {
                    if (isSpj) { // 特殊判题
                        Integer spjResult = specialJudge(inFile, realUserOutputFile, maxCpuTime, maxMemory, spjRule);
                        if (spjResult == null) {
                            result.set("result", Constants.SandBoxStatus.RESULT_SYSTEM_ERROR.getStatus());
                            result.set("msg", "The special Special judgment code doesn't exist.");
                        } else if (spjResult == SPJ_WA) {
                            result.set("result", Constants.SandBoxStatus.RESULT_WRONG_ANSWER.getStatus());
                        } else if (spjResult == SPJ_ERROR) {
                            result.set("result", Constants.SandBoxStatus.RESULT_SYSTEM_ERROR.getStatus());
                            result.set("msg", "There's something wrong with the special Special judging.");
                        }
                    } else {
                        // 与原测试数据输出的md5进行对比
                        HashMap<String, Object> data = compareOutput(testCaseId, realUserOutputFile);
                        result.set("outputMd5",data.get("outputMd5"));
                        if (!(Boolean) data.get("result")) {
                            result.set("result", Constants.SandBoxStatus.RESULT_WRONG_ANSWER.getStatus());
                        }
                    }
                }
            }

            if (getUserOutput) { // 如果需要获取用户对于该题目的输出
                FileReader fileReader = new FileReader(realUserOutputFile);
                result.set("output", fileReader.readString());
            }
            return result;
        } else {
            log.error("题号为：" + problemId + "的评测数据出错--------------->{}", "测试数据的统计数据info为空");
            return null;
        }
    }


    public List<JSONObject> judgeAllCase(String exePath, String exeDir, Long maxCpuTime, Long maxRealTime, Long maxMemory, String command, List<String> envs,
                                         String seccompRule, String spjRule, Integer memoryLimitCheckOnly) {
        // 使用线程池开启多线程测试每一测试输入数据
        ExecutorService threadPool = new ThreadPoolExecutor(
                cpuNum, // 核心线程数
                cpuNum * 2, // 最大线程数。最多几个线程并发。
                3,//当线程无任务时，几秒后结束该线程
                TimeUnit.SECONDS,// 结束线程时间单位
                new LinkedBlockingDeque<>(200), //阻塞队列，限制等候线程数
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy());//队列满了，尝试去和最早的竞争，也不会抛出异常！

        List<FutureTask<JSONObject>> futureTasks = new ArrayList<>();
        JSONArray testcaseList = (JSONArray) testCasesInfo.get("testCases");
        Boolean isSpj = testCasesInfo.getBool("isSpj");
        for (int index = 0; index < testcaseList.size(); index++) {
            // 将每个需要测试的线程任务加入任务列表中
            final int testCaseId = index;
            futureTasks.add(new FutureTask<>(new Callable<JSONObject>() {
                @Override
                public JSONObject call() throws Exception {
                    return judgeOneCase(testCaseId, false, isSpj, exePath, exeDir, maxCpuTime, maxRealTime,
                            maxMemory, command, envs, seccompRule, spjRule, memoryLimitCheckOnly);
                }
            }));
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
            try {
                JSONObject tmp = futureTasks.get(i).get();
                result.add(tmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    // 根据评测结果与用户程序跑出的字符串MD5进行对比
    public HashMap<String, Object> compareOutput(int testCaseId, String userOutputFile) {

        FileReader fileReader = new FileReader(userOutputFile, CharsetUtil.UTF_8);
        String code = fileReader.readString();
        String outputMd5 = DigestUtils.md5DigestAsHex(code.trim().getBytes());
        HashMap<String, Object> result = new HashMap<>();
        if (outputMd5.equals(getTestCasesInfo(testCaseId).getStr("strippedOutputMd5"))) {
            result.put("result", true);
        } else {
            result.put("result", false);
        }
        result.put("outputMd5", outputMd5);
        return result;
    }

    // 获取判题的运行时间，运行空间，OI得分
    public HashMap<String, Object> computeResultInfo(List<JSONObject> testCaseResultList, Boolean isACM, Integer errorCaseNum) {
        HashMap<String, Object> result = new HashMap<>();
        // 用时和内存占用保存为多个测试点中最长的
        testCaseResultList.stream().max(Comparator.comparing(t -> t.getLong("cpu_time")))
                .ifPresent(t -> result.put("time", t.getLong("cpu_time")));

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
                .filter(jsonObject -> jsonObject.getInt("result").intValue() != Constants.SandBoxStatus.RESULT_SUCCESS.getStatus())
                .collect(Collectors.toList());

        // 获取判题的运行时间，运行空间，OI得分
        HashMap<String, Object> result = computeResultInfo(testCaseResultList, isACM, errorTestCaseList.size());

        // 如果该题为ACM类型的题目，多个测试点全部正确则AC，否则取第一个错误的测试点的状态
        // 如果该题为OI类型的题目, 若多个测试点全部正确则AC，若全部错误则取第一个错误测试点状态，否则为部分正确
        if (errorTestCaseList.size() == 0) { // 全部测试点正确，则为AC
            result.put("code", Constants.Judge.STATUS_ACCEPTED.getStatus());
        } else if (isACM || errorTestCaseList.size() == testCaseResultList.size()) {
            result.put("code", errorTestCaseList.get(0).getInt("result"));
        } else {
            result.put("code", Constants.Judge.STATUS_PARTIAL_ACCEPTED.getStatus());
        }
        return result;
    }


    public abstract HashMap<String, Object> judge();
}

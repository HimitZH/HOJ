package top.hcode.hoj.util;


import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/1 13:00
 * @Description: 常量枚举类
 */
public class Constants {
    /**
     * @Description 提交评测结果的状态码
     * @Since 2021/1/1
     */
    public enum Judge {
        // 提交失败
        STATUS_NOT_SUBMITTED(-10, "Not Submitted"),
        STATUS_PRESENTATION_ERROR(-3, "Presentation Error"),
        STATUS_COMPILE_ERROR(-2, "Compile Error"),
        STATUS_WRONG_ANSWER(-1, "Wrong Answer"),
        STATUS_ACCEPTED(0, "Accepted"),
        STATUS_TIME_LIMIT_EXCEEDED(1, "Time Limit Exceeded"),
        STATUS_MEMORY_LIMIT_EXCEEDED(2, "Memory Limit Exceeded"),
        STATUS_RUNTIME_ERROR(3, "Runtime Error"),
        STATUS_SYSTEM_ERROR(4, "System Error"),
        STATUS_PENDING(5, "Pending"),
        STATUS_COMPILING(6, "Compiling"),
        // 正在等待结果
        STATUS_JUDGING(7, "Judging"),
        STATUS_PARTIAL_ACCEPTED(8, "Partial Accepted"),
        STATUS_SUBMITTING(9, "Submitting"),
        STATUS_SUBMITTED_FAILED(10,"Submitted Failed"),
        STATUS_NULL(15, "No Status");

        private final Integer status;
        private final String name;

        private Judge(Integer status, String name) {
            this.status = status;
            this.name = name;
        }

        public Integer getStatus() {
            return status;
        }

        public String getName() {
            return name;
        }

        public static Judge getTypeByStatus(int status) {
            for (Judge judge : Judge.values()) {
                if (judge.getStatus() == status) {
                    return judge;
                }
            }
            return STATUS_NULL;
        }
    }


    public enum RemoteJudge {

        /**
         * 提交问题队列
         */
        JUDGE_WAITING_SUBMIT_QUEUE("Remote Waiting Submit Queue"),

        /**
         * 等待结果队列
         */
        JUDGE_WAITING_RESULT_QUEUE("Remote Waiting Result Queue"),

        /**
         * 提交消息通知
         */
        JUDGE_SUBMIT_HANDLER("Remote Submit Handler"),

        /**
         * 查询结果通知
         */
        JUDGE_RESULT_HANDLER("Remote Result Handler"),

        HDU_JUDGE("HDU");

        private String name;

        RemoteJudge(String remoteJudgeName) {
            this.name = remoteJudgeName;
        }

        public static RemoteJudge getTypeByName(String judgeName) {
            if (judgeName == null) return null;
            for (RemoteJudge remoteJudge : RemoteJudge.values()) {
                if (remoteJudge.getName().equals(judgeName)) {
                    return remoteJudge;
                }
            }
            return null;
        }

        public String getName() {
            return name;
        }
    }
//    public enum JudgeServer {
//        HDU_JUDGE("HDU", "hdu.com");
//        private String judgeName;
//        private final String judgeHost;
//
//        JudgeServer(String judgeName, String judgeHost) {
//            this.judgeName = judgeName;
//            this.judgeHost = judgeHost;
//        }
//
//        public final String getName() {
//            return judgeName;
//        }
//    }


    public enum JudgeDir {

        RUN_WORKPLACE_DIR("/judge/run"),

        TEST_CASE_DIR("/judge/test_case"),

        SPJ_WORKPLACE_DIR("/judge/spj"),

        TMPFS_DIR("/w");


        private String content;

        JudgeDir(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    public static List<String> defaultEnv = Arrays.asList(
            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "LANG=en_US.UTF-8",
            "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8");

    public static List<String> python3Env = Arrays.asList("LANG=en_US.UTF-8",
            "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8", "PYTHONIOENCODING=utf-8");


    /*
            {0} --> tmpfs_dir
            {1} --> srcName
            {2} --> exeName
     */
    public enum CompileConfig {
        C("C", "main.c", "main", 3000L, 10000L, 256 * 1024 * 1024L, "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c11 {1} -lm -o {2}", defaultEnv),

        CPP("C++", "main.cpp", "main", 10000L, 20000L, 1024 * 1024 * 1024L, "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {1} -lm -o {2}", defaultEnv),

        JAVA("Java", "Main.java", "Main.class", 5000L, 10000L, 1024 * 1024 * 1024L, "/usr/bin/javac -d {0} -encoding utf8 ./{1}", defaultEnv),

        PYTHON2("Python2", "main.py", "main.pyc", 3000L, 10000L, 128 * 1024 * 1024L, "/usr/bin/python -m py_compile ./{1}", defaultEnv),

        PYTHON3("Python3", "main.py", "__pycache__/main.cpython-36.pyc", 3000L, 10000L, 128 * 1024 * 1024L, "/usr/bin/python3 -m py_compile ./{1}", defaultEnv),

        SPJ_C("SPJ-C", "spj.c", "spj", 3000L, 5000L, 1024 * 1024 * 1024L, "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c99 {1} -lm -o {2}", defaultEnv),

        SPJ_CPP("SPJ-C++", "spj.cpp", "spj", 10000L, 20000L, 1024 * 1024 * 1024L, "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {1} -lm -o {2}", defaultEnv);

        private String language;
        private String srcName;
        private String exeName;
        private Long maxCpuTime;
        private Long maxRealTime;
        private Long maxMemory;
        private String command;
        private List<String> envs;

        CompileConfig(String language, String srcName, String exeName, Long maxCpuTime, Long maxRealTime, Long maxMemory,
                      String command, List<String> envs) {
            this.language = language;
            this.srcName = srcName;
            this.exeName = exeName;
            this.maxCpuTime = maxCpuTime;
            this.maxRealTime = maxRealTime;
            this.maxMemory = maxMemory;
            this.command = command;
            this.envs = envs;
        }

        public String getLanguage() {
            return language;
        }

        public String getSrcName() {
            return srcName;
        }

        public String getExeName() {
            return exeName;
        }

        public Long getMaxCpuTime() {
            return maxCpuTime;
        }

        public Long getMaxRealTime() {
            return maxRealTime;
        }

        public Long getMaxMemory() {
            return maxMemory;
        }

        public String getCommand() {
            return command;
        }

        public List<String> getEnvs() {
            return envs;
        }

        public static CompileConfig getCompilerByLanguage(String language) {
            for (CompileConfig compileConfig : CompileConfig.values()) {
                if (compileConfig.getLanguage().equals(language)) {
                    return compileConfig;
                }
            }
            return null;
        }
    }


    /*
        {0} --> tmpfs_dir
        {1} --> exeName
        {2} --> The test case standard output file name of question
 */
    public enum RunConfig {
        C("C", "{0}/{1}", "main", defaultEnv),

        CPP("C++", "{0}/{1}", "main", defaultEnv),

        JAVA("Java", "/usr/bin/java Main", "Main.class", defaultEnv),

        PYTHON2("Python2", "/usr/bin/python {1}", "main", defaultEnv),


        PYTHON3("Python3", "/usr/bin/python3 {1}", "main", python3Env),

        SPJ_C("SPJ-C", "{0}/{1} {2}", "main", defaultEnv),

        SPJ_CPP("SPJ-C++", "{0}/{1} {2}", "main", defaultEnv);

        private String language;
        private String command;
        private String exeName;
        private List<String> envs;

        RunConfig(String language, String command, String exeName, List<String> envs) {
            this.language = language;
            this.command = command;
            this.exeName = exeName;
            this.envs = envs;
        }

        public String getLanguage() {
            return language;
        }

        public String getCommand() {
            return command;
        }

        public String getExeName() {
            return exeName;
        }

        public List<String> getEnvs() {
            return envs;
        }

        public static RunConfig getRunnerByLanguage(String language) {
            for (RunConfig runConfig : RunConfig.values()) {
                if (runConfig.getLanguage().equals(language)) {
                    return runConfig;
                }
            }
            return null;
        }

    }


    /**
     * @Description 比赛相关的常量
     * @Since 2021/1/7
     */
    public enum Contest {
        TYPE_ACM(0, "ACM"),
        TYPE_OI(1, "OI"),

        STATUS_SCHEDULED(-1, "Scheduled"),
        STATUS_RUNNING(0, "Running"),
        STATUS_ENDED(1, "Ended"),

        AUTH_PUBLIC(0, "Public"),
        AUTH_PRIVATE(1, "Private"),
        AUTH_PROTECT(2, "Protect"),

        RECORD_NOT_AC_PENALTY(-1, "未AC通过算罚时"),
        RECORD_NOT_AC_NOT_PENALTY(0, "未AC通过不罚时"),
        RECORD_AC(1, "AC通过");

        private final Integer code;
        private final String name;

        Contest(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public Integer getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }


}
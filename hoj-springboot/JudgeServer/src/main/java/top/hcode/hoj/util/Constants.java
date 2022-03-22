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
        STATUS_SUBMITTED_FAILED(10, "Submitted Failed"),
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


        HDU_JUDGE("HDU"),

        CF_JUDGE("CF"),

        GYM_JUDGE("GYM"),

        POJ_JUDGE("POJ"),

        SPOJ_JUDGE("SPOJ"),

        ATCODER_JUDGE("AC"),

        HDU_REMOTE_JUDGE_ACCOUNT("Hdu Remote Judge Account"),

        CF_REMOTE_JUDGE_ACCOUNT("Codeforces Remote Judge Account");

        private final String name;

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

        public static String getListNameByOJName(String judgeName) {
            if (judgeName == null) return null;
            switch (judgeName) {
                case "HDU":
                    return RemoteJudge.HDU_REMOTE_JUDGE_ACCOUNT.getName();
                case "CF":
                    return RemoteJudge.CF_REMOTE_JUDGE_ACCOUNT.getName();
            }
            return null;
        }

        public String getName() {
            return name;
        }
    }


    public enum JudgeMode {
        DEFAULT("default"),
        SPJ("spj"),
        INTERACTIVE("interactive");

        private final String mode;

        JudgeMode(String mode) {
            this.mode = mode;
        }

        public String getMode() {
            return mode;
        }

        public static JudgeMode getJudgeMode(String mode){
            for (JudgeMode judgeMode : JudgeMode.values()) {
                if (judgeMode.getMode().equals(mode)) {
                    return judgeMode;
                }
            }
            return null;
        }
    }

    public enum JudgeDir {

        RUN_WORKPLACE_DIR("/judge/run"),

        TEST_CASE_DIR("/judge/test_case"),

        SPJ_WORKPLACE_DIR("/judge/spj"),

        INTERACTIVE_WORKPLACE_DIR("/judge/interactive"),

        TMPFS_DIR("/w");


        private final String content;

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
            "LC_ALL=en_US.UTF-8",
            "LANGUAGE=en_US:en",
            "HOME=/w");

    public static List<String> python3Env = Arrays.asList("LANG=en_US.UTF-8",
            "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8", "PYTHONIOENCODING=utf-8");

    public static List<String> golangEnv = Arrays.asList("GODEBUG=madvdontneed=1",
            "GOCACHE=off", "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "LANG=en_US.UTF-8", "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8");

    /*
            {0} --> tmpfs_dir
            {1} --> srcName
            {2} --> exeName
     */
    public enum CompileConfig {
        C("C", "main.c", "main", 3000L, 10000L, 256 * 1024 * 1024L, "/usr/bin/gcc -DONLINE_JUDGE -w -fmax-errors=3 -std=c11 {1} -lm -o {2}", defaultEnv),

        CWithO2("C With O2", "main.c", "main", 3000L, 10000L, 256 * 1024 * 1024L, "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c11 {1} -lm -o {2}", defaultEnv),

        CPP("C++", "main.cpp", "main", 10000L, 20000L, 512 * 1024 * 1024L, "/usr/bin/g++ -DONLINE_JUDGE -w -fmax-errors=3 -std=c++14 {1} -lm -o {2}", defaultEnv),

        CPPWithO2("C++ With O2", "main.cpp", "main", 10000L, 20000L, 512 * 1024 * 1024L, "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {1} -lm -o {2}", defaultEnv),

        JAVA("Java", "Main.java", "Main.jar", 10000L, 20000L, 512 * 1024 * 1024L, "/bin/bash -c \"javac -encoding utf8 {1} && jar -cvf {2} *.class\"", defaultEnv),

        PYTHON2("Python2", "main.py", "main.pyc", 3000L, 10000L, 128 * 1024 * 1024L, "/usr/bin/python -m py_compile ./{1}", defaultEnv),

        PYTHON3("Python3", "main.py", "__pycache__/main.cpython-37.pyc", 3000L, 10000L, 128 * 1024 * 1024L, "/usr/bin/python3.7 -m py_compile ./{1}", defaultEnv),

        GOLANG("Golang", "main.go", "main", 3000L, 5000L, 512 * 1024 * 1024L, "/usr/bin/go build -o {2} {1}", defaultEnv),

        CS("C#", "Main.cs", "main", 5000L, 10000L, 512 * 1024 * 1024L, "/usr/bin/mcs -optimize+ -out:{0}/{2} {0}/{1}", defaultEnv),

        PyPy2("PyPy2", "main.py", "__pycache__/main.pypy-73.pyc", 3000L, 10000L, 256 * 1024 * 1024L, "/usr/bin/pypy -m py_compile {0}/{1}", defaultEnv),

        PyPy3("PyPy3", "main.py", "__pycache__/main.pypy38.pyc", 3000L, 10000L, 256 * 1024 * 1024L, "/usr/bin/pypy3 -m py_compile {0}/{1}", defaultEnv),

        SPJ_C("SPJ-C", "spj.c", "spj", 3000L, 5000L, 512 * 1024 * 1024L, "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c99 {1} -lm -o {2}", defaultEnv),

        SPJ_CPP("SPJ-C++", "spj.cpp", "spj", 10000L, 20000L, 512 * 1024 * 1024L, "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {1} -lm -o {2}", defaultEnv),

        INTERACTIVE_C("INTERACTIVE-C", "interactive.c", "interactive", 3000L, 5000L, 512 * 1024 * 1024L, "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c99 {1} -lm -o {2}", defaultEnv),

        INTERACTIVE_CPP("INTERACTIVE-C++", "interactive.cpp", "interactive", 10000L, 20000L, 512 * 1024 * 1024L, "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {1} -lm -o {2}", defaultEnv);

        private final String language;
        private final String srcName;
        private final String exeName;
        private final Long maxCpuTime;
        private final Long maxRealTime;
        private final Long maxMemory;
        private final String command;
        private final List<String> envs;

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
        {1} --> exeName (user or spj)
        {2} --> The test case standard input file name of question
        {3} --> The user's program output file name of question
        {4} --> The test case standard output file name of question
 */
    public enum RunConfig {
        C("C", "{0}/{1}", "main", defaultEnv),

        CWithO2("C With O2", "{0}/{1}", "main", defaultEnv),

        CPP("C++", "{0}/{1}", "main", defaultEnv),

        CPPWithO2("C++ With O2", "{0}/{1}", "main", defaultEnv),

        JAVA("Java", "/usr/bin/java -cp {0}/{1} Main", "Main.jar", defaultEnv),

        PYTHON2("Python2", "/usr/bin/python {1}", "main", defaultEnv),

        PYTHON3("Python3", "/usr/bin/python3.7 {1}", "main", python3Env),

        GOLANG("Golang", "{0}/{1}", "main", golangEnv),

        CS("C#", "/usr/bin/mono {0}/{1}", "main", defaultEnv),

        PyPy2("PyPy2", "/usr/bin/pypy {1}", "main.pyc", defaultEnv),

        PyPy3("PyPy3", "/usr/bin/pypy3 {1}", "main.pyc", python3Env),

        PHP("PHP","/usr/bin/php {1}","main.php",defaultEnv),

        JS_NODE("JavaScript Node","/usr/bin/node {1}","main.js",defaultEnv),

        JS_V8("JavaScript V8","/usr/bin/jsv8/d8 {1}","main.js",defaultEnv),

        SPJ_C("SPJ-C", "{0}/{1} {2} {3} {4}", "spj", defaultEnv),

        SPJ_CPP("SPJ-C++", "{0}/{1} {2} {3} {4}", "spj", defaultEnv),

        INTERACTIVE_C("INTERACTIVE-C", "{0}/{1} {2} {3} {4}", "interactive", defaultEnv),

        INTERACTIVE_CPP("INTERACTIVE-C++", "{0}/{1} {2} {3} {4}", "interactive", defaultEnv);

        private final String language;
        private final String command;
        private final String exeName;
        private final List<String> envs;

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
package top.hcode.hoj.util;


import java.util.Arrays;
import java.util.List;

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
        STATUS_NOT_SUBMITTED(-10, "Not Submitted"),
        STATUS_PRESENTATION_ERROR(-3, "Presentation Error"),
        STATUS_COMPILE_ERROR(-2, "Compile Error"),
        STATUS_WRONG_ANSWER(-1, "Wrong Answer"),
        STATUS_ACCEPTED(0, "Accepted"),
        STATUS_CPU_TIME_LIMIT_EXCEEDED(1, "CPU Time Limit Exceeded"),
        STATUS_REAL_TIME_LIMIT_EXCEEDED(2, "Real Time Limit Exceeded"),
        STATUS_MEMORY_LIMIT_EXCEEDED(3, "Memory Limit Exceeded"),
        STATUS_RUNTIME_ERROR(4, "Runtime Error"),
        STATUS_SYSTEM_ERROR(5, "System Error"),
        STATUS_PENDING(6, "Pending"),
        STATUS_JUDGING(7, "Judging"),
        STATUS_PARTIAL_ACCEPTED(8, "Partial Accepted"),
        STATUS_SUBMITTING(9, "Submitting"),
        STATUS_NULL(10, "No Status");

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


    public enum SandBoxStatus {
        UNLIMITED(-1),
        VERSION(0x020101),

        RESULT_SUCCESS (0),
        RESULT_WRONG_ANSWER (-1),
        RESULT_CPU_TIME_LIMIT_EXCEEDED (1),
        RESULT_REAL_TIME_LIMIT_EXCEEDED (2),
        RESULT_MEMORY_LIMIT_EXCEEDED (3),
        RESULT_RUNTIME_ERROR (4),
        RESULT_SYSTEM_ERROR (5),

        ERROR_INVALID_CONFIG (-1),
        ERROR_FORK_FAILED (-2),
        ERROR_PTHREAD_FAILED (-3),
        ERROR_WAIT_FAILED (-4),
        ERROR_ROOT_REQUIRED (-5),
        ERROR_LOAD_SECCOMP_FAILED (-6),
        ERROR_SETRLIMIT_FAILED (-7),
        ERROR_DUP2_FAILED (-8),
        ERROR_SETUID_FAILED (-9),
        ERROR_EXECVE_FAILED (-10),
        ERROR_SPJ_ERROR (-11);

        private Integer status;
        SandBoxStatus(Integer status) {
            this.status = status;
        }

        public Integer getStatus() {
            return status;
        }
    }

    public enum Compiler {

        WORKPLACE("/judge/run"),

        JUDGER_LIB_PATH("/usr/lib/judger/libjudger.so"),

        COMPILE_LOG_PATH("/log/compiler.log"),

        RUN_LOG_PATH("/log/judger.log"),

        TEST_CASE_DIR("/judge/test_case"),

        SPJ_SRC_DIR("/judge/spj"),

        SPJ_EXE_DIR("/judge/spj");

        private String content;

        Compiler(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    public enum CompileConfig {
        C("main.c", "main", 3000L, 10000L, 256 * 1024 * 1024L, "/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c11 {0} -lm -o {1}"),

        CPP("main.cpp", "main", 10000L, 20000L, 1024 * 1024 * 1024L, "/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {0} -lm -o {1}"),

        JAVA("Main.java", "Main", 5000L, 10000L, -1L, "/usr/bin/javac {0} -d {1} -encoding UTF8"),

        PYTHON2("main.py", "main.pyc", 3000L, 10000L, 128 * 1024 * 1024L, "/usr/bin/python -m py_compile {0}"),

        PYTHON3("main.py", "__pycache__/main.cpython-36.pyc", 3000L, 10000L, 128 * 1024 * 1024L, "/usr/bin/python3 -m py_compile {0}"),

        SPJ_C("spj-{0}.c","spj-{0}",3000L,5000L,1024*1024*1024L,"/usr/bin/gcc -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c99 {0} -lm -o {1}"),

        SPJ_CPP("spj-{0}.cpp","spj-{0}",10000L,20000L,1024*1024*1024L,"/usr/bin/g++ -DONLINE_JUDGE -O2 -w -fmax-errors=3 -std=c++14 {0} -lm -o {1}");


        private String srcName;
        private String exeName;
        private Long maxCpuTime;
        private Long maxRealTime;
        private Long maxMemory;
        private String command;

        CompileConfig(String srcName, String exeName, Long maxCpuTime, Long maxRealTime, Long maxMemory, String command) {
            this.srcName = srcName;
            this.exeName = exeName;
            this.maxCpuTime = maxCpuTime;
            this.maxRealTime = maxRealTime;
            this.maxMemory = maxMemory;
            this.command = command;
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
    }


    public static List<String> defaultEnv = Arrays.asList("LANG=en_US.UTF-8",
            "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8");

    public static List<String> python3Env = Arrays.asList("LANG=en_US.UTF-8",
            "LANGUAGE=en_US:en", "LC_ALL=en_US.UTF-8", "PYTHONIOENCODING=utf-8");

    public enum RunConfig {
        C("{0}", "c_cpp", defaultEnv, 0),

        CPP("{0}", "c_cpp", defaultEnv, 0),

        JAVA("/usr/bin/java -cp {1} -Djava.security.manager " +
                "-Dfile.encoding=UTF-8 -Djava.security.policy==policy -Djava.awt.headless=true Main",
                null, defaultEnv, 1),

        PYTHON2("/usr/bin/python {0}", "general", defaultEnv, 0),


        PYTHON3("/usr/bin/python3 {0}", "general", python3Env, 0),

        SPJ_C("{0} {1} {2}","c_cpp",null,0),

        SPJ_CPP("{0} {1} {2}","c_cpp",null,0);

        private String command;
        private String seccompRule;
        private List<String> envs;
        private Integer memoryLimitCheckOnly;

        RunConfig(String command, String seccompRule, List<String> envs, Integer memoryLimitCheckOnly) {
            this.command = command;
            this.seccompRule = seccompRule;
            this.envs = envs;
            this.memoryLimitCheckOnly = memoryLimitCheckOnly;
        }

        public String getCommand() {
            return command;
        }

        public String getSeccompRule() {
            return seccompRule;
        }

        public List<String> getEnvs() {
            return envs;
        }

        public Integer getMemoryLimitCheckOnly() {
            return memoryLimitCheckOnly;
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
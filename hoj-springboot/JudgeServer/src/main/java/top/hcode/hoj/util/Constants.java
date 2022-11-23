package top.hcode.hoj.util;


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
        STATUS_CANCELLED(-4, "Cancelled"),
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
        TEST("test"),
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

        public static JudgeMode getJudgeMode(String mode) {
            for (JudgeMode judgeMode : JudgeMode.values()) {
                if (judgeMode.getMode().equals(mode)) {
                    return judgeMode;
                }
            }
            return null;
        }
    }

    public enum JudgeCaseMode{
        DEFAULT("default"),
        SUBTASK_LOWEST("subtask_lowest"),
        SUBTASK_AVERAGE("subtask_average"),
        ERGODIC_WITHOUT_ERROR("ergodic_without_error");

        private final String mode;

        JudgeCaseMode(String mode) {
            this.mode = mode;
        }

        public String getMode() {
            return mode;
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
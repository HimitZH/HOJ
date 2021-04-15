package top.hcode.hoj.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/1 13:00
 * @Description: 常量枚举类
 */
@Component
public class Constants {

    public static String ojAddr;

    public static String ojName;

    public static String ojShortName;

    public static String ojEmailBg;

    public static String ojEmailFrom;


    @Value("${hoj-backstage.addr}")
    public void setOjAddr(String ojAddr) {
        Constants.ojAddr = ojAddr;
    }

    @Value("${hoj-backstage.name}")
    public void setOjName(String ojName) {
        Constants.ojName = ojName;
    }

    @Value("${hoj-backstage.short-name}")
    public void setOjShortName(String ojShortName) {
        Constants.ojShortName = ojShortName;
    }

    @Value("${hoj-backstage.email-bg}")
    public void setOjEmailBg(String ojEmailBg) {
        Constants.ojEmailBg = ojEmailBg;
    }

    @Value("${hoj.mail.username}")
    public void setOjEmailFrom(String ojEmailFrom) {
        Constants.ojEmailFrom = ojEmailFrom;
    }

    /**
     * @Description 提交评测结果的状态码
     * @Since 2021/1/1
     */
    public enum Judge {
        STATUS_NOT_SUBMITTED(-10, "Not Submitted", null),
        STATUS_PRESENTATION_ERROR(-3, "Presentation Error", "pe"),
        STATUS_COMPILE_ERROR(-2, "Compile Error", "ce"),
        STATUS_WRONG_ANSWER(-1, "Wrong Answer", "wa"),
        STATUS_ACCEPTED(0, "Accepted", "ac"),
        STATUS_TIME_LIMIT_EXCEEDED(1, "Time Limit Exceeded", "tle"),
        STATUS_MEMORY_LIMIT_EXCEEDED(2, "Memory Limit Exceeded", "mle"),
        STATUS_RUNTIME_ERROR(3, "Runtime Error", "re"),
        STATUS_SYSTEM_ERROR(4, "System Error", "se"),
        STATUS_PENDING(5, "Pending", null),
        STATUS_COMPILING(6, "Compiling", null),
        STATUS_JUDGING(7, "Judging", null),
        STATUS_PARTIAL_ACCEPTED(8, "Partial Accepted", "pa"),
        STATUS_SUBMITTING(9, "Submitting", null),
        STATUS_SUBMITTED_FAILED(10, "Submitted Failed", null),
        STATUS_NULL(15, "No Status", null),
        STATUS_JUDGE_WAITING(-100, "Waiting Queue", null),
        STATUS_REMOTE_JUDGE_WAITING_HANDLE(-200, "Remote Waiting Handle Queue", null),
        STATUS_HDU_REMOTE_JUDGE_ACCOUNT(-1000, "Hdu Remote Judge Account", null),
        STATUS_CF_REMOTE_JUDGE_ACCOUNT(-1001, "Codeforces Remote Judge Account", null),
        JUDGE_SERVER_SUBMIT_PREFIX(-1002,"Judge SubmitId-ServerId:",null);
        private Judge(Integer status, String name, String columnName) {
            this.status = status;
            this.name = name;
            this.columnName = columnName;
        }

        private final Integer status;
        private final String name;
        private final String columnName;

        public Integer getStatus() {
            return status;
        }

        public String getName() {
            return name;
        }

        public String getColumnName() {
            return columnName;
        }

        public static String getTableColumnNameByStatus(int status) {
            for (Judge judge : Judge.values()) {
                if (judge.getStatus() == status) {
                    return judge.getColumnName();
                }
            }
            return null;
        }

        public static String getListNameByOJName(String judgeName) {
            if (judgeName == null) return null;
            switch (judgeName) {
                case "HDU":
                    return Judge.STATUS_HDU_REMOTE_JUDGE_ACCOUNT.getName();
                case "CF":
                    return Judge.STATUS_CF_REMOTE_JUDGE_ACCOUNT.getName();
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

    /**
     * @Description 账户相关常量
     * @Since 2021/1/8
     */

    public enum Account {
        CODE_CHANGE_PASSWORD_FAIL("change-password-fail:"),
        CODE_CHANGE_PASSWORD_LOCK("change-password-lock:"),
        CODE_ACCOUNT_LOCK("account-lock:"),
        CODE_CHANGE_EMAIL_FAIL("change-email-fail:"),
        CODE_CHANGE_EMAIL_LOCK("change-email-lock:");

        private final String code;

        Account(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }


    /**
     * @Description 文件操作的一些常量
     * @Since 2021/1/10
     */
    public enum File {

        USER_FILE_HOST(ojAddr),

        USER_AVATAR_FOLDER("/hoj/file/avatar"),

        MARKDOWN_IMG_FOLDER("/hoj/md"),

        IMG_API("/api/public/img/"),

        TESTCASE_BASE_FOLDER("/hoj/file/zip"),

        TESTCASE_DOWNLOAD_TMP_FOLDER("/hoj/file/zip/download"),

        CONTEST_AC_SUBMISSION_TMP_FOLDER("/hoj/file/zip/contest_ac");

        private final String path;

        File(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }


    /**
     * @Description 邮件任务的一些常量
     * @Since 2021/1/14
     */

    public enum Email {

        OJ_URL(ojAddr),
        OJ_NAME(ojName),
        OJ_SHORT_NAME(ojShortName),
        EMAIL_FROM(ojEmailFrom),
        EMAIL_BACKGROUND_IMG(ojEmailBg),
        REGISTER_KEY_PREFIX("register-user:"),
        RESET_PASSWORD_KEY_PREFIX("reset-password:");

        private final String value;

        Email(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Schedule {
        RECENT_OTHER_CONTEST("recent-other-contest");

        private final String code;

        Schedule(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;

        }
    }
}
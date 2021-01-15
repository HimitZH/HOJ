package top.hcode.hoj.utils;

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
    public enum Judge{
        STATUS_NOT_SUBMITTED (-10,"Not Submitted",null),
        STATUS_PRESENTATION_ERROR (-3,"Presentation Error","pe"),
        STATUS_COMPILE_ERROR (-2,"Compile Error","ce"),
        STATUS_WRONG_ANSWER (-1,"Wrong Answer","wa"),
        STATUS_ACCEPTED (0,"Accepted","ac"),
        STATUS_CPU_TIME_LIMIT_EXCEEDED (1,"CPU Time Limit Exceeded","tle"),
        STATUS_REAL_TIME_LIMIT_EXCEEDED (2,"Real Time Limit Exceeded","tle"),
        STATUS_MEMORY_LIMIT_EXCEEDED (3,"Memory Limit Exceeded","mle"),
        STATUS_RUNTIME_ERROR (4,"Runtime Error","re"),
        STATUS_SYSTEM_ERROR (5,"System Error","se"),
        STATUS_PENDING (6,"Pending",null),
        STATUS_JUDGING (7,"Judging",null),
        STATUS_PARTIAL_ACCEPTED (8,"Partial Accepted","pa"),
        STATUS_SUBMITTING (9,"Submitting",null);



        private Judge(Integer status, String name,String columnName) {
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

        public static String getTableColumnNameByStatus(int status){
            for(Judge judge:Judge.values()){
                if (judge.getStatus()==status){
                    return judge.getColumnName();
                }
            }
            return null;
        }
    }

    /**
     * @Description 比赛相关的常量
     * @Since 2021/1/7
     */
    public enum Contest{
        TYPE_ACM(0,"ACM"),
        TYPE_OI(1,"OI");

        private final Integer code;
        private final String name;
        Contest(Integer code,String name){
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
    
    public enum Account{
        CODE_CHANGE_PASSWORD_FAIL("change-password-fail:"),
        CODE_CHANGE_PASSWORD_LOCK("change-password-lock:"),
        CODE_ACCOUNT_LOCK("account-lock:"),
        CODE_CHANGE_EMAIL_FAIL("change-email-fail:"),
        CODE_CHANGE_EMAIL_LOCK("change-email-lock:");

        private final String code;
        Account(String code){
            this.code =code;
        }

        public String getCode() {
            return code;
        }
    }


    /**
     * @Description 文件操作的一些常量
     * @Since 2021/1/10
     */
    public enum File{

        USER_FILE_HOST("http://localhost:9010"),
        USER_AVATAR_FOLDER("D:\\avatar\\"),
        USER_AVATAR_API("/public/img/");

        private final String path;

        File(String path){
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

    public enum Email{
        OJ_URL("http://localhost:8080"),
        OJ_NAME("Hcode Online Judge"),
        OJ_SHORT_NAME("HOJ"),
        EMAIL_FROM("oj.hcode@qq.com"),
        EMAIL_BACKGROUND_IMG("https://cdn.jsdelivr.net/gh/HimitZH/CDN/images/HCODE.png"),
        REGISTER_KEY_PREFIX("register-user:"),
        RESET_PASSWORD_KEY_PREFIX("reset-password:");
        private String value;

        Email(String value){
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
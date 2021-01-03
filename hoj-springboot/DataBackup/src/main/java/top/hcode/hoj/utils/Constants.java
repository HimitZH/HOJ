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
}
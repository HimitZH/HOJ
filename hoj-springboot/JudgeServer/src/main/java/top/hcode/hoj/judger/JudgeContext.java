package top.hcode.hoj.judger;

import java.util.HashMap;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/22 21:16
 * @Description:
 */
public class JudgeContext {

    private JudgeStrategy judgeStrategy;

    public JudgeContext(JudgeStrategy judgeStrategy) {
        this.judgeStrategy = judgeStrategy;
    }

    // 判题的上下文接口
    public HashMap<String, Object> judge() {
       return judgeStrategy.judge();
    }
}
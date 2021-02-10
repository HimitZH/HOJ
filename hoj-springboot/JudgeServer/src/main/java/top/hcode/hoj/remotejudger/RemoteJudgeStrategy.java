package top.hcode.hoj.remotejudger;


import java.util.HashMap;


/**
 * 远程判题机接口
 */
public interface RemoteJudgeStrategy {

    /**
     * @param problemId 提交的题目id
     * @return 返回结果
     */
    HashMap<String, Object> judge(Long problemId);

}

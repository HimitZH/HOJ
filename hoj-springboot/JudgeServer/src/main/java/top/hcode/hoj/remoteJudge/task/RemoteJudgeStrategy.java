package top.hcode.hoj.remoteJudge.task;


import java.util.Map;


/**
 * 远程判题机接口
 */
public interface RemoteJudgeStrategy {

    /**
     * @param problemId 提交的题目id
     * @param userCode  用户代码
     * @return 返回对应题库的提交Id
     */
    Long submit(Long problemId, String language, String userCode);

    /**
     * @param submitId 题库的提交ID
     * @return 返回结果
     */
    String result(Long submitId);

    Map<String, String> getLoginCookie();

}

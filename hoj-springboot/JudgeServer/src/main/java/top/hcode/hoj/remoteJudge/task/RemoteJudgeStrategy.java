package top.hcode.hoj.remoteJudge.task;


import java.io.IOException;
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
    Long submit(Long problemId, String language, String userCode) throws Exception;

    /**
     * @param submitId 题库的提交ID
     * @return 返回结果
     */
    Map<String, Object> result(Long submitId) throws Exception;

    Map<String, String> getLoginCookie() throws Exception;

    String getLanguage(String language);

}

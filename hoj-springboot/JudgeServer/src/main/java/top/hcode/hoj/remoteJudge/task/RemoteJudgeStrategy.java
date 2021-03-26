package top.hcode.hoj.remoteJudge.task;


import java.util.HashMap;
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
    Map<String, Object> submit(String username, String password, String problemId, String language, String userCode) throws Exception;

    /**
     * @param submitId 题库的提交ID
     * @param username 题库的提交者的账号
     * @return 返回结果
     */
    Map<String, Object> result(Long submitId, String username, String token, HashMap<String,String> cookies) throws Exception;

    Map<String, Object> getLoginUtils(String username, String password) throws Exception;

    String getLanguage(String language);

}

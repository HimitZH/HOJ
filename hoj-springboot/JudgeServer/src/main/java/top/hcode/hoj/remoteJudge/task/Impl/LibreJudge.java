package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.apache.commons.lang.StringUtils;
import top.hcode.hoj.judge.entity.Pair_;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName LibreJudge
 * @Description 远程判题
 * @Author Nine
 * @Date 2024/2/23 18:57
 * @Version 1.0
 */
public class LibreJudge extends RemoteJudgeStrategy {
    public static final String HOST = "https://api.loj.ac/api";
    public static final String LOGIN_URL = "/auth/login";
    public static final String SUBMIT_URL = "/submission/submit";
    public static final String SUBMISSION_RESULT_URL = "/submission/getSubmissionDetail";
    private static final Map<String, Constants.Judge> statusTypeMap = new HashMap<>();

    private static Map<String, Pair_<String, String>> language = new HashMap<>();
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36";


    static {
        //支持语言初始化
        language.put("C++ 11 (G++)", new Pair_<>("cpp", "{\"compiler\":\"g++\",\"std\":\"c++11\",\"O\":\"0\",\"m\":\"64\"}"));
        language.put("C++ 17 (G++)", new Pair_<>("cpp", "{\"compiler\":\"g++\",\"std\":\"c++17\",\"O\":\"0\",\"m\":\"64\"}"));
        language.put("C++ 11 (Clang++)", new Pair_<>("cpp", "{\"compiler\":\"clang++\",\"std\":\"c++11\",\"O\":\"0\",\"m\":\"64\"}"));
        language.put("C++ 17 (Clang++)", new Pair_<>("cpp", "{\"compiler\":\"clang++\",\"std\":\"c++17\",\"O\":\"0\",\"m\":\"64\"}"));
        language.put("C++ 11 O2(G++)", new Pair_<>("cpp", "{\"compiler\":\"g++\",\"std\":\"c++11\",\"O\":\"2\",\"m\":\"64\"}"));
        language.put("C++ 17 O2(G++)", new Pair_<>("cpp", "{\"compiler\":\"g++\",\"std\":\"c++17\",\"O\":\"2\",\"m\":\"64\"}"));
        language.put("C++ 11 O2(Clang++)", new Pair_<>("cpp", "{\"compiler\":\"clang++\",\"std\":\"c++11\",\"O\":\"2\",\"m\":\"64\"}"));
        language.put("C++ 17 O2(Clang++)", new Pair_<>("cpp", "{\"compiler\":\"clang++\",\"std\":\"c++17\",\"O\":\"2\",\"m\":\"64\"}"));
        language.put("C 11 (GCC)", new Pair_<>("c", "{\"compiler\":\"gcc\",\"std\":\"c11\",\"O\":\"0\",\"m\":\"64\"}"));
        language.put("C 17 (GCC)", new Pair_<>("c", "{\"compiler\":\"gcc\",\"std\":\"c17\",\"O\":\"0\",\"m\":\"64\"}"));
        language.put("C 11 (Clang)", new Pair_<>("c", "{\"compiler\":\"clang\",\"std\":\"c11\",\"O\":\"0\",\"m\":\"64\"}"));
        language.put("C 17 (Clang)", new Pair_<>("c", "{\"compiler\":\"clang\" ,new Pair_<>(\"std\":\"c17\",\"O\":\"0\",\"m\":\"64\"}"));
        language.put("Java", new Pair_<>("java", null));
        language.put("Kotlin 1.8 (JVM)", new Pair_<>("kotlin", "{\"version\":\"1.8\",\"platform\":\"jvm\"}"));
        language.put("Pascal", new Pair_<>("pascal", "{\"optimize\":\"-\"}"));
        language.put("Python 3.10", new Pair_<>("python", "{\"version\":\"3.10\"}"));
        language.put("Python 3.9", new Pair_<>("python", "{\"version\":\"3.9\"}"));
        language.put("Python 2.7", new Pair_<>("python", "{\"version\":\"2.7\"}"));
        language.put("Rust 2021", new Pair_<>("rust", "{\"version\":\"2021\",\"optimize\":\"0\"}"));
        language.put("Rust 2018", new Pair_<>("rust", "{\"version\":\"2018\",\"optimize\":\"0\"}"));
        language.put("Rust 2015", new Pair_<>("rust", "{\"version\":\"2015\",\"optimize\":\"0\"}"));
        language.put("Go 1.x", new Pair_<>("go", "{\"version\":\"1.x\"}"));
        language.put("C# 9", new Pair_<>("csharp", "{\"version\":\"9\"}"));
        language.put("C# 7", new Pair_<>("csharp", "{\"version\":\"7.3\"}"));

        //result返回结果初始化
        statusTypeMap.put("Pending", Constants.Judge.STATUS_PENDING);
        statusTypeMap.put("Accepted", Constants.Judge.STATUS_ACCEPTED);
        statusTypeMap.put("PartiallyCorrect", Constants.Judge.STATUS_PARTIAL_ACCEPTED);
        statusTypeMap.put("WrongAnswer", Constants.Judge.STATUS_WRONG_ANSWER);
        statusTypeMap.put("CompilationError", Constants.Judge.STATUS_COMPILE_ERROR);
        statusTypeMap.put("RuntimeError", Constants.Judge.STATUS_RUNTIME_ERROR);
        statusTypeMap.put("TimeLimitExceeded", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
        statusTypeMap.put("MemoryLimitExceeded", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
        statusTypeMap.put("OutputLimitExceeded", Constants.Judge.STATUS_RUNTIME_ERROR);
        statusTypeMap.put("FileError", Constants.Judge.STATUS_RUNTIME_ERROR);
        statusTypeMap.put("JudgementFailed", Constants.Judge.STATUS_RUNTIME_ERROR);
        statusTypeMap.put("ConfigurationError", Constants.Judge.STATUS_RUNTIME_ERROR);
        statusTypeMap.put("SystemError", Constants.Judge.STATUS_SYSTEM_ERROR);
        statusTypeMap.put("SysCanceledtemError", Constants.Judge.STATUS_CANCELLED);
    }


    @Override
    public void submit() {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        if (remoteJudgeDTO.getCompleteProblemId() == null || remoteJudgeDTO.getUserCode() == null) {
            return;
        }
        try {
            login();
        }catch (cn.hutool.core.io.IORuntimeException e){
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ignored) {
            }
            // 超时重试
            login();
        }
        JSONObject json = new JSONObject();
        JSONObject content = new JSONObject();
        json.put("problemId", Integer.parseInt(remoteJudgeDTO.getCompleteProblemId()));
        content.put("code", remoteJudgeDTO.getUserCode());
        content.put("language", language.get(remoteJudgeDTO.getLanguage()).getKey());
        content.put("compileAndRunOptions", JSONUtil.parseObj(language.get(remoteJudgeDTO.getLanguage()).getValue()));
        json.put("content", content);
        json.put("uploadInfo", null);
        String body = HttpRequest
                .post(HOST + SUBMIT_URL)
                .header("Authorization", "Bearer " + remoteJudgeDTO.getCsrfToken())
                .header("User-Agent", UA)
                .body(JSONUtil.toJsonStr(JSONUtil.parseObj(json, false)))
                .execute()
                .body();
        String submissionId = JSONUtil.parseObj(body).getStr("submissionId");
        //提交成功
        if (StringUtils.isNotBlank(submissionId)) {
            remoteJudgeDTO.setSubmitId(Long.parseLong(submissionId));
        } else {
            //再试一次
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException ignore){}
            body = HttpRequest
                    .post(HOST + SUBMIT_URL)
                    .header("Authorization", "Bearer " + remoteJudgeDTO.getCsrfToken())
                    .body(JSONUtil.toJsonStr(JSONUtil.parseObj(json, false)))
                    .execute()
                    .body();
            submissionId = JSONUtil.parseObj(body).getStr("submissionId");
            if (StringUtils.isNotBlank(submissionId)) {
                remoteJudgeDTO.setSubmitId(Long.parseLong(submissionId));
            } else {
                throw new RuntimeException("[LibreOJ] Failed to submit!");
            }
        }
    }
    @Override
    public RemoteJudgeRes result() {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        JSONObject json = new JSONObject();
        json.put("submissionId", remoteJudgeDTO.getSubmitId().toString());
        json.put("locale", "zh_CN");
        String body = HttpRequest.post(HOST + SUBMISSION_RESULT_URL)
                .body(json.toString())
                .execute().body();
        JSONObject parseObj = JSONUtil.parseObj(body);
        if (StringUtils.isNotBlank(parseObj.getStr("error"))) {
            throw new RuntimeException("[LibreOJ] Failed to getSubmitDetail!" + parseObj.getStr("error"));
        }
        JSONObject meta = JSONUtil.parseObj(parseObj.get("meta"));
        String status = meta.getStr("status");
        Constants.Judge judge = statusTypeMap.getOrDefault(status, Constants.Judge.STATUS_PENDING);
        if (status.compareTo(Constants.Judge.STATUS_PENDING.getName()) == 0) {
            return RemoteJudgeRes.builder()
                    .status(Constants.Judge.STATUS_PENDING.getStatus())
                    .build();
        }
        String msg = ReUtil.get("(?<=\\\"message\\\":\\\")[^\\\"]*(?=\\\")", body, 0);
        if (msg != null){
            // 使用正则表达式替换掉所有的ANSI颜色编码和文题编码
            msg = msg.replaceAll("\\\\u001b\\[[0-9;]*[a-zA-Z]", "")
                    .replaceAll("\\\\n", "\\\n");
        }
        return RemoteJudgeRes.builder()
                .status(judge.getStatus())
                .memory(Optional.ofNullable(meta.getStr("memoryUsed")).map(Integer::parseInt).orElse(0))
                .time(Optional.ofNullable(meta.getStr("timeUsed")).map(Integer::parseInt).orElse(0))
                .errorInfo(msg)
                .build();
    }

    @Override
    public void login() {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        JSONObject json = new JSONObject();
        json.put("username", remoteJudgeDTO.getUsername());
        json.put("password", remoteJudgeDTO.getPassword());
        String body = HttpRequest.post(HOST + LOGIN_URL)
                .body(json.toString())
                .header("User-Agent", UA)
                .timeout(5000)
                .execute()
                .body();
        JSONObject parse = JSONUtil.parseObj(body);
        if (StringUtils.isNotBlank(parse.getStr("error"))) {
            throw new RuntimeException("[LibreOJ] Failed to login! The possible cause is connection failure, and the returned status code is " + parse.getStr("error"));
        }
        remoteJudgeDTO.setCsrfToken(parse.getStr("token"));
    }

    @Override
    public String getLanguage(String language) {
        return null;
    }

}

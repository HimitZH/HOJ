package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.http.HttpEntity;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.JsoupUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class CodeForcesJudge implements RemoteJudgeStrategy {
    public static final String HOST = "https://codeforces.com";
    public static final String LOGIN_URL = "/enter";
    public static final String SUBMIT_URL = "/problemset/submit?csrf_token=%s";
    public static final String SUBMISSION_RESULT_URL = "/api/user.status?handle=%s&from=1&count=20";
    public static final String CE_INFO_URL = "/data/judgeProtocol";
    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("Host", "codeforces.com")
            .put("origin","https://codeforces.com")
            .put("referer","https://codeforces.com")
            .map();

    private static final Map<String, Constants.Judge> statusMap = new HashMap<String, Constants.Judge>() {{
        put("FAILED", Constants.Judge.STATUS_SUBMITTED_FAILED);
        put("OK", Constants.Judge.STATUS_ACCEPTED);
        put("PARTIAL", Constants.Judge.STATUS_PARTIAL_ACCEPTED);
        put("COMPILATION_ERROR", Constants.Judge.STATUS_COMPILE_ERROR);
        put("RUNTIME_ERROR", Constants.Judge.STATUS_RUNTIME_ERROR);
        put("WRONG_ANSWER", Constants.Judge.STATUS_WRONG_ANSWER);
        put("PRESENTATION_ERROR", Constants.Judge.STATUS_PRESENTATION_ERROR);
        put("TIME_LIMIT_EXCEEDED", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
        put("MEMORY_LIMIT_EXCEEDED", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
        put("IDLENESS_LIMIT_EXCEEDED", Constants.Judge.STATUS_RUNTIME_ERROR);
        put("SECURITY_VIOLATED", Constants.Judge.STATUS_RUNTIME_ERROR);
        put("CRASHED", Constants.Judge.STATUS_SYSTEM_ERROR);
        put("INPUT_PREPARATION_CRASHED", Constants.Judge.STATUS_SYSTEM_ERROR);
        put("CHALLENGED", Constants.Judge.STATUS_SYSTEM_ERROR);
        put("SKIPPED", Constants.Judge.STATUS_SYSTEM_ERROR);
        put("TESTING", Constants.Judge.STATUS_JUDGING);
        put("REJECTED", Constants.Judge.STATUS_SYSTEM_ERROR);
    }};


    @Override
    public Map<String, Object> submit(String username, String password, String problemId, String language, String userCode) throws Exception {
        if (problemId == null || userCode == null) {
            return null;
        }
        String contestNum = problemId.replaceAll("\\D.*", "");
        String problemNum = problemId.replaceAll("^\\d*", "");

        Map<String, Object> loginUtils = getLoginUtils(username, password);
        CodeForcesToken token = (CodeForcesToken) loginUtils.get("token");
        Connection connection = JsoupUtils.getConnectionFromUrl(HOST + String.format(SUBMIT_URL,token.csrf_token), headers, token.cookies);
        Connection.Response response = JsoupUtils.postResponse(connection, MapUtil
                .builder(new HashMap<String, String>())
                .put("csrf_token", token.csrf_token)
                .put("_tta", token._tta)
                .put("action", "submitSolutionFormSubmitted")
                .put("contestId", contestNum) // 比赛号
                .put("submittedProblemIndex", problemNum) // 题号：1A
                .put("programTypeId", getLanguage(language)) // 语言号
                .put("source", userCode + getRandomBlankString())
                .put("sourceFile", "")
                .map());
        if (response.statusCode() != 200) {
            log.error("进行题目提交时发生错误：提交题目失败，" + CodeForcesJudge.class.getName() + "，题号:" + problemId);
            return null;
        }
        // 获取提交的题目id
        Long maxRunId = getMaxRunId(connection, username, problemId);
        return MapUtil.builder(new HashMap<String, Object>())
                .put("token", token.csrf_token)
                .put("cookies", token.cookies)
                .put("runId", maxRunId)
                .map();
    }

    private Long getMaxRunId(Connection connection, String username, String problemId) throws IOException {
        String url = String.format(SUBMISSION_RESULT_URL, username);
        connection.url(HOST + url);
        connection.ignoreContentType(true);
        Connection.Response response = JsoupUtils.getResponse(connection, null);

        String contestNum = problemId.replaceAll("\\D.*", "");
        String problemNum = problemId.replaceAll("^\\d*", "");
        try {
            Map<String, Object> json = JSONUtil.parseObj(response.body());
            List<Map<String, Object>> results = (List<Map<String, Object>>) json.get("result");
            for (Map<String, Object> result : results) {
                Long runId = Long.valueOf(result.get("id").toString());
                Map<String, Object> problem = (Map<String, Object>) result.get("problem");
                if (contestNum.equals(problem.get("contestId").toString()) &&
                        problemNum.equals(problem.get("index").toString())) {
                    return runId;
                }
            }
        } catch (Exception e) {
            log.error("进行题目获取runID发生错误：提交题目失败，" + CodeForcesJudge.class.getName()
                    + "，题号:" + problemId + "，异常描述：" + e.getMessage());
            return -1L;
        }
        return -1L;
    }

    @Override
    public Map<String, Object> result(Long submitId, String username, String token, HashMap<String, String> cookies) throws Exception {
        String url = HOST + String.format(SUBMISSION_RESULT_URL, username);
        Connection connection = JsoupUtils.getConnectionFromUrl(url, headers, cookies);
        connection.ignoreContentType(true);
        Connection.Response response = JsoupUtils.getResponse(connection, null);

        JSONObject jsonObject = JSONUtil.parseObj(response.body());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", Constants.Judge.STATUS_JUDGING.getStatus());

        JSONArray results = (JSONArray) jsonObject.get("result");

        for (Object tmp : results) {
            JSONObject result = (JSONObject) tmp;
            long runId = Long.parseLong(result.get("id").toString());
            if (runId == submitId) {
                String verdict = (String) result.get("verdict");
                Constants.Judge statusType = statusMap.get(verdict);
                if (statusType == Constants.Judge.STATUS_JUDGING) {
                    return MapUtil.builder(new HashMap<String, Object>())
                            .put("status", statusType.getStatus()).build();
                }
                resultMap.put("time", result.get("timeConsumedMillis"));
                resultMap.put("memory", result.get("memoryConsumedBytes"));
                Constants.Judge resultStatus = statusMap.get(verdict);
                if (resultStatus == Constants.Judge.STATUS_COMPILE_ERROR) {

                    Connection CEInfoConnection = JsoupUtils.getConnectionFromUrl(HOST + CE_INFO_URL, headers, cookies);
                    CEInfoConnection.ignoreContentType(true);

                    Connection.Response CEInfoResponse = JsoupUtils.postResponse(CEInfoConnection, MapUtil
                            .builder(new HashMap<String, String>())
                            .put("csrf_token", token)
                            .put("submissionId", submitId.toString()).map());


                    resultMap.put("CEInfo", UnicodeUtil.toString(CEInfoResponse.body()).replaceAll("(\\\\r)?\\\\n", "\n")
                            .replaceAll("\\\\\\\\", "\\\\"));
                }
                resultMap.put("status", resultStatus.getStatus());
                return resultMap;
            }
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getLoginUtils(String username, String password) throws Exception {
        // 获取token
        CodeForcesToken token = getTokens();
        Connection connection = JsoupUtils.getConnectionFromUrl(HOST + LOGIN_URL, headers, token.cookies);

        Connection.Response response = JsoupUtils.postResponse(connection, MapUtil
                .builder(new HashMap<String, String>())
                .put("csrf_token", token.csrf_token)
                .put("_tta", token._tta)
                .put("action", "enter")
                .put("remember", "on") // 是否记住登录
                .put("handleOrEmail", username)
                .put("password", password).map());
        // 混合cookie才能确认身份
        token.cookies.putAll(response.cookies());
        return MapUtil.builder(new HashMap<String, Object>()).put("token", token).map();
    }

    @Override
    public String getLanguage(String language) {
        if (language.startsWith("GNU GCC C11")) {
            return "43";
        } else if (language.startsWith("Clang++17 Diagnostics")) {
            return "52";
        } else if (language.startsWith("GNU G++11")) {
            return "42";
        } else if (language.startsWith("GNU G++14")) {
            return "50";
        } else if (language.startsWith("GNU G++17")) {
            return "54";
        } else if (language.startsWith("Microsoft Visual C++ 2010")) {
            return "2";
        } else if (language.startsWith("Microsoft Visual C++ 2017")) {
            return "59";
        } else if (language.startsWith("C# 8, .NET Core")) {
            return "65";
        } else if (language.startsWith("C# Mono")) {
            return "9";
        } else if (language.startsWith("D DMD32")) {
            return "28";
        } else if (language.startsWith("Go")) {
            return "32";
        } else if (language.startsWith("Haskell GHC")) {
            return "12";
        } else if (language.startsWith("Java 11")) {
            return "60";
        } else if (language.startsWith("Java 1.8")) {
            return "36";
        } else if (language.startsWith("Kotlin")) {
            return "48";
        } else if (language.startsWith("OCaml")) {
            return "19";
        } else if (language.startsWith("Delphi")) {
            return "3";
        } else if (language.startsWith("Free Pascal")) {
            return "4";
        } else if (language.startsWith("PascalABC.NET")) {
            return "51";
        } else if (language.startsWith("Perl")) {
            return "13";
        } else if (language.startsWith("PHP")) {
            return "6";
        } else if (language.startsWith("Python 2")) {
            return "7";
        } else if (language.startsWith("Python 3")) {
            return "31";
        } else if (language.startsWith("PyPy 2")) {
            return "40";
        } else if (language.startsWith("PyPy 3")) {
            return "41";
        } else if (language.startsWith("Ruby")) {
            return "67";
        } else if (language.startsWith("Rust")) {
            return "49";
        } else if (language.startsWith("Scala")) {
            return "20";
        } else if (language.startsWith("JavaScript")) {
            return "34";
        } else if (language.startsWith("Node.js")) {
            return "55";
        } else {
            return null;
        }
    }


    public static class CodeForcesToken {
        public String _tta;
        public String csrf_token;
        public Map<String, String> cookies;

        public CodeForcesToken(String _tta, String csrf_token, Map<String, String> cookies) {
            this._tta = _tta;
            this.csrf_token = csrf_token;
            this.cookies = cookies;
        }
    }

    public static CodeForcesToken getTokens() throws Exception {
        Connection connection = JsoupUtils.getConnectionFromUrl(HOST, headers, null);
        Connection.Response response = JsoupUtils.getResponse(connection, null);
        String html = response.body();
        String csrfToken = ReUtil.get("data-csrf='(\\w+)'", html, 1);
        String _39ce7 = null;
        Map<String, String> cookies = response.cookies();
        if (cookies.containsKey("39ce7")) {
            _39ce7 = cookies.get("39ce7");
        } else {
            throw new Exception("网络错误,无法找到cookies");
        }
        int _tta = 0;
        for (int c = 0; c < _39ce7.length(); c++) {
            _tta = (_tta + (c + 1) * (c + 2) * _39ce7.charAt(c)) % 1009;
            if (c % 3 == 0)
                _tta++;
            if (c % 2 == 0)
                _tta *= 2;
            if (c > 0)
                _tta -= (_39ce7.charAt(c / 2) / 2) * (_tta % 5);
            while (_tta < 0)
                _tta += 1009;
            while (_tta >= 1009)
                _tta -= 1009;
        }
        return new CodeForcesToken(Integer.toString(_tta), csrfToken, cookies);
    }

    protected String getRandomBlankString() {
        StringBuilder string = new StringBuilder("\n");
        int random = new Random().nextInt(Integer.MAX_VALUE);
        while (random > 0) {
            string.append(random % 2 == 0 ? ' ' : '\t');
            random /= 2;
        }
        return string.toString();
    }
}

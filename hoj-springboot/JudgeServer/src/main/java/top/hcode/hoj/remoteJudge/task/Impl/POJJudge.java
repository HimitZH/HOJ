package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.*;
import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Himit_ZH
 * @Date: 2021/6/24 21:19
 * @Description:
 */
@Slf4j(topic = "hoj")
public class POJJudge implements RemoteJudgeStrategy {
    public static final String HOST = "http://poj.org";
    public static final String LOGIN_URL = "/login";
    public static final String SUBMIT_URL = "/submit";
    public static final String STATUS_URL = "/status?user_id=%s&problem_id=%s";
    public static final String QUERY_URL = "/showsource?solution_id=%s";
    public static final String ERROR_URL = "/showcompileinfo?solution_id=%s";
    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("Host", "poj.org")
            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .map();

    /**
     * @param problemId 提交的题目id
     * @param language
     * @param userCode  用户代码
     * @return
     */
    @Override
    public Map<String, Object> submit(String username, String password, String problemId, String language, String userCode) throws Exception {
        if (problemId == null || userCode == null) {
            return null;
        }
        Map<String, Object> loginUtils = getLoginUtils(username, password);
        String cookies = (String) loginUtils.get("cookie");

        HttpRequest request = HttpUtil.createPost(HOST + SUBMIT_URL)
                .addHeaders(headers)
                .cookie(cookies);

        HttpResponse response = request.form(MapUtil.builder(new HashMap<String, Object>())
                .put("language", getLanguage(language))
                .put("submit", "Submit")
                .put("problem_id", problemId)
                .put("source", Base64.encode(userCode))
                .put("encoded", 1).map())
                .execute();
        if (response.getStatus() != 302 && response.getStatus() != 200) {
            log.error("进行题目提交时发生错误：提交题目失败，" + POJJudge.class.getName() + "，题号:" + problemId);
            return null;
        }
        // 下面的请求都是GET
        request.setMethod(Method.GET);
        // 获取提交的题目id
        Long maxRunId = getMaxRunId(request, username, problemId);

        if (maxRunId == -1L) { // 等待2s再次查询，如果还是失败，则表明提交失败了
            TimeUnit.SECONDS.sleep(2);
            maxRunId = getMaxRunId(request, username, problemId);
        }
        return MapUtil.builder(new HashMap<String, Object>())
                .put("cookies", cookies)
                .put("runId", maxRunId)
                .map();
    }

    @Override
    public Map<String, Object> result(Long submitId, String username, String cookies) {
        String url = HOST + String.format(QUERY_URL, submitId);
        HttpRequest request = HttpUtil.createGet(url)
                .cookie(cookies)
                .addHeaders(headers);
        HttpResponse response = request.execute();

        String statusStr = ReUtil.get("<b>Result:</b>(.+?)</td>", response.body(), 1)
                .replaceAll("<.*?>", "")
                .trim();

        Constants.Judge statusType = statusMap.get(statusStr);
        if (statusType == null) {
            return MapUtil.builder(new HashMap<String, Object>())
                    .put("status", Constants.Judge.STATUS_JUDGING).build();
        }
        // 返回的结果map
        Map<String, Object> result = MapUtil.builder(new HashMap<String, Object>())
                .put("status", statusType.getStatus()).build();
        // 如果CE了，需要获得错误信息
        if (statusType == Constants.Judge.STATUS_COMPILE_ERROR) {
            request.setUrl(HOST + String.format(ERROR_URL, submitId));
            String CEHtml = request.execute().body();
            String compilationErrorInfo = ReUtil.get("<pre>([\\s\\S]*?)</pre>", CEHtml, 1);
            result.put("CEInfo", HtmlUtil.unescape(compilationErrorInfo));
        } else {
            // 如果不是CE,获取其他信息
            String executionTime = ReUtil.get("<b>Memory:</b> ([-\\d]+)", response.body(), 1);
            result.put("time", executionTime == null ? null : Integer.parseInt(executionTime));
            String executionMemory = ReUtil.get("<b>Time:</b> ([-\\d]+)", response.body(), 1);
            result.put("memory", executionMemory == null ? null : Integer.parseInt(executionMemory));
        }
        return result;
    }


    @Override
    public Map<String, Object> getLoginUtils(String username, String password) {

        HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL);
        HttpResponse response = request.form(MapUtil.builder(new HashMap<String, Object>())
                .put("user_id1", username)
                .put("B1", "login")
                .put("url", ".")
                .put("password1", password).map()).execute();

        return MapUtil.builder(new HashMap<String, Object>()).put("cookie", response.getCookieStr()).map();
    }

    @Override
    public String getLanguage(String language) {
        switch (language) {
            case "G++":
                return "0";
            case "GCC":
                return "1";
            case "Java":
                return "2";
            case "Pascal":
                return "3";
            case "C++":
                return "4";
            case "C":
                return "5";
            case "Fortran":
                return "6";
            default:
                // TODO 抛出没有这个语言的异常
                return null;
        }
    }


    public Long getMaxRunId(HttpRequest request, String userName, String problemId) {
        String url = String.format(STATUS_URL, userName, problemId);
        request.setUrl(HOST + url);
        String html = request.execute().body();
        Matcher matcher = Pattern.compile("<tr align=center><td>(\\d+)").matcher(html);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : -1L;
    }


    // TODO 添加结果对应的状态
    private static final Map<String, Constants.Judge> statusMap = new HashMap<String, Constants.Judge>() {
        {
            put("Compiling", Constants.Judge.STATUS_COMPILING);
            put("Accepted", Constants.Judge.STATUS_ACCEPTED);
            put("Presentation Error", Constants.Judge.STATUS_PRESENTATION_ERROR);
            put("Time Limit Exceeded", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
            put("Memory Limit Exceeded", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
            put("Wrong Answer", Constants.Judge.STATUS_WRONG_ANSWER);
            put("Runtime Error", Constants.Judge.STATUS_RUNTIME_ERROR);
            put("Output Limit Exceeded", Constants.Judge.STATUS_RUNTIME_ERROR);
            put("Compile Error", Constants.Judge.STATUS_COMPILE_ERROR);
        }
    };
}
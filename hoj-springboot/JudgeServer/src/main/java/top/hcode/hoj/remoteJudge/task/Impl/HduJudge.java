package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.*;
import lombok.extern.slf4j.Slf4j;

import org.jsoup.helper.Validate;

import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j(topic = "hoj")
public class HduJudge implements RemoteJudgeStrategy {
    public static final String HOST = "https://acm.hdu.edu.cn";
    public static final String LOGIN_URL = "/userloginex.php?action=login";
    public static final String SUBMIT_URL = "/submit.php?action=submit";
    public static final String STATUS_URL = "/status.php?user=%s&pid=%s";
    public static final String QUERY_URL = "/status.php?first=%d";
    public static final String ERROR_URL = "/viewerror.php?rid=%d";
    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("Host", "acm.hdu.edu.cn")
            .put("origin", "https://acm.hdu.edu.cn")
            .put("referer", "https://acm.hdu.edu.cn")
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

        HttpResponse response = request.form(MapUtil
                .builder(new HashMap<String, Object>())
                .put("check", "0")
                .put("language", getLanguage(language))
                .put("problemid", problemId)
                .put("usercode", userCode)
                .map())
                .execute();
        if (response.getStatus() != 200 && response.getStatus() != 302) {
            log.error("进行题目提交时发生错误：提交题目失败，" + HduJudge.class.getName() + "，题号:" + problemId);
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
    public Map<String, Object> result(Long submitId, String username, String password, String cookies) throws Exception {
        String url = HOST + String.format(QUERY_URL, submitId);
        HttpRequest request = HttpUtil.createGet(url)
                .cookie(cookies)
                .addHeaders(headers);
        HttpResponse response = request.execute();
        // 1提交时间 2结果 3执行时间 4执行空间 5代码长度
        // 一般情况下 代码长度和提交时间不需要，想要也行，自行添加
        Pattern pattern = Pattern.compile(">" + submitId + "</td><td>[\\s\\S]*?</td><td>([\\s\\S]*?)</td><td>[\\s\\S]*?</td><td>(\\d*?)MS</td><td>(\\d*?)K</td>");
        Matcher matcher = pattern.matcher(response.body());
        // 找到时
        Validate.isTrue(matcher.find());
        String rawStatus = matcher.group(1).replaceAll("<[\\s\\S]*?>", "").trim();
        Constants.Judge statusType = statusTypeMap.getOrDefault(rawStatus, Constants.Judge.STATUS_PENDING);
        if (statusType == Constants.Judge.STATUS_PENDING) {
            return MapUtil.builder(new HashMap<String, Object>())
                    .put("status", statusType.getStatus()).build();
        }
        // 返回的结果map
        Map<String, Object> result = MapUtil.builder(new HashMap<String, Object>())
                .put("status", statusType.getStatus()).build();
        // 获取其他信息
        String executionTime = matcher.group(2);
        result.put("time", Integer.parseInt(executionTime));
        String executionMemory = matcher.group(3);
        result.put("memory", Integer.parseInt(executionMemory));

        // 如果CE了，则还需要获得错误信息
        if (statusType == Constants.Judge.STATUS_COMPILE_ERROR) {
            request.setUrl(HOST + String.format(ERROR_URL, submitId));
            String CEHtml = request.execute().body();
            String compilationErrorInfo = ReUtil.get("<pre>([\\s\\S]*?)</pre>", CEHtml, 1);
            result.put("CEInfo", HtmlUtil.unescape(compilationErrorInfo));
        }
        return result;
    }


    @Override
    public Map<String, Object> getLoginUtils(String username, String password) {

        HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL).addHeaders(headers);
        HttpResponse response = request.form(MapUtil
                .builder(new HashMap<String, Object>())
                .put("username", username)
                .put("login", "Sign In")
                .put("userpass", password).map())
                .execute();
        return MapUtil.builder(new HashMap<String, Object>()).put("cookie", response.getCookieStr()).map();
    }

    @Override
    public String getLanguage(String language) {
        switch (language) {
            case "G++":
                return "0";
            case "GCC":
                return "1";
            case "C++":
                return "2";
            case "C":
                return "3";
            case "Pascal":
                return "4";
            case "Java":
                return "5";
            case "C#":
                return "6";
            default:
                // TODO 抛出没有这个语言的异常
                return null;
        }
    }


    public Long getMaxRunId(HttpRequest request, String userName, String problemId) {
        String url = HOST + String.format(STATUS_URL, userName, problemId);
        HttpResponse response = HttpUtil.createGet(url).addHeaders(headers).execute();
        String maxRunId = ReUtil.get("<td height=22px>(\\d+)", response.body(), 1);
        return maxRunId != null ? Long.parseLong(maxRunId) : -1L;
    }


    // TODO 添加结果对应的状态
    private static final Map<String, Constants.Judge> statusTypeMap = new HashMap<String, Constants.Judge>() {
        {
            put("Submitted", Constants.Judge.STATUS_PENDING);
            put("Accepted", Constants.Judge.STATUS_ACCEPTED);
            put("Wrong Answer", Constants.Judge.STATUS_WRONG_ANSWER);
            put("Compilation Error", Constants.Judge.STATUS_COMPILE_ERROR);
            put("Queuing", Constants.Judge.STATUS_PENDING);
            put("Running", Constants.Judge.STATUS_JUDGING);
            put("Compiling", Constants.Judge.STATUS_COMPILING);
            put("Time Limit Exceeded", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
            put("Memory Limit Exceeded", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
            put("Presentation Error", Constants.Judge.STATUS_PRESENTATION_ERROR);
        }
    };
}

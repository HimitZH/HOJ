package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Himit_ZH
 * @Date: 2021/6/24 21:19
 * @Description:
 */
@Slf4j(topic = "hoj")
public class POJJudge extends RemoteJudgeStrategy {
    public static final String HOST = "http://poj.org";
    public static final String LOGIN_URL = "/login";
    public static final String SUBMIT_URL = "/submit";
    public static final String STATUS_URL = "/status?user_id=%s&problem_id=%s";
    public static final String QUERY_URL = "/showsource?solution_id=%s";
    public static final String ERROR_URL = "/showcompileinfo?solution_id=%s";
    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .map();

    @Override
    public void submit() {

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        if (remoteJudgeDTO.getCompleteProblemId() == null || remoteJudgeDTO.getUserCode() == null) {
            return;
        }

        login();

        List<HttpCookie> cookies = remoteJudgeDTO.getCookies();

        HttpRequest request = HttpUtil.createPost(HOST + SUBMIT_URL)
                .addHeaders(headers)
                .cookie(cookies);

        HttpResponse response = request.form(MapUtil.builder(new HashMap<String, Object>())
                        .put("language", getLanguage(remoteJudgeDTO.getLanguage()))
                        .put("submit", "Submit")
                        .put("problem_id", remoteJudgeDTO.getCompleteProblemId())
                        .put("source", Base64.encode(remoteJudgeDTO.getUserCode() + getRandomBlankString()))
                        .put("encoded", 1).map())
                .execute();
        remoteJudgeDTO.setSubmitStatus(response.getStatus());
        if (response.getStatus() != 302 && response.getStatus() != 200) {
            String log = String.format("[POJ] [%s]: Failed to submit code, the http response status is [%s].", remoteJudgeDTO.getCompleteProblemId(), response.getStatus());
            throw new RuntimeException(log);
        }
        // 下面的请求都是GET
        request.setMethod(Method.GET);
        // 获取提交的题目id
        Long maxRunId = getMaxRunId(request, remoteJudgeDTO.getUsername(), remoteJudgeDTO.getCompleteProblemId());

        if (maxRunId == -1L) { // 等待2s再次查询，如果还是失败，则表明提交失败了
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            maxRunId = getMaxRunId(request, remoteJudgeDTO.getUsername(), remoteJudgeDTO.getCompleteProblemId());
        }

        remoteJudgeDTO.setCookies(cookies)
                .setSubmitId(maxRunId);
    }

    @Override
    public RemoteJudgeRes result() {

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        List<HttpCookie> cookies = remoteJudgeDTO.getCookies();
        Long submitId = remoteJudgeDTO.getSubmitId();

        if (CollectionUtils.isEmpty(cookies)) {
            login();
            cookies = remoteJudgeDTO.getCookies();
        }
        String url = HOST + String.format(QUERY_URL, submitId);
        HttpRequest request = HttpUtil.createGet(url)
                .cookie(cookies)
                .addHeaders(headers);

        HttpResponse response = request.execute();

        if (!response.body().contains("<b>Result:</b>")) {
            log.error(submitId + " error:{}", response.body());
        }

        String statusStr = ReUtil.get("<b>Result:</b>(.+?)</td>", response.body(), 1)
                .replaceAll("<.*?>", "")
                .trim();

        Constants.Judge judgeStatus = statusMap.get(statusStr);

        if (judgeStatus == null) {
            return RemoteJudgeRes.builder()
                    .status(Constants.Judge.STATUS_PENDING.getStatus())
                    .build();
        }

        RemoteJudgeRes remoteJudgeRes = RemoteJudgeRes.builder()
                .status(judgeStatus.getStatus())
                .build();

        // 如果CE了，需要获得错误信息
        if (judgeStatus == Constants.Judge.STATUS_COMPILE_ERROR) {
            request.setUrl(HOST + String.format(ERROR_URL, submitId));
            String CEHtml = request.execute().body();
            String compilationErrorInfo = ReUtil.get("<pre>([\\s\\S]*?)</pre>", CEHtml, 1);
            remoteJudgeRes.setErrorInfo(HtmlUtil.unescape(compilationErrorInfo));
        } else {
            // 如果不是CE,获取其他信息
            String executionMemory = ReUtil.get("<b>Memory:</b> ([-\\d]+)", response.body(), 1);
            remoteJudgeRes.setMemory(executionMemory == null ? null : Integer.parseInt(executionMemory));
            String executionTime = ReUtil.get("<b>Time:</b> ([-\\d]+)", response.body(), 1);
            remoteJudgeRes.setTime(executionTime == null ? null : Integer.parseInt(executionTime));
        }
        return remoteJudgeRes;
    }

    @Override
    public void login() {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL);
        HttpResponse response = request.form(MapUtil.builder(new HashMap<String, Object>())
                .put("user_id1", remoteJudgeDTO.getUsername())
                .put("B1", "login")
                .put("url", ".")
                .put("password1", remoteJudgeDTO.getPassword()).map()).execute();

        if (response.getStatus() != 302) {
            throw new RuntimeException("[POJ] Failed to login! The possible cause is connection failure, and the returned status code is " + response.getStatus());
        }

        HttpRequest homeRequest = HttpUtil.createGet(HOST);
        homeRequest.cookie(response.getCookies());
        HttpResponse homeResponse = homeRequest.execute();
        String body = homeResponse.body();
        if (!body.contains(remoteJudgeDTO.getUsername()) || !body.contains("Log Out")) {
            throw new RuntimeException("[POJ] Failed to login! The possible cause is wrong account or password, account: " + remoteJudgeDTO.getUsername());
        }
        remoteJudgeDTO.setCookies(response.getCookies())
                .setLoginStatus(response.getStatus());
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
            put("Running & Judging", Constants.Judge.STATUS_JUDGING);
            put("Presentation Error", Constants.Judge.STATUS_PRESENTATION_ERROR);
            put("Time Limit Exceeded", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
            put("Memory Limit Exceeded", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
            put("Wrong Answer", Constants.Judge.STATUS_WRONG_ANSWER);
            put("Runtime Error", Constants.Judge.STATUS_RUNTIME_ERROR);
            put("Output Limit Exceeded", Constants.Judge.STATUS_RUNTIME_ERROR);
            put("Compile Error", Constants.Judge.STATUS_COMPILE_ERROR);
        }
    };

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
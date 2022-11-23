package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.Validate;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;

import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j(topic = "hoj")
public class HDUJudge extends RemoteJudgeStrategy {
    public static final String HOST = "http://acm.hdu.edu.cn";
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
                .form(MapUtil
                        .builder(new HashMap<String, Object>())
                        .put("check", "0")
                        .put("language", getLanguage(remoteJudgeDTO.getLanguage()))
                        .put("problemid", remoteJudgeDTO.getCompleteProblemId())
                        .put("_usercode", Base64.encode(URLEncoder.encode(remoteJudgeDTO.getUserCode() + getRandomBlankString())))
                        .map())
                .cookie(cookies);

        HttpResponse response = request.execute();
        remoteJudgeDTO.setSubmitStatus(response.getStatus());
        // 提交频率限制了 等待5秒再次提交
        if (response.getStatus() == 200 && response.body() != null && response.body().contains("Please don't re-submit")) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            response = request.execute();
            remoteJudgeDTO.setSubmitStatus(response.getStatus());
            if (response.getStatus() != 302) {
                String log = String.format("[HDU] [%s]: Failed to submit code, the http response status is [%s].", remoteJudgeDTO.getCompleteProblemId(), response.getStatus());
                throw new RuntimeException(log);
            }
        } else if (response.getStatus() != 302) {
            String log = String.format("[HDU] [%s]: Failed to submit code, the http response status is [%s].", remoteJudgeDTO.getCompleteProblemId(), response.getStatus());
            throw new RuntimeException(log);
        }
        // 获取提交的题目id
        Long maxRunId = getMaxRunId(remoteJudgeDTO.getUsername(), remoteJudgeDTO.getCompleteProblemId());
        if (maxRunId == -1L) { // 等待2s再次查询，如果还是失败，则表明提交失败了
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            maxRunId = getMaxRunId(remoteJudgeDTO.getUsername(), remoteJudgeDTO.getCompleteProblemId());
        }
        remoteJudgeDTO.setCookies(cookies)
                .setSubmitId(maxRunId);
    }

    @Override
    public RemoteJudgeRes result() {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        String url = HOST + String.format(QUERY_URL, remoteJudgeDTO.getSubmitId());
        HttpRequest request = HttpUtil.createGet(url)
                .cookie(remoteJudgeDTO.getCookies())
                .addHeaders(headers);
        HttpResponse response = request.execute();
        // 1提交时间 2结果 3执行时间 4执行空间 5代码长度
        // 一般情况下 代码长度和提交时间不需要，想要也行，自行添加
        Pattern pattern = Pattern.compile(">" + remoteJudgeDTO.getSubmitId() + "</td><td>[\\s\\S]*?</td><td>([\\s\\S]*?)</td><td>[\\s\\S]*?</td><td>(\\d*?)MS</td><td>(\\d*?)K</td>");
        Matcher matcher = pattern.matcher(response.body());
        // 找到时
        Validate.isTrue(matcher.find());
        String rawStatus = matcher.group(1).replaceAll("<[\\s\\S]*?>", "").trim();
        Constants.Judge judgeStatus;
        if (rawStatus.contains("Runtime Error")){
            judgeStatus = Constants.Judge.STATUS_RUNTIME_ERROR;
        }else{
            judgeStatus = statusTypeMap.getOrDefault(rawStatus, Constants.Judge.STATUS_PENDING);
        }
        RemoteJudgeRes remoteJudgeRes = RemoteJudgeRes.builder()
                .status(judgeStatus.getStatus())
                .build();

        if (judgeStatus == Constants.Judge.STATUS_PENDING) {
            return remoteJudgeRes;
        }


        // 获取其他信息
        String executionTime = matcher.group(2);
        remoteJudgeRes.setTime(Integer.parseInt(executionTime));
        String executionMemory = matcher.group(3);
        remoteJudgeRes.setMemory(Integer.parseInt(executionMemory));

        // 如果CE了，则还需要获得错误信息
        if (judgeStatus == Constants.Judge.STATUS_COMPILE_ERROR) {
            request.setUrl(HOST + String.format(ERROR_URL, remoteJudgeDTO.getSubmitId()));
            String CEHtml = request.execute().body();
            String compilationErrorInfo = ReUtil.get("<pre>([\\s\\S]*?)</pre>", CEHtml, 1);
            remoteJudgeRes.setErrorInfo(HtmlUtil.unescape(compilationErrorInfo));
        }
        return remoteJudgeRes;
    }


    @Override
    public void login() {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL).addHeaders(headers);
        HttpResponse response = request.form(MapUtil
                        .builder(new HashMap<String, Object>())
                        .put("username", remoteJudgeDTO.getUsername())
                        .put("login", "Sign In")
                        .put("userpass", remoteJudgeDTO.getPassword()).map())
                .execute();
        if (response.getStatus() != 302) {
            throw new RuntimeException("[HDU] Failed to login! The possible cause is connection failure, and the returned status code is " + response.getStatus());
        }
        remoteJudgeDTO.setLoginStatus(response.getStatus());
        remoteJudgeDTO.setCookies(response.getCookies());
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


    public Long getMaxRunId(String userName, String problemId) {
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
            put("Runtime Error", Constants.Judge.STATUS_RUNTIME_ERROR);
            put("Time Limit Exceeded", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
            put("Memory Limit Exceeded", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
            put("Output Limit Exceeded", Constants.Judge.STATUS_RUNTIME_ERROR);
            put("Presentation Error", Constants.Judge.STATUS_PRESENTATION_ERROR);
        }
    };

    protected static String getRandomBlankString() {
        StringBuilder string = new StringBuilder("\n");
        int random = new Random().nextInt(Integer.MAX_VALUE);
        while (random > 0) {
            string.append(random % 2 == 0 ? ' ' : '\t');
            random /= 2;
        }
        return string.toString();
    }
}

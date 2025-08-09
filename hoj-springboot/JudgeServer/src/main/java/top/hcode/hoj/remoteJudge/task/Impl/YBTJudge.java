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
public class YBTJudge extends RemoteJudgeStrategy {
    // half done 
    // anyone continue this work , refer to https://github.com/zhblue/hustoj/blob/master/trunk/web/include/remote_bas.php
    public static final String HOST = "http://www.ssoier.cn:18087/pubtest";
    public static final String LOGIN_URL = "";
    public static final String SUBMIT_URL = "/acx.php";
    public static final String STATUS_URL = "";
    public static final String QUERY_URL = "/stux.php?first=%d";
    public static final String ERROR_URL = "";
    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("Host", "www.ssoier.cn")
            .put("origin", "http://www.ssoier.cn:18087/pubtest")
            .put("referer", "http://www.ssoier.cn:18087/pubtest")
            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .map();

    @Override
    public void submit() {

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        if (remoteJudgeDTO.getCompleteProblemId() == null || remoteJudgeDTO.getUserCode() == null) {
            return;
        }

      //  login(); // YBT no need

        List<HttpCookie> cookies = remoteJudgeDTO.getCookies();

        HttpRequest request = HttpUtil.createPost(HOST + SUBMIT_URL)
                .addHeaders(headers)
                .form(MapUtil
                        .builder(new HashMap<String, Object>())
                        .put("user_id",  remoteJudgeDTO.getUsername())
                        .put("password",  remoteJudgeDTO.getPassword()).map())  // map() ? not sure
                        .put("language", getLanguage(remoteJudgeDTO.getLanguage()))
                        .put("problem_id", remoteJudgeDTO.getCompleteProblemId())
                        .put("data_id", "bas")
                        .put("submit", "提交")
                        .put("source", Base64.encode(URLEncoder.encode(remoteJudgeDTO.getUserCode() + getRandomBlankString())))
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
                String log = String.format("[YBT] [%s]: Failed to submit code, the http response status is [%s].", remoteJudgeDTO.getCompleteProblemId(), response.getStatus());
                throw new RuntimeException(log);
            }
        } else if (response.getStatus() != 302) {
            String log = String.format("[YBT] [%s]: Failed to submit code, the http response status is [%s].", remoteJudgeDTO.getCompleteProblemId(), response.getStatus());
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
    public RemoteJudgeRes result() {   // YBT will need add login info here , change GET to POST, please
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
                        .put("user_id",  remoteJudgeDTO.getUsername())
                        .put("password",  remoteJudgeDTO.getPassword()).map())  // map() ? not sure
                        .map())
                .execute();
        if (response.getStatus() != 302) {
            throw new RuntimeException("[YBT] not need login! submit user/pass with solution " + response.getStatus());
        }
        remoteJudgeDTO.setLoginStatus(response.getStatus());
        remoteJudgeDTO.setCookies(response.getCookies());
    }


    @Override
    public String getLanguage(String language) {
        switch (language) {
            case "G++":
                return "7";
            case "GCC":
                return "7";
            case "C++":
                return "7";
            case "C":
                return "7";
            case "Python":
                return "5";
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
            put("AC", Constants.Judge.STATUS_ACCEPTED);
            put("WA", Constants.Judge.STATUS_WRONG_ANSWER);
            put("CE", Constants.Judge.STATUS_COMPILE_ERROR);
            put("RE", Constants.Judge.STATUS_RUNTIME_ERROR);
            put("RF", Constants.Judge.STATUS_RUNTIME_ERROR);
            put("TLE", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
            put("MLE", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
            put("OLE", Constants.Judge.STATUS_RUNTIME_ERROR);
            put("PE", Constants.Judge.STATUS_PRESENTATION_ERROR);
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

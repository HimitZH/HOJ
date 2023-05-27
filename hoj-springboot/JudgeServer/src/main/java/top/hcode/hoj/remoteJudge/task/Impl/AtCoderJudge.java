package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/28 23:20
 * @Description:
 */
@Slf4j(topic = "hoj")
public class AtCoderJudge extends RemoteJudgeStrategy {

    public static final String HOST = "https://atcoder.jp";
    public static final String LOGIN_URL = "/login";
    public static final String SUBMIT_URL = "/contests/%s/submit";
    public static final String SUBMISSION_RESULT_URL = "/contests/%s/submissions/%s";

    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .map();

    private static final Map<String, Constants.Judge> statusMap = new HashMap<String, Constants.Judge>() {{
        put("CE", Constants.Judge.STATUS_COMPILE_ERROR);
        put("RE", Constants.Judge.STATUS_RUNTIME_ERROR);
        put("QLE", Constants.Judge.STATUS_RUNTIME_ERROR);
        put("OLE", Constants.Judge.STATUS_RUNTIME_ERROR);
        put("IE", Constants.Judge.STATUS_RUNTIME_ERROR);
        put("WA", Constants.Judge.STATUS_WRONG_ANSWER);
        put("AC", Constants.Judge.STATUS_ACCEPTED);
        put("TLE", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
        put("MLE", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED);
        put("WJ", Constants.Judge.STATUS_PENDING);
        put("WR", Constants.Judge.STATUS_PENDING); // Waiting Rejudge
        put("Judging", Constants.Judge.STATUS_JUDGING); // Waiting Rejudge
    }};

    private static final Map<String, String> languageMap = new HashMap<>();

    @Override
    public void submit() {
        login();
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        if (remoteJudgeDTO.getLoginStatus() != 302) {
            log.error("Login to AtCoder failed, the response status:{},username:{},password:{}",
                    remoteJudgeDTO.getLoginStatus(), remoteJudgeDTO.getUsername(), remoteJudgeDTO.getPassword());
            throw new RuntimeException("[AtCoder] Failed to Login, the response status:" + remoteJudgeDTO.getLoginStatus());
        }

        HttpResponse response = trySubmit();

        if (response.getStatus() == 200) { // 说明被限制提交频率了，
            String timeStr = ReUtil.get("Wait for (\\d+) second to submit again.", response.body(), 1);
            if (timeStr != null) {
                int time = Integer.parseInt(timeStr);
                try {
                    TimeUnit.SECONDS.sleep(time + 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                response = trySubmit();
            }
        }

        if (response.getStatus() != 302) {
            log.error("Submit to AtCoder failed, the response status:{}, It may be that the frequency of submission operation is too fast. Please try later", response.getStatus());
            throw new RuntimeException("[AtCoder] Failed to Submit, the response status:" + response.getStatus());
        }

        // 停留3秒钟后再获取id，之后归还账号，避免提交频率过快
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Long maxRunId = getMaxRunId(remoteJudgeDTO.getUsername(), remoteJudgeDTO.getContestId(), remoteJudgeDTO.getCompleteProblemId());

        remoteJudgeDTO.setCookies(remoteJudgeDTO.getCookies())
                .setSubmitId(maxRunId);

    }

    private HttpResponse trySubmit() {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        List<HttpCookie> cookies = remoteJudgeDTO.getCookies();
        String csrfToken = remoteJudgeDTO.getCsrfToken();

        String submitUrl = HOST + String.format(SUBMIT_URL, remoteJudgeDTO.getContestId());
        HttpRequest request = HttpUtil.createPost(submitUrl);
        HttpRequest httpRequest = request.form(MapUtil.builder(new HashMap<String, Object>())
                .put("data.TaskScreenName", remoteJudgeDTO.getCompleteProblemId())
                .put("data.LanguageId", getLanguage(remoteJudgeDTO.getLanguage()))
                .put("sourceCode", remoteJudgeDTO.getUserCode())
                .put("csrf_token", csrfToken).map());
        httpRequest.cookie(cookies);
        HttpResponse response = httpRequest.execute();
        remoteJudgeDTO.setSubmitStatus(response.getStatus());
        return response;
    }

    @Override
    public RemoteJudgeRes result() {

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        String url = HOST + String.format(SUBMISSION_RESULT_URL, remoteJudgeDTO.getContestId(), remoteJudgeDTO.getSubmitId());
        String body = HttpUtil.get(url);
        String status = ReUtil.get("<th>Status</th>[\\s\\S]*?<td id=\"judge-status\" class=\"[\\s\\S]*?\"><span [\\s\\S]*?>([\\s\\S]*?)</span></td>", body, 1);
        Constants.Judge judgeStatus = statusMap.get(status);
        if (judgeStatus == Constants.Judge.STATUS_JUDGING || judgeStatus == Constants.Judge.STATUS_PENDING) {
            return RemoteJudgeRes.builder()
                    .status(judgeStatus.getStatus())
                    .build();
        }

        String time = ReUtil.get("<th>Exec Time</th>[\\s\\S]*?<td [\\s\\S]*?>([\\s\\S]*?) ms</td>", body, 1);
        String memory = ReUtil.get("<th>Memory</th>[\\s\\S]*?<td [\\s\\S]*?>([\\s\\S]*?) KB</td>", body, 1);

        RemoteJudgeRes remoteJudgeRes = RemoteJudgeRes.builder()
                .status(judgeStatus.getStatus())
                .time(time == null ? null : Integer.parseInt(time))
                .memory(memory == null ? null : Integer.parseInt(memory))
                .build();
        if (judgeStatus == Constants.Judge.STATUS_COMPILE_ERROR) {
            String CEInfo = ReUtil.get("<h4>Compile Error</h4>[\\s\\S]*?<pre>([\\s\\S]*?)</pre>", body, 1);
            remoteJudgeRes.setErrorInfo(HtmlUtil.unescape(CEInfo));
        }
        return remoteJudgeRes;
    }

    @Override
    public void login() {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        String csrfToken = getCsrfToken(HOST + LOGIN_URL);
        HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL);
        request.addHeaders(headers);
        HttpResponse response = request.form(MapUtil.builder(new HashMap<String, Object>())
                .put("username", remoteJudgeDTO.getUsername())
                .put("password", remoteJudgeDTO.getPassword())
                .put("csrf_token", csrfToken).map()).execute();

        remoteJudgeDTO.setLoginStatus(response.getStatus())
                .setCookies(response.getCookies())
                .setCsrfToken(csrfToken);
    }

    @Override
    public String getLanguage(String language) {
        return languageMap.get(language);
    }

    private Long getMaxRunId(String username, String contestId, String problemId) {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        String url = HOST + String.format("/contests/%s/submissions?f.Task=%s&f.User=%s", contestId, problemId, username);
        HttpRequest httpRequest = HttpUtil.createGet(url);
        httpRequest.cookie(remoteJudgeDTO.getCookies());
        httpRequest.addHeaders(headers);
        String body = httpRequest.execute().body();
        String maxRunId = ReUtil.get("<a href=\"/contests/" + contestId + "/submissions/(\\d+)\">Detail</a>", body, 1);
        return maxRunId != null ? Long.parseLong(maxRunId) : -1L;
    }

    private String getCsrfToken(String url) {
        HttpRequest request = HttpUtil.createGet(url);
        request.addHeaders(headers);
        HttpResponse response = request.execute();
        String body = response.body();
        return ReUtil.get("var csrfToken = \"([\\s\\S]*?)\"", body, 1);
    }

    static {
        languageMap.put("C (GCC 9.2.1)", "4001");
        languageMap.put("C (Clang 10.0.0)", "4002");
        languageMap.put("C++ (GCC 9.2.1)", "4003");
        languageMap.put("C++ (Clang 10.0.0)", "4004");
        languageMap.put("Java (OpenJDK 11.0.6)", "4005");
        languageMap.put("Python (3.8.2)", "4006");
        languageMap.put("Bash (5.0.11)", "4007");
        languageMap.put("bc (1.07.1)", "4008");
        languageMap.put("Awk (GNU Awk 4.1.4)", "4009");
        languageMap.put("C# (.NET Core 3.1.201)", "4010");
        languageMap.put("C# (Mono-mcs 6.8.0.105)", "4011");
        languageMap.put("C# (Mono-csc 3.5.0)", "4012");
        languageMap.put("Clojure (1.10.1.536)", "4013");
        languageMap.put("Crystal (0.33.0)", "4014");
        languageMap.put("D (DMD 2.091.0)", "4015");
        languageMap.put("D (GDC 9.2.1)", "4016");
        languageMap.put("D (LDC 1.20.1)", "4017");
        languageMap.put("Dart (2.7.2)", "4018");
        languageMap.put("dc (1.4.1)", "4019");
        languageMap.put("Erlang (22.3)", "4020");
        languageMap.put("Elixir (1.10.2)", "4021");
        languageMap.put("F# (.NET Core 3.1.201)", "4022");
        languageMap.put("F# (Mono 10.2.3)", "4023");
        languageMap.put("Forth (gforth 0.7.3)", "4024");
        languageMap.put("Fortran (GNU Fortran 9.2.1)", "4025");
        languageMap.put("Go (1.14.1)", "4026");
        languageMap.put("Haskell (GHC 8.8.3)", "4027");
        languageMap.put("Haxe (4.0.3); js", "4028");
        languageMap.put("Haxe (4.0.3); Java", "4029");
        languageMap.put("JavaScript (Node.js 12.16.1)", "4030");
        languageMap.put("Julia (1.4.0)", "4031");
        languageMap.put("Kotlin (1.3.71)", "4032");
        languageMap.put("Lua (Lua 5.3.5)", "4033");
        languageMap.put("Lua (LuaJIT 2.1.0)", "4034");
        languageMap.put("Dash (0.5.8)", "4035");
        languageMap.put("Nim (1.0.6)", "4036");
        languageMap.put("Objective-C (Clang 10.0.0)", "4037");
        languageMap.put("Common Lisp (SBCL 2.0.3)", "4038");
        languageMap.put("OCaml (4.10.0)", "4039");
        languageMap.put("Octave (5.2.0)", "4040");
        languageMap.put("Pascal (FPC 3.0.4)", "4041");
        languageMap.put("Perl (5.26.1)", "4042");
        languageMap.put("Raku (Rakudo 2020.02.1)", "4043");
        languageMap.put("PHP (7.4.4)", "4044");
        languageMap.put("Prolog (SWI-Prolog 8.0.3)", "4045");
        languageMap.put("PyPy2 (7.3.0)", "4046");
        languageMap.put("PyPy3 (7.3.0)", "4047");
        languageMap.put("Racket (7.6)", "4048");
        languageMap.put("Ruby (2.7.1)", "4049");
        languageMap.put("Rust (1.42.0)", "4050");
        languageMap.put("Scala (2.13.1)", "4051");
        languageMap.put("Java (OpenJDK 1.8.0)", "4052");
        languageMap.put("Scheme (Gauche 0.9.9)", "4053");
        languageMap.put("Standard ML (MLton 20130715)", "4054");
        languageMap.put("Swift (5.2.1)", "4055");
        languageMap.put("Text (cat 8.28)", "4056");
        languageMap.put("TypeScript (3.8)", "4057");
        languageMap.put("Visual Basic (.NET Core 3.1.101)", "4058");
        languageMap.put("Zsh (5.4.2)", "4059");
        languageMap.put("COBOL - Fixed (OpenCOBOL 1.1.0)", "4060");
        languageMap.put("COBOL - Free (OpenCOBOL 1.1.0)", "4061");
        languageMap.put("Brainfuck (bf 20041219)", "4062");
        languageMap.put("Ada2012 (GNAT 9.2.1)", "4063");
        languageMap.put("Unlambda (2.0.0)", "4064");
        languageMap.put("Cython (0.29.16)", "4065");
        languageMap.put("Sed (4.4)", "4066");
        languageMap.put("Vim (8.2.0460)", "4067");
    }
}
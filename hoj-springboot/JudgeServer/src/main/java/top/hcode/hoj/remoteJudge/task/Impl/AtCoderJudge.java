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
        languageMap.put("C++ 20 (gcc 12.2)", "5001");
        languageMap.put("Go (go 1.20.6)", "5002");
        languageMap.put("C# 11.0 (.NET 7.0.7)", "5003");
        languageMap.put("Kotlin (Kotlin/JVM 1.8.20)", "5004");
        languageMap.put("Java (OpenJDK 17)", "5005");
        languageMap.put("Nim (Nim 1.6.14)", "5006");
        languageMap.put("V (V 0.4)", "5007");
        languageMap.put("Zig (Zig 0.10.1)", "5008");
        languageMap.put("JavaScript (Node.js 18.16.1)", "5009");
        languageMap.put("JavaScript (Deno 1.35.1)", "5010");
        languageMap.put("R (GNU R 4.2.1)", "5011");
        languageMap.put("D (DMD 2.104.0)", "5012");
        languageMap.put("D (LDC 1.32.2)", "5013");
        languageMap.put("Swift (swift 5.8.1)", "5014");
        languageMap.put("Dart (Dart 3.0.5)", "5015");
        languageMap.put("PHP (php 8.2.8)", "5016");
        languageMap.put("C (gcc 12.2.0)", "5017");
        languageMap.put("Ruby (ruby 3.2.2)", "5018");
        languageMap.put("Crystal (Crystal 1.9.1)", "5019");
        languageMap.put("Brainfuck (bf 20041219)", "5020");
        languageMap.put("F# 7.0 (.NET 7.0.7)", "5021");
        languageMap.put("Julia (Julia 1.9.2)", "5022");
        languageMap.put("Bash (bash 5.2.2)", "5023");
        languageMap.put("Text (cat 8.32)", "5024");
        languageMap.put("Haskell (GHC 9.4.5)", "5025");
        languageMap.put("Fortran (gfortran 12.2)", "5026");
        languageMap.put("Lua (LuaJIT 2.1.0-beta3)", "5027");
        languageMap.put("C++ 23 (gcc 12.2)", "5028");
        languageMap.put("Common Lisp (SBCL 2.3.6)", "5029");
        languageMap.put("COBOL (Free) (GnuCOBOL 3.1.2)", "5030");
        languageMap.put("C++ 23 (Clang 16.0.6)", "5031");
        languageMap.put("Zsh (Zsh 5.9)", "5032");
        languageMap.put("SageMath (SageMath 9.5)", "5033");
        languageMap.put("Sed (GNU sed 4.8)", "5034");
        languageMap.put("bc (bc 1.07.1)", "5035");
        languageMap.put("dc (dc 1.07.1)", "5036");
        languageMap.put("Perl (perl  5.34)", "5037");
        languageMap.put("AWK (GNU Awk 5.0.1)", "5038");
        languageMap.put("なでしこ (cnako3 3.4.20)", "5039");
        languageMap.put("Assembly x64 (NASM 2.15.05)", "5040");
        languageMap.put("Pascal (fpc 3.2.2)", "5041");
        languageMap.put("C# 11.0 AOT (.NET 7.0.7)", "5042");
        languageMap.put("Lua (Lua 5.4.6)", "5043");
        languageMap.put("Prolog (SWI-Prolog 9.0.4)", "5044");
        languageMap.put("PowerShell (PowerShell 7.3.1)", "5045");
        languageMap.put("Scheme (Gauche 0.9.12)", "5046");
        languageMap.put("Scala 3.3.0 (Scala Native 0.4.14)", "5047");
        languageMap.put("Visual Basic 16.9 (.NET 7.0.7)", "5048");
        languageMap.put("Forth (gforth 0.7.3)", "5049");
        languageMap.put("Clojure (babashka 1.3.181)", "5050");
        languageMap.put("Erlang (Erlang 26.0.2)", "5051");
        languageMap.put("TypeScript 5.1 (Deno 1.35.1)", "5052");
        languageMap.put("C++ 17 (gcc 12.2)", "5053");
        languageMap.put("Rust (rustc 1.70.0)", "5054");
        languageMap.put("Python (CPython 3.11.4)", "5055");
        languageMap.put("Scala (Dotty 3.3.0)", "5056");
        languageMap.put("Koka (koka 2.4.0)", "5057");
        languageMap.put("TypeScript 5.1 (Node.js 18.16.1)", "5058");
        languageMap.put("OCaml (ocamlopt 5.0.0)", "5059");
        languageMap.put("Raku (Rakudo 2023.06)", "5060");
        languageMap.put("Vim (vim 9.0.0242)", "5061");
        languageMap.put("Emacs Lisp (Native Compile) (GNU Emacs 28.2)", "5062");
        languageMap.put("Python (Mambaforge / CPython 3.10.10)", "5063");
        languageMap.put("Clojure (clojure 1.11.1)", "5064");
        languageMap.put("プロデル (mono版プロデル 1.9.1182)", "5065");
        languageMap.put("ECLiPSe (ECLiPSe 7.1_13)", "5066");
        languageMap.put("Nibbles (literate form) (nibbles 1.01)", "5067");
        languageMap.put("Ada (GNAT 12.2)", "5068");
        languageMap.put("jq (jq 1.6)", "5069");
        languageMap.put("Cyber (Cyber v0.2-Latest)", "5070");
        languageMap.put("Carp (Carp 0.5.5)", "5071");
        languageMap.put("C++ 17 (Clang 16.0.6)", "5072");
        languageMap.put("C++ 20 (Clang 16.0.6)", "5073");
        languageMap.put("LLVM IR (Clang 16.0.6)", "5074");
        languageMap.put("Emacs Lisp (Byte Compile) (GNU Emacs 28.2)", "5075");
        languageMap.put("Factor (Factor 0.98)", "5076");
        languageMap.put("D (GDC 12.2)", "5077");
        languageMap.put("Python (PyPy 3.10-v7.3.12)", "5078");
        languageMap.put("Whitespace (whitespacers 1.0.0)", "5079");
        languageMap.put("><> (fishr 0.1.0)", "5080");
        languageMap.put("ReasonML (reason 3.9.0)", "5081");
        languageMap.put("Python (Cython 0.29.34)", "5082");
        languageMap.put("Octave (GNU Octave 8.2.0)", "5083");
        languageMap.put("Haxe (JVM) (Haxe 4.3.1)", "5084");
        languageMap.put("Elixir (Elixir 1.15.2)", "5085");
        languageMap.put("Mercury (Mercury 22.01.6)", "5086");
        languageMap.put("Seed7 (Seed7 3.2.1)", "5087");
        languageMap.put("Emacs Lisp (No Compile) (GNU Emacs 28.2)", "5088");
        languageMap.put("Unison (Unison M5b)", "5089");
        languageMap.put("COBOL (GnuCOBOL(Fixed) 3.1.2)", "5090");
    }
}
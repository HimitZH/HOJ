package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.Validate;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/27 23:19
 * @Description:
 */
@Slf4j(topic = "hoj")
public class SPOJJudge extends RemoteJudgeStrategy {

    public static final String HOST = "https://www.spoj.com";
    public static final String LOGIN_URL = "/login/";
    public static final String SUBMIT_URL = "/submit/complete/";
    public static final String SUBMISSION_RESULT_URL = "/status/ajax=1,ajaxdiff=1";
    public static final String CE_INFO_URL = "/error/%s";

    private static final Map<String, Constants.Judge> statusMap = new HashMap<String, Constants.Judge>() {{
        put("11", Constants.Judge.STATUS_COMPILE_ERROR);
        put("12", Constants.Judge.STATUS_RUNTIME_ERROR);
        put("13", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
        put("14", Constants.Judge.STATUS_WRONG_ANSWER);
        put("15", Constants.Judge.STATUS_ACCEPTED);
    }};

    private static final Map<String, String> languageMap = new HashMap<>();


    @Override
    public void submit() {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        if (remoteJudgeDTO.getCompleteProblemId() == null || remoteJudgeDTO.getUserCode() == null) {
            return;
        }
        login();

        List<HttpCookie> cookies = remoteJudgeDTO.getCookies();

        HttpRequest request = HttpUtil.createPost(HOST + SUBMIT_URL)
                .cookie(cookies);

        HttpResponse response = request.form(MapUtil
                .builder(new HashMap<String, Object>())
                .put("lang", getLanguage(remoteJudgeDTO.getLanguage()))
                .put("problemcode", remoteJudgeDTO.getCompleteProblemId())
                .put("file", remoteJudgeDTO.getUserCode())
                .map())
                .execute();
        remoteJudgeDTO.setSubmitStatus(response.getStatus());
        if (response.body().contains("submit in this language for this problem")) {
            throw new RuntimeException("Language Error");
        } else if (response.body().contains("Wrong problem code!")) {
            throw new RuntimeException("Wrong problem code!");
        } else if (response.body().contains("solution is too long")) {
            throw new RuntimeException("Code Length Exceeded");
        }

        String runId = ReUtil.get("name=\"newSubmissionId\" value=\"(\\d+)\"", response.body(), 1);
        if (runId == null) {
            remoteJudgeDTO.setSubmitId(-1L);
        } else {
            remoteJudgeDTO.setSubmitId(Long.parseLong(runId));
        }
    }

    @Override
    public RemoteJudgeRes result() {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        List<HttpCookie> cookies = remoteJudgeDTO.getCookies();
        Long submitId = remoteJudgeDTO.getSubmitId();

        String url = HOST + SUBMISSION_RESULT_URL;
        HttpResponse response = HttpUtil.createPost(url)
                .cookie(cookies)
                .form(MapUtil.builder(new HashMap<String, Object>())
                        .put("ids", submitId).map())
                .execute();

        String html = response.body();
        html = html.replaceAll("\\\\[nt]", "").replaceAll(">(run|edit|ideone it)<", "><")
                .replaceAll("<.*?>", "").replace("&nbsp;", "")
                .replaceAll("\n","")
                .trim();


        Pattern pattern = Pattern.compile("\"status_description\":\"[\\s\\S]*?\", \"id\":" + submitId +
                ", \"status\":([\\s\\S]*?),\"time\":\"([\\s\\S]*?)\",\"mem\":\"([\\s\\S]*?)\",\"final\":\"([\\s\\S]*?)\"");
        Matcher matcher = pattern.matcher(html);
        Validate.isTrue(matcher.find());

        String finalTag = matcher.group(4).trim();
        if (!"1".equals(finalTag)) {
            return RemoteJudgeRes.builder()
                    .status(Constants.Judge.STATUS_PENDING.getStatus())
                    .build();
        }

        String rawStatus = matcher.group(1).trim();
        String rawTime = matcher.group(2).trim();
        String rawMemory = matcher.group(3).trim();

        Constants.Judge judgeResult = statusMap.get(rawStatus);

        RemoteJudgeRes remoteJudgeRes = RemoteJudgeRes.builder()
                .status(judgeResult.getStatus())
                .build();

        if (!rawMemory.equals("-")) {
            int mul = matcher.group(3).contains("M") ? 1024 : 1;
            int memory = (int) (0.5 + mul * Double.parseDouble(rawMemory.replaceAll("[Mk]", "").trim()));
            remoteJudgeRes.setMemory(memory);
        }
        if (!rawTime.equals("-")) {
            int time = (int) (0.5 + 1000 * Double.parseDouble(rawTime));
            remoteJudgeRes.setTime(time);
        }
        if (judgeResult.equals(Constants.Judge.STATUS_COMPILE_ERROR)) {
            String errorInfoUrl = HOST + String.format(CE_INFO_URL, submitId);
            HttpRequest request = HttpUtil.createGet(errorInfoUrl);
            html = request.cookie(cookies).execute().body();
            String errorInfo = ReUtil.get("<div align=\"left\"><pre><small>([\\s\\S]*?)</small></pre>", html, 1);
            remoteJudgeRes.setErrorInfo(errorInfo);
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
                .put("login_user", remoteJudgeDTO.getUsername())
                .put("autologin", "1")
                .put("submit", "Log In")
                .put("password", remoteJudgeDTO.getPassword()).map()).execute();
        remoteJudgeDTO.setLoginStatus(response.getStatus())
                .setCookies(response.getCookies());
    }

    @Override
    public String getLanguage(String language) {
        if (language.equals("Perl (perl 2018.12)")) {
            return languageMap.get(language);
        }
        String key = language.substring(0, language.lastIndexOf(" ")).trim();
        return languageMap.get(key);
    }


    static {
        languageMap.put("Ada95 (gnat", "7");
        languageMap.put("Assembler 32 (gcc", "45");
        languageMap.put("Assembler 32 (nasm", "13");
        languageMap.put("Assembler 64 (nasm)", "42");
        languageMap.put("AWK (gawk", "104");
        languageMap.put("AWK (mawk", "105");
        languageMap.put("Bash (bash", "28");
        languageMap.put("BC (bc", "110");
        languageMap.put("Brainf**k (bff", "12");
        languageMap.put("C (clang", "81");
        languageMap.put("C (gcc", "11");
        languageMap.put("C# (gmcs", "27");
        languageMap.put("C++ (g++", "41");
        languageMap.put("C++ (gcc", "1");
        languageMap.put("C++14 (gcc", "44");
        languageMap.put("C++14 (clang", "82");
        languageMap.put("C99 (gcc", "34");
        languageMap.put("Clips (clips", "14");
        languageMap.put("Clojure (clojure", "111");
        languageMap.put("Cobol (gnucobol", "118");
        languageMap.put("CoffeeScript (coffee", "91");
        languageMap.put("Common Lisp (sbcl", "31");
        languageMap.put("Common Lisp (clisp", "32");
        languageMap.put("D (dmd", "102");
        languageMap.put("D (ldc", "84");
        languageMap.put("D (gdc", "20");
        languageMap.put("Dart (dart", "48");
        languageMap.put("Elixir (elixir", "96");
        languageMap.put("Erlang (erl", "36");
        languageMap.put("F# (mono", "124");
        languageMap.put("Fantom (fantom", "92");
        languageMap.put("Forth (gforth", "107");
        languageMap.put("Fortran (gfortran", "5");
        languageMap.put("Go (go", "114");
        languageMap.put("Gosu (gosu", "98");
        languageMap.put("Groovy (groovy", "121");
        languageMap.put("Haskell (ghc", "21");
        languageMap.put("Icon (iconc", "16");
        languageMap.put("Intercal (ick", "9");
        languageMap.put("JAR (JavaSE", "24");
        languageMap.put("Java (HotSpot", "10");
        languageMap.put("JavaScript (rhino", "35");
        languageMap.put("JavaScript (SMonkey", "112");
        languageMap.put("Kotlin (kotlin", "47");
        languageMap.put("Lua (luac", "26");
        languageMap.put("Nemerle (ncc", "30");
        languageMap.put("Nice (nicec", "25");
        languageMap.put("Nim (nim", "122");
        languageMap.put("Node.js (node", "56");
        languageMap.put("Objective-C (gcc", "43");
        languageMap.put("Objective-C (clang", "83");
        languageMap.put("Ocaml (ocamlopt", "8");
        languageMap.put("Octave (octave", "127");
        languageMap.put("Pascal (gpc", "2");
        languageMap.put("Pascal (fpc", "22");
        languageMap.put("Perl (perl 2018.12)", "54");
        languageMap.put("Perl (perl", "3");
        languageMap.put("PHP (php", "29");
        languageMap.put("Pico Lisp (pico", "94");
        languageMap.put("Pike (pike", "19");
        languageMap.put("Prolog (swi", "15");
        languageMap.put("Prolog (gprolog", "108");
        languageMap.put("Python (cpython", "4");
        languageMap.put("Python (PyPy", "99");
        languageMap.put("Python 3 (python", "116");
        languageMap.put("Python 3 nbc (python", "126");
        languageMap.put("R (R", "117");
        languageMap.put("Racket (racket", "95");
        languageMap.put("Ruby (ruby", "17");
        languageMap.put("Rust (rust", "93");
        languageMap.put("Scala (scala", "39");
        languageMap.put("Scheme (stalin", "18");
        languageMap.put("Scheme (guile", "33");
        languageMap.put("Scheme (chicken", "97");
        languageMap.put("Sed (sed", "46");
        languageMap.put("Smalltalk (gst", "23");
        languageMap.put("SQLite (sqlite", "40");
        languageMap.put("Swift (swift", "85");
        languageMap.put("TCL (tcl", "38");
        languageMap.put("Text (plain", "62");
        languageMap.put("Unlambda (unlambda", "115");
        languageMap.put("VB.net (mono", "50");
        languageMap.put("Whitespace (wspace", "6");
    }
}
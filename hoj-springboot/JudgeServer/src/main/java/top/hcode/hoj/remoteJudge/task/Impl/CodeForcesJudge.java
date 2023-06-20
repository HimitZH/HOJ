package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.util.StringUtils;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.CodeForcesUtils;
import top.hcode.hoj.util.Constants;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "hoj")
public class CodeForcesJudge extends RemoteJudgeStrategy {


    public static final String IMAGE_HOST = "https://codeforces.com";
    public static final String HOST = "https://codeforces.com";
    public static final String LOGIN_URL = "/enter";
    public static final String SUBMIT_URL = "/contest/%s/submit";
    public static final String SUBMISSION_RESULT_URL = "/api/user.status?handle=%s&from=1&count=%s";
    public static final String SUBMIT_SOURCE_URL = "/data/submitSource";
    public static final String MY_SUBMISSION = "/problemset/status?my=on";
    public static final String SUBMISSION_RUN_ID_URL = "/problemset/status?my=on";

    protected static final Map<String, Constants.Judge> statusMap = new HashMap<String, Constants.Judge>() {{
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
        put("RUNNING & JUDGING", Constants.Judge.STATUS_JUDGING);
    }};

    @Override
    public void submit() {

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        if (remoteJudgeDTO.getCompleteProblemId() == null || remoteJudgeDTO.getUserCode() == null) {
            return;
        }

        HttpRequest httpRequest = HttpUtil.createGet(IMAGE_HOST);
        httpRequest.setConnectionTimeout(60000);
        httpRequest.setReadTimeout(60000);
        httpRequest.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.101 Safari/537.36 Edg/91.0.864.48");
        httpRequest.header("cookie", "RCPC=" + CodeForcesUtils.getRCPC());
        HttpResponse httpResponse = httpRequest.execute();
        String homePage = httpResponse.body();

        if (homePage.contains("Redirecting... Please, wait.")) {
            List<String> list = ReUtil.findAll("[a-z0-9]+[a-z0-9]{31}", homePage, 0, new ArrayList<>());
            CodeForcesUtils.updateRCPC(list);
            httpRequest.removeHeader("cookie");
            httpRequest.header("cookie", "RCPC=" + CodeForcesUtils.getRCPC());
            httpResponse = httpRequest.execute();
            homePage = httpResponse.body();
        }

        if (!homePage.contains("/logout\">") || !homePage.contains("<a href=\"/profile/" + remoteJudgeDTO.getUsername() + "\"")) {
            login();
            if (remoteJudgeDTO.getLoginStatus() != HttpStatus.SC_MOVED_TEMPORARILY) {
                log.error("[Codeforces] Error Username:[{}], Password:[{}]", remoteJudgeDTO.getUsername(), remoteJudgeDTO.getPassword());
                String msg = "[Codeforces] Failed to Login, possibly due to incorrect remote judge account or password of codeforces!";
                throw new RuntimeException(msg);
            }
        } else {
            remoteJudgeDTO.setCookies(httpResponse.getCookies());
        }

        submitCode(remoteJudgeDTO);
        if (remoteJudgeDTO.getSubmitStatus() == 403) {
            // 如果提交出现403可能是cookie失效了，再执行登录，重新提交
            remoteJudgeDTO.setCookies(null);
            login();
            if (remoteJudgeDTO.getLoginStatus() != HttpStatus.SC_MOVED_TEMPORARILY) {
                log.error("[Codeforces] Error Username:[{}], Password:[{}]", remoteJudgeDTO.getUsername(), remoteJudgeDTO.getPassword());
                String msg = "[Codeforces] Failed to Login, possibly due to incorrect remote judge account or password of codeforces!";
                throw new RuntimeException(msg);
            }
            submitCode(remoteJudgeDTO);
            if (remoteJudgeDTO.getSubmitStatus() == 403) {
                String log = String.format("[Codeforces] [%s] [%s]:Failed to submit code, caused by `403 Forbidden`", remoteJudgeDTO.getContestId(), remoteJudgeDTO.getCompleteProblemId());
                throw new RuntimeException(log);
            }
        }
        // 获取提交的题目id
//        Long maxRunId = getMaxRunId(nowTime);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long maxRunId = getMaxIdByParseHtmlWithRetry();
        remoteJudgeDTO.setSubmitId(maxRunId);
    }

    @SuppressWarnings("unchecked")
    private Long getMaxRunId(long nowTime) {

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        int retryNum = 0;
        // 防止cf的nginx限制访问频率，重试5次
        while (retryNum != 10) {
            HttpResponse httpResponse = getMaxIdForSubmissionResult(remoteJudgeDTO.getUsername(), 30);
            if (httpResponse.getStatus() == 200) {
                try {
                    Map<String, Object> json;
                    try {
                        json = JSONUtil.parseObj(httpResponse.body());
                    } catch (JSONException e) {
                        // 接口限制，导致返回数据非json，此处替换成页面解析
                        return getMaxIdByParseHtml();
                    }
                    List<Map<String, Object>> results = (List<Map<String, Object>>) json.get("result");
                    for (Map<String, Object> result : results) {
                        Long runId = Long.valueOf(result.get("id").toString());
                        long creationTimeSeconds = Long.parseLong(result.get("creationTimeSeconds").toString());
                        if (creationTimeSeconds < nowTime && retryNum < 8) {
                            continue;
                        }
                        Map<String, Object> problem = (Map<String, Object>) result.get("problem");
                        if (remoteJudgeDTO.getContestId().equals(problem.get("contestId").toString()) &&
                                remoteJudgeDTO.getProblemNum().equals(problem.get("index").toString())) {
                            return runId;
                        }
                    }
                } catch (Exception e) {
                    String log = String.format("[Codeforces] Failed to get run id for problem: [%s], error:%s", remoteJudgeDTO.getCompleteProblemId(), e.toString());
                    throw new RuntimeException(log);
                }
            }
            retryNum++;
        }
        return -1L;
    }


    // CF的这个接口有每两秒的访问限制，所以需要加锁，保证只有一次查询
    public static synchronized HttpResponse getMaxIdForSubmissionResult(String username, Integer count) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String url = HOST + String.format(SUBMISSION_RESULT_URL, username, count);
        return HttpUtil.createGet(url)
                .timeout(30000)
                .execute();
    }

    private Long getMaxIdByParseHtmlWithRetry() {
        int count = 0;
        while (count < 3) {
            try {
                return getMaxIdByParseHtml();
            } catch (Exception e) {
                count++;
                if (count == 3) {
                    throw e;
                }
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException ignored) {
                }
            }
        }
        return null;
    }

    protected String getRunIdUrl(){
        return HOST + SUBMISSION_RUN_ID_URL;
    }

    private Long getMaxIdByParseHtml() {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        HttpRequest request = HttpUtil.createGet(getRunIdUrl());
        request.cookie(remoteJudgeDTO.getCookies());
        HttpResponse response = request.execute();
        String csrfToken = ReUtil.get("data-csrf='(\\w+)'", response.body(), 1);
        remoteJudgeDTO.setCsrfToken(csrfToken);
        String maxRunIdStr = ReUtil.get("data-submission-id=\"(\\d+)\"", response.body(), 1);
        if (StringUtils.isEmpty(maxRunIdStr)) {
            log.error("[Codeforces] Failed to parse submission html:{}", response.body());
            String log = String.format("[Codeforces] Failed to parse html to get run id for problem: [%s]", remoteJudgeDTO.getCompleteProblemId());
            throw new RuntimeException(log);
        } else {
            return Long.valueOf(maxRunIdStr);
        }
    }

    @Override
    public RemoteJudgeRes result() {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        if (remoteJudgeDTO.getCookies() == null) {
            login();
        }
        String csrfToken;
        if (StringUtils.isEmpty(remoteJudgeDTO.getCsrfToken())) {
            HttpRequest homeRequest = HttpUtil.createGet(HOST + MY_SUBMISSION);
            homeRequest.cookie(remoteJudgeDTO.getCookies());
            HttpResponse homeResponse = homeRequest.execute();
            csrfToken = ReUtil.get("data-csrf='(\\w+)'", homeResponse.body(), 1);
        } else {
            csrfToken = remoteJudgeDTO.getCsrfToken();
        }
        HttpRequest httpRequest = HttpUtil.createPost(HOST + SUBMIT_SOURCE_URL)
                .cookie(remoteJudgeDTO.getCookies())
                .header("Origin", HOST)
                .header("Referer", HOST + MY_SUBMISSION)
                .timeout(30000);
        httpRequest.form(MapUtil
                .builder(new HashMap<String, Object>())
                .put("csrf_token", csrfToken)
                .put("submissionId", remoteJudgeDTO.getSubmitId()).map());

        HttpResponse httpResponse = httpRequest.execute();

        RemoteJudgeRes remoteJudgeRes = RemoteJudgeRes.builder()
                .status(Constants.Judge.STATUS_JUDGING.getStatus())
                .build();

        if (httpResponse.getStatus() == 200) {
            JSONObject submissionInfoJson = JSONUtil.parseObj(httpResponse.body());
            String compilationError = submissionInfoJson.getStr("compilationError");
            if ("true".equals(compilationError)) {
                remoteJudgeRes
                        .setMemory(0)
                        .setTime(0)
                        .setStatus(Constants.Judge.STATUS_COMPILE_ERROR.getStatus());
                String CEMsg = submissionInfoJson.getStr("checkerStdoutAndStderr#1");
                if (StringUtils.isEmpty(CEMsg)) {
                    remoteJudgeRes.setErrorInfo("Oops! Because Codeforces does not provide compilation details, it is unable to provide the reason for compilation failure!");
                } else {
                    remoteJudgeRes.setErrorInfo(CEMsg);
                }
                return remoteJudgeRes;
            }
            Integer testcaseNum = remoteJudgeDTO.getTestcaseNum();
            Integer maxTime = remoteJudgeDTO.getMaxTime();
            Integer maxMemory = remoteJudgeDTO.getMaxMemory();
            if (testcaseNum == null) {
                testcaseNum = 1;
                maxTime = 0;
                maxMemory = 0;
            }
            List<JudgeCase> judgeCaseList = new ArrayList<>();
            String testCountStr = submissionInfoJson.getStr("testCount");
            int testCount = Integer.parseInt(testCountStr);
            for (; testcaseNum <= testCount; testcaseNum++) {
                String verdict = submissionInfoJson.getStr("verdict#" + testcaseNum);
                if (StringUtils.isEmpty(verdict)) {
                    continue;
                }
                Constants.Judge judgeRes = statusMap.get(verdict);
                Integer time = Integer.parseInt(submissionInfoJson.getStr("timeConsumed#" + testcaseNum));
                Integer memory = Integer.parseInt(submissionInfoJson.getStr("memoryConsumed#" + testcaseNum)) / 1024;
                String msg = submissionInfoJson.getStr("checkerStdoutAndStderr#" + testcaseNum);
                judgeCaseList.add(new JudgeCase()
                        .setSubmitId(remoteJudgeDTO.getJudgeId())
                        .setPid(remoteJudgeDTO.getPid())
                        .setUid(remoteJudgeDTO.getUid())
                        .setTime(time)
                        .setMemory(memory)
                        .setStatus(judgeRes.getStatus())
                        .setUserOutput(msg));
                if (time > maxTime) {
                    maxTime = time;
                }
                if (memory > maxMemory) {
                    maxMemory = memory;
                }
            }

            remoteJudgeDTO.setTestcaseNum(testcaseNum);
            remoteJudgeDTO.setMaxMemory(maxMemory);
            remoteJudgeDTO.setMaxTime(maxTime);
            remoteJudgeRes.setJudgeCaseList(judgeCaseList);
            if ("true".equals(submissionInfoJson.getStr("waiting"))) {
                return remoteJudgeRes;
            }
            Constants.Judge finalJudgeRes = statusMap.get(submissionInfoJson.getStr("verdict#" + testCount));
            remoteJudgeRes.setStatus(finalJudgeRes.getStatus())
                    .setTime(maxTime)
                    .setMemory(maxMemory);
            return remoteJudgeRes;
        } else {
            remoteJudgeRes.setStatus(Constants.Judge.STATUS_SYSTEM_ERROR.getStatus())
                    .setMemory(0)
                    .setTime(0)
                    .setErrorInfo("Oops! Error in obtaining the judging result. The status code returned by the interface is " + httpResponse.getStatus() + ".");
            return remoteJudgeRes;
        }
    }

    public HashMap<String, Object> getCsrfToken(String url, boolean needTTA) {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        HttpRequest request = HttpUtil.createGet(url);
        if (remoteJudgeDTO.getCookies() == null) {
            request.header("cookie", "RCPC=" + CodeForcesUtils.getRCPC());
        } else {
            request.cookie(remoteJudgeDTO.getCookies());
        }

        HttpResponse response = request.execute();
        String body = response.body();
        if (body.contains("Redirecting... Please, wait.")) {
            List<String> list = ReUtil.findAll("[a-z0-9]+[a-z0-9]{31}", body, 0, new ArrayList<>());
            CodeForcesUtils.updateRCPC(list);
            request.removeHeader("cookie");
            request.header("cookie", "RCPC=" + CodeForcesUtils.getRCPC());
            response = request.execute();
            body = response.body();
        }

        remoteJudgeDTO.setCookies(response.getCookies());
        HashMap<String, Object> res = new HashMap<>();
        String ftaa = response.getCookieValue("70a7c28f3de");
        res.put("ftaa", ftaa);

        String bfaa = ReUtil.get("_bfaa = \"(.{32})\"", body, 1);
        if (StringUtils.isEmpty(bfaa)) {
            bfaa = response.getCookieValue("raa");
            if (StringUtils.isEmpty(bfaa)) {
                bfaa = response.getCookieValue("bfaa");
            }
        }
        res.put("bfaa", bfaa);

        String csrfToken = ReUtil.get("data-csrf='(\\w+)'", body, 1);
        res.put("csrf_token", csrfToken);

        if (needTTA) {
            String _39ce7 = response.getCookieValue("39ce7");
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
            res.put("_tta", _tta);
        }
        return res;
    }

    @Override
    public void login() {
        // 清除当前线程的cookies缓存
        HttpRequest.getCookieManager().getCookieStore().removeAll();
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        HashMap<String, Object> keyMap = getCsrfToken(IMAGE_HOST + LOGIN_URL, false);

        HttpRequest httpRequest = new HttpRequest(IMAGE_HOST + LOGIN_URL);
        httpRequest.setConnectionTimeout(60000);
        httpRequest.setReadTimeout(60000);
        httpRequest.setMethod(Method.POST);
        httpRequest.cookie(remoteJudgeDTO.getCookies());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("csrf_token", keyMap.get("csrf_token"));
        hashMap.put("action", "enter");
        hashMap.put("ftaa", keyMap.get("ftaa"));
        hashMap.put("bfaa", keyMap.get("bfaa"));
        hashMap.put("handleOrEmail", remoteJudgeDTO.getUsername());
        hashMap.put("password", remoteJudgeDTO.getPassword());
        hashMap.put("remember", "on");
        httpRequest.form(hashMap);
        HttpResponse response = httpRequest.execute();
        remoteJudgeDTO.setCookies(response.getCookies());
        remoteJudgeDTO.setLoginStatus(response.getStatus());
    }

    protected String getSubmitUrl(String contestNum) {
        return IMAGE_HOST + String.format(SUBMIT_URL, contestNum);
    }

    public void submitCode(RemoteJudgeDTO remoteJudgeDTO) {
        HashMap<String, Object> keyMap = getCsrfToken(getSubmitUrl(remoteJudgeDTO.getContestId()), true);
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("csrf_token", keyMap.get("csrf_token"));
        paramMap.put("_tta", keyMap.get("_tta"));
        paramMap.put("bfaa", keyMap.get("bfaa"));
        paramMap.put("ftaa", keyMap.get("ftaa"));
        paramMap.put("action", "submitSolutionFormSubmitted");
        paramMap.put("submittedProblemIndex", remoteJudgeDTO.getProblemNum());
        paramMap.put("contestId", remoteJudgeDTO.getContestId());
        paramMap.put("programTypeId", getLanguage(remoteJudgeDTO.getLanguage()));
        paramMap.put("tabsize", 4);
        paramMap.put("source", remoteJudgeDTO.getUserCode() + getRandomBlankString());
        paramMap.put("sourceCodeConfirmed", true);
        paramMap.put("doNotShowWarningAgain", "on");
        HttpRequest request = HttpUtil.createPost(getSubmitUrl(remoteJudgeDTO.getContestId()) + "?csrf_token=" + keyMap.get("csrf_token"));
        request.setConnectionTimeout(60000);
        request.setReadTimeout(60000);
        request.form(paramMap);
        request.cookie(remoteJudgeDTO.getCookies());
        request.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
        HttpResponse response = request.execute();
        remoteJudgeDTO.setSubmitStatus(response.getStatus());
        if (response.getStatus() != HttpStatus.SC_MOVED_TEMPORARILY) {
            if (response.body().contains("error for__programTypeId")) {
                String log = String.format("Codeforces[%s] [%s]:Failed to submit code, caused by `Language Rejected`", remoteJudgeDTO.getContestId(), remoteJudgeDTO.getProblemNum());
                throw new RuntimeException(log);
            }
            if (response.body().contains("error for__source")) {
                String log = String.format("Codeforces[%s] [%s]:Failed to submit code, caused by `Source Code Error`", remoteJudgeDTO.getContestId(), remoteJudgeDTO.getProblemNum());
                throw new RuntimeException(log);
            }
        }
    }

    @Override
    public String getLanguage(String language) {
        if (language.startsWith("GNU GCC C11")) {
            return "43";
        } else if (language.startsWith("Clang++17 Diagnostics")) {
            return "52";
        } else if (language.startsWith("GNU G++11")) {
            return "50";
        } else if (language.startsWith("GNU G++14")) {
            return "50";
        } else if (language.startsWith("GNU G++17")) {
            return "54";
        } else if (language.startsWith("GNU G++20")) {
            return "73";
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

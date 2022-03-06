package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;

import java.net.HttpCookie;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "hoj")
public class CodeForcesJudge extends RemoteJudgeStrategy {
    public static final String IMAGE_HOST = "https://codeforces.com";
    public static final String HOST = "https://codeforces.com";
    public static final String LOGIN_URL = "/enter";
    public static final String SUBMIT_URL = "/contest/%s/submit";
    public static final String SUBMISSION_RESULT_URL = "/api/user.status?handle=%s&from=1&count=%s";
    public static final String CE_INFO_URL = "/data/submitSource";

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
        HttpResponse httpResponse = httpRequest.execute();
        String homePage = httpResponse.body();
        if (!homePage.contains("/logout\">") || !homePage.contains("<a href=\"/profile/" + remoteJudgeDTO.getUsername() + "\"")) {
            login();
            if (remoteJudgeDTO.getLoginStatus() != HttpStatus.SC_MOVED_TEMPORARILY) {
                log.error("进行题目提交时发生错误：登录失败，可能原因账号或密码错误，登录失败！" + CodeForcesJudge.class.getName() + "，题号:" + remoteJudgeDTO.getProblemNum());
                return;
            }
        } else {
            remoteJudgeDTO.setCookies(httpResponse.getCookies());
        }

        long nowTime = DateUtil.currentSeconds();


        submitCode(remoteJudgeDTO);

        if (remoteJudgeDTO.getSubmitStatus() == 403) {
            // 如果提交出现403可能是cookie失效了，再执行登录，重新提交
            remoteJudgeDTO.setCookies(null);
            login();
            if (remoteJudgeDTO.getLoginStatus() != HttpStatus.SC_MOVED_TEMPORARILY) {
                log.error("CF进行题目提交时发生错误：登录失败，可能原因账号或密码错误，登录失败！" + CodeForcesJudge.class.getName() + "，题号:" + remoteJudgeDTO.getProblemNum());
                return;
            }
            submitCode(remoteJudgeDTO);
            if (remoteJudgeDTO.getSubmitStatus() == 403) {
                String log = String.format("Codeforces[%s] [%s]:Failed to submit code, caused by `403 Forbidden`", remoteJudgeDTO.getContestId(), remoteJudgeDTO.getProblemNum());
                throw new RuntimeException(log);
            }
        }


        try {
            TimeUnit.MILLISECONDS.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 获取提交的题目id
        Long maxRunId = getMaxRunId(nowTime);

        remoteJudgeDTO.setSubmitId(maxRunId);
    }

    @SuppressWarnings("unchecked")
    private Long getMaxRunId(long nowTime) {

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();

        int retryNum = 0;
        // 防止cf的nginx限制访问频率，重试5次
        while (retryNum != 10) {
            HttpResponse httpResponse = getSubmissionResult(remoteJudgeDTO.getUsername(), 10);
            if (httpResponse.getStatus() == 200) {
                try {
                    Map<String, Object> json = JSONUtil.parseObj(httpResponse.body());
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
                    log.error("进行题目获取runID发生错误：获取提交ID失败，" + CodeForcesJudge.class.getName()
                            + "，题号:" + remoteJudgeDTO.getCompleteProblemId() + "，异常描述：" + e);
                    return -1L;
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retryNum++;
        }
        return -1L;
    }

    // CF的这个接口有每两秒的访问限制，所以需要加锁，保证只有一次查询
    public static synchronized HttpResponse getSubmissionResult(String username, Integer count) {
        String url = HOST + String.format(SUBMISSION_RESULT_URL, username, count);
        return HttpUtil.createGet(url)
                .timeout(60000)
                .execute();
    }

    @Override
    public RemoteJudgeRes result() {

        String resJson = getSubmissionResult(getRemoteJudgeDTO().getUsername(), 1000).body();

        JSONObject jsonObject = JSONUtil.parseObj(resJson);

        RemoteJudgeRes remoteJudgeRes = RemoteJudgeRes.builder()
                .status(Constants.Judge.STATUS_JUDGING.getStatus())
                .build();

        JSONArray results = (JSONArray) jsonObject.get("result");

        for (Object tmp : results) {
            JSONObject result = (JSONObject) tmp;
            long runId = Long.parseLong(result.get("id").toString());
            if (runId == getRemoteJudgeDTO().getSubmitId()) {
                String verdict = (String) result.get("verdict");
                Constants.Judge resultStatus = statusMap.get(verdict);
                if (resultStatus == Constants.Judge.STATUS_JUDGING) {
                    return RemoteJudgeRes.builder()
                            .status(resultStatus.getStatus())
                            .build();
                } else if (resultStatus == null) {
                    return RemoteJudgeRes.builder()
                            .status(Constants.Judge.STATUS_PENDING.getStatus())
                            .build();
                }
                remoteJudgeRes.setTime((Integer) result.get("timeConsumedMillis"));
                remoteJudgeRes.setMemory((int) result.get("memoryConsumedBytes") / 1024);
                if (resultStatus == Constants.Judge.STATUS_COMPILE_ERROR) {

                    String csrfToken = getCsrfToken(HOST);

                    HttpRequest httpRequest = HttpUtil.createPost(HOST + CE_INFO_URL)
                            .timeout(30000);

                    httpRequest.form(MapUtil
                            .builder(new HashMap<String, Object>())
                            .put("csrf_token", csrfToken)
                            .put("submissionId", getRemoteJudgeDTO().getSubmitId().toString()).map());

                    HttpResponse response = httpRequest.execute();
                    if (response.getStatus() == 200) {
                        JSONObject CEInfoJson = JSONUtil.parseObj(response.body());
                        String CEInfo = CEInfoJson.getStr("checkerStdoutAndStderr#1");
                        remoteJudgeRes.setErrorInfo(CEInfo);
                    } else {
                        // 非200则说明cf没有提供编译失败的详情
                        remoteJudgeRes.setErrorInfo("Oops! Because Codeforces does not provide compilation details, it is unable to provide the reason for compilation failure!");
                    }

                }
                remoteJudgeRes.setStatus(resultStatus.getStatus());
                return remoteJudgeRes;
            }
        }
        return remoteJudgeRes;
    }

    public String getCsrfToken(String url) {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        HttpRequest request = HttpUtil.createGet(url);
        request.cookie(remoteJudgeDTO.getCookies());
        HttpResponse response = request.execute();
        remoteJudgeDTO.setCookies(response.getCookies());
        String body = response.body();
        return ReUtil.get("data-csrf='(\\w+)'", body, 1);
    }

    @Override
    public void login() {
        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        String csrf_token = getCsrfToken(IMAGE_HOST + LOGIN_URL);
        HttpRequest httpRequest = new HttpRequest(IMAGE_HOST + LOGIN_URL);
        httpRequest.setConnectionTimeout(60000);
        httpRequest.setReadTimeout(60000);
        httpRequest.setMethod(Method.POST);
        httpRequest.cookie(remoteJudgeDTO.getCookies());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("csrf_token", csrf_token);
        hashMap.put("action", "enter");
        hashMap.put("ftaa", "");
        hashMap.put("bfaa", "");
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
        String csrfToken = getCsrfToken(getSubmitUrl(remoteJudgeDTO.getContestId()));
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("csrf_token", csrfToken);
        paramMap.put("_tta", 140);
        paramMap.put("bfaa", "f1b3f18c715565b589b7823cda7448ce");
        paramMap.put("ftaa", "");
        paramMap.put("action", "submitSolutionFormSubmitted");
        paramMap.put("submittedProblemIndex", remoteJudgeDTO.getProblemNum());
        paramMap.put("contestId", remoteJudgeDTO.getContestId());
        paramMap.put("programTypeId", getLanguage(remoteJudgeDTO.getLanguage()));
        paramMap.put("tabsize", 4);
        paramMap.put("source", remoteJudgeDTO.getUserCode() + getRandomBlankString());
        paramMap.put("sourceCodeConfirmed", true);
        paramMap.put("doNotShowWarningAgain", "on");
        HttpRequest request = HttpUtil.createPost(getSubmitUrl(remoteJudgeDTO.getContestId()) + "?csrf_token=" + csrfToken);
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

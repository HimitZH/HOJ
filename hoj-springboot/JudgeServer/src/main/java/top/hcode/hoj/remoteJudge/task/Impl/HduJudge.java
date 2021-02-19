package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.JsoupUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HduJudge implements RemoteJudgeStrategy {
    public static final String HOST = "http://acm.hdu.edu.cn";
    public static final String LOGIN_URL = "/userloginex.php?action=login";
    public static final String SUBMIT_URL = "/submit.php?action=submit";
    public static final String STATUS_URL = "/status.php?user=%s&pid=%d";
    public static final String QUERY_URL = "/status.php?first=%d";
    public static final String ERROR_URL = "/viewerror.php?rid=%d";

    /**
     * @param problemId 提交的题目id
     * @param language
     * @param userCode  用户代码
     * @return
     */
    @Override
    public Long submit(String username, String password, Long problemId, String language, String userCode) throws Exception {
        if (problemId == null || userCode == null) {
            return -1L;
        }
        Map<String, String> loginCookie = getLoginCookie(username, password);
        Connection connection = JsoupUtils.getConnectionFromUrl(HOST + SUBMIT_URL, null, loginCookie);
        Connection.Response response = JsoupUtils.postResponse(connection, MapUtil
                .builder(new HashMap<String, String>())
                .put("check", "0")
                .put("language", getLanguage(language))
                .put("problemid", String.valueOf(problemId))
                .put("usercode", userCode)
                .map());
        if (response.statusCode() != 200) {
            log.error("提交题目失败");
            return -1L;
        }
        // 获取提交的题目id
        return getMaxRunId(connection, username, problemId);
    }

    @Override
    public Map<String, Object> result(Long submitId) throws Exception {
        String url = HOST + String.format(QUERY_URL, submitId);
        Connection connection = JsoupUtils.getConnectionFromUrl(url, null, null);
        Connection.Response response = JsoupUtils.getResponse(connection, null);
        // 1提交时间 2结果 3执行时间 4执行空间 5代码长度
        // 一般情况下 代码长度和提交时间不需要，想要也行，自行添加
        Pattern pattern = Pattern.compile(">" + submitId + "</td><td>([\\s\\S]*?)</td><td>([\\s\\S]*?)</td><td>[\\s\\S]*?</td><td>(\\d*?)MS</td><td>(\\d*?)K</td><td>(\\d*?)B</td>");
        Matcher matcher = pattern.matcher(response.body());
        // 找到时
        Validate.isTrue(matcher.find());
        String rawStatus = matcher.group(2).replaceAll("<[\\s\\S]*?>", "").trim();
        Constants.Judge statusType = statusTypeMap.get(rawStatus);
        if (statusType == Constants.Judge.STATUS_PENDING) {
            return MapUtil.builder(new HashMap<String, Object>())
                    .put("status", statusType.getStatus()).build();
        }
        // 返回的结果map
        Map<String, Object> result = MapUtil.builder(new HashMap<String, Object>())
                .put("status", statusType.getStatus()).build();
        // 获取其他信息
        String executionTime = matcher.group(3);
        result.put("time", Integer.parseInt(executionTime));
        String executionMemory = matcher.group(4);
        result.put("memory", Integer.parseInt(executionMemory));
        // 如果CE了，则还需要获得错误信息
        if (statusType == Constants.Judge.STATUS_COMPILE_ERROR) {
            connection.url(HOST + String.format(ERROR_URL, submitId));
            response = JsoupUtils.getResponse(connection, null);
            String compilationErrorInfo = ReUtil.get("<pre>([\\s\\S]*?)</pre>", response.body(), 1);
            result.put("CEInfo", compilationErrorInfo);
        }
        return result;
    }


    @Override
    public Map<String, String> getLoginCookie(String username, String password) throws Exception {
        Connection connection = JsoupUtils.getConnectionFromUrl(HOST + LOGIN_URL, null, null);
        Connection.Response response = JsoupUtils.postResponse(connection, MapUtil
                .builder(new HashMap<String, String>())
                .put("username", username)
                .put("userpass", password).map());
        return response.cookies();
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


    public Long getMaxRunId(Connection connection, String userName, Long problemId) throws Exception {
        String url = String.format(STATUS_URL, userName, problemId);
        connection.url(HOST + url);
        Connection.Response response = JsoupUtils.getResponse(connection, null);
        Matcher matcher = Pattern.compile("<td height=22px>(\\d+)").matcher(response.body());
        return matcher.find() ? Long.parseLong(matcher.group(1)) : -1L;
    }


    // TODO 添加结果对应的状态
    private static final Map<String, Constants.Judge> statusTypeMap = new HashMap<String, Constants.Judge>() {
        {
            put("Submitted", Constants.Judge.STATUS_SUBMITTING);
            put("Accepted", Constants.Judge.STATUS_ACCEPTED);
            put("Wrong Answer", Constants.Judge.STATUS_WRONG_ANSWER);
            put("Compilation Error", Constants.Judge.STATUS_COMPILE_ERROR);
            put("Queuing", Constants.Judge.STATUS_PENDING);
            put("Compiling", Constants.Judge.STATUS_PENDING);
            put("Time Limit Exceeded", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED);
            put("Presentation Error", Constants.Judge.STATUS_PRESENTATION_ERROR);
        }
    };
}

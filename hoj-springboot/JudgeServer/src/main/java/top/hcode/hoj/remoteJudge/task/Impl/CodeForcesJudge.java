package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.JsoupUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Slf4j
public class CodeForcesJudge implements RemoteJudgeStrategy {
    public static final String HOST = "https://codeforces.com";
    public static final String LOGIN_URL = "/enter";
    public static final String SUBMIT_URL = "/problemset/submit?csrf_token=%s";
    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("Host", "codeforces.com")
            .map();

    @Override
    public Long submit(String username, String password, Long problemId, String language, String userCode) throws Exception {
        if (problemId == null || userCode == null) {
            return -1L;
        }
        Map<String, Object> loginUtils = getLoginUtils(username, password);
        CodeForcesToken token = (CodeForcesToken) loginUtils.get("token");
        Connection connection = JsoupUtils.getConnectionFromUrl(HOST + SUBMIT_URL, null, token.cookies);
        //            "csrf_token", token.csrf_token, //
        //            "_tta", token._tta, //
        //            "action", "submitSolutionFormSubmitted", //
        //            "submittedProblemCode", info.remoteProblemId, //
        //            "programTypeId", info.remotelanguage, //
        //            "source", info.sourceCode + getRandomBlankString(), //
        //            "sourceFile", "", //
        //            "sourceCodeConfirmed", "true", //
        //            "doNotShowWarningAgain", "on" //
        // contestId: 1451
        //submittedProblemIndex: A
        Connection.Response response = JsoupUtils.postResponse(connection, MapUtil
                .builder(new HashMap<String, String>())
                .put("csrf_token", token.csrf_token)
                .put("_tta", token._tta)
                .put("action", "submitSolutionFormSubmitted")
                .put("contestId", "") // 比赛号
                .put("submittedProblemIndex", "A") // 题号：A,B,C,D
                .put("programTypeId", "") // 语言号
                .put("source", userCode + getRandomBlankString())
                .put("sourceFile", "")
                .put("sourceCodeConfirmed", "true")
                .put("doNotShowWarningAgain", "on")
                .map());
        if (response.statusCode() != 200) {
            log.error("提交题目失败");
            return -1L;
        }
        // 获取提交的题目id
        return getMaxRunId(connection, username, problemId);
    }

    private Long getMaxRunId(Connection connection, String username, Long problemId) {
    }

    @Override
    public Map<String, Object> result(Long submitId) throws Exception {
        return null;
    }

    @Override
    public Map<String, Object> getLoginUtils(String username, String password) throws Exception {
        // 获取token
        CodeForcesToken token = getTokens();
        Connection connection = JsoupUtils.getConnectionFromUrl(HOST + LOGIN_URL, headers, token.cookies);

        Connection.Response response = JsoupUtils.postResponse(connection, MapUtil
                .builder(new HashMap<String, String>())
                .put("csrf_token", token.csrf_token)
                .put("_tta", token._tta)
                .put("action", "enter")
//                .put("remember", "on") // 是否记住登录
                .put("handleOrEmail", username)
                .put("password", password).map());
        // 混合cookie才能确认身份
        token.cookies.putAll(response.cookies());
        return MapUtil.builder(new HashMap<String, Object>()).put("token", token).map();
    }

    @Override
    public String getLanguage(String language) {
        return null;
    }


    public static class CodeForcesToken {
        public String _tta;
        public String csrf_token;
        public Map<String, String> cookies;

        public CodeForcesToken(String _tta, String csrf_token, Map<String, String> cookies) {
            this._tta = _tta;
            this.csrf_token = csrf_token;
            this.cookies = cookies;
        }
    }

    public static CodeForcesToken getTokens() throws Exception {
        Connection connection = JsoupUtils.getConnectionFromUrl(HOST, headers, null);
        Connection.Response response = JsoupUtils.getResponse(connection, null);
        String html = response.body();
        String csrfToken = ReUtil.get("data-csrf='(\\w+)'", html, 1);
        String _39ce7 = null;
        Map<String, String> cookies = response.cookies();
        if (cookies.containsKey("39ce7")) {
            _39ce7 = cookies.get("39ce7");
        } else {
            throw new Exception("网络错误,无法找到cookies");
        }
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
        return new CodeForcesToken(Integer.toString(_tta), csrfToken, cookies);
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

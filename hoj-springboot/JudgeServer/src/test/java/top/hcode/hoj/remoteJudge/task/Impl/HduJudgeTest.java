package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.UnicodeUtil;
import org.jsoup.Connection;
import org.junit.jupiter.api.Test;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.util.JsoupUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HduJudgeTest {

    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("Host", "codeforces.com")
            .map();

    @Test
    void getLoginCookie() throws Exception {

        Map<String, Object> loginUtils = getLoginUtils("username", "password");
        CodeForcesJudge.CodeForcesToken token = (CodeForcesJudge.CodeForcesToken) loginUtils.get("token");
        Connection CEInfoConnection = JsoupUtils.getConnectionFromUrl("https://codeforces.com/data/judgeProtocol", headers, token.cookies);
        CEInfoConnection.ignoreContentType(true);
        CEInfoConnection.header("content-type", "application/x-www-form-urlencoded; charset=UTF-8");

        Connection.Response CEInfoResponse = JsoupUtils.postResponse(CEInfoConnection, MapUtil
                .builder(new HashMap<String, String>())
                .put("csrf_token", token.csrf_token)
                .put("submissionId", "111090897").map());
        System.out.println(CEInfoResponse.body());
        System.out.println(UnicodeUtil.toString(CEInfoResponse.body()));

    }

    public Map<String, Object> getLoginUtils(String username, String password) throws Exception {
        // 获取token
        CodeForcesJudge.CodeForcesToken token = CodeForcesJudge.getTokens();
        Connection connection = JsoupUtils.getConnectionFromUrl("https://codeforces.com/enter", headers, token.cookies);

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
}
package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import org.jsoup.Connection;
import org.junit.jupiter.api.Test;
import top.hcode.hoj.util.JsoupUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CodeForcesJudgeTest {

    @Test
    void submit() {
    }

    @Test
    void result() {
    }

    @Test
    void getLoginCookie() throws Exception {
        CodeForcesJudge codeForcesJudge = new CodeForcesJudge();
        Map<String, String> cookie = codeForcesJudge.getLoginCookie("xxx", "xxx");
        System.out.println(cookie);
        Connection connection = JsoupUtils.getConnectionFromUrl(CodeForcesJudge.HOST, MapUtil
                .builder(new HashMap<String, String>())
                .put("Host", "codeforces.com")
                .map(), cookie);
        Connection.Response response = JsoupUtils.getResponse(connection, null);;

    }

    @Test
    void getLanguage() {
    }

    @Test
    void getTokens() {
    }
}
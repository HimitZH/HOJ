package top.hcode.hoj.remoteJudge.task.Impl;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.junit.jupiter.api.Test;

import java.net.HttpCookie;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class AtcoderJudgeTest {

    public static final String HOST = "https://atcoder.jp";
    public static final String LOGIN_URL = "/login";
    public static final String SUBMIT_URL = "/contests/%s/submit";
    public static final String SUBMISSION_RESULT_URL = "/contests/%s/submissions/%s";
    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .put("Origin", "https://atcoder.jp")
            .put("Referer", "https://atcoder.jp/contests/abc217/submit")
            .map();

    @Test
    void test() throws Exception {
        String csrfToken = getCsrfToken(HOST + LOGIN_URL);
        List<HttpCookie> cookies = login("****", "****", csrfToken);
        System.out.println(cookies);

        String code = "#include <bits/stdc++.h>\n" +
                "\n" +
                "using namespace std;\n" +
                "\n" +
                "#define N 410\n" +
                "#define ll long long\n" +
                "#define MOD 998244353\n" +
                "#define rep(i, n) for(int i = 0; i < n; ++i)\n" +
                "\n" +
                "int main(void) {\n" +
                "\tint n, m;\n" +
                "\tint a, b;\n" +
                "\tll x, y, z;\n" +
                "\tbool can[N][N];\n" +
                "\tll dp[N][(N / 2)];\n" +
                "    ll binom[N][N];\n" +
                "\tbinom[0][0] = 1;\n" +
                "\tfor (int i = 1; i < N; i++) {\n" +
                "\t\tbinom[i][0] = 1;\n" +
                "\t\tbinom[i][i] = 1;\n" +
                "\t\trep(j, i - 1) {\n" +
                "\t\t\tbinom[i][j + 1] = (binom[i - 1][j] + binom[i - 1][j + 1]) % MOD;\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\n" +
                "\tcin >> n >> m;\n" +
                "\trep(i, 2 * n)rep(j, 2 * n)can[i][j] = false;\n" +
                "\trep(i, m) {\n" +
                "\t\tcin >> a >> b;\n" +
                "\t\tcan[a - 1][b - 1] = true;\n" +
                "\t\tcan[b - 1][a - 1] = true;\n" +
                "\t}\n" +
                "\trep(i, 2 * n + 1)dp[i][0] = 1;\n" +
                "\tfor (int j = 1; j <= n; j++) {\n" +
                "\t\trep(i, 2 * (n - j) + 1) {\n" +
                "\t\t\tdp[i][j] = 0;\n" +
                "\t\t\trep(k, j) {\n" +
                "\t\t\t\tif (can[i][i + (2 * k) + 1]) {\n" +
                "\t\t\t\t\tx = (dp[i + 1][k] * dp[i + (2 * k) + 2][j - k - 1]) % MOD;\n" +
                "\t\t\t\t\tdp[i][j] = ((x*binom[j][k + 1]) + dp[i][j]) % MOD;\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\tcout << dp[0][n] << endl;\n" +
                "\n" +
                "\treturn 0;\n" +
                "}\n" +
                "\n";
//        String token = getCsrfToken(HOST + "/contests/abc217/submit");
        submit("abc217", "abc217_f", code, csrfToken, cookies);
    }

    private java.util.List<java.net.HttpCookie> login(String username, String password, String csrfToken) {
        HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL);
        request.addHeaders(headers);
        HttpResponse response = request.form(MapUtil.builder(new HashMap<String, Object>())
                .put("username", username)
                .put("password", password)
                .put("csrf_token", csrfToken).map()).execute();

        System.out.println(response.getStatus());
        return response.getCookies();
    }

    private String getCsrfToken(String url) {
        HttpRequest request = HttpUtil.createGet(url);
        request.addHeaders(headers);
        HttpResponse response = request.execute();
        String body = response.body();
        return ReUtil.get("var csrfToken = \"([\\s\\S]*?)\"", body, 1);
    }

    private void submit(String contestId, String problemId, String code, String csrfToken, Collection<HttpCookie> cookies) {
        String submitUrl = HOST + String.format(SUBMIT_URL, contestId);
        HttpRequest request = HttpUtil.createPost(submitUrl);
        HttpRequest httpRequest = request.form(MapUtil.builder(new HashMap<String, Object>())
                .put("data.TaskScreenName", problemId)
                .put("data.LanguageId", "5001")
                .put("sourceCode", code)
                .put("csrf_token", csrfToken).map());
        httpRequest.cookie(cookies);
//        httpRequest.addHeaders(headers);
        HttpResponse response = httpRequest.execute();
        System.out.println(response.getStatus());
    }

}
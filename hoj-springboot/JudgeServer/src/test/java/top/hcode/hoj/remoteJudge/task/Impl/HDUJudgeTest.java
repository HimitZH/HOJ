package top.hcode.hoj.remoteJudge.task.Impl;


import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;


class HDUJudgeTest {

    public static final String HOST = "http://acm.hdu.edu.cn";
    public static final String LOGIN_URL = "/userloginex.php?action=login";
    public static final String SUBMIT_URL = "/submit.php?action=submit";
    public static final String STATUS_URL = "/status.php?user=%s&pid=%s";
    public static final String QUERY_URL = "/status.php?first=%d";
    public static final String ERROR_URL = "/viewerror.php?rid=%d";
    public static Map<String, String> headers = MapUtil
            .builder(new HashMap<String, String>())
            .put("Host", "acm.hdu.edu.cn")
            .put("origin", "https://acm.hdu.edu.cn")
            .put("referer", "https://acm.hdu.edu.cn")
            .put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36")
            .map();


    @Test
    void test() throws Exception {
        System.out.println(login("****", "****"));
    }

    private java.util.List<java.net.HttpCookie> login(String username, String password){
        HttpRequest request = HttpUtil.createPost(HOST + LOGIN_URL).addHeaders(headers);
        HttpResponse response = request.form(MapUtil
                        .builder(new HashMap<String, Object>())
                        .put("username", username)
                        .put("login", "Sign In")
                        .put("userpass", password).map())
                .execute();
        if (response.getStatus() != 302) {
            throw new RuntimeException("[HDU] Failed to login! The possible cause is connection failure, and the returned status code is " + response.getStatus());
        }
        System.out.println(response.getStatus());
       return response.getCookies();
    }


}
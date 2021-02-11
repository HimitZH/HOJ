package top.hcode.hoj.util;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

public class JsoupUtils {

    /**
     * 获取连接
     *
     * @param url     api网址
     * @param headers 用户头
     * @return 返回一个object
     * @throws IOException
     */
    public static Connection getConnectionFromUrl(String url, Map<String, String> headers, Map<String, String> cookies) throws IOException {
        Connection connection = Jsoup.connect(url);
        // 设置用户代理
        connection.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36");
        // 设置超时时间5秒
        connection.timeout(5000);
        // 设置cookie，保存信息
        connection.cookies(cookies);
        // 设置请求头
        if (headers != null) {
            connection.headers(headers);
        }
        return connection;
    }

    public static Connection.Response postResponse(Connection connection, Map<String, String> postData) throws IOException {
        //添加参数
        if (postData != null) {
            for (Map.Entry<String, String> entry : postData.entrySet()) {
                connection.data(entry.getKey(), entry.getValue());
            }
        }
        return connection.method(Connection.Method.POST).execute();
    }

    public static Connection.Response getResponse(Connection connection, Map<String, String> postData) throws IOException {
        //添加参数
        if (postData != null) {
            for (Map.Entry<String, String> entry : postData.entrySet()) {
                connection.data(entry.getKey(), entry.getValue());
            }
        }
        return connection.method(Connection.Method.GET).execute();
    }


}

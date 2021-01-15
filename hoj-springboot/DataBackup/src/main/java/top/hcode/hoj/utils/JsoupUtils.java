package top.hcode.hoj.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import java.io.IOException;
import java.util.Map;

public class JsoupUtils {

    /**
     * 获取连接
     *
     * @param url     api网址
     * @param params  请求参数
     * @param headers 用户头
     * @return 返回一个object
     * @throws IOException
     */
    public static Connection getConnectionFromUrl(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        Connection connection = Jsoup.connect(url);
        // 设置用户代理
        connection.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36");
        // 设置超时时间10秒
        connection.timeout(10000);
        // 设置忽略请求类型
        connection.ignoreContentType(true);
        // 设置请求头
        if (headers != null) {
            connection.headers(headers);
        }
        // 给url添加参数
        if (params != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(url);
            if (url.indexOf("?") <= 0) {
                sb.append("?");
            }
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
            url = sb.toString();
        }
        return connection;
    }

    /**
     * 通过jsoup连接返回json格式
     * @param connection Jsoup的connection连接
     * @return
     * @throws IOException
     */
    public static JSONObject getJsonFromConnection(Connection connection) throws IOException {
        String body = connection.execute().body();
        return new JSONObject(body);
    }
}

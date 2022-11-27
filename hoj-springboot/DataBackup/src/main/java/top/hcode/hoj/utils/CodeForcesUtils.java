package top.hcode.hoj.utils;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;

import javax.script.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "hoj")
public class CodeForcesUtils {
    private static String RCPC;

    public static String getRCPC() {
        if (RCPC == null){
            HttpRequest request = HttpRequest.get("https://codeforces.com")
                    .timeout(20000);
            String html = request.execute().body();
            List<String> list = ReUtil.findAll("[a-z0-9]+[a-z0-9]{31}", html, 0, new ArrayList<>());
            updateRCPC(list);
        }
        return RCPC;
    }

    public static void updateRCPC(List<String> list) {

        ScriptEngine se = new ScriptEngineManager().getEngineByName("javascript");
        Bindings bindings = se.createBindings();
        bindings.put("string", 4);
        se.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

        String file = ResourceUtil.readUtf8Str("CodeForcesAES.js");
        try {
            se.eval(file);
            // 是否可调用
            if (se instanceof Invocable) {
                Invocable in = (Invocable) se;
                RCPC = (String) in.invokeFunction("getRCPC", list.get(0), list.get(1), list.get(2));
            }
        } catch (ScriptException e) {
            log.error("CodeForcesUtils.updateRCPC throw ScriptException ->", e);
        } catch (NoSuchMethodException e) {
            log.error("CodeForcesUtils.updateRCPC throw NoSuchMethodException ->", e);
        }
    }

    public static void downloadPDF(String urlStr, String savePath) {
        try {
            int byteRead;
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.54 Safari/537.36");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");//注意编码，gzip可能会乱码
            conn.setRequestProperty("Content-Encoding", "utf8");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
            conn.setRequestProperty("cookie", "RCPC=" + getRCPC());
            conn.setRequestProperty("Cache-Control", "max-age=0");
            conn.setRequestProperty("Content-Type", "application/pdf");

            InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(savePath);

            byte[] buffer = new byte[1024];
            while ((byteRead = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
            }
            inStream.close();
            fs.close();
        } catch (FileNotFoundException e1) {
            log.error("CodeForcesUtils.downloadPDF throw FileNotFoundException ->", e1);
        } catch (MalformedURLException e2) {
            log.error("CodeForcesUtils.downloadPDF throw MalformedURLException ->", e2);
        } catch (IOException e3) {
            log.error("CodeForcesUtils.downloadPDF throw IOException ->", e3);
        }
    }
}

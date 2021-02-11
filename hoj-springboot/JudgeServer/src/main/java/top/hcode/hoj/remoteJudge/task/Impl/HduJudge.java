package top.hcode.hoj.remoteJudge.task.Impl;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.util.JsoupUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HduJudge implements RemoteJudgeStrategy {
    public static final String host = "acm.hdu.edu.cn";
    public static final String loginUrl = "/userloginex.php?action=login";

    /**
     * TODO 提交题目
     *
     * @param problemId 提交的题目id
     * @param language
     * @param userCode  用户代码
     * @return
     */
    @Override
    public Long submit(Long problemId, String language, String userCode) {
        if (problemId == null || userCode == null) {
            return -1L;
        }
        return null;
    }

    @Override
    public String result(Long submitId) {
        return null;
    }

    @Override
    public Map<String, String> getLoginCookie() {
        try {
            Connection connection = JsoupUtils.getConnectionFromUrl(host + loginUrl, null, null);
            Connection.Response response = JsoupUtils.postResponse(connection, MapUtil
                    .builder(new HashMap<String, String>())
                    .put("username", "12")
                    .put("userpass", "").map());
            return response.cookies();
        } catch (IOException e) {
            log.error("网络错误");
        }
        return null;
    }
}

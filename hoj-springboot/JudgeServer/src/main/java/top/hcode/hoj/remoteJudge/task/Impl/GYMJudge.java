package top.hcode.hoj.remoteJudge.task.Impl;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.util.Constants;

import java.util.HashMap;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/6 11:17
 * @Description:
 */
public class GYMJudge extends CodeForcesJudge {

    private final static String JUDGE_PROTOCOL = "/data/judgeProtocol";

    @Override
    protected String getSubmitUrl(String contestNum) {
        return IMAGE_HOST + "/gym/" + contestNum + "/submit";
    }

    @Override
    public RemoteJudgeRes result() {
        return getNotTestCaseDetailResult();
    }

    private RemoteJudgeRes getNotTestCaseDetailResult() {
        HttpRequest.getCookieManager().getCookieStore().removeAll();

        RemoteJudgeDTO remoteJudgeDTO = getRemoteJudgeDTO();
        Long submitId = remoteJudgeDTO.getSubmitId();

        RemoteJudgeRes remoteJudgeRes = RemoteJudgeRes.builder()
                .status(Constants.Judge.STATUS_JUDGING.getStatus())
                .build();

        String url = HOST + String.format(SUBMISSION_RESULT_URL, remoteJudgeDTO.getUsername(), 30);
        HttpResponse response = HttpUtil.createGet(url)
                .timeout(30000)
                .execute();

        if (response.getStatus() == 200) {
            JSONObject jsonObject = JSONUtil.parseObj(response.body());
            JSONArray results = (JSONArray) jsonObject.get("result");
            for (Object tmp : results) {
                JSONObject result = (JSONObject) tmp;
                long runId = Long.parseLong(result.get("id").toString());
                if (runId == submitId) {
                    String verdict = (String) result.get("verdict");
                    Constants.Judge statusType = statusMap.get(verdict);
                    if (statusType == Constants.Judge.STATUS_JUDGING) {
                        return remoteJudgeRes;
                    }
                    remoteJudgeRes.setTime(result.getInt("timeConsumedMillis"));
                    Integer memoryConsumedBytes = result.getInt("memoryConsumedBytes");
                    if (memoryConsumedBytes != 0) {
                        remoteJudgeRes.setMemory(memoryConsumedBytes / 1024);
                    } else {
                        remoteJudgeRes.setMemory(0);
                    }
                    remoteJudgeRes.setStatus(statusType.getStatus());
                    if (statusType == Constants.Judge.STATUS_COMPILE_ERROR) {
                        if (remoteJudgeDTO.getCookies() == null) {
                            login();
                        }
                        HttpRequest homeRequest = HttpUtil.createGet(HOST + MY_SUBMISSION);
                        homeRequest.cookie(remoteJudgeDTO.getCookies());
                        HttpResponse homeResponse = homeRequest.execute();
                        String csrfToken = ReUtil.get("data-csrf='(\\w+)'", homeResponse.body(), 1);

                        HttpRequest CEINFORequest = HttpUtil.createPost(HOST + JUDGE_PROTOCOL)
                                .cookie(remoteJudgeDTO.getCookies())
                                .timeout(30000);

                        CEINFORequest.form(MapUtil
                                .builder(new HashMap<String, Object>())
                                .put("csrf_token", csrfToken)
                                .put("submissionId", remoteJudgeDTO.getSubmitId()).map());

                        HttpResponse CEINFOResp = CEINFORequest.execute();
                        String CEInfo = UnicodeUtil.toString(CEINFOResp.body()).replaceAll("(\\\\r)?\\\\n", "\n")
                                .replaceAll("\\\\\\\\", "\\\\");
                        remoteJudgeRes.setErrorInfo(CEInfo);
                    }
                }
            }
        }
        return remoteJudgeRes;
    }

}
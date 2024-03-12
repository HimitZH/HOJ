package top.hcode.hoj.crawler.problem;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName LibreProblemStrategy
 * @Description 策略类
 * @Author Nine
 * @Date 2024/2/21 20:08
 * @Version 1.0
 */
public class LibreProblemStrategy extends ProblemStrategy {

    public static final String NAME = "LIBRE";

    public static final String API_HOST = "https://api.loj.ac";

    public static final String HOST = "https://loj.ac";

    public static final String PROBLEM_URL = "/api/problem/getProblem";


    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {
        //题号仅为正整数
        if (!problemId.matches("[1-9]\\d*")) {
            throw new IllegalArgumentException("LibreOJ: Incorrect problem id format!");
        }
        JSONObject param = new JSONObject();
        param.put("displayId", Integer.parseInt(problemId));
        param.put("localizedContentsOfLocale", "zh_CN");
        param.put("samples", Boolean.TRUE);
        param.put("judgeInfoToBePreprocessed", Boolean.TRUE);
        param.put("statistics", Boolean.TRUE);
        param.put("tagsOfLocale", "zh_CN");
        param.put("judgeInfo", Boolean.TRUE);
        String body = null;
        try {
            body = HttpRequest.post(API_HOST + PROBLEM_URL)
                    .body(param.toString())
                    .timeout(5000)
                    .execute()
                    .body();
        }catch (cn.hutool.core.io.IORuntimeException e){
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ignored) {
            }
            // 超时重试
            body = HttpRequest.post(API_HOST + PROBLEM_URL)
                    .body(param.toString())
                    .timeout(5000)
                    .execute()
                    .body();
        }
        JSONObject parseObj = JSONUtil.parseObj(body);
        if (parseObj.containsKey("error")) {
            throw new IllegalArgumentException("LibreOJ: Incorrect problem id!");
        }
        //实际提交id和展示id并非一个，该id为实际提交id
        String id = JSONUtil.parseObj(parseObj.getStr("meta")).getStr("id");
        JSONObject localizedContentsOfLocale = JSONUtil.parseObj(parseObj.get("localizedContentsOfLocale"));
        JSONObject judgeInfo = JSONUtil.parseObj(parseObj.get("judgeInfo"));
        JSONArray problemDetailArray = JSONUtil.parseArray(localizedContentsOfLocale.get("contentSections"));
        //删除第一行可能会出现的题目原题pdf链接 链接可能已经失效
        String desctiption = ReUtil.delFirst("\\*\\*(.*?)\\*\\*", JSONUtil.parseObj(problemDetailArray.get(0)).getStr("text"));
        //样例处理
        JSONArray samples = JSONUtil.parseArray(parseObj.get("samples"));
        StringBuilder examples = new StringBuilder();
        samples.forEach(e -> {
            JSONObject tempObj = JSONUtil.parseObj(e);
            examples.append("<input>").append(tempObj.getStr("inputData")).append("</input>");
            examples.append("<output>").append(tempObj.getStr("outputData")).append("</output>");
        });
        Problem problem = new Problem();
        String finalProblemId = null;
        if (Objects.equals(problemId, id)) {
            finalProblemId = NAME + "-" + problemId;
        } else {
            finalProblemId = NAME + "-" + problemId + "(" + id + ")";
        }
        problem.setProblemId(finalProblemId)
                .setTitle(localizedContentsOfLocale.getStr("title"))
                .setTimeLimit(Integer.parseInt(judgeInfo.getStr("timeLimit")))
                .setMemoryLimit(Integer.parseInt(judgeInfo.getStr("memoryLimit")))
                .setDescription(desctiption)
                .setInput(JSONUtil.parseObj(problemDetailArray.get(1)).getStr("text"))
                .setOutput(JSONUtil.parseObj(problemDetailArray.get(2)).getStr("text"))
                .setExamples(examples.toString())
                .setAuthor(author)
                .setIsRemote(true)
                .setSource("<a style='color:#1A5CC8' href='" + HOST + "/p/" + problemId + "'>" + NAME + "-" + problemId + "</a>")
                .setAuth(1)
                .setOpenCaseResult(false)
                .setIsGroup(false)
                .setIsRemoveEndBlank(false)
                .setDifficulty(1);

        List<Tag> tagList = new ArrayList<>();
        JSONArray allTags = JSONUtil.parseArray(parseObj.get("tagsOfLocale"));
        for (Object tmp : allTags) {
            tagList.add(new Tag().setName(JSONUtil.parseObj(tmp).getStr("name")));
        }
        return new RemoteProblemInfo()
                .setProblem(problem)
                .setTagList(tagList)
                .setRemoteOJ(Constants.RemoteOJ.LIBRE);
    }
}

package top.hcode.hoj.crawler.problem;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpRequest;
import org.springframework.util.StringUtils;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.utils.CodeForcesUtils;
import top.hcode.hoj.utils.Constants;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: Himit_ZH
 * @Date: 2021/3/3 15:00
 * @Description:
 */
public class CFProblemStrategy extends ProblemStrategy {

    public static final String JUDGE_NAME = "CF";
    public static final String HOST = "https://codeforces.com";
    public static final String PROBLEM_URL = "/problemset/problem/%s/%s";

    protected List<HttpCookie> cookies;

    public String getJudgeName() {
        return JUDGE_NAME;
    }

    public String getProblemUrl(String contestId, String problemNum) {
        return HOST + String.format(PROBLEM_URL, contestId, problemNum);
    }

    public String getProblemSource(String html, String problemId, String contestId, String problemNum) {
        return String.format("<p>Problem：<a style='color:#1A5CC8' href='https://codeforces.com/problemset/problem/%s/%s'>%s</a></p><p>" +
                        "Contest：" + ReUtil.get("(<a[^<>]+/contest/\\d+\">.+?</a>)", html, 1)
                        .replace("/contest", HOST + "/contest")
                        .replace("color: black", "color: #009688;") + "</p>",
                contestId, problemNum, getJudgeName() + "-" + problemId);
    }

    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {

        String contestId;
        String problemNum;
        if (NumberUtil.isInteger(problemId)) {
            contestId = ReUtil.get("([0-9]+)[0-9]{2}", problemId, 1);
            problemNum = ReUtil.get("[0-9]+([0-9]{2})", problemId, 1);
        } else {
            contestId = ReUtil.get("([0-9]+)[A-Z]{1}[0-9]{0,1}", problemId, 1);
            problemNum = ReUtil.get("[0-9]+([A-Z]{1}[0-9]{0,1})", problemId, 1);
        }

        if (contestId == null || problemNum == null) {
            throw new IllegalArgumentException("Codeforces: Incorrect problem id format!");
        }

        HttpRequest request = HttpRequest.get(getProblemUrl(contestId, problemNum))
                .header("cookie", "RCPC=" + CodeForcesUtils.getRCPC())
                .timeout(20000);
        if (cookies != null) {
            request.cookie(cookies);
        }
        String html = request.execute().body();
        // 重定向失效，更新RCPC
        if (html.contains("Redirecting... Please, wait.")) {
            List<String> list = ReUtil.findAll("[a-z0-9]+[a-z0-9]{31}", html, 0, new ArrayList<>());
            CodeForcesUtils.updateRCPC(list);
            html = HttpRequest.get(getProblemUrl(contestId, problemNum))
                    .header("cookie", "RCPC=" + CodeForcesUtils.getRCPC())
                    .timeout(20000)
                    .execute()
                    .body();
        }

        Problem info = new Problem();
        info.setProblemId(getJudgeName() + "-" + problemId);

        info.setTitle(ReUtil.get("<div class=\"title\">\\s*" + problemNum + "\\. ([\\s\\S]*?)</div>", html, 1).trim());

        String timeLimitStr = ReUtil.get("</div>\\s*([\\d\\.]+) (seconds?|s)\\s*</div>", html, 1);
        if (StringUtils.isEmpty(timeLimitStr)) {
            timeLimitStr = ReUtil.get("</div>\\s*<span .*?>(\\d+) (seconds?|s)\\s*</span>\\s*</div>", html, 1);
        }

        double timeLimit = 1000 * Double.parseDouble(timeLimitStr);
        info.setTimeLimit((int) timeLimit);

        String memoryLimitStr = ReUtil.get("</div>\\s*(\\d+) (megabytes|MB)\\s*</div>", html, 1);
        if (StringUtils.isEmpty(memoryLimitStr)) {
            memoryLimitStr = ReUtil.get("</div>\\s*<span .*?>(\\d+) (megabytes|MB)\\s*</span>\\s*</div>", html, 1);
        }

        info.setMemoryLimit(Integer.parseInt(memoryLimitStr));

        String tmpDesc = ReUtil.get("standard output\\s*</div>\\s*</div>\\s*<div>([\\s\\S]*?)</div>\\s*<div class=\"input-specification",
                html, 1);
        if (StringUtils.isEmpty(tmpDesc)) {
            tmpDesc = ReUtil.get("<div class=\"input-file\">([\\s\\S]*?)</div><div class=\"input-specification", html, 1);
        }

        if (StringUtils.isEmpty(tmpDesc)) {
            // 交互题
            tmpDesc = ReUtil.get("standard output\\s*</div>\\s*</div>\\s*<div>([\\s\\S]*?)</div>\\s*<div>\\s*<div class=\"section-title", html, 1);
        }

        if (StringUtils.isEmpty(tmpDesc)) {
            // 单单只有题面描述
            tmpDesc = ReUtil.get("standard output\\s*</div>\\s*</div>\\s*<div>([\\s\\S]*?)</div>",
                    html, 1);
        }

        if (!StringUtils.isEmpty(tmpDesc)) {
            tmpDesc = tmpDesc.replaceAll("\\$\\$\\$", "\\$")
                    .replaceAll("src=\"../../", "src=\"" + HOST + "/")
                    .trim();
        }

        info.setDescription(tmpDesc);

        String inputDesc = ReUtil.get("<div class=\"section-title\">\\s*Input\\s*</div>([\\s\\S]*?)</div>\\s*<div class=\"output-specification\">", html, 1);

        if (StringUtils.isEmpty(inputDesc)) {
            inputDesc = ReUtil.get("<div class=\"section-title\">\\s*Interaction\\s*</div>([\\s\\S]*?)</div>\\s*<div class=\"sample-tests\">", html, 1);
        }
        if (StringUtils.isEmpty(inputDesc)) {
            inputDesc = ReUtil.get("<div class=\"input-specification\">\\s*<div class=\"section-title\">\\s*Input\\s*</div>([\\s\\S]*?)</div>", html, 1);
        }
        if (!StringUtils.isEmpty(inputDesc)) {
            inputDesc = inputDesc.replaceAll("\\$\\$\\$", "\\$").trim();
        }

        info.setInput(inputDesc);

        String outputDesc = ReUtil.get("<div class=\"section-title\">\\s*Output\\s*</div>([\\s\\S]*?)</div>\\s*<div class=\"sample-tests\">", html, 1);
        if (!StringUtils.isEmpty(outputDesc)) {
            outputDesc = outputDesc.replaceAll("\\$\\$\\$", "\\$").trim();
        }
        info.setOutput(outputDesc);

        List<String> inputExampleList = ReUtil.findAll(Pattern.compile("<div class=\"input\">\\s*<div class=\"title\">\\s*Input\\s*</div>\\s*<pre>([\\s\\S]*?)</pre>\\s*</div>"), html, 1);

        List<String> outputExampleList = ReUtil.findAll(Pattern.compile("<div class=\"output\">\\s*<div class=\"title\">\\s*Output\\s*</div>\\s*<pre>([\\s\\S]*?)</pre>\\s*</div>"), html, 1);


        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < inputExampleList.size() && i < outputExampleList.size(); i++) {
            sb.append("<input>");
            String input = inputExampleList.get(i)
                    .replaceAll("<br>", "\n")
                    .replaceAll("<br />", "\n")
                    .replaceAll("<div .*?>", "")
                    .replaceAll("</div>", "\n")
                    .trim();
            sb.append(HtmlUtil.unescape(input)).append("</input>");
            sb.append("<output>");
            String output = outputExampleList.get(i)
                    .replaceAll("<br>", "\n")
                    .replaceAll("<br />", "\n")
                    .trim();
            sb.append(HtmlUtil.unescape(output)).append("</output>");
        }

        info.setExamples(sb.toString());

        String tmpHint = ReUtil.get("<div class=\"section-title\">\\s*Note\\s*</div>([\\s\\S]*?)</div>\\s*</div>", html, 1);
        if (tmpHint != null) {
            info.setHint(tmpHint.replaceAll("\\$\\$\\$", "\\$").trim());
        }

        info.setIsRemote(true);

        info.setSource(getProblemSource(html, problemId, contestId, problemNum));

        info.setType(0)
                .setAuth(1)
                .setAuthor(author)
                .setOpenCaseResult(true)
                .setIsRemoveEndBlank(false)
                .setIsGroup(false)
                .setDifficulty(1); // 默认为中等

        List<String> allTags = ReUtil.findAll(Pattern.compile("<span class=\"tag-box\" style=\"font-size:1\\.2rem;\" title=\"[\\s\\S]*?\">([\\s\\S]*?)</span>"), html, 1);
        List<Tag> tagList = new LinkedList<>();
        for (String tmp : allTags) {
            tagList.add(new Tag().setName(tmp.trim()));
        }
        return new RemoteProblemInfo()
                .setProblem(info)
                .setTagList(tagList)
                .setRemoteOJ(Constants.RemoteOJ.CODEFORCES);
    }


}
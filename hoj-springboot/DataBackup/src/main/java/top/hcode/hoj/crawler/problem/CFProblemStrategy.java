package top.hcode.hoj.crawler.problem;

import cn.hutool.core.util.ReUtil;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.util.StringUtils;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.pojo.entity.Tag;
import top.hcode.hoj.utils.JsoupUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
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

    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {

        String contestId = ReUtil.get("([0-9]+)[A-Z]{1}[0-9]{0,1}", problemId, 1);
        String problemNum = ReUtil.get("[0-9]+([A-Z]{1}[0-9]{0,1})", problemId, 1);
        if (contestId == null || problemNum == null) {
            throw new Exception("Codeforces的题号格式错误！");
        }

        String url = HOST + String.format(PROBLEM_URL, contestId, problemNum);
        Connection connection = JsoupUtils.getConnectionFromUrl(url, null, null);
        Document document = JsoupUtils.getDocument(connection, null);
        String html = document.html();


        Problem info = new Problem();
        info.setProblemId(JUDGE_NAME + "-" + problemId);

        info.setTitle(ReUtil.get("<div class=\"title\">\\s*" + problemNum + "\\. ([\\s\\S]*?)</div>", html, 1).trim());


        info.setTimeLimit(1000 * Integer.parseInt(ReUtil.get("</div>([\\d\\.]+) (seconds?|s)\\s*</div>", html, 1)));

        info.setMemoryLimit(Integer.parseInt(ReUtil.get("</div>(\\d+) (megabytes|MB)\\s*</div>", html, 1)));

        String tmpDesc = ReUtil.get("standard output\\s*</div></div><div>([\\s\\S]*?)</div><div class=\"input-specification",
                html, 1);
        if (StringUtils.isEmpty(tmpDesc)) {
            tmpDesc = ReUtil.get("<div class=\"input-file\">([\\s\\S]*?)</div><div class=\"input-specification", html, 1);
        }

        info.setDescription(tmpDesc.replaceAll("\\$\\$\\$", "\\$").replaceAll("src=\"../../", "src=\"" + HOST + "/"));


        info.setInput(ReUtil.get("<div class=\"section-title\">\\s*Input\\s*</div>([\\s\\S]*?)</div><div class=\"output-specification\">", html, 1).replaceAll("\\$\\$\\$", "\\$"));

        info.setOutput(ReUtil.get("<div class=\"section-title\">\\s*Output\\s*</div>([\\s\\S]*?)</div><div class=\"sample-tests\">", html, 1).replaceAll("\\$\\$\\$", "\\$"));

        List<String> inputExampleList = ReUtil.findAll(Pattern.compile("<div class=\"input\"><div class=\"title\">Input</div><pre>([\\s\\S]*?)</pre></div>"), html, 1);

        List<String> outputExampleList = ReUtil.findAll(Pattern.compile("<div class=\"output\"><div class=\"title\">Output</div><pre>([\\s\\S]*?)</pre></div>"), html, 1);


        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < inputExampleList.size() && i < outputExampleList.size(); i++) {
            sb.append("<input>");
            sb.append(inputExampleList.get(i).replace("<br>", "")).append("</input>");
            sb.append("<output>");
            sb.append(outputExampleList.get(i).replace("<br>", "")).append("</output>");
        }

        info.setExamples(sb.toString());

        String tmpHint = ReUtil.get("<div class=\"section-title\">\\s*Note\\s*</div>([\\s\\S]*?)</div></div>", html, 1);
        if (tmpHint != null) {
            info.setHint(tmpHint.replaceAll("\\$\\$\\$", "\\$"));
        }

        info.setIsRemote(true);

        info.setSource(String.format("<p>Problem：<a style='color:#1A5CC8' href='https://codeforces.com/problemset/problem/%s/%s'>%s</a></p><p>" +
                        "Contest：" + ReUtil.get("(<a[^<>]+/contest/\\d+\">.+?</a>)", html, 1).replace("/contest", HOST + "/contest")
                        .replace("color: black", "color: #009688;") + "</p>",
                contestId, problemNum, JUDGE_NAME + "-" + problemId));

        info.setType(0)
                .setAuth(1)
                .setAuthor(author)
                .setOpenCaseResult(false)
                .setIsRemoveEndBlank(false)
                .setDifficulty(1); // 默认为中等

        List<String> all = ReUtil.findAll(Pattern.compile("<span class=\"tag-box\" style=\"font-size:1\\.2rem;\" title=\"[\\s\\S]*?\">([\\s\\S]*?)</span>"), html, 1);
        List<Tag> tagList = new LinkedList<>();
        for (String tmp : all) {
            tagList.add(new Tag().setName(tmp.trim()));
        }
        return new RemoteProblemInfo().setProblem(info).setTagList(tagList);
    }


}
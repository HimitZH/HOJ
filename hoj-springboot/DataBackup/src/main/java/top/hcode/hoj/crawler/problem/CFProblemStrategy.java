package top.hcode.hoj.crawler.problem;

import cn.hutool.core.util.ReUtil;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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

        // 模拟一个浏览器
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        // 设置webClient的相关参数
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        webClient.getOptions().setActiveXNative(false);
        //设置ajax
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        //设置禁止js
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setWebSocketEnabled(false);
        webClient.getOptions().setDownloadImages(false);
        //CSS渲染禁止
        webClient.getOptions().setCssEnabled(false);
        //超时时间
        webClient.getOptions().setTimeout(4000);
        webClient.setJavaScriptTimeout(500);

        //设置js抛出异常:false
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        //允许重定向
        webClient.getOptions().setRedirectEnabled(true);
        //允许cookie
        webClient.getCookieManager().setCookiesEnabled(true);

        webClient.getOptions().setUseInsecureSSL(true);
        // 模拟浏览器打开一个目标网址
        HtmlPage page = webClient.getPage(url);
        webClient.close();
        String html = page.getWebResponse().getContentAsString();

        Problem info = new Problem();
        info.setProblemId(JUDGE_NAME + "-" + problemId);

        info.setTitle(ReUtil.get("<div class=\"title\">\\s*" + problemNum + "\\. ([\\s\\S]*?)</div>", html, 1).trim());

        double timeLimit = 1000 * Double.parseDouble(ReUtil.get("</div>\\s*([\\d\\.]+) (seconds?|s)\\s*</div>", html, 1));
        info.setTimeLimit((int) timeLimit);

        info.setMemoryLimit(Integer.parseInt(ReUtil.get("</div>\\s*(\\d+) (megabytes|MB)\\s*</div>", html, 1)));

        String tmpDesc = ReUtil.get("standard output\\s*</div>\\s*</div>\\s*<div>([\\s\\S]*?)</div>\\s*<div class=\"input-specification",
                html, 1);
        if (StringUtils.isEmpty(tmpDesc)) {
            tmpDesc = ReUtil.get("<div class=\"input-file\">([\\s\\S]*?)</div><div class=\"input-specification", html, 1);
        }
        tmpDesc = tmpDesc.trim();
        info.setDescription(tmpDesc.replaceAll("\\$\\$\\$", "\\$").replaceAll("src=\"../../", "src=\"" + HOST + "/"));

        String inputDesc = ReUtil.get("<div class=\"section-title\">\\s*Input\\s*</div>([\\s\\S]*?)</div>\\s*<div class=\"output-specification\">", html, 1).replaceAll("\\$\\$\\$", "\\$");
        info.setInput(inputDesc.trim());

        String outputDesc = ReUtil.get("<div class=\"section-title\">\\s*Output\\s*</div>([\\s\\S]*?)</div>\\s*<div class=\"sample-tests\">", html, 1).replaceAll("\\$\\$\\$", "\\$");
        info.setOutput(outputDesc.trim());

        List<String> inputExampleList = ReUtil.findAll(Pattern.compile("<div class=\"input\">\\s*<div class=\"title\">\\s*Input\\s*</div>\\s*<pre>([\\s\\S]*?)</pre>\\s*</div>"), html, 1);

        List<String> outputExampleList = ReUtil.findAll(Pattern.compile("<div class=\"output\">\\s*<div class=\"title\">\\s*Output\\s*</div>\\s*<pre>([\\s\\S]*?)</pre>\\s*</div>"), html, 1);


        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < inputExampleList.size() && i < outputExampleList.size(); i++) {
            sb.append("<input>");
            String input = inputExampleList.get(i)
                    .replaceAll("<br>", "\n")
                    .replaceAll("<br />", "\n");
            sb.append(input).append("</input>");
            sb.append("<output>");
            String output = outputExampleList.get(i)
                    .replaceAll("<br>", "\n")
                    .replaceAll("<br />", "\n");
            sb.append(output).append("</output>");
        }

        info.setExamples(sb.toString());

        String tmpHint = ReUtil.get("<div class=\"section-title\">\\s*Note\\s*</div>([\\s\\S]*?)</div>\\s*</div>", html, 1);
        if (tmpHint != null) {
            info.setHint(tmpHint.replaceAll("\\$\\$\\$", "\\$").trim());
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
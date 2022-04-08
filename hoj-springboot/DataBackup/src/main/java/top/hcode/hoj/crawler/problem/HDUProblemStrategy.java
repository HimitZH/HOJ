package top.hcode.hoj.crawler.problem;

import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.extension.api.R;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.springframework.util.Assert;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.JsoupUtils;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/17 22:42
 * @Description:
 */
public class HDUProblemStrategy extends ProblemStrategy {
    public static final String JUDGE_NAME = "HDU";
    public static final String HOST = "http://acm.hdu.edu.cn";
    public static final String PROBLEM_URL = "/showproblem.php?pid=%s";

    /**
     * @param problemId String的原因是因为某些题库题号不是纯数字
     * @param author    导入该题目的管理员用户名
     * @return 返回Problem对象
     * @throws Exception
     */
    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {
        // 验证题号是否符合规范
        Assert.isTrue(problemId.matches("[1-9]\\d*"), "HDU题号格式错误！");
        Problem info = new Problem();
        String url = HOST + String.format(PROBLEM_URL, problemId);
        Connection connection = JsoupUtils.getConnectionFromUrl(url, null, null);
        Document document = JsoupUtils.getDocument(connection, null);
        String html = document.html();
        info.setProblemId(JUDGE_NAME + "-" + problemId);
        info.setTitle(ReUtil.get("color:#1A5CC8\">([\\s\\S]*?)</h1>", html, 1).trim());
        info.setTimeLimit(Integer.parseInt(ReUtil.get("(\\d*) MS", html, 1)));
        info.setMemoryLimit(Integer.parseInt(ReUtil.get("/(\\d*) K", html, 1)) / 1024);
        info.setDescription(ReUtil.get(">Problem Description</div> <div class=.*?>([\\s\\S]*?)</div>", html, 1)
                .replaceAll("src=\"[../]*", "src=\"" + HOST + "/"));
        info.setInput(ReUtil.get(">Input</div> <div class=.*?>([\\s\\S]*?)</div>", html, 1));
        info.setOutput(ReUtil.get(">Output</div> <div class=.*?>([\\s\\S]*?)</div>", html, 1));
        StringBuilder sb = new StringBuilder("<input>");
        sb.append(ReUtil.get(">Sample Input</div><div .*?,monospace;\">([\\s\\S]*?)</div></pre>", html, 1));
        sb.append("</input><output>");
        sb.append(ReUtil.get(">Sample Output</div><div .*?monospace;\">([\\s\\S]*?)(<div style=.*?</div><i style=.*?</i>)*?</div></pre>", html, 1)).append("</output>");
        info.setExamples(sb.toString());
        info.setHint(ReUtil.get("<i>Hint</i></div>([\\s\\S]*?)</div><i .*?<br><[^<>]*?panel_title[^<>]*?>", html, 1));
        info.setIsRemote(true);
        info.setSource(String.format("<a style='color:#1A5CC8' href='https://acm.hdu.edu.cn/showproblem.php?pid=%s'>%s</a>", problemId, JUDGE_NAME + "-" + problemId));
        info.setType(0)
                .setAuth(1)
                .setAuthor(author)
                .setOpenCaseResult(false)
                .setIsRemoveEndBlank(false)
                .setIsGroup(false)
                .setDifficulty(1); // 默认为简单

        return new RemoteProblemInfo()
                .setProblem(info)
                .setTagList(null)
                .setRemoteOJ(Constants.RemoteOJ.HDU);
    }
}
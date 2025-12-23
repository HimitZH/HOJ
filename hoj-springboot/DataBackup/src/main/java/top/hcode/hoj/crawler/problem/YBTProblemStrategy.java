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
public class YBTProblemStrategy extends ProblemStrategy {
    public static final String JUDGE_NAME = "YBT";
    public static final String HOST = "http://ybt.ssoier.cn:8088";
    public static final String PROBLEM_URL = "/problem_show.php?pid=%s";

    /**
     * @param problemId String的原因是因为某些题库题号不是纯数字
     * @param author    导入该题目的管理员用户名
     * @return 返回Problem对象
     * @throws Exception
     */
    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {
        // 验证题号是否符合规范
        Assert.isTrue(problemId.matches("[1-9]\\d*"), "YBT题号格式错误！");
        Problem info = new Problem();
        String url = HOST + String.format(PROBLEM_URL, problemId);
        Connection connection = JsoupUtils.getConnectionFromUrl(url, null, null);
        Document document = JsoupUtils.getDocument(connection, null);
        String html = document.html();
        info.setProblemId(JUDGE_NAME + "-" + problemId);
        info.setTitle(ReUtil.get("<h3>([\\s\\S]*?)</h3>", html, 1).trim());
        info.setTimeLimit(Integer.parseInt(ReUtil.get("时间限制: (\\d*) ms", html, 1)));
        info.setMemoryLimit(Integer.parseInt(ReUtil.get("时间限制: (\\d*) KB", html, 1)) / 1024);
        info.setDescription(ReUtil.get("【题目描述】</h3>([\\s\\S]*?)<h3>【输入】</h3>", html, 1)
                .replaceAll("src=\"[../]*", "src=\"" + HOST + "/"));
        info.setInput(ReUtil.get(""<h3>【输入】</h3>([\\s\\S]*?)<h3>【输出】</h3>", html, 1));
        info.setOutput(ReUtil.get("<h3>【输出】</h3>([\\s\\S]*?)<h3>【输入样例】</h3>", html, 1));
        StringBuilder sb = new StringBuilder("<input>");
        sb.append(ReUtil.get("【输入样例】</h3>\n<font size=3><pre>([\\s\\S]*?)</pre></font>", html, 1));
        sb.append("</input><output>");
        sb.append(ReUtil.get("【输出样例】</h3>\n<font size=3><pre>([\\s\\S]*?)</pre></font>", html, 1));
        info.setExamples(sb.toString());
        info.setHint(ReUtil.get("<i>Hint</i></div>([\\s\\S]*?)</div><i .*?<br><[^<>]*?panel_title[^<>]*?>", html, 1));
        info.setIsRemote(true);
        info.setSource(String.format("<a style='color:#1A5CC8' href='%s'>%s</a>", url, JUDGE_NAME + "-" + problemId));
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

package top.hcode.hoj.crawler.problem;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/25 14:11
 * @Description:
 */
public class SPOJProblemStrategy extends ProblemStrategy {

    public static final String JUDGE_NAME = "SPOJ";
    public static final String HOST = "https://www.spoj.com";
    public static final String PROBLEM_URL = "/problems/%s/";
    public static final String SUBMIT_URL = "/submit/%s/";

    public String getJudgeName() {
        return JUDGE_NAME;
    }

    public String getProblemUrl(String problemId) {
        return HOST + String.format(PROBLEM_URL, problemId);
    }

    public String getSubmitUrl(String problemId) {
        return HOST + String.format(SUBMIT_URL, problemId);
    }

    public String getProblemSource(String problemId) {
        return String.format("<a style='color:#1A5CC8' href='" + getProblemUrl(problemId) + "'>%s</a>", getJudgeName() + "-" + problemId);
    }

    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {
        problemId = problemId.toUpperCase();
        String body = HttpUtil.get(getProblemUrl(problemId));
        String title = ReUtil.get("<h2 id=\"problem-name\" class=\"text-center\">[\\s\\S]*? - ([\\s\\S]*?)</h2>", body, 1);
        String timeLimit = ReUtil.get("Time limit:</td><td>([\\s\\S]*?)s", body, 1);
        String memoryLimit = ReUtil.get("Memory limit:</td><td>([\\s\\S]*?)MB", body, 1);
        String desc = ReUtil.get("<div id=\"problem-body\">([\\s\\S]*?)</div>[\\s]*?<div class=\"text-center\">", body, 1);
        desc = desc.replaceAll("src=\"/", "src=\"" + HOST + "/")
                .replaceAll("<br>[\\s]*+","<br>\n")
                .replaceAll("<pre>", "<pre style=\"padding:9px!important;background-color: #f5f5f5!important\">");
        desc = desc.replaceAll("<!-- here starts your code -->", "");

        Pattern tagPattern = Pattern.compile("<a href=\".*?\"><span class=\".*?\" data-tagid=\".*?\">([\\s\\S]*?)<span class=\".*?\" style=\"display: none\"></span></span></a>");
        List<String> allTags = ReUtil.findAll(tagPattern, body, 1);

        Problem problem = new Problem();
        problem.setProblemId(getJudgeName() + "-" + problemId)
                .setAuthor(author)
                .setTitle(title)
                .setType(0)
                .setTimeLimit((int) (Double.parseDouble(timeLimit) * 1000))
                .setMemoryLimit(Integer.parseInt(memoryLimit))
                .setDescription(desc.trim())
                .setIsRemote(true)
                .setSource(getProblemSource(problemId))
                .setAuth(1)
                .setOpenCaseResult(false)
                .setIsGroup(false)
                .setIsRemoveEndBlank(false)
                .setDifficulty(1); // 默认为中等

        List<Tag> tagList = new ArrayList<>();
        for (String tmp : allTags) {
            tagList.add(new Tag().setName(tmp.trim()));
        }

        String submitPageBody = HttpUtil.get(getSubmitUrl(problemId));
        Pattern pattern = Pattern.compile("<option value=\"([\\s\\S]*?)\" >[\\s\\S]*?</option>");
        List<String> langIdList = ReUtil.findAll(pattern, submitPageBody, 1);

        return new RemoteProblemInfo()
                .setProblem(problem)
                .setTagList(tagList)
                .setLangIdList(langIdList)
                .setRemoteOJ(Constants.RemoteOJ.SPOJ);

    }
}
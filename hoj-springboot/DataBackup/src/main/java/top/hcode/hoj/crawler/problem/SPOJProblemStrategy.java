package top.hcode.hoj.crawler.problem;

/**
 * @Author: Himit_ZH
 * @Date: 2022/1/25 14:11
 * @Description:
 */
public class SPOJProblemStrategy extends ProblemStrategy{

    public static final String JUDGE_NAME = "SPOJ";
    public static final String HOST = "https://www.spoj.com";
    public static final String PROBLEM_URL = "/problems/%s/";

    @Override
    public RemoteProblemInfo getProblemInfo(String problemId, String author) throws Exception {
        return null;
    }
}
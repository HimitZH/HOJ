package top.hcode.hoj.crawler.problem;

import top.hcode.hoj.pojo.entity.Problem;

public abstract class ProblemStrategy {

    public abstract Problem getProblemInfo(String problemId,String author) throws Exception;
}

package top.hcode.hoj.crawler.problem;

import lombok.extern.slf4j.Slf4j;
import top.hcode.hoj.pojo.entity.Problem;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/17 22:40
 * @Description:
 */
@Slf4j
public class ProblemContext {

    ProblemStrategy problemStrategy;

    public ProblemContext(ProblemStrategy problemStrategy) {
        this.problemStrategy = problemStrategy;
    }

    //上下文接口
    public ProblemStrategy.RemoteProblemInfo getProblemInfo(String problemId, String author) {

        try {
            return problemStrategy.getProblemInfo(problemId, author);
        } catch (Exception e) {
            log.error("获取题目详情失败---------------->{}", e.getMessage());
        }
        return null;
    }
}
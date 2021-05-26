package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.hcode.hoj.crawler.problem.ProblemStrategy;
import top.hcode.hoj.pojo.dto.ProblemDto;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.pojo.entity.Problem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */

public interface ProblemService extends IService<Problem> {
    Page<ProblemVo> getProblemList(int limit, int currentPage, Long pid, String title,
                                   Integer difficulty, Long tid, String oj);

    boolean adminUpdateProblem(ProblemDto problemDto);

    boolean adminAddProblem(ProblemDto problemDto);

    ProblemStrategy.RemoteProblemInfo getOtherOJProblemInfo(String OJName, String problemId, String author) throws Exception;

    boolean adminAddOtherOJProblem(ProblemStrategy.RemoteProblemInfo remoteProblemInfo, String OJName);
}

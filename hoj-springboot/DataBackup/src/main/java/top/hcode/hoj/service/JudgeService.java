package top.hcode.hoj.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.pojo.entity.Judge;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.ProblemCountVo;



/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */

public interface JudgeService extends IService<Judge> {
    IPage<JudgeVo> getCommonJudgeList(Integer limit, Integer currentPage, String searchPid, Integer status, String username,
                                      String uid);

    IPage<JudgeVo> getContestJudgeList(Integer limit, Integer currentPage, String displayId, Long cid, Integer status, String username,
                                       String uid, Boolean beforeContestSubmit);


    void failToUseRedisPublishJudge(Long submitId, Long pid, Boolean isContest);

    ProblemCountVo getContestProblemCount(Long pid, Long cpid, Long cid);

    ProblemCountVo getProblemCount(Long pid);
}

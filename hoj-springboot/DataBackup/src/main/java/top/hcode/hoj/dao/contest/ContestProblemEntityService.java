package top.hcode.hoj.dao.contest;

import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.ContestProblemVO;
import top.hcode.hoj.pojo.vo.ProblemFullScreenListVO;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface ContestProblemEntityService extends IService<ContestProblem> {
    List<ContestProblemVO> getContestProblemList(Long cid,
                                                 Date startTime,
                                                 Date endTime,
                                                 Date sealTime,
                                                 Boolean isAdmin,
                                                 String contestAuthorUid,
                                                 List<String> groupRootUidList,
                                                 Boolean isContainsContestEndJudge);

    List<ProblemFullScreenListVO> getContestFullScreenProblemList(Long cid);

    void syncContestRecord(Long pid, Long cid, String displayId);
}

package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.judge.Judge;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.hcode.hoj.pojo.vo.ContestScrollBoardSubmissionVo;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.ProblemCountVo;

import java.util.Date;
import java.util.List;


/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface JudgeMapper extends BaseMapper<Judge> {
    IPage<JudgeVo> getCommonJudgeList(Page<JudgeVo> page,
                                      @Param("searchPid") String searchPid,
                                      @Param("status") Integer status,
                                      @Param("username") String username,
                                      @Param("uid") String uid,
                                      @Param("completeProblemID") Boolean completeProblemID,
                                      @Param("gid") Long gid);

    IPage<JudgeVo> getContestJudgeList(Page<JudgeVo> page,
                                       @Param("displayId") String displayId,
                                       @Param("cid") Long cid,
                                       @Param("status") Integer status,
                                       @Param("username") String username,
                                       @Param("uid") String uid,
                                       @Param("beforeContestSubmit") Boolean beforeContestSubmit,
                                       @Param("rule") String rule,
                                       @Param("startTime") Date startTime,
                                       @Param("sealRankTime") Date sealRankTime,
                                       @Param("sealTimeUid") String sealTimeUid,
                                       @Param("completeProblemID") Boolean completeProblemID);

    int getTodayJudgeNum();

    ProblemCountVo getContestProblemCount(@Param("pid") Long pid,
                                          @Param("cpid") Long cpid,
                                          @Param("cid") Long cid,
                                          @Param("startTime") Date startTime,
                                          @Param("sealRankTime") Date sealRankTime,
                                          @Param("adminList") List<String> adminList);

    ProblemCountVo getProblemCount(@Param("pid") Long pid, @Param("gid") Long gid);

    List<ProblemCountVo> getProblemListCount(@Param("pidList") List<Long> pidList);

    List<Judge> getLastYearUserJudgeList(@Param("uid") String uid, @Param("username") String username);

    List<ContestScrollBoardSubmissionVo> getContestScrollBoardSubmission(@Param("cid") Long cid,
                                                                         @Param("uidList") List<String> uidList);
}

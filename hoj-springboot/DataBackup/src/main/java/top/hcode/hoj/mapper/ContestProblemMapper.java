package top.hcode.hoj.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.hcode.hoj.pojo.vo.ContestProblemVO;
import top.hcode.hoj.pojo.vo.ProblemFullScreenListVO;

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
public interface ContestProblemMapper extends BaseMapper<ContestProblem> {
    List<ContestProblemVO> getContestProblemList(@Param("cid") Long cid, @Param("startTime") Date startTime,
                                                 @Param("endTime") Date endTime, @Param("sealTime") Date sealTime,
                                                 @Param("isAdmin") Boolean isAdmin, @Param("adminList") List<String> adminList,
                                                 @Param("isHideContestEndJudge") Boolean isHideContestEndJudge);

    List<ProblemFullScreenListVO> getContestFullScreenProblemList(@Param("cid") Long cid);
}

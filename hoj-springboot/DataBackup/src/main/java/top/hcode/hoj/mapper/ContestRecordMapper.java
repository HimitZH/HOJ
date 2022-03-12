package top.hcode.hoj.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.hcode.hoj.pojo.vo.ContestRecordVo;

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
public interface ContestRecordMapper extends BaseMapper<ContestRecord> {
    List<ContestRecord> getACInfo(@Param("status") Integer status, @Param("cid") Long cid);

    List<ContestRecordVo> getOIContestRecordByRecentSubmission(@Param("cid") Long cid,
                                                                @Param("contestAuthor") String contestAuthor,
                                                                @Param("isOpenSealRank") Boolean isOpenSealRank,
                                                                @Param("sealTime") Date sealTime,
                                                                @Param("startTime") Date startTime,
                                                                @Param("endTime") Date endTime);

    List<ContestRecordVo> getOIContestRecordByHighestSubmission(@Param("cid") Long cid,
                                                                @Param("contestAuthor") String contestAuthor,
                                                                @Param("isOpenSealRank") Boolean isOpenSealRank,
                                                                @Param("sealTime") Date sealTime,
                                                                @Param("startTime") Date startTime,
                                                                @Param("endTime") Date endTime);

    List<ContestRecordVo> getACMContestRecord(@Param("username") String username, @Param("cid") Long cid);
}

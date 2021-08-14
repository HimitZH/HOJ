package top.hcode.hoj.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.ContestRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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

    List<ContestRecord> getOIContestRecord(@Param("cid") Long cid, @Param("contestAuthor") String contestAuthor,
                                           @Param("isOpenSealRank") Boolean isOpenSealRank,
                                           @Param("sealTime") Date sealTime, @Param("startTime") Date startTime,
                                           @Param("endTime") Date endTime);
}

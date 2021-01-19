package top.hcode.hoj.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.vo.ContestRecordVo;
import top.hcode.hoj.pojo.entity.ContestRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.hcode.hoj.pojo.vo.JudgeVo;

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
    IPage<ContestRecord> getACInfo(IPage iPage, @Param("status") Integer status, @Param("cid") Long cid);

    List<ContestRecord> getOIContestRecord(@Param("cid") Long cid, @Param("isOpenSealRank") Boolean isOpenSealRank,
                                           @Param("sealTime") Date sealTime, @Param("endTime") Date endTime);
}

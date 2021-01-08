package top.hcode.hoj.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.pojo.entity.Contest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface ContestMapper extends BaseMapper<Contest> {
    List<ContestVo> getContestList(IPage page,@Param("type") Integer type,
                                   @Param("status")Integer status,@Param("keyword")String keyword);
    ContestVo getContestInfoById(@Param("cid")long cid);
    List<ContestVo> getWithinNext14DaysContests();
}

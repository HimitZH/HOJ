package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.vo.ContestRegisterCountVO;
import top.hcode.hoj.pojo.vo.ContestVO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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
public interface ContestMapper extends BaseMapper<Contest> {

    List<ContestVO> getContestList(IPage page,
                                   @Param("type") Integer type,
                                   @Param("status") Integer status,
                                   @Param("keyword") String keyword);

    List<ContestRegisterCountVO> getContestRegisterCount(@Param("cidList") List<Long> cidList);

    ContestVO getContestInfoById(@Param("cid") long cid);

    List<ContestVO> getWithinNext14DaysContests();
}

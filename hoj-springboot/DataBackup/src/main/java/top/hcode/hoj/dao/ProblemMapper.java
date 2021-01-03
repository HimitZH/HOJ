package top.hcode.hoj.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.pojo.entity.Problem;
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
public interface ProblemMapper extends BaseMapper<Problem> {
    List<ProblemVo> getProblemList(IPage page, @Param("pid") Long pid, @Param("keyword")String keyword,
                                   @Param("difficulty")Integer difficulty, @Param("tid")Long tid);
}

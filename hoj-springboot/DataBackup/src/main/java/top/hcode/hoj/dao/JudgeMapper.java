package top.hcode.hoj.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.Judge;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.hcode.hoj.pojo.vo.JudgeVo;

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
    List<JudgeVo> getCommonJudgeList(IPage page, @Param("pid") long pid, @Param("source") String source,
                                     @Param("language") String language, @Param("status") int status,
                                     @Param("username") String username, @Param("cid") long cid);
}

package top.hcode.hoj.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.vo.RankVo;
import top.hcode.hoj.pojo.entity.UserRecord;
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
public interface UserRecordMapper extends BaseMapper<UserRecord> {
    List<RankVo> getRankList(IPage page);
}

package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.judge.JudgeServer;

@Mapper
@Repository
public interface JudgeServerMapper extends BaseMapper<JudgeServer> {
}

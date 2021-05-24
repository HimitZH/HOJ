package top.hcode.hoj.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.JudgeServer;


@Mapper
@Repository
public interface JudgeServerMapper extends BaseMapper<JudgeServer> {

}

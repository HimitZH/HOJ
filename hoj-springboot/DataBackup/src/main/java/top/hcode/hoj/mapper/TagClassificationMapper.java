package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.problem.TagClassification;

/**
 * @Author Himit_ZH
 * @Date 2022/8/3
 */
@Mapper
@Repository
public interface TagClassificationMapper extends BaseMapper<TagClassification> {
}

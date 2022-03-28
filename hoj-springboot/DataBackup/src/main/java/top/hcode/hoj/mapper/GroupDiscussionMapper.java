package top.hcode.hoj.mapper;

import top.hcode.hoj.pojo.entity.discussion.Discussion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Mapper
@Repository
public interface GroupDiscussionMapper extends BaseMapper<Discussion> {

}

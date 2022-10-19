package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.vo.DiscussionVO;


@Mapper
@Repository
public interface DiscussionMapper extends BaseMapper<Discussion> {
    DiscussionVO getDiscussion(@Param("did") Integer did, @Param("uid") String uid);
}

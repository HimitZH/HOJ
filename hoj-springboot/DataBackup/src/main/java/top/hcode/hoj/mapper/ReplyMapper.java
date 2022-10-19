package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.discussion.Reply;
import top.hcode.hoj.pojo.vo.ReplyVO;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/5 22:07
 * @Description:
 */

@Mapper
@Repository
public interface ReplyMapper extends BaseMapper<Reply> {

    public List<ReplyVO> getAllReplyByCommentId(@Param("commentId") Integer commentId,
                                                @Param("myAndAdminUidList") List<String> myAndAdminUidList);
}
package top.hcode.hoj.service.discussion;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.pojo.entity.discussion.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.discussion.Reply;
import top.hcode.hoj.pojo.vo.CommentsVo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface CommentService extends IService<Comment> {
    IPage<CommentsVo> getCommentList(int limit, int currentPage, Long cid, Integer did, Boolean isRoot, String uid);
    List<Reply> getAllReplyByCommentId(Long cid, String uid, Boolean isRoot, Integer commentId);
}

package top.hcode.hoj.dao.discussion;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.pojo.entity.discussion.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.CommentVo;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface CommentEntityService extends IService<Comment> {

    IPage<CommentVo> getCommentList(int limit, int currentPage, Long cid, Integer did, Boolean isRoot, String uid);

    void updateCommentMsg(String recipientId, String senderId, String content, Integer discussionId, Long gid);

    void updateCommentLikeMsg(String recipientId, String senderId, Integer sourceId, String sourceType);
}

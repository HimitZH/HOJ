package top.hcode.hoj.service.oj;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ReplyDTO;
import top.hcode.hoj.pojo.entity.discussion.Comment;
import top.hcode.hoj.pojo.vo.CommentListVO;
import top.hcode.hoj.pojo.vo.CommentVO;
import top.hcode.hoj.pojo.vo.ReplyVO;

import java.util.List;

public interface CommentService {

    public CommonResult<CommentListVO> getComments(Long cid, Integer did, Integer limit, Integer currentPage);

    public CommonResult<CommentVO> addComment(Comment comment);

    public CommonResult<Void> deleteComment(Comment comment);

    public CommonResult<Void> addCommentLike(Integer cid, Boolean toLike, Integer sourceId, String sourceType);

    public CommonResult<List<ReplyVO>> getAllReply(Integer commentId, Long cid);

    public CommonResult<ReplyVO> addReply(ReplyDTO replyDto);

    public CommonResult<Void> deleteReply(ReplyDTO replyDto);
}

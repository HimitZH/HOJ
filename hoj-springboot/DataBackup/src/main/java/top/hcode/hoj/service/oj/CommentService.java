package top.hcode.hoj.service.oj;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ReplyDto;
import top.hcode.hoj.pojo.entity.discussion.Comment;
import top.hcode.hoj.pojo.entity.discussion.Reply;
import top.hcode.hoj.pojo.vo.CommentListVo;
import top.hcode.hoj.pojo.vo.CommentVo;

import java.util.List;

public interface CommentService {

    public CommonResult<CommentListVo> getComments(Long cid, Integer did, Integer limit, Integer currentPage);

    public CommonResult<CommentVo> addComment(Comment comment);

    public CommonResult<Void> deleteComment(Comment comment);

    public CommonResult<Void> addDiscussionLike(Integer cid, Boolean toLike, Integer sourceId, String sourceType);

    public CommonResult<List<Reply>> getAllReply(Integer commentId, Long cid);

    public CommonResult<Void> addReply(ReplyDto replyDto);

    public CommonResult<Void> deleteReply(ReplyDto replyDto);
}

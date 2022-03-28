package top.hcode.hoj.service.oj.impl;

import top.hcode.hoj.pojo.vo.ReplyVo;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.CommentManager;
import top.hcode.hoj.pojo.dto.ReplyDto;
import top.hcode.hoj.pojo.entity.discussion.Comment;
import top.hcode.hoj.pojo.entity.discussion.Reply;
import top.hcode.hoj.pojo.vo.CommentListVo;
import top.hcode.hoj.pojo.vo.CommentVo;
import top.hcode.hoj.service.oj.CommentService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 16:21
 * @Description:
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentManager commentManager;

    @Override
    public CommonResult<CommentListVo> getComments(Long cid, Integer did, Integer limit, Integer currentPage) {
        try {
            return CommonResult.successResponse(commentManager.getComments(cid, did, limit, currentPage));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<CommentVo> addComment(Comment comment) {
        try {
            return CommonResult.successResponse(commentManager.addComment(comment));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> deleteComment(Comment comment) {
        try {
            commentManager.deleteComment(comment);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> addCommentLike(Integer cid, Boolean toLike, Integer sourceId, String sourceType) {
        try {
            commentManager.addCommentLike(cid, toLike, sourceId, sourceType);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<List<ReplyVo>> getAllReply(Integer commentId, Long cid) {
        try {
            return CommonResult.successResponse(commentManager.getAllReply(commentId, cid));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<ReplyVo> addReply(ReplyDto replyDto) {
        try {
            return CommonResult.successResponse(commentManager.addReply(replyDto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> deleteReply(ReplyDto replyDto) {
        try {
            commentManager.deleteReply(replyDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}
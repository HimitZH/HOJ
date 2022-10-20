package top.hcode.hoj.service.oj.impl;

import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.exception.AccessException;
import top.hcode.hoj.manager.oj.CommentManager;
import top.hcode.hoj.pojo.dto.ReplyDTO;
import top.hcode.hoj.pojo.entity.discussion.Comment;
import top.hcode.hoj.pojo.vo.CommentListVO;
import top.hcode.hoj.pojo.vo.CommentVO;
import top.hcode.hoj.pojo.vo.ReplyVO;
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
    public CommonResult<CommentListVO> getComments(Long cid, Integer did, Integer limit, Integer currentPage) {
        try {
            return CommonResult.successResponse(commentManager.getComments(cid, did, limit, currentPage));
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<CommentVO> addComment(Comment comment) {
        try {
            return CommonResult.successResponse(commentManager.addComment(comment));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
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
        } catch (StatusForbiddenException | AccessException e) {
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
    public CommonResult<List<ReplyVO>> getAllReply(Integer commentId, Long cid) {
        try {
            return CommonResult.successResponse(commentManager.getAllReply(commentId, cid));
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<ReplyVO> addReply(ReplyDTO replyDto) {
        try {
            return CommonResult.successResponse(commentManager.addReply(replyDto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> deleteReply(ReplyDTO replyDto) {
        try {
            commentManager.deleteReply(replyDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}
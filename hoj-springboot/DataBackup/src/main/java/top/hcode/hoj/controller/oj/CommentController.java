package top.hcode.hoj.controller.oj;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ReplyDto;
import top.hcode.hoj.pojo.entity.discussion.Comment;
import top.hcode.hoj.pojo.entity.discussion.Reply;
import top.hcode.hoj.pojo.vo.CommentListVo;
import top.hcode.hoj.pojo.vo.CommentVo;
import top.hcode.hoj.pojo.vo.ReplyVo;
import top.hcode.hoj.service.oj.CommentService;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/5 15:41
 * @Description:
 */
@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;


    @GetMapping("/comments")
    public CommonResult<CommentListVo> getComments(@RequestParam(value = "cid", required = false) Long cid,
                                                   @RequestParam(value = "did", required = false) Integer did,
                                                   @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
                                                   @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage) {
        return commentService.getComments(cid, did, limit, currentPage);
    }


    @PostMapping("/comment")
    @RequiresPermissions("comment_add")
    @RequiresAuthentication
    public CommonResult<CommentVo> addComment(@RequestBody Comment comment) {
        return commentService.addComment(comment);
    }

    @DeleteMapping("/comment")
    @RequiresAuthentication
    public CommonResult<Void> deleteComment(@RequestBody Comment comment) {
        return commentService.deleteComment(comment);
    }

    @GetMapping("/comment-like")
    @RequiresAuthentication
    public CommonResult<Void> addCommentLike(@RequestParam("cid") Integer cid,
                                                @RequestParam("toLike") Boolean toLike,
                                                @RequestParam("sourceId") Integer sourceId,
                                                @RequestParam("sourceType") String sourceType) {
        return commentService.addCommentLike(cid, toLike, sourceId, sourceType);
    }

    @GetMapping("/reply")
    public CommonResult<List<ReplyVo>> getAllReply(@RequestParam("commentId") Integer commentId,
                                                   @RequestParam(value = "cid", required = false) Long cid) {
        return commentService.getAllReply(commentId, cid);
    }

    @PostMapping("/reply")
    @RequiresPermissions("reply_add")
    @RequiresAuthentication
    public CommonResult<ReplyVo> addReply(@RequestBody ReplyDto replyDto) {
        return commentService.addReply(replyDto);
    }

    @DeleteMapping("/reply")
    @RequiresAuthentication
    public CommonResult<Void> deleteReply(@RequestBody ReplyDto replyDto) {
        return commentService.deleteReply(replyDto);
    }

}

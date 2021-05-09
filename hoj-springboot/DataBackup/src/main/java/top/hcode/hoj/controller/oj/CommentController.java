package top.hcode.hoj.controller.oj;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.binarywang.java.emoji.EmojiConverter;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.Comment;
import top.hcode.hoj.pojo.entity.CommentLike;
import top.hcode.hoj.pojo.entity.Reply;
import top.hcode.hoj.pojo.vo.CommentsVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.impl.CommentLikeServiceImpl;
import top.hcode.hoj.service.impl.CommentServiceImpl;
import top.hcode.hoj.service.impl.ReplyServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.LinkedList;
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
    private CommentServiceImpl commentService;

    @Autowired
    private CommentLikeServiceImpl commentLikeService;

    @Autowired
    private ReplyServiceImpl replyService;

    private static EmojiConverter emojiConverter = EmojiConverter.getInstance();

    @GetMapping("/comments")
    public CommonResult getComments(@RequestParam(value = "cid", required = false) Long cid,
                                    @RequestParam(value = "did", required = false) Integer did,
                                    @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
                                    @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                                    HttpServletRequest request) {

        IPage<CommentsVo> commentList = commentService.getCommentList(limit, currentPage, cid, did);

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        HashMap<Integer, Boolean> commentLikeMap = new HashMap<>();

        if (userRolesVo != null) {
            // 如果是有登录 需要检查是否对评论有点赞
            List<Integer> commentIdList = new LinkedList<>();

            for (CommentsVo commentsVo : commentList.getRecords()) {
                commentIdList.add(commentsVo.getId());
            }

            if (commentIdList.size() > 0) {

                QueryWrapper<CommentLike> commentLikeQueryWrapper = new QueryWrapper<>();
                commentLikeQueryWrapper.in("cid", commentIdList);

                List<CommentLike> commentLikeList = commentLikeService.list(commentLikeQueryWrapper);

                // 如果存在记录需要修正Map为true
                for (CommentLike tmp : commentLikeList) {
                    commentLikeMap.put(tmp.getCid(), true);
                }
            }
        }

        return CommonResult.successResponse(MapUtil.builder()
                .put("commentList", commentList)
                .put("commentLikeMap", commentLikeMap).map(), "获取成功");

    }


    @PostMapping("/comment")
    @RequiresAuthentication
    public CommonResult addComment(@RequestBody Comment comment, HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        comment.setFromAvatar(userRolesVo.getAvatar())
                .setFromName(userRolesVo.getUsername())
                .setFromUid(userRolesVo.getUid());

        if (SecurityUtils.getSubject().hasRole("root")) {
            comment.setFromRole("root");
        } else if (SecurityUtils.getSubject().hasRole("admin")) {
            comment.setFromRole("admin");
        } else {
            comment.setFromRole("user");
        }

        // 带有表情的字符串转换为编码
        comment.setContent(emojiConverter.toHtml(comment.getContent()));

        boolean isOk = commentService.saveOrUpdate(comment);

        if (isOk) {
            CommentsVo commentsVo = new CommentsVo();
            commentsVo.setContent(comment.getContent());
            commentsVo.setId(comment.getId());
            commentsVo.setFromAvatar(comment.getFromAvatar());
            commentsVo.setFromName(comment.getFromName());
            commentsVo.setFromUid(comment.getFromUid());
            commentsVo.setLikeNum(0);
            commentsVo.setGmtCreate(comment.getGmtCreate());
            commentsVo.setReplyList(new LinkedList<>());
            return CommonResult.successResponse(commentsVo, "评论成功");
        } else {
            return CommonResult.errorResponse("评论失败，请重新尝试！");
        }

    }

    @GetMapping("/comment-like")
    @RequiresAuthentication
    @Transactional
    public CommonResult addDiscussionLike(@RequestParam("cid") Integer cid,
                                          @RequestParam("toLike") Boolean toLike,
                                          HttpServletRequest request) {

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        QueryWrapper<CommentLike> commentLikeQueryWrapper = new QueryWrapper<>();
        commentLikeQueryWrapper.eq("cid", cid).eq("uid", userRolesVo.getUid());

        CommentLike commentLike = commentLikeService.getOne(commentLikeQueryWrapper, false);

        if (toLike) { // 添加点赞
            if (commentLike == null) { // 如果不存在就添加
                boolean isSave = commentLikeService.saveOrUpdate(new CommentLike()
                        .setUid(userRolesVo.getUid())
                        .setCid(cid));
                if (!isSave) {
                    return CommonResult.errorResponse("点赞失败，请重试尝试！");
                }
            }
            // 点赞+1
            UpdateWrapper<Comment> commentUpdateWrapper = new UpdateWrapper<>();
            commentUpdateWrapper.setSql("like_num=like_num+1").eq("id", cid);
            commentService.update(commentUpdateWrapper);
            return CommonResult.successResponse(null, "点赞成功");
        } else { // 取消点赞
            if (commentLike != null) { // 如果存在就删除
                boolean isDelete = commentLikeService.removeById(commentLike.getId());
                if (!isDelete) {
                    return CommonResult.errorResponse("取消点赞失败，请重试尝试！");
                }
            }
            // 点赞-1
            UpdateWrapper<Comment> commentUpdateWrapper = new UpdateWrapper<>();
            commentUpdateWrapper.setSql("like_num=like_num-1").eq("id", cid);
            commentService.update(commentUpdateWrapper);
            return CommonResult.successResponse(null, "取消成功");
        }

    }

    @GetMapping("/reply")
    public CommonResult getAllReply(@RequestParam("commentId") Integer commentId) {

        QueryWrapper<Reply> replyQueryWrapper = new QueryWrapper<>();
        replyQueryWrapper.eq("comment_id", commentId);
        replyQueryWrapper.orderByDesc("gmt_create");

        List<Reply> replyList = replyService.list(replyQueryWrapper);

        return CommonResult.successResponse(replyList, "获取全部回复列表成功");
    }

    @PostMapping("/reply")
    @RequiresAuthentication
    public CommonResult addReply(@RequestBody Reply reply, HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        reply.setFromAvatar(userRolesVo.getAvatar())
                .setFromName(userRolesVo.getUsername())
                .setFromUid(userRolesVo.getUid());

        if (SecurityUtils.getSubject().hasRole("root")) {
            reply.setFromRole("root");
        } else if (SecurityUtils.getSubject().hasRole("admin")) {
            reply.setFromRole("admin");
        } else {
            reply.setFromRole("user");
        }
        // 带有表情的字符串转换为编码
        reply.setContent(emojiConverter.toHtml(reply.getContent()));

        boolean isOk = replyService.saveOrUpdate(reply);

        if (isOk) {
            return CommonResult.successResponse(reply, "评论成功");
        } else {
            return CommonResult.errorResponse("评论失败，请重新尝试！");
        }

    }


}

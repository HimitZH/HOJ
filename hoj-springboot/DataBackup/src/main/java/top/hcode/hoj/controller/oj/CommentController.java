package top.hcode.hoj.controller.oj;

import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.emoji.EmojiUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ReplyDto;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.CommentsVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.impl.*;

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

    @Autowired
    private DiscussionServiceImpl discussionService;

    @Autowired
    private UserAcproblemServiceImpl userAcproblemService;



    @GetMapping("/comments")
    public CommonResult getComments(@RequestParam(value = "cid", required = false) Long cid,
                                    @RequestParam(value = "did", required = false) Integer did,
                                    @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
                                    @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                                    HttpServletRequest request) {

        // 如果有登录，则获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        IPage<CommentsVo> commentList = commentService.getCommentList(limit, currentPage, cid, did, isRoot,
                userRolesVo != null ? userRolesVo.getUid() : null);

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
    @RequiresPermissions("comment_add")
    @RequiresAuthentication
    @Transactional
    public CommonResult addComment(@RequestBody Comment comment, HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        // 比赛外的评论 除管理员外 只有AC 10道以上才可评论
        if (comment.getCid() == null ) {
            if (!SecurityUtils.getSubject().hasRole("root")
                    && !SecurityUtils.getSubject().hasRole("admin")
                    && !SecurityUtils.getSubject().hasRole("problem_admin")) {
                QueryWrapper<UserAcproblem> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("uid", userRolesVo.getUid()).select("distinct pid");
                int userAcProblemCount = userAcproblemService.count(queryWrapper);

                if (userAcProblemCount < 10) {
                    return CommonResult.errorResponse("对不起，您暂时不能评论！请先去提交题目通过10道以上~", CommonResult.STATUS_FORBIDDEN);
                }
            }
        }

        comment.setFromAvatar(userRolesVo.getAvatar())
                .setFromName(userRolesVo.getUsername())
                .setFromUid(userRolesVo.getUid());

        if (SecurityUtils.getSubject().hasRole("root")) {
            comment.setFromRole("root");
        } else if (SecurityUtils.getSubject().hasRole("admin")
                || SecurityUtils.getSubject().hasRole("problem_admin")) {
            comment.setFromRole("admin");
        } else {
            comment.setFromRole("user");
        }

        // 带有表情的字符串转换为编码
        comment.setContent(EmojiUtil.toHtml(comment.getContent()));

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
            // 如果是讨论区的回复，发布成功需要添加统计该讨论的回复数
            if (comment.getDid() != null) {
                Discussion discussion = discussionService.getById(comment.getDid());
                if (discussion != null) {
                    discussion.setCommentNum(discussion.getCommentNum() + 1);
                    discussionService.updateById(discussion);
                    // 更新消息
                    commentService.updateCommentMsg(discussion.getUid(),
                            userRolesVo.getUid(),
                            comment.getContent(),
                            comment.getDid());
                }

            }
            return CommonResult.successResponse(commentsVo, "评论成功");
        } else {
            return CommonResult.errorResponse("评论失败，请重新尝试！");
        }

    }

    @DeleteMapping("/comment")
    @RequiresAuthentication
    @Transactional
    public CommonResult deleteComment(@RequestBody Comment comment, HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        // 如果不是评论本人 或者不是管理员 无权限删除该评论
        if (comment.getFromUid().equals(userRolesVo.getUid()) || SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin") || SecurityUtils.getSubject().hasRole("problem_admin")) {

            // 获取需要删除该评论的回复数
            int replyNum = replyService.count(new QueryWrapper<Reply>().eq("comment_id", comment.getId()));

            // 删除该数据 包括关联外键的reply表数据
            boolean isDeleteComment = commentService.removeById(comment.getId());

            // 同时需要删除该评论的回复表数据
            replyService.remove(new UpdateWrapper<Reply>().eq("comment_id", comment.getId()));

            if (isDeleteComment) {
                // 如果是讨论区的回复，删除成功需要减少统计该讨论的回复数
                if (comment.getDid() != null) {
                    UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
                    discussionUpdateWrapper.eq("id", comment.getDid())
                            .setSql("comment_num=comment_num-" + (replyNum + 1));
                    discussionService.update(discussionUpdateWrapper);
                }
                return CommonResult.successResponse(null, "删除成功");
            } else {
                return CommonResult.errorResponse("删除失败，请重新尝试");
            }

        } else {
            return CommonResult.errorResponse("无权删除该评论", CommonResult.STATUS_FORBIDDEN);
        }
    }

    @GetMapping("/comment-like")
    @RequiresAuthentication
    @Transactional
    public CommonResult addDiscussionLike(@RequestParam("cid") Integer cid,
                                          @RequestParam("toLike") Boolean toLike,
                                          @RequestParam("sourceId") Integer sourceId,
                                          @RequestParam("sourceType") String sourceType,
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
            Comment comment = commentService.getById(cid);
            if (comment != null) {
                comment.setLikeNum(comment.getLikeNum() + 1);
                commentService.updateById(comment);
                commentService.updateCommentLikeMsg(comment.getFromUid(), userRolesVo.getUid(), sourceId, sourceType);
            }
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
    public CommonResult getAllReply(@RequestParam("commentId") Integer commentId,
                                    @RequestParam(value = "cid", required = false) Long cid,
                                    HttpServletRequest request) {

        // 如果有登录，则获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        List<Reply> replyList = commentService.getAllReplyByCommentId(cid, userRolesVo != null ? userRolesVo.getUid() : null,
                isRoot, commentId);

        return CommonResult.successResponse(replyList, "获取全部回复列表成功");
    }

    @PostMapping("/reply")
    @RequiresPermissions("reply_add")
    @RequiresAuthentication
    public CommonResult addReply(@RequestBody ReplyDto replyDto, HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        Reply reply = replyDto.getReply();
        reply.setFromAvatar(userRolesVo.getAvatar())
                .setFromName(userRolesVo.getUsername())
                .setFromUid(userRolesVo.getUid());

        if (SecurityUtils.getSubject().hasRole("root")) {
            reply.setFromRole("root");
        } else if (SecurityUtils.getSubject().hasRole("admin") || SecurityUtils.getSubject().hasRole("problem_admin")) {
            reply.setFromRole("admin");
        } else {
            reply.setFromRole("user");
        }
        // 带有表情的字符串转换为编码
        reply.setContent(EmojiUtil.toHtml(reply.getContent()));

        boolean isOk = replyService.saveOrUpdate(reply);

        if (isOk) {
            // 如果是讨论区的回复，发布成功需要增加统计该讨论的回复数
            if (replyDto.getDid() != null) {
                UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
                discussionUpdateWrapper.eq("id", replyDto.getDid())
                        .setSql("comment_num=comment_num+1");
                discussionService.update(discussionUpdateWrapper);
                // 更新消息
                replyService.updateReplyMsg(replyDto.getDid(),
                        "Discussion",
                        reply.getContent(),
                        replyDto.getQuoteId(),
                        replyDto.getQuoteType(),
                        reply.getToUid(),
                        reply.getFromUid());
            }
            return CommonResult.successResponse(reply, "评论成功");
        } else {
            return CommonResult.errorResponse("评论失败，请重新尝试！");
        }
    }

    @DeleteMapping("/reply")
    @RequiresAuthentication
    public CommonResult deleteReply(@RequestBody ReplyDto replyDto, HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        Reply reply = replyDto.getReply();
        // 如果不是评论本人 或者不是管理员 无权限删除该评论
        if (reply.getFromUid().equals(userRolesVo.getUid()) || SecurityUtils.getSubject().hasRole("root")
                || SecurityUtils.getSubject().hasRole("admin") || SecurityUtils.getSubject().hasRole("problem_admin")) {
            // 删除该数据
            boolean isOk = replyService.removeById(reply.getId());
            if (isOk) {
                // 如果是讨论区的回复，删除成功需要减少统计该讨论的回复数
                if (replyDto.getDid() != null) {
                    UpdateWrapper<Discussion> discussionUpdateWrapper = new UpdateWrapper<>();
                    discussionUpdateWrapper.eq("id", replyDto.getDid())
                            .setSql("comment_num=comment_num-1");
                    discussionService.update(discussionUpdateWrapper);
                }
                return CommonResult.successResponse(null, "删除成功");
            } else {
                return CommonResult.errorResponse("删除失败，请重新尝试");
            }

        } else {
            return CommonResult.errorResponse("无权删除该回复", CommonResult.STATUS_FORBIDDEN);
        }
    }

}

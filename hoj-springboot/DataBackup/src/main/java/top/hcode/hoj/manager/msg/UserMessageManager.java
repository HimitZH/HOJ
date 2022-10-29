package top.hcode.hoj.manager.msg;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.contest.ContestEntityService;
import top.hcode.hoj.dao.discussion.CommentEntityService;
import top.hcode.hoj.dao.discussion.DiscussionEntityService;
import top.hcode.hoj.dao.discussion.ReplyEntityService;
import top.hcode.hoj.dao.msg.MsgRemindEntityService;
import top.hcode.hoj.dao.msg.UserSysNoticeEntityService;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.discussion.Comment;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.discussion.Reply;
import top.hcode.hoj.pojo.entity.msg.MsgRemind;
import top.hcode.hoj.pojo.entity.msg.UserSysNotice;
import top.hcode.hoj.pojo.vo.UserMsgVO;
import top.hcode.hoj.pojo.vo.UserUnreadMsgCountVO;
import top.hcode.hoj.shiro.AccountProfile;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 10:36
 * @Description:
 */
@Component
public class UserMessageManager {

    @Resource
    private MsgRemindEntityService msgRemindEntityService;

    @Resource
    private ContestEntityService contestEntityService;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private DiscussionEntityService discussionEntityService;

    @Resource
    private CommentEntityService commentEntityService;

    @Resource
    private ReplyEntityService replyEntityService;

    @Resource
    private UserSysNoticeEntityService userSysNoticeEntityService;

    public UserUnreadMsgCountVO getUnreadMsgCount() {
        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        UserUnreadMsgCountVO userUnreadMsgCount = msgRemindEntityService.getUserUnreadMsgCount(userRolesVo.getUid());
        if (userUnreadMsgCount == null) {
            userUnreadMsgCount = new UserUnreadMsgCountVO(0, 0, 0, 0, 0);
        }
        return userUnreadMsgCount;
    }


    public void cleanMsg(String type, Long id) throws StatusFailException {

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        boolean isOk = cleanMsgByType(type, id, userRolesVo.getUid());
        if (!isOk) {
            throw new StatusFailException("清空失败");
        }
    }


    public IPage<UserMsgVO> getCommentMsg(Integer limit, Integer currentPage) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 5;
        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        return getUserMsgList(userRolesVo.getUid(), "Discuss", limit, currentPage);
    }


    public IPage<UserMsgVO> getReplyMsg(Integer limit, Integer currentPage) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 5;

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        return getUserMsgList(userRolesVo.getUid(), "Reply", limit, currentPage);
    }


    public IPage<UserMsgVO> getLikeMsg(Integer limit, Integer currentPage) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 5;

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        return getUserMsgList(userRolesVo.getUid(), "Like", limit, currentPage);
    }


    private boolean cleanMsgByType(String type, Long id, String uid) {

        switch (type) {
            case "Like":
            case "Discuss":
            case "Reply":
                UpdateWrapper<MsgRemind> updateWrapper1 = new UpdateWrapper<>();
                updateWrapper1
                        .eq(id != null, "id", id)
                        .eq("recipient_id", uid);
                return msgRemindEntityService.remove(updateWrapper1);
            case "Sys":
            case "Mine":
                UpdateWrapper<UserSysNotice> updateWrapper2 = new UpdateWrapper<>();
                updateWrapper2
                        .eq(id != null, "id", id)
                        .eq("recipient_id", uid);
                return userSysNoticeEntityService.remove(updateWrapper2);
        }
        return false;
    }


    private IPage<UserMsgVO> getUserMsgList(String uid, String action, int limit, int currentPage) {
        Page<UserMsgVO> page = new Page<>(currentPage, limit);
        IPage<UserMsgVO> userMsgList = msgRemindEntityService.getUserMsg(page, uid, action);
        if (userMsgList.getTotal() > 0) {
            switch (action) {
                case "Discuss":  // 评论我的
                    return getUserDiscussMsgList(userMsgList);
                case "Reply": // 回复我的
                    return getUserReplyMsgList(userMsgList);
                case "Like":
                    return getUserLikeMsgList(userMsgList);
                default:
                    throw new RuntimeException("invalid action:" + action);
            }
        } else {
            return userMsgList;
        }
    }


    private IPage<UserMsgVO> getUserDiscussMsgList(IPage<UserMsgVO> userMsgList) {

        List<Integer> discussionIds = userMsgList.getRecords()
                .stream()
                .map(UserMsgVO::getSourceId)
                .collect(Collectors.toList());
        Collection<Discussion> discussions = discussionEntityService.listByIds(discussionIds);
        for (Discussion discussion : discussions) {
            for (UserMsgVO userMsgVo : userMsgList.getRecords()) {
                if (Objects.equals(discussion.getId(), userMsgVo.getSourceId())) {
                    userMsgVo.setSourceTitle(discussion.getTitle());
                    break;
                }
            }
        }
        applicationContext.getBean(UserMessageManager.class).updateUserMsgRead(userMsgList);
        return userMsgList;
    }

    private IPage<UserMsgVO> getUserReplyMsgList(IPage<UserMsgVO> userMsgList) {

        for (UserMsgVO userMsgVo : userMsgList.getRecords()) {
            if ("Discussion".equals(userMsgVo.getSourceType())) {
                Discussion discussion = discussionEntityService.getById(userMsgVo.getSourceId());
                if (discussion != null) {
                    userMsgVo.setSourceTitle(discussion.getTitle());
                } else {
                    userMsgVo.setSourceTitle("原讨论帖已被删除!【The original discussion post has been deleted!】");
                }
            } else if ("Contest".equals(userMsgVo.getSourceType())) {
                Contest contest = contestEntityService.getById(userMsgVo.getSourceId());
                if (contest != null) {
                    userMsgVo.setSourceTitle(contest.getTitle());
                } else {
                    userMsgVo.setSourceTitle("原比赛已被删除!【The original contest has been deleted!】");
                }
            }

            if ("Comment".equals(userMsgVo.getQuoteType())) {
                Comment comment = commentEntityService.getById(userMsgVo.getQuoteId());
                if (comment != null) {
                    String content;
                    if (comment.getContent().length() < 100) {
                        content = comment.getFromName() + " : "
                                + comment.getContent();

                    } else {
                        content = comment.getFromName() + " : "
                                + comment.getContent().substring(0, 100) + "...";
                    }
                    userMsgVo.setQuoteContent(content);
                } else {
                    userMsgVo.setQuoteContent("您的原评论信息已被删除！【Your original comments have been deleted!】");
                }

            } else if ("Reply".equals(userMsgVo.getQuoteType())) {
                Reply reply = replyEntityService.getById(userMsgVo.getQuoteId());
                if (reply != null) {
                    String content;
                    if (reply.getContent().length() < 100) {
                        content = reply.getFromName() + " : @" + reply.getToName() + "："
                                + reply.getContent();

                    } else {
                        content = reply.getFromName() + " : @" + reply.getToName() + "："
                                + reply.getContent().substring(0, 100) + "...";
                    }
                    userMsgVo.setQuoteContent(content);
                } else {
                    userMsgVo.setQuoteContent("您的原回复信息已被删除！【Your original reply has been deleted!】");
                }
            }

        }

        applicationContext.getBean(UserMessageManager.class).updateUserMsgRead(userMsgList);
        return userMsgList;
    }

    private IPage<UserMsgVO> getUserLikeMsgList(IPage<UserMsgVO> userMsgList) {
        for (UserMsgVO userMsgVo : userMsgList.getRecords()) {
            if ("Discussion".equals(userMsgVo.getSourceType())) {
                Discussion discussion = discussionEntityService.getById(userMsgVo.getSourceId());
                if (discussion != null) {
                    userMsgVo.setSourceTitle(discussion.getTitle());
                } else {
                    userMsgVo.setSourceTitle("原讨论帖已被删除!【The original discussion post has been deleted!】");
                }
            } else if ("Contest".equals(userMsgVo.getSourceType())) {
                Contest contest = contestEntityService.getById(userMsgVo.getSourceId());
                if (contest != null) {
                    userMsgVo.setSourceTitle(contest.getTitle());
                } else {
                    userMsgVo.setSourceTitle("原比赛已被删除!【The original contest has been deleted!】");
                }
            }
        }
        applicationContext.getBean(UserMessageManager.class).updateUserMsgRead(userMsgList);
        return userMsgList;
    }


    @Async
    public void updateUserMsgRead(IPage<UserMsgVO> userMsgList) {
        List<Long> idList = userMsgList.getRecords().stream()
                .filter(userMsgVo -> !userMsgVo.getState())
                .map(UserMsgVO::getId)
                .collect(Collectors.toList());
        if (idList.size() == 0) {
            return;
        }
        UpdateWrapper<MsgRemind> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", idList)
                .set("state", true);
        msgRemindEntityService.update(null, updateWrapper);
    }

}
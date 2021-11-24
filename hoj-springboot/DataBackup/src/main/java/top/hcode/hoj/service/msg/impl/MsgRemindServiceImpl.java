package top.hcode.hoj.service.msg.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.CommentMapper;
import top.hcode.hoj.dao.DiscussionMapper;
import top.hcode.hoj.dao.MsgRemindMapper;
import top.hcode.hoj.dao.ReplyMapper;

import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.discussion.Comment;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.discussion.Reply;
import top.hcode.hoj.pojo.entity.msg.MsgRemind;
import top.hcode.hoj.pojo.entity.msg.UserSysNotice;
import top.hcode.hoj.pojo.vo.UserMsgVo;
import top.hcode.hoj.pojo.vo.UserUnreadMsgCountVo;
import top.hcode.hoj.service.contest.impl.ContestServiceImpl;
import top.hcode.hoj.service.msg.MsgRemindService;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:36
 * @Description:
 */
@Service
public class MsgRemindServiceImpl extends ServiceImpl<MsgRemindMapper, MsgRemind> implements MsgRemindService {

    @Resource
    private MsgRemindMapper msgRemindMapper;

    @Resource
    private DiscussionMapper discussionMapper;

    @Resource
    private ContestServiceImpl contestService;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private ReplyMapper replyMapper;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserSysNoticeServiceImpl userSysNoticeService;

    @Override
    public UserUnreadMsgCountVo getUserUnreadMsgCount(String uid) {
        return msgRemindMapper.getUserUnreadMsgCount(uid);
    }

    @Override
    public boolean cleanMsgByType(String type, Long id, String uid) {

        switch (type) {
            case "Like":
            case "Discuss":
            case "Reply":
                UpdateWrapper<MsgRemind> updateWrapper1 = new UpdateWrapper<>();
                updateWrapper1
                        .eq(id != null, "id", id)
                        .eq("recipient_id", uid);
                return msgRemindMapper.delete(updateWrapper1) > 0;
            case "Sys":
            case "Mine":
                UpdateWrapper<UserSysNotice> updateWrapper2 = new UpdateWrapper<>();
                updateWrapper2
                        .eq(id != null, "id", id)
                        .eq("recipient_id", uid);
                return userSysNoticeService.remove(updateWrapper2);
        }
        return false;
    }

    @Override
    public IPage<UserMsgVo> getUserMsgList(String uid, String action, int limit, int currentPage) {
        Page<UserMsgVo> page = new Page<>(currentPage, limit);
        IPage<UserMsgVo> userMsgList = msgRemindMapper.getUserMsg(page, uid, action);
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

    @Async
    public void updateUserMsgRead(IPage<UserMsgVo> userMsgList) {
        List<Long> idList = userMsgList.getRecords().stream()
                .filter(userMsgVo -> !userMsgVo.getState())
                .map(UserMsgVo::getId)
                .collect(Collectors.toList());
        if (idList.size() == 0) {
            return;
        }
        UpdateWrapper<MsgRemind> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", idList)
                .set("state", true);
        msgRemindMapper.update(null, updateWrapper);
    }

    public IPage<UserMsgVo> getUserDiscussMsgList(IPage<UserMsgVo> userMsgList) {

        List<Integer> discussionIds = userMsgList.getRecords()
                .stream()
                .map(UserMsgVo::getSourceId)
                .collect(Collectors.toList());
        Collection<Discussion> discussions = discussionMapper.selectBatchIds(discussionIds);
        for (Discussion discussion : discussions) {
            for (UserMsgVo userMsgVo : userMsgList.getRecords()) {
                if (Objects.equals(discussion.getId(), userMsgVo.getSourceId())) {
                    userMsgVo.setSourceTitle(discussion.getTitle());
                    break;
                }
            }
        }
        applicationContext.getBean(MsgRemindServiceImpl.class).updateUserMsgRead(userMsgList);
        return userMsgList;
    }

    public IPage<UserMsgVo> getUserReplyMsgList(IPage<UserMsgVo> userMsgList) {

        for (UserMsgVo userMsgVo : userMsgList.getRecords()) {
            if ("Discussion".equals(userMsgVo.getSourceType())) {
                Discussion discussion = discussionMapper.selectById(userMsgVo.getSourceId());
                userMsgVo.setSourceTitle(discussion.getTitle());
            } else if ("Contest".equals(userMsgVo.getSourceType())) {
                Contest contest = contestService.getById(userMsgVo.getSourceId());
                userMsgVo.setSourceTitle(contest.getTitle());
            }

            if ("Comment".equals(userMsgVo.getQuoteType())) {
                Comment comment = commentMapper.selectById(userMsgVo.getQuoteId());
                String content;
                if (comment.getContent().length() < 100) {
                    content = comment.getFromName() + " : "
                            + comment.getContent();

                } else {
                    content = comment.getFromName() + " : "
                            + comment.getContent().substring(0, 100) + "...";
                }
                userMsgVo.setQuoteContent(content);

            } else if ("Reply".equals(userMsgVo.getQuoteType())) {
                Reply reply = replyMapper.selectById(userMsgVo.getQuoteId());

                String content;
                if (reply.getContent().length() < 100) {
                    content = reply.getFromName() + " : @" + reply.getToName() + "："
                            + reply.getContent();

                } else {
                    content = reply.getFromName() + " : @" + reply.getToName() + "："
                            + reply.getContent().substring(0, 100) + "...";
                }
                userMsgVo.setQuoteContent(content);
            }

        }

        applicationContext.getBean(MsgRemindServiceImpl.class).updateUserMsgRead(userMsgList);
        return userMsgList;
    }

    public IPage<UserMsgVo> getUserLikeMsgList(IPage<UserMsgVo> userMsgList) {
        for (UserMsgVo userMsgVo : userMsgList.getRecords()) {
            if ("Discussion".equals(userMsgVo.getSourceType())) {
                Discussion discussion = discussionMapper.selectById(userMsgVo.getSourceId());
                userMsgVo.setSourceTitle(discussion.getTitle());
            } else if ("Contest".equals(userMsgVo.getSourceType())) {
                Contest contest = contestService.getById(userMsgVo.getSourceId());
                userMsgVo.setSourceTitle(contest.getTitle());
            }
        }
        applicationContext.getBean(MsgRemindServiceImpl.class).updateUserMsgRead(userMsgList);
        return userMsgList;
    }
}
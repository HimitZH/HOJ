package top.hcode.hoj.dao.discussion.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hcode.hoj.mapper.DiscussionMapper;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.msg.MsgRemind;
import top.hcode.hoj.pojo.vo.DiscussionVO;
import top.hcode.hoj.dao.discussion.DiscussionEntityService;
import top.hcode.hoj.dao.msg.MsgRemindEntityService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/4 22:31
 * @Description:
 */
@Service
public class DiscussionEntityServiceImpl extends ServiceImpl<DiscussionMapper, Discussion> implements DiscussionEntityService {

    @Autowired
    private DiscussionMapper discussionMapper;

    @Override
    public DiscussionVO getDiscussion(Integer did, String uid) {
        return discussionMapper.getDiscussion(did, uid);
    }

    @Resource
    private MsgRemindEntityService msgRemindEntityService;

    @Async
    public void updatePostLikeMsg(String recipientId, String senderId, Integer discussionId, Long gid) {

        MsgRemind msgRemind = new MsgRemind();
        msgRemind.setAction("Like_Post")
                .setRecipientId(recipientId)
                .setSenderId(senderId)
                .setSourceId(discussionId)
                .setSourceType("Discussion")
                .setUrl("/discussion-detail/" + discussionId);

        if (gid != null) {
            msgRemind.setUrl("/group/" + gid + "/discussion-detail/" + discussionId);
        } else {
            msgRemind.setUrl("/discussion-detail/" + discussionId);
        }

        msgRemindEntityService.saveOrUpdate(msgRemind);
    }
}
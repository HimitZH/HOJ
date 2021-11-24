package top.hcode.hoj.service.discussion.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.DiscussionMapper;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.msg.MsgRemind;
import top.hcode.hoj.pojo.vo.DiscussionVo;
import top.hcode.hoj.service.discussion.DiscussionService;
import top.hcode.hoj.service.msg.impl.MsgRemindServiceImpl;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/4 22:31
 * @Description:
 */
@Service
public class DiscussionServiceImpl extends ServiceImpl<DiscussionMapper, Discussion> implements DiscussionService {

    @Autowired
    private DiscussionMapper discussionMapper;

    @Override
    public DiscussionVo getDiscussion(Integer did, String uid) {
        return discussionMapper.getDiscussion(did, uid);
    }

    @Resource
    private MsgRemindServiceImpl msgRemindService;

    @Async
    public void updatePostLikeMsg(String recipientId, String senderId, Integer discussionId) {

        MsgRemind msgRemind = new MsgRemind();
        msgRemind.setAction("Like_Post")
                .setRecipientId(recipientId)
                .setSenderId(senderId)
                .setSourceId(discussionId)
                .setSourceType("Discussion")
                .setUrl("/discussion-detail/" + discussionId);
        msgRemindService.saveOrUpdate(msgRemind);
    }
}
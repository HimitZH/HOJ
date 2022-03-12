package top.hcode.hoj.dao.discussion.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hcode.hoj.mapper.ReplyMapper;
import top.hcode.hoj.pojo.entity.msg.MsgRemind;
import top.hcode.hoj.pojo.entity.discussion.Reply;
import top.hcode.hoj.dao.discussion.ReplyEntityService;
import top.hcode.hoj.dao.msg.MsgRemindEntityService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/5 22:09
 * @Description:
 */
@Service
public class ReplyEntityServiceImpl extends ServiceImpl<ReplyMapper, Reply> implements ReplyEntityService {

    @Resource
    private MsgRemindEntityService msgRemindEntityService;

    @Async
    @Override
    public void updateReplyMsg(Integer sourceId, String sourceType, String content,
                               Integer quoteId, String quoteType, String recipientId,String senderId) {
        if (content.length() > 200) {
            content = content.substring(0, 200) + "...";
        }

        MsgRemind msgRemind = new MsgRemind();
        msgRemind.setAction("Reply")
                .setSourceId(sourceId)
                .setSourceType(sourceType)
                .setSourceContent(content)
                .setQuoteId(quoteId)
                .setQuoteType(quoteType)
                .setUrl(sourceType.equals("Discussion") ? "/discussion-detail/" + sourceId : "/contest/" + sourceId + "/comment")
                .setRecipientId(recipientId)
                .setSenderId(senderId);

        msgRemindEntityService.saveOrUpdate(msgRemind);
    }
}
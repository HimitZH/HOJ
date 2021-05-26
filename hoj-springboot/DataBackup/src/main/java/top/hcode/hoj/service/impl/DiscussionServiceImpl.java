package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.DiscussionMapper;
import top.hcode.hoj.pojo.entity.Discussion;
import top.hcode.hoj.pojo.vo.DiscussionVo;
import top.hcode.hoj.service.DiscussionService;

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
}
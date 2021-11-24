package top.hcode.hoj.service.discussion.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.DiscussionLikeMapper;
import top.hcode.hoj.pojo.entity.discussion.DiscussionLike;
import top.hcode.hoj.service.discussion.DiscussionLikeService;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/4 22:31
 * @Description:
 */
@Service
public class DiscussionLikeServiceImpl extends ServiceImpl<DiscussionLikeMapper, DiscussionLike> implements DiscussionLikeService {
}
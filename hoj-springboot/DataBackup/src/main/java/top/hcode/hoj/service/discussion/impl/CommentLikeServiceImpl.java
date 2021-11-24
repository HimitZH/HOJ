package top.hcode.hoj.service.discussion.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.CommentLikeMapper;
import top.hcode.hoj.pojo.entity.discussion.CommentLike;
import top.hcode.hoj.service.discussion.CommentLikeService;


/**
 * @Author: Himit_ZH
 * @Date: 2021/5/4 22:31
 * @Description:
 */
@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements CommentLikeService {
}
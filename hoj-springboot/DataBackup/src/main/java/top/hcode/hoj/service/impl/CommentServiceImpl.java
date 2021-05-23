package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.entity.Comment;
import top.hcode.hoj.dao.CommentMapper;
import top.hcode.hoj.pojo.vo.CommentsVo;
import top.hcode.hoj.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public IPage<CommentsVo> getCommentList(int limit, int currentPage, Long cid, Integer did) {
        //新建分页
        Page<CommentsVo> page = new Page<>(currentPage, limit);
        return commentMapper.getCommentList(page, cid, did);
    }
}

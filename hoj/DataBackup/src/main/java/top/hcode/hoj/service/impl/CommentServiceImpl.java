package top.hcode.hoj.service.impl;

import top.hcode.hoj.pojo.entity.Comment;
import top.hcode.hoj.dao.CommentMapper;
import top.hcode.hoj.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

}

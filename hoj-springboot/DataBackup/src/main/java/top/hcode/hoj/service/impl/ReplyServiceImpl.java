package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.ReplyMapper;
import top.hcode.hoj.pojo.entity.Reply;
import top.hcode.hoj.service.ReplyService;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/5 22:09
 * @Description:
 */
@Service
public class ReplyServiceImpl extends ServiceImpl<ReplyMapper, Reply> implements ReplyService {
}
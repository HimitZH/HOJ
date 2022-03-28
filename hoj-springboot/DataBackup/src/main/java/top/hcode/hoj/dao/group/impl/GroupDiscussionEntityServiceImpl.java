package top.hcode.hoj.dao.group.impl;

import top.hcode.hoj.dao.group.GroupDiscussionEntityService;
import top.hcode.hoj.mapper.GroupDiscussionMapper;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Service
public class GroupDiscussionEntityServiceImpl extends ServiceImpl<GroupDiscussionMapper, Discussion> implements GroupDiscussionEntityService {
}

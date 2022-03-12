package top.hcode.hoj.dao.discussion.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.mapper.DiscussionReportMapper;
import top.hcode.hoj.pojo.entity.discussion.DiscussionReport;
import top.hcode.hoj.dao.discussion.DiscussionReportEntityService;

/**
 * @Author: Himit_ZH
 * @Date: 2021/5/11 21:46
 * @Description:
 */
@Service
public class DiscussionReportEntityServiceImpl extends ServiceImpl<DiscussionReportMapper, DiscussionReport> implements DiscussionReportEntityService {
}
package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.entity.Announcement;
import top.hcode.hoj.pojo.entity.ContestAnnouncement;
import top.hcode.hoj.dao.AnnouncementMapper;
import top.hcode.hoj.pojo.vo.AnnouncementVo;
import top.hcode.hoj.service.AnnouncementService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public IPage<AnnouncementVo> getAnnouncementList(int limit, int currentPage) {
        //新建分页
        Page<AnnouncementVo> page = new Page<>(currentPage, limit);
        return announcementMapper.getAnnouncementList(page);
    }

    @Override
    public IPage<AnnouncementVo> getContestAnnouncement(long cid,int limit, int currentPage) {
        Page<AnnouncementVo> page = new Page<>(currentPage, limit);
        return announcementMapper.getContestAnnouncement(page,cid);
    }
}

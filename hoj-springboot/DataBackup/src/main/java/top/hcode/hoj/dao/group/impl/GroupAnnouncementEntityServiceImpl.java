package top.hcode.hoj.dao.group.impl;

import top.hcode.hoj.dao.group.GroupAnnouncementEntityService;
import top.hcode.hoj.mapper.GroupAnnouncementMapper;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.vo.AnnouncementVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Service
public class GroupAnnouncementEntityServiceImpl extends ServiceImpl<GroupAnnouncementMapper, Announcement> implements GroupAnnouncementEntityService {

    @Autowired
    private GroupAnnouncementMapper groupAnnouncementMapper;

    @Override
    public IPage<AnnouncementVo> getAnnouncementList(int limit, int currentPage, Long gid) {
        IPage<AnnouncementVo> iPage = new Page<>(currentPage, limit);

        List<AnnouncementVo> announcementList = groupAnnouncementMapper.getAnnouncementList(iPage, gid);

        return iPage.setRecords(announcementList);
    }

    @Override
    public IPage<AnnouncementVo> getAdminAnnouncementList(int limit, int currentPage, Long gid) {
        IPage<AnnouncementVo> iPage = new Page<>(currentPage, limit);

        List<AnnouncementVo> announcementList = groupAnnouncementMapper.getAdminAnnouncementList(iPage, gid);

        return iPage.setRecords(announcementList);
    }

}

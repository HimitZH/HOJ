package top.hcode.hoj.service.admin.announcement.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.admin.announcement.AdminAnnouncementManager;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.vo.AnnouncementVO;
import top.hcode.hoj.service.admin.announcement.AdminAnnouncementService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 22:15
 * @Description:
 */
@Service
public class AdminAnnouncementServiceImpl implements AdminAnnouncementService {

    @Resource
    private AdminAnnouncementManager adminAnnouncementManager;

    @Override
    public CommonResult<IPage<AnnouncementVO>> getAnnouncementList(Integer limit, Integer currentPage) {
        return CommonResult.successResponse(adminAnnouncementManager.getAnnouncementList(limit, currentPage));
    }

    @Override
    public CommonResult<Void> deleteAnnouncement(Long aid) {
        try {
            adminAnnouncementManager.deleteAnnouncement(aid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> addAnnouncement(Announcement announcement) {
        try {
            adminAnnouncementManager.addAnnouncement(announcement);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> updateAnnouncement(Announcement announcement) {
        try {
            adminAnnouncementManager.updateAnnouncement(announcement);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
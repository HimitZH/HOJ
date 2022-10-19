package top.hcode.hoj.service.admin.contest.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.admin.contest.AdminContestAnnouncementManager;
import top.hcode.hoj.pojo.dto.AnnouncementDTO;
import top.hcode.hoj.pojo.vo.AnnouncementVO;
import top.hcode.hoj.service.admin.contest.AdminContestAnnouncementService;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 11:18
 * @Description:
 */

@Service
public class AdminContestAnnouncementServiceImpl implements AdminContestAnnouncementService {

    @Autowired
    private AdminContestAnnouncementManager adminContestAnnouncementManager;

    @Override
    public CommonResult<IPage<AnnouncementVO>> getAnnouncementList(Integer limit, Integer currentPage, Long cid) {
        IPage<AnnouncementVO> announcementList = adminContestAnnouncementManager.getAnnouncementList(limit, currentPage, cid);
        return CommonResult.successResponse(announcementList);
    }

    @Override
    public CommonResult<Void> deleteAnnouncement(Long aid) {
        try {
            adminContestAnnouncementManager.deleteAnnouncement(aid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> addAnnouncement(AnnouncementDTO announcementDto) {
        try {
            adminContestAnnouncementManager.addAnnouncement(announcementDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> updateAnnouncement(AnnouncementDTO announcementDto) {
        try {
            adminContestAnnouncementManager.updateAnnouncement(announcementDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
package top.hcode.hoj.service.group.contest.impl;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.group.contest.GroupContestAnnouncementManager;
import top.hcode.hoj.pojo.dto.AnnouncementDTO;
import top.hcode.hoj.pojo.vo.AnnouncementVO;
import top.hcode.hoj.service.group.contest.GroupContestAnnouncementService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Service
public class GroupContestAnnouncementServiceImpl implements GroupContestAnnouncementService {

    @Autowired
    private GroupContestAnnouncementManager groupContestAnnouncementManager;

    @Override
    public CommonResult<IPage<AnnouncementVO>> getContestAnnouncementList(Integer limit, Integer currentPage, Long cid) {
        try {
            return CommonResult.successResponse(groupContestAnnouncementManager.getContestAnnouncementList(limit, currentPage, cid));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        }
    }

    @Override
    public CommonResult<Void> addContestAnnouncement(AnnouncementDTO announcementDto) {
        try {
            groupContestAnnouncementManager.addContestAnnouncement(announcementDto);
            return CommonResult.successResponse();
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<Void> updateContestAnnouncement(AnnouncementDTO announcementDto) {
        try {
            groupContestAnnouncementManager.updateContestAnnouncement(announcementDto);
            return CommonResult.successResponse();
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }

    @Override
    public CommonResult<Void> deleteContestAnnouncement(Long aid, Long cid) {
        try {
            groupContestAnnouncementManager.deleteContestAnnouncement(aid, cid);
            return CommonResult.successResponse();
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }
}

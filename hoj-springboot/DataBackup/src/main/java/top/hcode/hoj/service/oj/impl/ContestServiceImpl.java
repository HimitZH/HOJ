package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.ContestManager;
import top.hcode.hoj.pojo.dto.ContestPrintDto;
import top.hcode.hoj.pojo.dto.ContestRankDto;
import top.hcode.hoj.pojo.dto.RegisterContestDto;
import top.hcode.hoj.pojo.dto.UserReadContestAnnouncementDto;
import top.hcode.hoj.pojo.entity.common.Announcement;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.service.oj.ContestService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/12 15:06
 * @Description:
 */
@Service
public class ContestServiceImpl implements ContestService {

    @Resource
    private ContestManager contestManager;

    @Override
    public CommonResult<IPage<ContestVo>> getContestList(Integer limit, Integer currentPage, Integer status, Integer type, String keyword) {
        return CommonResult.successResponse(contestManager.getContestList(limit, currentPage, status, type, keyword));
    }

    @Override
    public CommonResult<ContestVo> getContestInfo(Long cid) {
        try {
            return CommonResult.successResponse(contestManager.getContestInfo(cid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> toRegisterContest(RegisterContestDto registerContestDto) {
        try {
            contestManager.toRegisterContest(registerContestDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<AccessVo> getContestAccess(Long cid) {
        try {
            return CommonResult.successResponse(contestManager.getContestAccess(cid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<List<ContestProblemVo>> getContestProblem(Long cid) {
        try {
            return CommonResult.successResponse(contestManager.getContestProblem(cid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<ProblemInfoVo> getContestProblemDetails(Long cid, String displayId) {
        try {
            return CommonResult.successResponse(contestManager.getContestProblemDetails(cid, displayId));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        }
    }

    @Override
    public CommonResult<IPage<JudgeVo>> getContestSubmissionList(Integer limit,
                                                                 Integer currentPage,
                                                                 Boolean onlyMine,
                                                                 String displayId,
                                                                 Integer searchStatus,
                                                                 String searchUsername,
                                                                 Long searchCid,
                                                                 Boolean beforeContestSubmit,
                                                                 Boolean completeProblemID) {
        try {
            return CommonResult.successResponse(contestManager.getContestSubmissionList(limit,
                    currentPage,
                    onlyMine,
                    displayId,
                    searchStatus,
                    searchUsername,
                    searchCid,
                    beforeContestSubmit,
                    completeProblemID));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<IPage> getContestRank(ContestRankDto contestRankDto) {
        try {
            return CommonResult.successResponse(contestManager.getContestRank(contestRankDto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<IPage<AnnouncementVo>> getContestAnnouncement(Long cid, Integer limit, Integer currentPage) {
        try {
            return CommonResult.successResponse(contestManager.getContestAnnouncement(cid, limit, currentPage));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<List<Announcement>> getContestUserNotReadAnnouncement(UserReadContestAnnouncementDto userReadContestAnnouncementDto) {

        return CommonResult.successResponse(contestManager.getContestUserNotReadAnnouncement(userReadContestAnnouncementDto));
    }

    @Override
    public CommonResult<Void> submitPrintText(ContestPrintDto contestPrintDto) {
        try {
            contestManager.submitPrintText(contestPrintDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}
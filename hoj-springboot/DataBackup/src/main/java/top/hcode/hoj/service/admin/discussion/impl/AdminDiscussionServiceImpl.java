package top.hcode.hoj.service.admin.discussion.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.admin.discussion.AdminDiscussionManager;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.discussion.DiscussionReport;
import top.hcode.hoj.pojo.vo.DiscussionReportVO;
import top.hcode.hoj.service.admin.discussion.AdminDiscussionService;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 16:05
 * @Description:
 */

@Service
public class AdminDiscussionServiceImpl implements AdminDiscussionService {

    @Autowired
    private AdminDiscussionManager adminDiscussionManager;

    @Override
    public CommonResult<Void> updateDiscussion(Discussion discussion) {
        try {
            adminDiscussionManager.updateDiscussion(discussion);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> removeDiscussion(List<Integer> didList) {
        try {
            adminDiscussionManager.removeDiscussion(didList);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<IPage<DiscussionReportVO>> getDiscussionReport(Integer limit, Integer currentPage) {
        IPage<DiscussionReportVO> discussionReportIPage = adminDiscussionManager.getDiscussionReport(limit, currentPage);
        return CommonResult.successResponse(discussionReportIPage);
    }


    @Override
    public CommonResult<Void> updateDiscussionReport(DiscussionReport discussionReport) {
        try {
            adminDiscussionManager.updateDiscussionReport(discussionReport);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
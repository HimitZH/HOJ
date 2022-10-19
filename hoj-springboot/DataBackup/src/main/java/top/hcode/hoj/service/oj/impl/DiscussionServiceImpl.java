package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.exception.AccessException;
import top.hcode.hoj.manager.oj.DiscussionManager;
import top.hcode.hoj.pojo.entity.discussion.Discussion;
import top.hcode.hoj.pojo.entity.discussion.DiscussionReport;
import top.hcode.hoj.pojo.entity.problem.Category;
import top.hcode.hoj.pojo.vo.DiscussionVO;
import top.hcode.hoj.service.oj.DiscussionService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 15:47
 * @Description:
 */
@Service
public class DiscussionServiceImpl implements DiscussionService {

    @Resource
    private DiscussionManager discussionManager;

    @Override
    public CommonResult<IPage<Discussion>> getDiscussionList(Integer limit, Integer currentPage, Integer categoryId, String pid, Boolean onlyMine, String keyword, Boolean admin) {
        return CommonResult.successResponse(discussionManager.getDiscussionList(limit, currentPage, categoryId, pid, onlyMine, keyword, admin));
    }

    @Override
    public CommonResult<DiscussionVO> getDiscussion(Integer did) {
        try {
            return CommonResult.successResponse(discussionManager.getDiscussion(did));
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusForbiddenException | AccessException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> addDiscussion(Discussion discussion) {
        try {
            discussionManager.addDiscussion(discussion);
            return CommonResult.successResponse();
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        }
    }

    @Override
    public CommonResult<Void> updateDiscussion(Discussion discussion) {
        try {
            discussionManager.updateDiscussion(discussion);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> removeDiscussion(Integer did) {
        try {
            discussionManager.removeDiscussion(did);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> addDiscussionLike(Integer did, Boolean toLike) {
        try {
            discussionManager.addDiscussionLike(did, toLike);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<List<Category>> getDiscussionCategory() {
        return CommonResult.successResponse(discussionManager.getDiscussionCategory());
    }

    @Override
    public CommonResult<List<Category>> upsertDiscussionCategory(List<Category> categoryList) {
        try {
            return CommonResult.successResponse(discussionManager.upsertDiscussionCategory(categoryList));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> addDiscussionReport(DiscussionReport discussionReport) {
        try {
            discussionManager.addDiscussionReport(discussionReport);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
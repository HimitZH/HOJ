package top.hcode.hoj.service.admin.problem.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.admin.problem.AdminGroupProblemManager;
import top.hcode.hoj.pojo.dto.ChangeGroupProblemProgressDTO;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.service.admin.problem.AdminGroupProblemService;

import javax.annotation.Resource;

/**
 * @Author Himit_ZH
 * @Date 2022/4/13
 */
@Service
public class AdminGroupProblemServiceImpl implements AdminGroupProblemService {

    @Resource
    private AdminGroupProblemManager adminGroupProblemManager;

    @Override
    public CommonResult<IPage<Problem>> getProblemList(Integer currentPage, Integer limit, String keyword, Long gid) {
        return CommonResult.successResponse(adminGroupProblemManager.list(currentPage, limit, keyword, gid));
    }

    @Override
    public CommonResult<Void> changeProgress(ChangeGroupProblemProgressDTO changeGroupProblemProgressDto) {
        try {
            adminGroupProblemManager.changeProgress(changeGroupProblemProgressDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FAIL);
        }
    }
}

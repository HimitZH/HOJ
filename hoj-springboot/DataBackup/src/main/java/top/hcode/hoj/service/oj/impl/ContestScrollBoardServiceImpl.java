package top.hcode.hoj.service.oj.impl;

import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.oj.ContestScrollBoardManager;
import top.hcode.hoj.pojo.vo.ContestScrollBoardInfoVo;
import top.hcode.hoj.pojo.vo.ContestScrollBoardSubmissionVo;
import top.hcode.hoj.service.oj.ContestScrollBoardService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author Himit_ZH
 * @Date 2022/10/3
 */
@Service
public class ContestScrollBoardServiceImpl implements ContestScrollBoardService{

    @Resource
    private ContestScrollBoardManager contestScrollBoardManager;

    @Override
    public CommonResult<ContestScrollBoardInfoVo> getContestScrollBoardInfo(Long cid) {
        try {
            return CommonResult.successResponse(contestScrollBoardManager.getContestScrollBoardInfo(cid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<List<ContestScrollBoardSubmissionVo>> getContestScrollBoardSubmission(Long cid) {
        try {
            return CommonResult.successResponse(contestScrollBoardManager.getContestScrollBoardSubmission(cid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}

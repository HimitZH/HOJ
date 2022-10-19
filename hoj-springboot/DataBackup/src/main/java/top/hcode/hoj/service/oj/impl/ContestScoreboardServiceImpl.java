package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.ContestScoreboardManager;
import top.hcode.hoj.pojo.dto.ContestRankDTO;
import top.hcode.hoj.pojo.vo.ContestOutsideInfoVO;
import top.hcode.hoj.service.oj.ContestScoreboardService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 22:18
 * @Description:
 */
@Service
public class ContestScoreboardServiceImpl implements ContestScoreboardService {

    @Resource
    private ContestScoreboardManager contestScoreboardManager;

    @Override
    public CommonResult<ContestOutsideInfoVO> getContestOutsideInfo(Long cid) {
        try {
            return CommonResult.successResponse(contestScoreboardManager.getContestOutsideInfo(cid));
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<IPage> getContestOutsideScoreboard(ContestRankDTO contestRankDto) {
        try {
            return CommonResult.successResponse(contestScoreboardManager.getContestOutsideScoreboard(contestRankDto));
        }  catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
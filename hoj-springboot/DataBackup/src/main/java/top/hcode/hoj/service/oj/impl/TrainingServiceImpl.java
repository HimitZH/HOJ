package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusAccessDeniedException;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.TrainingManager;
import top.hcode.hoj.pojo.dto.RegisterTrainingDto;
import top.hcode.hoj.pojo.vo.AccessVo;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.pojo.vo.TrainingRankVo;
import top.hcode.hoj.pojo.vo.TrainingVo;
import top.hcode.hoj.service.oj.TrainingService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 20:40
 * @Description:
 */
@Service
public class TrainingServiceImpl implements TrainingService {

    @Resource
    private TrainingManager trainingManager;

    @Override
    public CommonResult<IPage<TrainingVo>> getTrainingList(Integer limit, Integer currentPage, String keyword, Long categoryId, String auth) {
        return CommonResult.successResponse(trainingManager.getTrainingList(limit, currentPage, keyword, categoryId, auth));
    }

    @Override
    public CommonResult<TrainingVo> getTraining(Long tid) {
        try {
            return CommonResult.successResponse(trainingManager.getTraining(tid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusAccessDeniedException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.ACCESS_DENIED);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<List<ProblemVo>> getTrainingProblemList(Long tid) {
        try {
            return CommonResult.successResponse(trainingManager.getTrainingProblemList(tid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusAccessDeniedException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.ACCESS_DENIED);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> toRegisterTraining(RegisterTrainingDto registerTrainingDto) {
        try {
            trainingManager.toRegisterTraining(registerTrainingDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<AccessVo> getTrainingAccess(Long tid) {
        try {
            return CommonResult.successResponse(trainingManager.getTrainingAccess(tid));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<IPage<TrainingRankVo>> getTrainingRank(Long tid, Integer limit, Integer currentPage) {
        try {
            return CommonResult.successResponse(trainingManager.getTrainingRank(tid, limit, currentPage));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusAccessDeniedException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.ACCESS_DENIED);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}
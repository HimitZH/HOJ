package top.hcode.hoj.service.admin.training.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.admin.training.AdminTrainingManager;
import top.hcode.hoj.pojo.dto.TrainingDTO;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.service.admin.training.AdminTrainingService;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 20:44
 * @Description:
 */
@Service
public class AdminTrainingServiceImpl implements AdminTrainingService {

    @Autowired
    private AdminTrainingManager adminTrainingManager;

    @Override
    public CommonResult<IPage<Training>> getTrainingList(Integer limit, Integer currentPage, String keyword) {
        return CommonResult.successResponse(adminTrainingManager.getTrainingList(limit, currentPage, keyword));
    }

    @Override
    public CommonResult<TrainingDTO> getTraining(Long tid) {
        try {
            TrainingDTO training = adminTrainingManager.getTraining(tid);
            return CommonResult.successResponse(training);
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> deleteTraining(Long tid) {
        try {
            adminTrainingManager.deleteTraining(tid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> addTraining(TrainingDTO trainingDto) {
        try {
            adminTrainingManager.addTraining(trainingDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> updateTraining(TrainingDTO trainingDto) {
        try {
            adminTrainingManager.updateTraining(trainingDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> changeTrainingStatus(Long tid, String author, Boolean status) {
        try {
            adminTrainingManager.changeTrainingStatus(tid, author, status);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }
}
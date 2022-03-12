package top.hcode.hoj.service.admin.training.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.admin.training.AdminTrainingCategoryManager;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.service.admin.training.AdminTrainingCategoryService;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 19:38
 * @Description:
 */

@Service
public class AdminTrainingCategoryServiceImpl implements AdminTrainingCategoryService {

    @Autowired
    private AdminTrainingCategoryManager adminTrainingCategoryManager;

    @Override
    public CommonResult<TrainingCategory> addTrainingCategory(TrainingCategory trainingCategory) {
        try {
            return CommonResult.successResponse(adminTrainingCategoryManager.addTrainingCategory(trainingCategory));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> updateTrainingCategory(TrainingCategory trainingCategory) {
        try {
            adminTrainingCategoryManager.updateTrainingCategory(trainingCategory);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> deleteTrainingCategory(Long cid) {
        try {
            adminTrainingCategoryManager.deleteTrainingCategory(cid);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
package top.hcode.hoj.service.group.training.impl;

import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.group.training.GroupTrainingProblemManager;
import top.hcode.hoj.pojo.dto.TrainingProblemDTO;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.service.group.training.GroupTrainingProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Service
public class GroupTrainingProblemServiceImpl implements GroupTrainingProblemService {

    @Autowired
    private GroupTrainingProblemManager groupTrainingProblemManager;

    @Override
    public CommonResult<HashMap<String, Object>> getTrainingProblemList(Integer limit, Integer currentPage, String keyword, Boolean queryExisted, Long tid) {
        try {
            return CommonResult.successResponse(groupTrainingProblemManager.getTrainingProblemList(limit, currentPage, keyword, queryExisted, tid));
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        } catch (StatusNotFoundException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.NOT_FOUND);
        }
    }

    @Override
    public CommonResult<Void> updateTrainingProblem(TrainingProblem trainingProblem) {
        try {
            groupTrainingProblemManager.updateTrainingProblem(trainingProblem);
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
    public CommonResult<Void> deleteTrainingProblem(Long pid, Long tid) {
        try {
            groupTrainingProblemManager.deleteTrainingProblem(pid, tid);
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
    public CommonResult<Void> addProblemFromPublic(TrainingProblemDTO trainingProblemDto) {
        try {
            groupTrainingProblemManager.addProblemFromPublic(trainingProblemDto);
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
    public CommonResult<Void> addProblemFromGroup(String problemId, Long tid) {
        try {
            groupTrainingProblemManager.addProblemFromGroup(problemId, tid);
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

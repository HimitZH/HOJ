package top.hcode.hoj.service.oj.impl;

import org.springframework.stereotype.Service;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.oj.CommonManager;
import top.hcode.hoj.pojo.entity.problem.CodeTemplate;
import top.hcode.hoj.pojo.entity.problem.Language;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.pojo.vo.CaptchaVO;
import top.hcode.hoj.pojo.vo.ProblemTagVO;
import top.hcode.hoj.service.oj.CommonService;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 16:42
 * @Description:
 */
@Service
public class CommonServiceImpl implements CommonService {

    @Resource
    private CommonManager commonManager;

    @Override
    public CommonResult<CaptchaVO> getCaptcha() {
        return CommonResult.successResponse(commonManager.getCaptcha());
    }

    @Override
    public CommonResult<List<TrainingCategory>> getTrainingCategory() {
        return CommonResult.successResponse(commonManager.getTrainingCategory());
    }

    @Override
    public CommonResult<List<Tag>> getAllProblemTagsList(String oj) {
        return CommonResult.successResponse(commonManager.getAllProblemTagsList(oj));
    }

    @Override
    public CommonResult<List<ProblemTagVO>> getProblemTagsAndClassification(String oj) {
        return CommonResult.successResponse(commonManager.getProblemTagsAndClassification(oj));
    }

    @Override
    public CommonResult<Collection<Tag>> getProblemTags(Long pid) {
        return CommonResult.successResponse(commonManager.getProblemTags(pid));
    }

    @Override
    public CommonResult<List<Language>> getLanguages(Long pid, Boolean all) {
        return CommonResult.successResponse(commonManager.getLanguages(pid, all));
    }

    @Override
    public CommonResult<Collection<Language>> getProblemLanguages(Long pid) {
        return CommonResult.successResponse(commonManager.getProblemLanguages(pid));
    }

    @Override
    public CommonResult<List<CodeTemplate>> getProblemCodeTemplate(Long pid) {
        return CommonResult.successResponse(commonManager.getProblemCodeTemplate(pid));
    }
}
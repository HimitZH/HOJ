package top.hcode.hoj.service.oj;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.problem.CodeTemplate;
import top.hcode.hoj.pojo.entity.problem.Language;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.pojo.vo.CaptchaVo;
import top.hcode.hoj.pojo.vo.ProblemTagVo;

import java.util.Collection;
import java.util.List;

public interface CommonService {

    public CommonResult<CaptchaVo> getCaptcha();

    public CommonResult<List<TrainingCategory>> getTrainingCategory();

    public CommonResult<List<Tag>> getAllProblemTagsList(String oj);

    public CommonResult<List<ProblemTagVo>> getProblemTagsAndClassification(String oj);

    public CommonResult<Collection<Tag>> getProblemTags(Long pid);

    public CommonResult<List<Language>> getLanguages(Long pid, Boolean all);

    public CommonResult<Collection<Language>> getProblemLanguages(Long pid);

    public CommonResult<List<CodeTemplate>> getProblemCodeTemplate(Long pid);
}

package top.hcode.hoj.controller.oj;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.problem.CodeTemplate;
import top.hcode.hoj.pojo.entity.problem.Language;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.pojo.vo.CaptchaVO;
import top.hcode.hoj.pojo.vo.ProblemTagVO;
import top.hcode.hoj.service.oj.CommonService;

import java.util.Collection;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/12 23:25
 * @Description: 通用的请求控制处理类
 */
@RestController
@RequestMapping("/api")
public class CommonController {

    @Autowired
    private CommonService commonService;


    @GetMapping("/captcha")
    public CommonResult<CaptchaVO> getCaptcha() {
        return commonService.getCaptcha();
    }


    @GetMapping("/get-training-category")
    public CommonResult<List<TrainingCategory>> getTrainingCategory() {
        return commonService.getTrainingCategory();
    }

    @GetMapping("/get-all-problem-tags")
    public CommonResult<List<Tag>> getAllProblemTagsList(@RequestParam(value = "oj", defaultValue = "ME") String oj) {
        return commonService.getAllProblemTagsList(oj);
    }

    @GetMapping("/get-problem-tags-and-classification")
    public CommonResult<List<ProblemTagVO>> getProblemTagsAndClassification(@RequestParam(value = "oj", defaultValue = "ME") String oj) {
        return commonService.getProblemTagsAndClassification(oj);
    }

    @GetMapping("/get-problem-tags")
    public CommonResult<Collection<Tag>> getProblemTags(Long pid) {
        return commonService.getProblemTags(pid);
    }


    @GetMapping("/languages")
    public CommonResult<List<Language>> getLanguages(@RequestParam(value = "pid", required = false) Long pid,
                                                     @RequestParam(value = "all", required = false) Boolean all) {
        return commonService.getLanguages(pid, all);
    }

    @GetMapping("/get-problem-languages")
    public CommonResult<Collection<Language>> getProblemLanguages(@RequestParam("pid") Long pid) {
        return commonService.getProblemLanguages(pid);
    }

    @GetMapping("/get-problem-code-template")
    public CommonResult<List<CodeTemplate>> getProblemCodeTemplate(@RequestParam("pid") Long pid) {
        return commonService.getProblemCodeTemplate(pid);
    }

}
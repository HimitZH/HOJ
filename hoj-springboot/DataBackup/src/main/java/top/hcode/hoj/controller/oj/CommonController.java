package top.hcode.hoj.controller.oj;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.service.impl.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/12 23:25
 * @Description:
 */
@RestController
@RequestMapping("/api")
public class CommonController {

    @Autowired
    private TagServiceImpl tagService;

    @Autowired
    private ProblemTagServiceImpl problemTagService;

    @Autowired
    private LanguageServiceImpl languageService;

    @Autowired
    private ProblemLanguageServiceImpl problemLanguageService;


    @GetMapping("/get-all-problem-tags")
    public CommonResult getAllProblemTagsList(){
        List<Tag> list = tagService.list();
        if (list!=null){
            return CommonResult.successResponse(list,"获取题目标签列表成功！");
        }else{
            return CommonResult.errorResponse("获取题目标签列表失败！");
        }
    }

    @GetMapping("/get-problem-tags")
    public CommonResult getProblemTags(@Valid @RequestParam("pid") Long pid){
        Map<String, Object> map = new HashMap<>();
        map.put("pid", pid);
        List<Long> tidList = problemTagService.listByMap(map).stream().map(ProblemTag::getTid).collect(Collectors.toList());
        List<Tag> tags = (List<Tag>) tagService.listByIds(tidList);
        if (tags!=null){
            return CommonResult.successResponse(tags,"获取该题目的标签列表成功！");
        }else{
            return CommonResult.errorResponse("获取该题目的标签列表失败！");
        }
    }



    @GetMapping("/languages")
    public CommonResult getLanguages(){
        List<Language> list = languageService.list();
        if (list!=null){
            return CommonResult.successResponse(list,"获取编程语言列表成功！");
        }else{
            return CommonResult.errorResponse("获取编程语言列表失败！");
        }
    }

    @GetMapping("/get-Problem-languages")
    public CommonResult getProblemLanguages(@Valid @RequestParam("pid") Long pid){
        QueryWrapper<ProblemLanguage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid).select("lid");
        List<Long> idList = problemLanguageService.list(queryWrapper)
                .stream().map(ProblemLanguage::getLid).collect(Collectors.toList());
        List<Language> languages = (List<Language>) languageService.listByIds(idList);
        if (languages!=null){
            return CommonResult.successResponse(languages,"获取该题目的编程语言列表成功！");
        }else{
            return CommonResult.errorResponse("获取该题目的编程语言列表失败！");
        }
    }
}
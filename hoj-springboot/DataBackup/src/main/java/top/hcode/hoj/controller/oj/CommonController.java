package top.hcode.hoj.controller.oj;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.service.impl.*;
import top.hcode.hoj.utils.RedisUtils;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/12 23:25
 * @Description: 通用的请求控制处理类
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

    @Autowired
    private RedisUtils redisUtil;

    @Autowired
    private ProblemServiceImpl problemService;


    @GetMapping("/captcha")
    public CommonResult getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(90, 30, 4);
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);
        String verCode = specCaptcha.text().toLowerCase();
        String key = IdUtil.simpleUUID();
        // 存入redis并设置过期时间为30分钟
        redisUtil.set(key, verCode, 1800);
        // 将key和base64返回给前端
        return CommonResult.successResponse(MapUtil.builder().put("img", specCaptcha.toBase64())
                .put("captchaKey", key).map(), "获取成功");
    }


    @GetMapping("/get-all-problem-tags")
    public CommonResult getAllProblemTagsList() {
        List<Tag> list = tagService.list();
        if (list != null) {
            return CommonResult.successResponse(list, "获取题目标签列表成功！");
        } else {
            return CommonResult.errorResponse("获取题目标签列表失败！");
        }
    }

    @GetMapping("/get-problem-tags")
    public CommonResult getProblemTags(@Valid @RequestParam("pid") Long pid) {
        Map<String, Object> map = new HashMap<>();
        map.put("pid", pid);
        List<Long> tidList = problemTagService.listByMap(map).stream().map(ProblemTag::getTid).collect(Collectors.toList());
        List<Tag> tags = (List<Tag>) tagService.listByIds(tidList);
        if (tags != null) {
            return CommonResult.successResponse(tags, "获取该题目的标签列表成功！");
        } else {
            return CommonResult.errorResponse("获取该题目的标签列表失败！");
        }
    }


    @GetMapping("/languages")
    public CommonResult getLanguages(@RequestParam(value = "pid", required = false) Long pid,
                                     @RequestParam(value = "all", required = false) Boolean all) {

        String OJ = "ME";
        if (pid != null) {
            Problem problem = problemService.getById(pid);
            if (problem.getIsRemote()) {
                OJ = problem.getProblemId().split("-")[0];
            }
        }

        QueryWrapper<Language> queryWrapper = new QueryWrapper<>();
        // 获取对应OJ支持的语言列表
        queryWrapper.eq(all != null && !all, "oj", OJ);
        List<Language> list = languageService.list(queryWrapper);
        if (list != null) {
            return CommonResult.successResponse(list, "获取编程语言列表成功！");
        } else {
            return CommonResult.errorResponse("获取编程语言列表失败！");
        }
    }

    @GetMapping("/get-Problem-languages")
    public CommonResult getProblemLanguages(@Valid @RequestParam("pid") Long pid) {
        QueryWrapper<ProblemLanguage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid).select("lid");
        List<Long> idList = problemLanguageService.list(queryWrapper)
                .stream().map(ProblemLanguage::getLid).collect(Collectors.toList());
        List<Language> languages = (List<Language>) languageService.listByIds(idList);
        if (languages != null) {
            return CommonResult.successResponse(languages, "获取该题目的编程语言列表成功！");
        } else {
            return CommonResult.errorResponse("获取该题目的编程语言列表失败！");
        }
    }
}
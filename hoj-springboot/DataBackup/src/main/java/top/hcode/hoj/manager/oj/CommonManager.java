package top.hcode.hoj.manager.oj;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.pojo.entity.problem.*;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.pojo.vo.CaptchaVo;
import top.hcode.hoj.dao.problem.*;
import top.hcode.hoj.dao.training.TrainingCategoryEntityService;
import top.hcode.hoj.utils.RedisUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 16:28
 * @Description:
 */
@Component
public class CommonManager {

    @Autowired
    private TagEntityService tagEntityService;

    @Autowired
    private ProblemTagEntityService problemTagEntityService;

    @Autowired
    private LanguageEntityService languageEntityService;

    @Autowired
    private ProblemLanguageEntityService problemLanguageEntityService;

    @Autowired
    private RedisUtils redisUtil;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private CodeTemplateEntityService codeTemplateEntityService;

    @Autowired
    private TrainingCategoryEntityService trainingCategoryEntityService;

    public CaptchaVo getCaptcha() {
        ArithmeticCaptcha specCaptcha = new ArithmeticCaptcha(90, 30, 4);
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);
        // 2位数运算
        specCaptcha.setLen(2);
        String verCode = specCaptcha.text().toLowerCase();
        String key = IdUtil.simpleUUID();
        // 存入redis并设置过期时间为30分钟
        redisUtil.set(key, verCode, 1800);
        // 将key和base64返回给前端
        CaptchaVo captchaVo = new CaptchaVo();
        captchaVo.setImg(specCaptcha.toBase64());
        captchaVo.setCaptchaKey(key);
        return captchaVo;
    }


    public List<TrainingCategory> getTrainingCategory() {
        QueryWrapper<TrainingCategory> trainingCategoryQueryWrapper = new QueryWrapper<>();
        trainingCategoryQueryWrapper.isNull("gid");
        return trainingCategoryEntityService.list(trainingCategoryQueryWrapper);
    }

    public List<Tag> getAllProblemTagsList(String oj) {
        List<Tag> tagList;
        oj = oj.toUpperCase();
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.isNull("gid");
        if (oj.equals("ALL")) {
            tagList = tagEntityService.list(tagQueryWrapper);
        } else {
            tagQueryWrapper.eq("oj", oj);
            tagList = tagEntityService.list(tagQueryWrapper);
        }
        return tagList;
    }


    public Collection<Tag> getProblemTags(Long pid) {
        Map<String, Object> map = new HashMap<>();
        map.put("pid", pid);
        List<Long> tidList = problemTagEntityService.listByMap(map)
                .stream()
                .map(ProblemTag::getTid)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tidList)) {
            return new ArrayList<>();
        }
        return tagEntityService.listByIds(tidList);
    }


    public List<Language> getLanguages(Long pid, Boolean all) {

        String oj = "ME";
        if (pid != null) {
            Problem problem = problemEntityService.getById(pid);
            if (problem.getIsRemote()) {
                oj = problem.getProblemId().split("-")[0];
            }
        }

        if (oj.equals("GYM")) {  // GYM用与CF一样的编程语言列表
            oj = "CF";
        }

        QueryWrapper<Language> queryWrapper = new QueryWrapper<>();
        // 获取对应OJ支持的语言列表
        queryWrapper.eq(all != null && !all, "oj", oj);
        return languageEntityService.list(queryWrapper);
    }

    public Collection<Language> getProblemLanguages(Long pid) {
        QueryWrapper<ProblemLanguage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid).select("lid");
        List<Long> idList = problemLanguageEntityService.list(queryWrapper)
                .stream().map(ProblemLanguage::getLid).collect(Collectors.toList());
        return languageEntityService.listByIds(idList);

    }

    public List<CodeTemplate> getProblemCodeTemplate(Long pid) {
        QueryWrapper<CodeTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pid", pid);
        return codeTemplateEntityService.list(queryWrapper);
    }
}
package top.hcode.hoj.service.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.CodeTemplateMapper;
import top.hcode.hoj.pojo.entity.problem.CodeTemplate;
import top.hcode.hoj.service.problem.CodeTemplateService;

/**
 * @Author: Himit_ZH
 * @Date: 2021/4/24 10:27
 * @Description:
 */
@Service
public class CodeTemplateServiceImpl extends ServiceImpl<CodeTemplateMapper, CodeTemplate> implements CodeTemplateService {
}
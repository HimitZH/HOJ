package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.CodeTemplateMapper;
import top.hcode.hoj.pojo.entity.CodeTemplate;
import top.hcode.hoj.service.CodeTemplateService;

/**
 * @Author: Himit_ZH
 * @Date: 2021/4/24 10:27
 * @Description:
 */
@Service
public class CodeTemplateServiceImpl extends ServiceImpl<CodeTemplateMapper, CodeTemplate> implements CodeTemplateService {
}
package top.hcode.hoj.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.mapper.LanguageMapper;
import top.hcode.hoj.pojo.entity.problem.Language;
import top.hcode.hoj.dao.problem.LanguageEntityService;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/12 23:23
 * @Description:
 */
@Service
public class LanguageEntityServiceImpl extends ServiceImpl<LanguageMapper, Language> implements LanguageEntityService {
}
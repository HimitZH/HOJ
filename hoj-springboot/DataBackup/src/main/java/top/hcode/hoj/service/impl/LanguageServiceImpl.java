package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.LanguageMapper;
import top.hcode.hoj.pojo.entity.Language;
import top.hcode.hoj.service.LanguageService;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/12 23:23
 * @Description:
 */
@Service
public class LanguageServiceImpl extends ServiceImpl<LanguageMapper, Language> implements LanguageService {
}
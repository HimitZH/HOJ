package top.hcode.hoj.dao.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.mapper.ProblemLanguageMapper;
import top.hcode.hoj.pojo.entity.problem.ProblemLanguage;
import top.hcode.hoj.dao.problem.ProblemLanguageEntityService;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/13 00:04
 * @Description:
 */
@Service
public class ProblemLanguageEntityServiceImpl extends ServiceImpl<ProblemLanguageMapper, ProblemLanguage> implements ProblemLanguageEntityService {
}
package top.hcode.hoj.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.mapper.ProblemCaseMapper;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.dao.ProblemCaseEntityService;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/14 19:59
 * @Description:
 */
@Service
public class ProblemCaseEntityServiceImpl extends ServiceImpl<ProblemCaseMapper, ProblemCase> implements ProblemCaseEntityService {
}
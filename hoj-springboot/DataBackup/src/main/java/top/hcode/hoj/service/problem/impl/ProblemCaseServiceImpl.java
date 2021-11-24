package top.hcode.hoj.service.problem.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.ProblemCaseMapper;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.service.problem.ProblemCaseService;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/14 19:59
 * @Description:
 */
@Service
public class ProblemCaseServiceImpl extends ServiceImpl<ProblemCaseMapper, ProblemCase> implements ProblemCaseService {
}
package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.ProblemCaseMapper;
import top.hcode.hoj.pojo.entity.ProblemCase;
import top.hcode.hoj.service.ProblemCaseService;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/14 19:59
 * @Description:
 */
@Service
public class ProblemCaseServiceImpl extends ServiceImpl<ProblemCaseMapper, ProblemCase> implements ProblemCaseService {
}
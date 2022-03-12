package top.hcode.hoj.dao.problem.impl;

import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.entity.problem.ProblemCount;
import top.hcode.hoj.mapper.ProblemCountMapper;
import top.hcode.hoj.dao.problem.ProblemCountEntityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class ProblemCountServiceImpl extends ServiceImpl<ProblemCountMapper, ProblemCount> implements ProblemCountEntityService {

    @Autowired
    private ProblemCountMapper problemCountMapper;

    @Override
    public ProblemCount getContestProblemCount(Long pid, Long cpid, Long cid) {
        return problemCountMapper.getContestProblemCount(pid,cpid, cid);
    }
}

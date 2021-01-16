package top.hcode.hoj.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.entity.ProblemCount;
import top.hcode.hoj.dao.ProblemCountMapper;
import top.hcode.hoj.service.ProblemCountService;
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
public class ProblemCountServiceImpl extends ServiceImpl<ProblemCountMapper, ProblemCount> implements ProblemCountService {

    @Autowired
    private ProblemCountMapper problemCountMapper;

    @Override
    public ProblemCount getContestProblemCount(Long pid, Long cpid, Long cid) {
        return problemCountMapper.getContestProblemCount(pid,cpid, cid);
    }
}

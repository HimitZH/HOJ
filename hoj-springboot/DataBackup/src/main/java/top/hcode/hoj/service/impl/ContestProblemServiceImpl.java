package top.hcode.hoj.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.entity.ContestProblem;
import top.hcode.hoj.dao.ContestProblemMapper;
import top.hcode.hoj.pojo.vo.ContestProblemVo;
import top.hcode.hoj.service.ContestProblemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class ContestProblemServiceImpl extends ServiceImpl<ContestProblemMapper, ContestProblem> implements ContestProblemService {

    @Autowired
    private ContestProblemMapper contestProblemMapper;

    @Override
    public List<ContestProblemVo> getContestProblemList(Long cid, Date startTime) {
        return contestProblemMapper.getContestProblemList(cid, startTime);
    }
}

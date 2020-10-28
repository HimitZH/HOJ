package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.pojo.entity.Problem;
import top.hcode.hoj.dao.ProblemMapper;
import top.hcode.hoj.service.ProblemService;
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
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemService {

    @Autowired
    private ProblemMapper problemMapper;

    @Override
    public Page<ProblemVo> getProblemList(int limit, int currentPage, long pid, String title) {

        //新建分页
        Page<ProblemVo> page =new Page<>(currentPage,limit);

        return page.setRecords(problemMapper.getProblemList(page, pid, title));
    }
}

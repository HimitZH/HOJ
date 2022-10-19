package top.hcode.hoj.dao.group.impl;

import top.hcode.hoj.dao.group.GroupProblemEntityService;
import top.hcode.hoj.mapper.GroupProblemMapper;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.vo.ProblemVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Service
public class GroupProblemEntityServiceImpl extends ServiceImpl<GroupProblemMapper, Problem> implements GroupProblemEntityService {

    @Autowired
    private GroupProblemMapper groupProblemMapper;

    @Override
    public IPage<ProblemVO> getProblemList(int limit, int currentPage, Long gid) {
        IPage<ProblemVO> iPage = new Page<>(currentPage, limit);

        List<ProblemVO> problemList = groupProblemMapper.getProblemList(iPage, gid);

        return iPage.setRecords(problemList);
    }

    @Override
    public IPage<Problem> getAdminProblemList(int limit, int currentPage, Long gid) {
        IPage<Problem> iPage = new Page<>(currentPage, limit);

        List<Problem> problemList = groupProblemMapper.getAdminProblemList(iPage, gid);

        return iPage.setRecords(problemList);
    }

}

package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.pojo.entity.Contest;
import top.hcode.hoj.dao.ContestMapper;
import top.hcode.hoj.service.ContestService;
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
public class ContestServiceImpl extends ServiceImpl<ContestMapper, Contest> implements ContestService {

    @Autowired
    private ContestMapper contestMapper;

    @Override
    public Page<ContestVo> getContestList(int limit, int currentPage) {
        //新建分页
        Page<ContestVo> page =new Page<>(currentPage,limit);

        return page.setRecords(contestMapper.getContestList(page));
    }

    @Override
    public ContestVo getContestInfoById(long cid) {
        return contestMapper.getContestInfoById(cid);
    }
}

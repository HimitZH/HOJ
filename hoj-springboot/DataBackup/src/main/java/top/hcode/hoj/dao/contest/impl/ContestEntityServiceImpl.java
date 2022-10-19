package top.hcode.hoj.dao.contest.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.pojo.vo.ContestRegisterCountVO;
import top.hcode.hoj.pojo.vo.ContestVO;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.mapper.ContestMapper;
import top.hcode.hoj.dao.contest.ContestEntityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class ContestEntityServiceImpl extends ServiceImpl<ContestMapper, Contest> implements ContestEntityService {

    @Autowired
    private ContestMapper contestMapper;

    @Override
    public List<ContestVO> getWithinNext14DaysContests() {
        List<ContestVO> contestList = contestMapper.getWithinNext14DaysContests();
        setRegisterCount(contestList);

        return contestList;
    }

    @Override
    public IPage<ContestVO> getContestList(Integer limit, Integer currentPage, Integer type, Integer status, String keyword) {
        //新建分页
        IPage<ContestVO> page = new Page<>(currentPage, limit);

        List<ContestVO> contestList = contestMapper.getContestList(page, type, status, keyword);
        setRegisterCount(contestList);

        return page.setRecords(contestList);
    }

    @Override
    public ContestVO getContestInfoById(long cid) {
        List<Long> cidList = Collections.singletonList(cid);
        ContestVO contestVo = contestMapper.getContestInfoById(cid);
        if (contestVo != null) {
            List<ContestRegisterCountVO> contestRegisterCountVOList = contestMapper.getContestRegisterCount(cidList);
            if(!CollectionUtils.isEmpty(contestRegisterCountVOList)) {
                ContestRegisterCountVO contestRegisterCountVo = contestRegisterCountVOList.get(0);
                contestVo.setCount(contestRegisterCountVo.getCount());
            }
        }
        return contestVo;
    }


    private void setRegisterCount(List<ContestVO> contestList){
        List<Long> cidList = contestList.stream().map(ContestVO::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(cidList)) {
            List<ContestRegisterCountVO> contestRegisterCountVOList = contestMapper.getContestRegisterCount(cidList);
            for (ContestRegisterCountVO contestRegisterCountVo : contestRegisterCountVOList) {
                for (ContestVO contestVo : contestList) {
                    if (contestRegisterCountVo.getCid().equals(contestVo.getId())) {
                        contestVo.setCount(contestRegisterCountVo.getCount());
                        break;
                    }
                }
            }
        }
    }


}

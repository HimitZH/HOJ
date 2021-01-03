package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.dao.JudgeMapper;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.service.JudgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class JudgeServiceImpl extends ServiceImpl<JudgeMapper, Judge> implements JudgeService {

    @Autowired
    private JudgeMapper judgeMapper;

    @Override
    public IPage<JudgeVo> getCommonJudgeList(Integer limit, Integer currentPage, Long pid, Integer status, String username,
                                             Long cid,String uid) {
        //新建分页
        Page<JudgeVo> page = new Page<>(currentPage, limit);

        return judgeMapper.getCommonJudgeList(page, pid, status, username, cid,uid);
    }
}

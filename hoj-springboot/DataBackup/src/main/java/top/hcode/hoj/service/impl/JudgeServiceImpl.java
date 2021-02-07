package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import top.hcode.hoj.judge.JudgeDispatcher;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.dao.JudgeMapper;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.service.JudgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
public class JudgeServiceImpl extends ServiceImpl<JudgeMapper, Judge> implements JudgeService {

    @Autowired
    private JudgeMapper judgeMapper;

    @Autowired
    private JudgeDispatcher judgeDispatcher;

    @Override
    public IPage<JudgeVo> getCommonJudgeList(Integer limit, Integer currentPage, Long pid, Integer status, String username,
                                             String uid) {
        //新建分页
        Page<JudgeVo> page = new Page<>(currentPage, limit);

        return judgeMapper.getCommonJudgeList(page, pid, status, username, uid);
    }

    @Override
    public IPage<JudgeVo> getContestJudgeList(Integer limit, Integer currentPage, String displayId, Long cid, Integer status, String username, String uid, Boolean beforeContestSubmit) {
        //新建分页
        Page<JudgeVo> page = new Page<>(currentPage, limit);

        return judgeMapper.getContestJudgeList(page, displayId, cid, status, username, uid, beforeContestSubmit);
    }


    @Override
    @Async
    public void rejudgeContestProblem(List<Judge> judgeList, String judgeToken) {
        for (Judge judge : judgeList) {
            // 进入重判队列，等待调用判题服务
            judgeDispatcher.sendTask(judge.getSubmitId(), judge.getPid(), judgeToken, judge.getPid() == 0);
        }
    }

}

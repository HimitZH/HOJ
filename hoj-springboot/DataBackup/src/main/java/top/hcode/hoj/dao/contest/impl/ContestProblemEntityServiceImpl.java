package top.hcode.hoj.dao.contest.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import top.hcode.hoj.mapper.ContestProblemMapper;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import top.hcode.hoj.pojo.vo.ContestProblemVO;
import top.hcode.hoj.dao.contest.ContestProblemEntityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.contest.ContestRecordEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.pojo.vo.ProblemFullScreenListVO;

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
public class ContestProblemEntityServiceImpl extends ServiceImpl<ContestProblemMapper, ContestProblem> implements ContestProblemEntityService {

    @Autowired
    private ContestProblemMapper contestProblemMapper;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Autowired
    private ContestRecordEntityService contestRecordEntityService;

    @Override
    public List<ContestProblemVO> getContestProblemList(Long cid,
                                                        Date startTime,
                                                        Date endTime,
                                                        Date sealTime,
                                                        Boolean isAdmin,
                                                        String contestAuthorUid,
                                                        List<String> groupRootUidList,
                                                        Boolean isContainsContestEndJudge) {
        // 筛去 比赛管理员和超级管理员的提交
        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();
        superAdminUidList.add(contestAuthorUid);

        if (!CollectionUtils.isEmpty(groupRootUidList)) {
            superAdminUidList.addAll(groupRootUidList);
        }

        return contestProblemMapper.getContestProblemList(cid, startTime, endTime, sealTime, isAdmin, superAdminUidList, !isContainsContestEndJudge);
    }
    @Override
    public List<ProblemFullScreenListVO> getContestFullScreenProblemList(Long cid){
        return contestProblemMapper.getContestFullScreenProblemList(cid);
    }

    @Async
    @Override
    public void syncContestRecord(Long pid, Long cid, String displayId) {

        UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid)
                .eq("cid", cid)
                .set("display_id", displayId);
        contestRecordEntityService.update(updateWrapper);
    }
}

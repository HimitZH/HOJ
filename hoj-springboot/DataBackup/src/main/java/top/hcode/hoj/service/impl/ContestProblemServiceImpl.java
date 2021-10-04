package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import top.hcode.hoj.pojo.entity.ContestProblem;
import top.hcode.hoj.dao.ContestProblemMapper;
import top.hcode.hoj.pojo.entity.ContestRecord;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.pojo.vo.ContestProblemVo;
import top.hcode.hoj.service.ContestProblemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
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
public class ContestProblemServiceImpl extends ServiceImpl<ContestProblemMapper, ContestProblem> implements ContestProblemService {

    @Autowired
    private ContestProblemMapper contestProblemMapper;

    @Autowired
    private ContestRecordServiceImpl contestRecordService;

    @Override
    public List<ContestProblemVo> getContestProblemList(Long cid, Date startTime, Date endTime, Date sealTime, Boolean isAdmin, String contestAuthorUid) {
        // 筛去 比赛管理员和超级管理员的提交
        List<UserInfo> superAdminList = contestRecordService.getSuperAdminList();
        List<String> superAdminUidList = superAdminList.stream().map(UserInfo::getUuid).collect(Collectors.toList());
        superAdminUidList.add(contestAuthorUid);

        return contestProblemMapper.getContestProblemList(cid, startTime, endTime, sealTime, isAdmin, superAdminUidList);
    }

    @Async
    public void syncContestRecord(Long pid, Long cid, String displayId) {

        UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("pid", pid)
                .eq("cid", cid)
                .set("display_id", displayId);
        contestRecordService.update(updateWrapper);
    }
}

package top.hcode.hoj.dao.judge.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.dao.contest.ContestRecordEntityService;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.mapper.JudgeMapper;
import top.hcode.hoj.mapper.ProblemMapper;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.vo.ContestScrollBoardSubmissionVo;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.ProblemCountVo;
import top.hcode.hoj.utils.Constants;

import java.util.Date;
import java.util.HashMap;
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
@Slf4j(topic = "hoj")
public class JudgeEntityServiceImpl extends ServiceImpl<JudgeMapper, Judge> implements JudgeEntityService {

    @Autowired
    private JudgeMapper judgeMapper;

    @Autowired
    private ContestRecordEntityService contestRecordEntityService;

    @Autowired
    private ProblemMapper problemMapper;


    @Override
    public IPage<JudgeVo> getCommonJudgeList(Integer limit,
                                             Integer currentPage,
                                             String searchPid,
                                             Integer status,
                                             String username,
                                             String uid,
                                             Boolean completeProblemID,
                                             Long gid) {
        //新建分页
        Page<JudgeVo> page = new Page<>(currentPage, limit);

        IPage<JudgeVo> commonJudgeList = judgeMapper.getCommonJudgeList(page, searchPid, status, username, uid, completeProblemID, gid);
        List<JudgeVo> records = commonJudgeList.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Long> pidList = records.stream().map(JudgeVo::getPid).collect(Collectors.toList());
            QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
            problemQueryWrapper.select("id", "title")
                    .in("id", pidList);
            List<Problem> problemList = problemMapper.selectList(problemQueryWrapper);
            HashMap<Long, String> storeMap = new HashMap<>(limit);
            for (JudgeVo judgeVo : records) {
                judgeVo.setTitle(getProblemTitleByPid(judgeVo.getPid(), problemList, storeMap));
            }
        }
        return commonJudgeList;
    }

    private String getProblemTitleByPid(Long pid, List<Problem> problemList, HashMap<Long, String> storeMap) {
        String title = storeMap.get(pid);
        if (title != null) {
            return title;
        }
        for (Problem problem : problemList) {
            if (problem.getId().equals(pid)) {
                storeMap.put(pid, problem.getTitle());
                return problem.getTitle();
            }
        }
        return "";
    }

    @Override
    public IPage<JudgeVo> getContestJudgeList(Integer limit,
                                              Integer currentPage,
                                              String displayId,
                                              Long cid,
                                              Integer status,
                                              String username,
                                              String uid,
                                              Boolean beforeContestSubmit,
                                              String rule,
                                              Date startTime,
                                              Date sealRankTime,
                                              String sealTimeUid,
                                              Boolean completeProblemID) {
        //新建分页
        Page<JudgeVo> page = new Page<>(currentPage, limit);

        return judgeMapper.getContestJudgeList(page, displayId, cid, status, username, uid, beforeContestSubmit,
                rule, startTime, sealRankTime, sealTimeUid, completeProblemID);
    }


    @Override
    public void failToUseRedisPublishJudge(Long submitId, Long pid, Boolean isContest) {
        UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
        judgeUpdateWrapper.eq("submit_id", submitId)
                .set("error_message", "The something has gone wrong with the data Backup server. Please report this to administrator.")
                .set("status", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
        judgeMapper.update(null, judgeUpdateWrapper);
        // 更新contest_record表
        if (isContest) {
            UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("submit_id", submitId) // submit_id一定只有一个
                    .set("first_blood", false)
                    .set("status", Constants.Contest.RECORD_NOT_AC_NOT_PENALTY.getCode());
            contestRecordEntityService.update(updateWrapper);
        }
    }

    @Override
    public ProblemCountVo getContestProblemCount(Long pid, Long cpid, Long cid, Date startTime, Date sealRankTime, List<String> adminList) {
        return judgeMapper.getContestProblemCount(pid, cpid, cid, startTime, sealRankTime, adminList);
    }

    @Override
    public ProblemCountVo getProblemCount(Long pid, Long gid) {
        return judgeMapper.getProblemCount(pid, gid);
    }

    @Override
    public int getTodayJudgeNum() {
        return judgeMapper.getTodayJudgeNum();
    }

    @Override
    public List<ProblemCountVo> getProblemListCount(List<Long> pidList) {
        return judgeMapper.getProblemListCount(pidList);
    }


    public List<ContestScrollBoardSubmissionVo> getContestScrollBoardSubmission(Long cid, List<String> uidList) {
        return judgeMapper.getContestScrollBoardSubmission(cid, uidList);
    }

}

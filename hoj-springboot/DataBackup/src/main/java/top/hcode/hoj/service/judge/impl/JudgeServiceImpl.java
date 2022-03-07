package top.hcode.hoj.service.judge.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ToJudgeDto;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.dao.JudgeMapper;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.ProblemCountVo;
import top.hcode.hoj.service.contest.impl.ContestRecordServiceImpl;
import top.hcode.hoj.service.judge.JudgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.service.problem.impl.ProblemServiceImpl;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import java.util.Arrays;
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
@Slf4j(topic = "hoj")
public class JudgeServiceImpl extends ServiceImpl<JudgeMapper, Judge> implements JudgeService {

    @Autowired
    private JudgeMapper judgeMapper;

    @Autowired
    private ContestRecordServiceImpl contestRecordService;

    @Resource
    private ProblemServiceImpl problemService;

    private final static List<String> HOJ_LANGUAGE_LIST = Arrays.asList("C++", "C++ With O2",
            "C", "C With O2", "Python3", "Python2", "Java", "Golang", "C#", "PHP", "PyPy2", "PyPy3",
            "JavaScript Node", "JavaScript V8");

    @Override
    public CommonResult checkSubmissionInfo(ToJudgeDto toJudgeDto) {

        if (!toJudgeDto.getIsRemote() && !HOJ_LANGUAGE_LIST.contains(toJudgeDto.getLanguage())) {
            return CommonResult.errorResponse("提交的代码的语言错误！请使用" + HOJ_LANGUAGE_LIST + "中之一的语言！", CommonResult.STATUS_FAIL);
        }

        if (toJudgeDto.getCode().length() < 50
                && !toJudgeDto.getLanguage().contains("Py")
                && !toJudgeDto.getLanguage().contains("PHP")
                && !toJudgeDto.getLanguage().contains("JavaScript")) {
            return CommonResult.errorResponse("提交的代码是无效的，代码字符长度请不要低于50！", CommonResult.STATUS_FORBIDDEN);
        }

        if (toJudgeDto.getCode().length() > 65535) {
            return CommonResult.errorResponse("提交的代码是无效的，代码字符长度请不要超过65535！", CommonResult.STATUS_FORBIDDEN);
        }
        return null;
    }

    @Override
    public CommonResult submitProblem(ToJudgeDto judgeDto, Judge judge) {

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("problem_id", judgeDto.getPid());
        Problem problem = problemService.getOne(problemQueryWrapper, false);

        if (problem.getAuth() == 2) {
            return CommonResult.errorResponse("错误！当前题目不可提交！", CommonResult.STATUS_FORBIDDEN);
        }

        judge.setCpid(0L).setPid(problem.getId()).setDisplayPid(problem.getProblemId());

        // 将新提交数据插入数据库
        saveOrUpdate(judge);
        return null;
    }

    @Override
    public IPage<JudgeVo> getCommonJudgeList(Integer limit,
                                             Integer currentPage,
                                             String searchPid,
                                             Integer status,
                                             String username,
                                             String uid,
                                             Boolean completeProblemID) {
        //新建分页
        Page<JudgeVo> page = new Page<>(currentPage, limit);

        return judgeMapper.getCommonJudgeList(page, searchPid, status, username, uid, completeProblemID);
    }

    @Override
    public IPage<JudgeVo> getContestJudgeList(Integer limit, Integer currentPage, String displayId, Long cid, Integer status,
                                              String username, String uid, Boolean beforeContestSubmit, String rule,
                                              Date startTime, Date sealRankTime, String sealTimeUid, Boolean completeProblemID) {
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
            contestRecordService.update(updateWrapper);
        }
    }

    @Override
    public ProblemCountVo getContestProblemCount(Long pid, Long cpid, Long cid, Date startTime, Date sealRankTime, List<String> adminList) {
        return judgeMapper.getContestProblemCount(pid, cpid, cid, startTime, sealRankTime, adminList);
    }

    @Override
    public ProblemCountVo getProblemCount(Long pid) {
        return judgeMapper.getProblemCount(pid);
    }

}

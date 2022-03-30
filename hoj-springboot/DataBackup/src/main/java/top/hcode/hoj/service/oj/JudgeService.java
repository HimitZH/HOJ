package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.SubmitIdListDto;
import top.hcode.hoj.pojo.dto.ToJudgeDto;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.SubmissionInfoVo;

import java.util.HashMap;
import java.util.List;

public interface JudgeService {

    public CommonResult<Judge> submitProblemJudge(ToJudgeDto judgeDto);

    public CommonResult<Judge> resubmit(Long submitId);

    public CommonResult<SubmissionInfoVo> getSubmission(Long submitId);

    public CommonResult<IPage<JudgeVo>> getJudgeList(Integer limit,
                                                     Integer currentPage,
                                                     Boolean onlyMine,
                                                     String searchPid,
                                                     Integer searchStatus,
                                                     String searchUsername,
                                                     Boolean completeProblemID,
                                                     Long gid);

    public CommonResult<Void> updateSubmission(Judge judge);

    public CommonResult<HashMap<Long, Object>> checkCommonJudgeResult(SubmitIdListDto submitIdListDto);

    public CommonResult<HashMap<Long, Object>> checkContestJudgeResult(SubmitIdListDto submitIdListDto);

    public CommonResult<List<JudgeCase>> getALLCaseResult(Long submitId);
}

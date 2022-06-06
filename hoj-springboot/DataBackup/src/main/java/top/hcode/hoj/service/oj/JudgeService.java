package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.SubmitIdListDto;
import top.hcode.hoj.pojo.dto.TestJudgeDto;
import top.hcode.hoj.pojo.dto.SubmitJudgeDto;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.JudgeCase;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.SubmissionInfoVo;
import top.hcode.hoj.pojo.vo.TestJudgeVo;

import java.util.HashMap;
import java.util.List;

public interface JudgeService {

    public CommonResult<Judge> submitProblemJudge(SubmitJudgeDto judgeDto);

    public CommonResult<String> submitProblemTestJudge(TestJudgeDto testJudgeDto);

    public CommonResult<Judge> resubmit(Long submitId);

    public CommonResult<SubmissionInfoVo> getSubmission(Long submitId);

    public CommonResult<TestJudgeVo> getTestJudgeResult(String testJudgeKey);

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

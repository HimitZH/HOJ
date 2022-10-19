package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.SubmitIdListDTO;
import top.hcode.hoj.pojo.dto.SubmitJudgeDTO;
import top.hcode.hoj.pojo.dto.TestJudgeDTO;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.vo.JudgeCaseVO;
import top.hcode.hoj.pojo.vo.JudgeVO;
import top.hcode.hoj.pojo.vo.SubmissionInfoVO;
import top.hcode.hoj.pojo.vo.TestJudgeVO;

import java.util.HashMap;

public interface JudgeService {

    public CommonResult<Judge> submitProblemJudge(SubmitJudgeDTO judgeDto);

    public CommonResult<String> submitProblemTestJudge(TestJudgeDTO testJudgeDto);

    public CommonResult<Judge> resubmit(Long submitId);

    public CommonResult<SubmissionInfoVO> getSubmission(Long submitId);

    public CommonResult<TestJudgeVO> getTestJudgeResult(String testJudgeKey);

    public CommonResult<IPage<JudgeVO>> getJudgeList(Integer limit,
                                                     Integer currentPage,
                                                     Boolean onlyMine,
                                                     String searchPid,
                                                     Integer searchStatus,
                                                     String searchUsername,
                                                     Boolean completeProblemID,
                                                     Long gid);

    public CommonResult<Void> updateSubmission(Judge judge);

    public CommonResult<HashMap<Long, Object>> checkCommonJudgeResult(SubmitIdListDTO submitIdListDto);

    public CommonResult<HashMap<Long, Object>> checkContestJudgeResult(SubmitIdListDTO submitIdListDto);

    public CommonResult<JudgeCaseVO> getALLCaseResult(Long submitId);
}

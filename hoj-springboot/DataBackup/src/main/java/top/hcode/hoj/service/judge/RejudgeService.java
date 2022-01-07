package top.hcode.hoj.service.judge;

import top.hcode.hoj.common.result.CommonResult;

public interface RejudgeService {

    CommonResult rejudge(Long submitId);

    CommonResult rejudgeContestProblem(Long cid,Long pid);
}

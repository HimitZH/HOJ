package top.hcode.hoj.service.admin.rejudge;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.judge.Judge;

public interface RejudgeService {

    CommonResult<Judge> rejudge(Long submitId);

    CommonResult<Void> rejudgeContestProblem(Long cid,Long pid);
}

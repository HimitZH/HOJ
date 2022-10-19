package top.hcode.hoj.service.group.contest;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ContestProblemDTO;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupContestProblemService {

    public CommonResult<HashMap<String, Object>> getContestProblemList(Integer limit, Integer currentPage, String keyword, Long cid, Integer problemType, String oj);

    public CommonResult<Map<Object, Object>> addProblem(ProblemDTO problemDto);

    public CommonResult<ContestProblem> getContestProblem(Long pid, Long cid);

    public CommonResult<Void> updateContestProblem(ContestProblem contestProblem);

    public CommonResult<Void> deleteContestProblem(Long pid, Long cid);

    public CommonResult<Void> addProblemFromPublic(ContestProblemDTO contestProblemDto);

    public CommonResult<Void> addProblemFromGroup(String problemId, Long cid, String displayId);

}

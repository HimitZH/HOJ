package top.hcode.hoj.service.group.problem;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ProblemDto;
import top.hcode.hoj.pojo.entity.judge.CompileDTO;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.vo.ProblemVo;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupProblemService {

    public CommonResult<IPage<ProblemVo>> getProblemList(Integer limit, Integer currentPage, Long gid);

    public CommonResult<IPage<Problem>> getAdminProblemList(Integer limit, Integer currentPage, Long gid);

    public CommonResult<Problem> getProblem(Long pid);

    public CommonResult<Void> addProblem(ProblemDto problemDto);

    public CommonResult<Void> updateProblem(ProblemDto problemDto);

    public CommonResult<Void> deleteProblem(Long pid);

    public CommonResult<List<ProblemCase>> getProblemCases(Long pid, Boolean isUpload);

    public CommonResult<List<Tag>> getAllProblemTagsList(Long gid);

    public CommonResult<Void> compileSpj(CompileDTO compileDTO, Long gid);

    public CommonResult<Void> compileInteractive(CompileDTO compileDTO, Long gid);

    public CommonResult<Void> changeProblemAuth(Long pid, Integer auth);

    public CommonResult<Void> applyPublic(Long pid, Boolean isApplied);
}

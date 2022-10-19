package top.hcode.hoj.service.admin.problem;

import com.baomidou.mybatisplus.core.metadata.IPage;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.dto.CompileDTO;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import java.util.List;


public interface AdminProblemService {

    public CommonResult<IPage<Problem>> getProblemList(Integer limit, Integer currentPage, String keyword, Integer auth, String oj);

    public CommonResult<Problem> getProblem(Long pid);

    public CommonResult<Void> deleteProblem(Long pid);

    public CommonResult<Void> addProblem(ProblemDTO problemDto);

    public CommonResult<Void> updateProblem(ProblemDTO problemDto);

    public CommonResult<List<ProblemCase>> getProblemCases(Long pid, Boolean isUpload);

    public CommonResult compileSpj(CompileDTO compileDTO);

    public CommonResult compileInteractive(CompileDTO compileDTO);

    public CommonResult<Void> importRemoteOJProblem(String name,String problemId);

    public CommonResult<Void> changeProblemAuth(Problem problem);
}

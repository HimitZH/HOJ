package top.hcode.hoj.service.group.problem;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ProblemDTO;
import top.hcode.hoj.pojo.dto.CompileDTO;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.problem.ProblemCase;
import top.hcode.hoj.pojo.entity.problem.Tag;
import top.hcode.hoj.pojo.vo.ProblemVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupProblemService {

    public CommonResult<IPage<ProblemVO>> getProblemList(Integer limit, Integer currentPage, Long gid);

    public CommonResult<IPage<Problem>> getAdminProblemList(Integer limit, Integer currentPage, Long gid);

    public CommonResult<Problem> getProblem(Long pid);

    public CommonResult<Void> addProblem(ProblemDTO problemDto);

    public CommonResult<Void> updateProblem(ProblemDTO problemDto);

    public CommonResult<Void> deleteProblem(Long pid);

    public CommonResult<List<ProblemCase>> getProblemCases(Long pid, Boolean isUpload);

    public CommonResult<List<Tag>> getAllProblemTagsList(Long gid);

    public CommonResult<Void> compileSpj(CompileDTO compileDTO, Long gid);

    public CommonResult<Void> compileInteractive(CompileDTO compileDTO, Long gid);

    public CommonResult<Void> changeProblemAuth(Long pid, Integer auth);

    public CommonResult<Void> applyPublic(Long pid, Boolean isApplied);
}

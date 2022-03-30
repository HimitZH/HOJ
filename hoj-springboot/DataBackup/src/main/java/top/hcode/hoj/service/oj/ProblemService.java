package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.PidListDto;
import top.hcode.hoj.pojo.vo.ProblemInfoVo;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.pojo.vo.RandomProblemVo;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 11:04
 * @Description:
 */
public interface ProblemService {

    public CommonResult<Page<ProblemVo>> getProblemList(Integer limit, Integer currentPage,
                                                        String keyword, List<Long> tagId, Integer difficulty, String oj);

    public CommonResult<RandomProblemVo> getRandomProblem();

    public CommonResult<HashMap<Long, Object>> getUserProblemStatus(PidListDto pidListDto);

    public CommonResult<ProblemInfoVo> getProblemInfo(String problemId, Long gid);

}
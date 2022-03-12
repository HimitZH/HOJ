package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.RegisterTrainingDto;
import top.hcode.hoj.pojo.vo.AccessVo;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.pojo.vo.TrainingRankVo;
import top.hcode.hoj.pojo.vo.TrainingVo;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 20:29
 * @Description:
 */
public interface TrainingService {

    public CommonResult<IPage<TrainingVo>> getTrainingList(Integer limit, Integer currentPage, String keyword, Long categoryId, String auth);

    public CommonResult<TrainingVo> getTraining(Long tid);

    public CommonResult<List<ProblemVo>> getTrainingProblemList(Long tid);

    public CommonResult<Void> toRegisterTraining(RegisterTrainingDto registerTrainingDto);

    public CommonResult<AccessVo> getTrainingAccess( Long tid);

    public CommonResult<IPage<TrainingRankVo>> getTrainingRank(Long tid, Integer limit, Integer currentPage);
}
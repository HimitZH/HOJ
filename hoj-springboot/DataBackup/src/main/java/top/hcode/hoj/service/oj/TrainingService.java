package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.RegisterTrainingDTO;
import top.hcode.hoj.pojo.vo.AccessVO;
import top.hcode.hoj.pojo.vo.ProblemVO;
import top.hcode.hoj.pojo.vo.TrainingRankVO;
import top.hcode.hoj.pojo.vo.TrainingVO;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 20:29
 * @Description:
 */
public interface TrainingService {

    public CommonResult<IPage<TrainingVO>> getTrainingList(Integer limit, Integer currentPage, String keyword, Long categoryId, String auth);

    public CommonResult<TrainingVO> getTraining(Long tid);

    public CommonResult<List<ProblemVO>> getTrainingProblemList(Long tid);

    public CommonResult<Void> toRegisterTraining(RegisterTrainingDTO registerTrainingDto);

    public CommonResult<AccessVO> getTrainingAccess(Long tid);

    public CommonResult<IPage<TrainingRankVO>> getTrainingRank(Long tid, Integer limit, Integer currentPage);
}
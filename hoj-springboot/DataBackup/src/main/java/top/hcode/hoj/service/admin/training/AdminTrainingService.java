package top.hcode.hoj.service.admin.training;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.TrainingDTO;
import top.hcode.hoj.pojo.entity.training.Training;

public interface AdminTrainingService {

    public CommonResult<IPage<Training>> getTrainingList(Integer limit, Integer currentPage, String keyword);

    public CommonResult<TrainingDTO> getTraining(Long tid);

    public CommonResult<Void> deleteTraining(Long tid);

    public CommonResult<Void> addTraining(TrainingDTO trainingDto);

    public CommonResult<Void> updateTraining(TrainingDTO trainingDto);

    public CommonResult<Void> changeTrainingStatus(Long tid, String author, Boolean status);
}

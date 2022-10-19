package top.hcode.hoj.service.group.training;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.TrainingDTO;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.vo.TrainingVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupTrainingService {

    public CommonResult<IPage<TrainingVO>> getTrainingList(Integer limit, Integer currentPage, Long gid);

    public CommonResult<IPage<Training>> getAdminTrainingList(Integer limit, Integer currentPage, Long gid);

    public CommonResult<TrainingDTO> getTraining(Long tid);

    public CommonResult<Void> addTraining(TrainingDTO trainingDto);

    public CommonResult<Void> updateTraining(TrainingDTO trainingDto);

    public CommonResult<Void> deleteTraining(Long tid);

    public CommonResult<Void> changeTrainingStatus(Long tid, Boolean status);

}

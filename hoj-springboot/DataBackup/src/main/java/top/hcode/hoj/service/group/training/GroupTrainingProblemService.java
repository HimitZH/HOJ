package top.hcode.hoj.service.group.training;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.TrainingProblemDTO;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;

import java.util.HashMap;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
public interface GroupTrainingProblemService {

    public CommonResult<HashMap<String, Object>> getTrainingProblemList(Integer limit, Integer currentPage, String keyword, Boolean queryExisted, Long tid);

    public CommonResult<Void> updateTrainingProblem(TrainingProblem trainingProblem);

    public CommonResult<Void> deleteTrainingProblem(Long pid, Long tid);

    public CommonResult<Void> addProblemFromPublic(TrainingProblemDTO trainingProblemDto);

    public CommonResult<Void> addProblemFromGroup(String problemId, Long tid);

}

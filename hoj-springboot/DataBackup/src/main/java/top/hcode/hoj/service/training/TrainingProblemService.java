package top.hcode.hoj.service.training;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.pojo.vo.ProblemVo;

import java.util.HashMap;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/20 12:24
 * @Description:
 */
public interface TrainingProblemService extends IService<TrainingProblem> {
    public int getTrainingProblemCount(Long tid);

    public List<ProblemVo> getTrainingProblemList(Long tid);

    public HashMap<String, Object> getAdminTrainingProblemList(int limit, int currentPage,
                                                               String keyword, Long tid, boolean queryExisted);
}
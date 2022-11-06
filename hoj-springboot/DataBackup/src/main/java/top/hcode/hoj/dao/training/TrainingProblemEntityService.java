package top.hcode.hoj.dao.training;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.pojo.vo.ProblemFullScreenListVO;
import top.hcode.hoj.pojo.vo.ProblemVO;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/20 12:24
 * @Description:
 */
public interface TrainingProblemEntityService extends IService<TrainingProblem> {
    public List<Long> getTrainingProblemIdList(Long tid);

    public List<ProblemVO> getTrainingProblemList(Long tid);

    public Integer getUserTrainingACProblemCount(String uid, Long gid, List<Long> pidList);

    public List<TrainingProblem> getPrivateTrainingProblemListByPid(Long pid, String uid);

    public List<TrainingProblem> getTrainingListAcceptedCountByUid(List<Long> tidList, String uid);

    public List<TrainingProblem> getGroupTrainingListAcceptedCountByUid(List<Long> tidList, Long gid, String uid);

    public List<ProblemFullScreenListVO> getTrainingFullScreenProblemList(Long tid);
}
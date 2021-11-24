package top.hcode.hoj.service.training.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.TrainingProblemMapper;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.service.training.TrainingProblemService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/20 12:25
 * @Description:
 */
@Service
public class TrainingProblemServiceImpl extends ServiceImpl<TrainingProblemMapper, TrainingProblem> implements TrainingProblemService {

    @Resource
    private TrainingProblemMapper trainingProblemMapper;

    @Override
    public int getTrainingProblemCount(Long tid) {
        return trainingProblemMapper.getTrainingProblemCount(tid);
    }

    @Override
    public List<ProblemVo> getTrainingProblemList(Long tid) {
        return trainingProblemMapper.getTrainingProblemList(tid);
    }
}
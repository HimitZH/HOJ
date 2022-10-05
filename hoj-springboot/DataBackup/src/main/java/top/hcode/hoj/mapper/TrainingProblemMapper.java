package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.pojo.vo.ProblemVo;

import java.util.List;

@Mapper
@Repository
public interface TrainingProblemMapper extends BaseMapper<TrainingProblem> {

    public List<Long> getTrainingProblemCount(@Param("tid") Long tid);

    public List<ProblemVo> getTrainingProblemList(@Param("tid") Long tid);

    public List<TrainingProblem> getPrivateTrainingProblemListByPid(@Param("pid") Long pid, @Param("uid") String uid);

    public List<TrainingProblem> getTrainingProblemListAcceptedStatusByUid(@Param("tidList") List<Long> tidList, @Param("uid") String uid);
}

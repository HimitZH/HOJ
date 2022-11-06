package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.pojo.vo.ProblemFullScreenListVO;
import top.hcode.hoj.pojo.vo.ProblemVO;

import java.util.List;

@Mapper
@Repository
public interface TrainingProblemMapper extends BaseMapper<TrainingProblem> {

    public List<Long> getTrainingProblemCount(@Param("tid") Long tid);

    public List<ProblemVO> getTrainingProblemList(@Param("tid") Long tid);

    public List<TrainingProblem> getPrivateTrainingProblemListByPid(@Param("pid") Long pid, @Param("uid") String uid);

    public List<TrainingProblem> getTrainingListAcceptedCountByUid(@Param("tidList") List<Long> tidList,
                                                                   @Param("uid") String uid);

    public List<TrainingProblem> getGroupTrainingListAcceptedCountByUid(@Param("tidList") List<Long> tidList,
                                                                        @Param("gid") Long gid,
                                                                        @Param("uid") String uid);

    public List<ProblemFullScreenListVO> getTrainingFullScreenProblemList(@Param("tid") Long tid);
}

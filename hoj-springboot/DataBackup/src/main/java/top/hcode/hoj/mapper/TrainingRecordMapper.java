package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.training.TrainingRecord;
import top.hcode.hoj.pojo.vo.TrainingRecordVO;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/21 14:27
 * @Description:
 */

@Mapper
@Repository
public interface TrainingRecordMapper extends BaseMapper<TrainingRecord> {

    public List<TrainingRecordVO> getTrainingRecord(@Param("tid") Long tid);
}
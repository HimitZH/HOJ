package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.vo.TrainingVO;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/19 22:03
 * @Description:
 */
@Mapper
@Repository
public interface TrainingMapper extends BaseMapper<Training> {

    List<TrainingVO> getTrainingList(IPage page,
                                     @Param("categoryId") Long categoryId,
                                     @Param("auth") String auth,
                                     @Param("keyword") String keyword);
}
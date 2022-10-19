package top.hcode.hoj.mapper;

import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.vo.TrainingVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Mapper
@Repository
public interface GroupTrainingMapper extends BaseMapper<Training> {

    List<TrainingVO> getTrainingList(IPage iPage, @Param("gid") Long gid);

    List<Training> getAdminTrainingList(IPage iPage, @Param("gid") Long gid);

}

package top.hcode.hoj.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.vo.TrainingVo;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/19 22:03
 * @Description:
 */
@Mapper
@Repository
public interface TrainingMapper extends BaseMapper<Training> {

    List<TrainingVo> getTrainingList(@Param("categoryId") Long categoryId,
                                     @Param("auth") String auth,
                                     @Param("keyword") String keyword);
}
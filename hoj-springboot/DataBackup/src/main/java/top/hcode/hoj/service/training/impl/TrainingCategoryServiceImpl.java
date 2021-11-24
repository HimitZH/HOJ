package top.hcode.hoj.service.training.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.TrainingCategoryMapper;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.service.training.TrainingCategoryService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/20 12:15
 * @Description:
 */
@Service
public class TrainingCategoryServiceImpl extends ServiceImpl<TrainingCategoryMapper, TrainingCategory> implements TrainingCategoryService {

    @Resource
    private TrainingCategoryMapper trainingCategoryMapper;

    @Override
    public TrainingCategory getTrainingCategoryByTrainingId(Long tid) {
        return trainingCategoryMapper.getTrainingCategoryByTrainingId(tid);
    }
}
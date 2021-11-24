package top.hcode.hoj.service.training;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;

public interface TrainingCategoryService extends IService<TrainingCategory> {
    public TrainingCategory getTrainingCategoryByTrainingId(Long tid);
}

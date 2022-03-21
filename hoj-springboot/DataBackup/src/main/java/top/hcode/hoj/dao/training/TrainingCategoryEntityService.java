package top.hcode.hoj.dao.training;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;

public interface TrainingCategoryEntityService extends IService<TrainingCategory> {

    public TrainingCategory getTrainingCategoryByTrainingId(Long tid);
}

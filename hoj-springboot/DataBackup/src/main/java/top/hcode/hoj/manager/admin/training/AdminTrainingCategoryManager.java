package top.hcode.hoj.manager.admin.training;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.training.TrainingCategoryEntityService;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 19:29
 * @Description:
 */

@Component
public class AdminTrainingCategoryManager {

    @Resource
    private TrainingCategoryEntityService trainingCategoryEntityService;

    public TrainingCategory addTrainingCategory(TrainingCategory trainingCategory) throws StatusFailException {
        QueryWrapper<TrainingCategory> trainingCategoryQueryWrapper = new QueryWrapper<>();
        trainingCategoryQueryWrapper.eq(trainingCategory.getGid() != null, "gid", trainingCategory.getGid())
                .eq("name", trainingCategory.getName());
        TrainingCategory existedTrainingCategory = trainingCategoryEntityService.getOne(trainingCategoryQueryWrapper, false);

        if (existedTrainingCategory != null) {
            throw new StatusFailException("该分类名称已存在！请勿重复添加！");
        }

        boolean isOk = trainingCategoryEntityService.save(trainingCategory);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
        return trainingCategory;
    }

    public void updateTrainingCategory(TrainingCategory trainingCategory) throws StatusFailException {
        boolean isOk = trainingCategoryEntityService.updateById(trainingCategory);
        if (!isOk) {
            throw new StatusFailException("更新失败！");
        }
    }

    public void deleteTrainingCategory(Long cid) throws StatusFailException {
        boolean isOk = trainingCategoryEntityService.removeById(cid);
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
    }


}
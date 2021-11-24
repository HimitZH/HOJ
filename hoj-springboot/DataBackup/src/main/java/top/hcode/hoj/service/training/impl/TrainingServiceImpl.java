package top.hcode.hoj.service.training.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.dao.MappingTrainingCategoryMapper;
import top.hcode.hoj.dao.TrainingMapper;
import top.hcode.hoj.pojo.dto.TrainingDto;
import top.hcode.hoj.pojo.entity.training.MappingTrainingCategory;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.pojo.vo.TrainingVo;
import top.hcode.hoj.service.training.TrainingService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/19 22:01
 * @Description:
 */
@Service
public class TrainingServiceImpl extends ServiceImpl<TrainingMapper, Training> implements TrainingService {

    @Resource
    private TrainingMapper trainingMapper;

    @Resource
    private TrainingCategoryServiceImpl trainingCategoryService;

    @Resource
    private MappingTrainingCategoryMapper mappingTrainingCategoryMapper;

    @Override
    public IPage<TrainingVo> getTrainingList(int limit, int currentPage, Long categoryId, String auth, String keyword) {
        Page<TrainingVo> page = new Page<>(currentPage, limit);
        return trainingMapper.getTrainingList(page, categoryId, auth, keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTraining(TrainingDto trainingDto) {
        Training training = trainingDto.getTraining();
        trainingMapper.insert(training);
        TrainingCategory trainingCategory = trainingDto.getTrainingCategory();
        if (trainingCategory.getId() == null) {
            try {
                trainingCategoryService.save(trainingCategory);
            } catch (Exception ignored) {
                QueryWrapper<TrainingCategory> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("name", trainingCategory.getName());
                trainingCategory = trainingCategoryService.getOne(queryWrapper, false);
            }
        }

        int insert = mappingTrainingCategoryMapper.insert(new MappingTrainingCategory()
                .setTid(training.getId())
                .setCid(trainingCategory.getId()));
        return insert > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTraining(TrainingDto trainingDto) {

        Training training = trainingDto.getTraining();
        trainingMapper.updateById(training);
        TrainingCategory trainingCategory = trainingDto.getTrainingCategory();
        if (trainingCategory.getId() == null) {
            try {
                trainingCategoryService.save(trainingCategory);
            } catch (Exception ignored) {
                QueryWrapper<TrainingCategory> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("name", trainingCategory.getName());
                trainingCategory = trainingCategoryService.getOne(queryWrapper, false);
            }
        }
        UpdateWrapper<MappingTrainingCategory> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("tid", training.getId()).set("cid", trainingCategory.getId());
        int update = mappingTrainingCategoryMapper.update(null, updateWrapper);
        return update > 0;
    }
}
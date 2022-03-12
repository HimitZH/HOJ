package top.hcode.hoj.dao.training;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.vo.TrainingVo;

public interface TrainingEntityService extends IService<Training> {
    public IPage<TrainingVo> getTrainingList(int limit, int currentPage,
                                             Long categoryId, String auth, String keyword);

}

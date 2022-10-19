package top.hcode.hoj.dao.training;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.vo.TrainingVO;

public interface TrainingEntityService extends IService<Training> {

    public Page<TrainingVO> getTrainingList(int limit,
                                            int currentPage,
                                            Long categoryId,
                                            String auth,
                                            String keyword,
                                            String currentUid);

}

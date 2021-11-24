package top.hcode.hoj.service.training;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.training.TrainingRecord;
import top.hcode.hoj.pojo.vo.TrainingRankVo;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/21 23:38
 * @Description:
 */
public interface TrainingRecordService  extends IService<TrainingRecord> {

    public IPage<TrainingRankVo> getTrainingRank(Long tid, int currentPage, int limit);
}
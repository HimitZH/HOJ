package top.hcode.hoj.dao.training;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.training.TrainingRecord;
import top.hcode.hoj.pojo.vo.TrainingRecordVO;

import java.util.List;


/**
 * @Author: Himit_ZH
 * @Date: 2021/11/21 23:38
 * @Description:
 */
public interface TrainingRecordEntityService extends IService<TrainingRecord> {

    public List<TrainingRecordVO> getTrainingRecord(Long tid);

}
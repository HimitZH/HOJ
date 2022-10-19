package top.hcode.hoj.dao.training.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.mapper.TrainingRecordMapper;
import top.hcode.hoj.pojo.entity.training.TrainingRecord;
import top.hcode.hoj.pojo.vo.TrainingRecordVO;
import top.hcode.hoj.dao.training.TrainingRecordEntityService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/21 23:39
 * @Description:
 */
@Service
public class TrainingRecordEntityServiceImpl extends ServiceImpl<TrainingRecordMapper, TrainingRecord> implements TrainingRecordEntityService {

    @Resource
    private TrainingRecordMapper trainingRecordMapper;

    @Override
    public List<TrainingRecordVO> getTrainingRecord(Long tid){
        return trainingRecordMapper.getTrainingRecord(tid);
    }

}
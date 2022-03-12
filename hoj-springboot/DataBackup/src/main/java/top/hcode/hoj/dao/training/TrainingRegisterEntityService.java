package top.hcode.hoj.dao.training;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.training.TrainingRegister;
import java.util.List;

public interface TrainingRegisterEntityService extends IService<TrainingRegister> {


    public List<String> getAlreadyRegisterUidList(Long tid);

}

package top.hcode.hoj.service.training;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingRegister;
import top.hcode.hoj.pojo.vo.UserRolesVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface TrainingRegisterService extends IService<TrainingRegister> {

    public CommonResult checkTrainingAuth(Training training, HttpServletRequest request);

    public CommonResult checkTrainingAuth(Training training, UserRolesVo userRolesVo);

    public CommonResult toRegisterTraining(Long tid, String password, HttpServletRequest request);

    public List<String> getAlreadyRegisterUidList(Long tid);

    public void syncAlreadyRegisterUserRecord(Long tid, Long pid, Long tpId);
}

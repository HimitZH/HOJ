package top.hcode.hoj.service.training.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.TrainingRegisterMapper;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingRecord;
import top.hcode.hoj.pojo.entity.training.TrainingRegister;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.training.TrainingRegisterService;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/20 11:30
 * @Description:
 */
@Service
public class TrainingRegisterServiceImpl extends ServiceImpl<TrainingRegisterMapper, TrainingRegister> implements TrainingRegisterService {

    @Resource
    private TrainingRegisterMapper trainingRegisterMapper;

    @Resource
    private TrainingServiceImpl trainingService;

    @Resource
    @Lazy
    private TrainingRecordServiceImpl trainingRecordService;

    @Override
    public CommonResult checkTrainingAuth(Training training, HttpServletRequest request) {
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        return checkTrainingAuth(training, userRolesVo);
    }

    @Override
    public CommonResult checkTrainingAuth(Training training, UserRolesVo userRolesVo) {
        if (Constants.Training.AUTH_PRIVATE.getValue().equals(training.getAuth())) {
            if (userRolesVo == null) {
                return CommonResult.errorResponse("该训练属于私有题单，请先登录以校验权限！", CommonResult.STATUS_ACCESS_DENIED);
            }
            boolean root = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员
            boolean isAuthor = training.getAuthor().equals(userRolesVo.getUsername()); // 是否为该私有训练的创建者

            if (!root && !isAuthor) { // 如果两者都不是，需要做注册权限校验
                return checkTrainingRegister(training.getId(), userRolesVo.getUid());
            }
        }
        return null;
    }

    @Override
    public CommonResult toRegisterTraining(Long tid, String password, HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        Training training = trainingService.getById(tid);

        if (training == null || !training.getStatus()) {
            return CommonResult.errorResponse("对不起，该训练不存在或不允许显示!");
        }

        if (!training.getPrivatePwd().equals(password)) { // 密码不对
            return CommonResult.errorResponse("训练密码错误，请重新输入！");
        }

        QueryWrapper<TrainingRegister> registerQueryWrapper = new QueryWrapper<>();
        registerQueryWrapper.eq("tid", tid).eq("uid", userRolesVo.getUid());
        if (trainingRegisterMapper.selectOne(registerQueryWrapper) != null) {
            return CommonResult.errorResponse("您已注册过该训练，请勿重复注册！");
        }

        int insert = trainingRegisterMapper.insert(new TrainingRegister()
                .setTid(tid)
                .setUid(userRolesVo.getUid()));

        if (insert <= 0) {
            return CommonResult.errorResponse("校验训练密码失败，请稍后再试", CommonResult.STATUS_FAIL);
        } else {
            trainingRecordService.syncUserSubmissionToRecordByTid(tid, userRolesVo.getUid());
            return CommonResult.successResponse(null, "进入训练成功！");
        }
    }


    @Override
    public List<String> getAlreadyRegisterUidList(Long tid){
        QueryWrapper<TrainingRegister> trainingRegisterQueryWrapper = new QueryWrapper<>();
        trainingRegisterQueryWrapper.eq("tid", tid);
        return trainingRegisterMapper.selectList(trainingRegisterQueryWrapper).stream().map(TrainingRegister::getUid).collect(Collectors.toList());
    }

    @Override
    @Async
    public void syncAlreadyRegisterUserRecord(Long tid, Long pid, Long tpId) {

        Training training = trainingService.getById(tid);
        if (!Constants.Training.AUTH_PRIVATE.getValue().equals(training.getAuth())){
            return;
        }
        List<String> uidList = getAlreadyRegisterUidList(tid);
        trainingRecordService.syncNewProblemUserSubmissionToRecord(pid, tpId, tid, uidList);
    }

    private CommonResult checkTrainingRegister(Long tid, String uid) {
        QueryWrapper<TrainingRegister> trainingRegisterQueryWrapper = new QueryWrapper<>();
        trainingRegisterQueryWrapper.eq("tid", tid);
        trainingRegisterQueryWrapper.eq("uid", uid);
        TrainingRegister trainingRegister = trainingRegisterMapper.selectOne(trainingRegisterQueryWrapper);

        if (trainingRegister == null) {
            return CommonResult.errorResponse("该训练属于私有，请先使用专属密码注册！", CommonResult.STATUS_ACCESS_DENIED);
        }

        if (!trainingRegister.getStatus()) {
            return CommonResult.errorResponse("你已被禁止参加该训练！", CommonResult.STATUS_FORBIDDEN);
        }
        return null;
    }
}
package top.hcode.hoj.validator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusAccessDeniedException;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.training.TrainingRegisterEntityService;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingRegister;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/21 20:55
 * @Description:
 */
@Component
public class TrainingValidator {

    @Resource
    private TrainingRegisterEntityService trainingRegisterEntityService;

    @Autowired
    private GroupValidator groupValidator;

    @Resource
    private CommonValidator commonValidator;

    public void validateTraining(Training training) throws StatusFailException {
        commonValidator.validateContent(training.getTitle(), "训练标题", 500);
        commonValidator.validateContentLength(training.getDescription(), "训练描述", 65535);
        if (!Objects.equals(training.getAuth(), "Public")
                && !Objects.equals(training.getAuth(), "Private")) {
            throw new StatusFailException("训练的权限类型必须为公开训练(Public)、私有训练(Private)！");
        }
    }

    public void validateTrainingAuth(Training training) throws StatusAccessDeniedException, StatusForbiddenException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        validateTrainingAuth(training, userRolesVo);
    }


    public void validateTrainingAuth(Training training, AccountProfile userRolesVo) throws StatusAccessDeniedException, StatusForbiddenException {

        boolean isRoot = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员

        if (training.getIsGroup()) {
            if (!groupValidator.isGroupMember(userRolesVo.getUid(), training.getGid()) && !isRoot) {
                throw new StatusForbiddenException("对不起，您并非该团队内的成员，无权操作！");
            }
        }

        if (Constants.Training.AUTH_PRIVATE.getValue().equals(training.getAuth())) {

            if (userRolesVo == null) {
                throw new StatusAccessDeniedException("该训练属于私有题单，请先登录以校验权限！");
            }

            boolean isAuthor = training.getAuthor().equals(userRolesVo.getUsername()); // 是否为该私有训练的创建者

            if (isRoot || isAuthor || (training.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), training.getGid()))) {
                return;
            }

            // 如果三者都不是，需要做注册权限校验
            checkTrainingRegister(training.getId(), userRolesVo.getUid());
        }
    }

    private void checkTrainingRegister(Long tid, String uid) throws StatusAccessDeniedException, StatusForbiddenException {
        QueryWrapper<TrainingRegister> trainingRegisterQueryWrapper = new QueryWrapper<>();
        trainingRegisterQueryWrapper.eq("tid", tid);
        trainingRegisterQueryWrapper.eq("uid", uid);
        TrainingRegister trainingRegister = trainingRegisterEntityService.getOne(trainingRegisterQueryWrapper, false);

        if (trainingRegister == null) {
            throw new StatusAccessDeniedException("该训练属于私有，请先使用专属密码注册！");
        }

        if (!trainingRegister.getStatus()) {
            throw new StatusForbiddenException("错误：你已被禁止参加该训练！");
        }
    }

    public boolean isInTrainingOrAdmin(Training training, AccountProfile userRolesVo) throws StatusAccessDeniedException {
        if (Constants.Training.AUTH_PRIVATE.getValue().equals(training.getAuth())) {
            if (userRolesVo == null) {
                throw new StatusAccessDeniedException("该训练属于私有题单，请先登录以校验权限！");
            }
            boolean isRoot = SecurityUtils.getSubject().hasRole("root"); // 是否为超级管理员
            boolean isAuthor = training.getAuthor().equals(userRolesVo.getUsername()); // 是否为该私有训练的创建者


            if (isRoot
                    || isAuthor
                    || (training.getIsGroup() && groupValidator.isGroupRoot(userRolesVo.getUid(), training.getGid()))) {
                return true;
            }

            // 如果三者都不是，需要做注册权限校验
            QueryWrapper<TrainingRegister> trainingRegisterQueryWrapper = new QueryWrapper<>();
            trainingRegisterQueryWrapper.eq("tid", training.getId());
            trainingRegisterQueryWrapper.eq("uid", userRolesVo.getUid());
            TrainingRegister trainingRegister = trainingRegisterEntityService.getOne(trainingRegisterQueryWrapper, false);

            return trainingRegister != null && trainingRegister.getStatus();

        }
        return true;
    }
}
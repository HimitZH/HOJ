package top.hcode.hoj.manager.admin.training;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Component;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.training.TrainingCategoryEntityService;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.pojo.vo.UserRolesVO;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 19:29
 * @Description:
 */

@Component
@Slf4j(topic = "hoj")
public class AdminTrainingCategoryManager {

    @Resource
    private TrainingCategoryEntityService trainingCategoryEntityService;

    public TrainingCategory addTrainingCategory(TrainingCategory trainingCategory) throws StatusFailException {
        QueryWrapper<TrainingCategory> trainingCategoryQueryWrapper = new QueryWrapper<>();
        trainingCategoryQueryWrapper.eq(trainingCategory.getGid() != null, "gid", trainingCategory.getGid())
                .eq("name", trainingCategory.getName());
        TrainingCategory existedTrainingCategory = trainingCategoryEntityService.getOne(trainingCategoryQueryWrapper, false);

        if (existedTrainingCategory != null) {
            throw new StatusFailException("该分类名称已存在！请勿重复添加！");
        }

        boolean isOk = trainingCategoryEntityService.save(trainingCategory);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
        return trainingCategory;
    }

    public void updateTrainingCategory(TrainingCategory trainingCategory) throws StatusFailException {
        boolean isOk = trainingCategoryEntityService.updateById(trainingCategory);
        if (!isOk) {
            throw new StatusFailException("更新失败！");
        }
    }

    public void deleteTrainingCategory(Long cid) throws StatusFailException {
        boolean isOk = trainingCategoryEntityService.removeById(cid);
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");
        log.info("[{}],[{}],categoryId:[{}],operatorUid:[{}],operatorUsername:[{}]",
                "Admin_Training", "Delete_Category", cid, userRolesVo.getUid(), userRolesVo.getUsername());
    }


}
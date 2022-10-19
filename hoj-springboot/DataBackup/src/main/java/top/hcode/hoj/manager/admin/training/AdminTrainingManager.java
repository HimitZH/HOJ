package top.hcode.hoj.manager.admin.training;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.pojo.dto.TrainingDTO;
import top.hcode.hoj.pojo.entity.training.MappingTrainingCategory;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingCategory;
import top.hcode.hoj.pojo.entity.training.TrainingRegister;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.dao.training.*;
import top.hcode.hoj.utils.Constants;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 19:46
 * @Description:
 */

@Component
public class AdminTrainingManager {

    @Resource
    private TrainingEntityService trainingEntityService;

    @Resource
    private MappingTrainingCategoryEntityService mappingTrainingCategoryEntityService;

    @Resource
    private TrainingCategoryEntityService trainingCategoryEntityService;

    @Resource
    private TrainingRegisterEntityService trainingRegisterEntityService;

    @Resource
    private AdminTrainingRecordManager adminTrainingRecordManager;

    public IPage<Training> getTrainingList(Integer limit, Integer currentPage, String keyword) {

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        IPage<Training> iPage = new Page<>(currentPage, limit);
        QueryWrapper<Training> queryWrapper = new QueryWrapper<>();
        // 过滤密码
        queryWrapper.select(Training.class, info -> !info.getColumn().equals("private_pwd"));
        if (!StringUtils.isEmpty(keyword)) {
            keyword = keyword.trim();
            queryWrapper
                    .like("title", keyword).or()
                    .like("id", keyword).or()
                    .like("`rank`", keyword);
        }

        queryWrapper.eq("is_group", false).orderByAsc("`rank`");

        return trainingEntityService.page(iPage, queryWrapper);
    }


    public TrainingDTO getTraining(Long tid) throws StatusFailException, StatusForbiddenException {
        // 获取本场训练的信息
        Training training = trainingEntityService.getById(tid);
        if (training == null) { // 查询不存在
            throw new StatusFailException("查询失败：该训练不存在,请检查参数tid是否准确！");
        }

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 只有超级管理员和训练拥有者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(training.getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }

        TrainingDTO trainingDto = new TrainingDTO();
        trainingDto.setTraining(training);

        QueryWrapper<MappingTrainingCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", tid);
        MappingTrainingCategory mappingTrainingCategory = mappingTrainingCategoryEntityService.getOne(queryWrapper, false);
        TrainingCategory trainingCategory = null;
        if (mappingTrainingCategory != null) {
            trainingCategory = trainingCategoryEntityService.getById(mappingTrainingCategory.getCid());
        }
        trainingDto.setTrainingCategory(trainingCategory);
        return trainingDto;
    }


    public void deleteTraining(Long tid) throws StatusFailException {

        boolean isOk = trainingEntityService.removeById(tid);
        /*
        Training的id为其他表的外键的表中的对应数据都会被一起删除！
         */
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addTraining(TrainingDTO trainingDto) throws StatusFailException {

        Training training = trainingDto.getTraining();
        trainingEntityService.save(training);
        TrainingCategory trainingCategory = trainingDto.getTrainingCategory();
        if (trainingCategory.getId() == null) {
            try {
                trainingCategoryEntityService.save(trainingCategory);
            } catch (Exception ignored) {
                QueryWrapper<TrainingCategory> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("name", trainingCategory.getName());
                trainingCategory = trainingCategoryEntityService.getOne(queryWrapper, false);
            }
        }

        boolean isOk = mappingTrainingCategoryEntityService.save(new MappingTrainingCategory()
                .setTid(training.getId())
                .setCid(trainingCategory.getId()));
        if (!isOk) {
            throw new StatusFailException("添加失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTraining(TrainingDTO trainingDto) throws StatusForbiddenException, StatusFailException {
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 只有超级管理员和训练拥有者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(trainingDto.getTraining().getAuthor())) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }
        Training training = trainingDto.getTraining();
        Training oldTraining = trainingEntityService.getById(training.getId());
        trainingEntityService.updateById(training);

        // 私有训练 修改密码 需要清空之前注册训练的记录
        if (training.getAuth().equals(Constants.Training.AUTH_PRIVATE.getValue())) {
            if (!Objects.equals(training.getPrivatePwd(), oldTraining.getPrivatePwd())) {
                UpdateWrapper<TrainingRegister> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("tid", training.getId());
                trainingRegisterEntityService.remove(updateWrapper);
            }
        }


        TrainingCategory trainingCategory = trainingDto.getTrainingCategory();
        if (trainingCategory.getId() == null) {
            try {
                trainingCategoryEntityService.save(trainingCategory);
            } catch (Exception ignored) {
                QueryWrapper<TrainingCategory> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("name", trainingCategory.getName());
                trainingCategory = trainingCategoryEntityService.getOne(queryWrapper, false);
            }
        }

        MappingTrainingCategory mappingTrainingCategory = mappingTrainingCategoryEntityService
                .getOne(new QueryWrapper<MappingTrainingCategory>().eq("tid", training.getId()),
                        false);

        if (mappingTrainingCategory == null) {
            mappingTrainingCategoryEntityService.save(new MappingTrainingCategory()
                    .setTid(training.getId()).setCid(trainingCategory.getId()));
            adminTrainingRecordManager.checkSyncRecord(trainingDto.getTraining());
        } else {
            if (!mappingTrainingCategory.getCid().equals(trainingCategory.getId())) {
                UpdateWrapper<MappingTrainingCategory> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("tid", training.getId()).set("cid", trainingCategory.getId());
                boolean isOk = mappingTrainingCategoryEntityService.update(null, updateWrapper);
                if (isOk) {
                    adminTrainingRecordManager.checkSyncRecord(trainingDto.getTraining());
                } else {
                    throw new StatusFailException("修改失败");
                }
            }
        }

    }

    public void changeTrainingStatus(Long tid, String author, Boolean status) throws StatusForbiddenException, StatusFailException {
        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");
        // 是否为超级管理员
        boolean isRoot = SecurityUtils.getSubject().hasRole("root");
        // 只有超级管理员和训练拥有者才能操作
        if (!isRoot && !userRolesVo.getUsername().equals(author)) {
            throw new StatusForbiddenException("对不起，你无权限操作！");
        }

        boolean isOk = trainingEntityService.saveOrUpdate(new Training().setId(tid).setStatus(status));
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }


}
package top.hcode.hoj.manager.group.training;

import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.dao.group.GroupTrainingEntityService;
import top.hcode.hoj.dao.training.*;
import top.hcode.hoj.manager.admin.training.AdminTrainingRecordManager;
import top.hcode.hoj.pojo.dto.TrainingDTO;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.entity.training.*;
import top.hcode.hoj.pojo.vo.TrainingVO;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.validator.GroupValidator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Component
public class GroupTrainingManager {

    @Autowired
    private GroupTrainingEntityService groupTrainingEntityService;

    @Autowired
    private GroupEntityService groupEntityService;

    @Autowired
    private TrainingEntityService trainingEntityService;

    @Autowired
    private TrainingCategoryEntityService trainingCategoryEntityService;

    @Autowired
    private MappingTrainingCategoryEntityService mappingTrainingCategoryEntityService;

    @Autowired
    private TrainingRegisterEntityService trainingRegisterEntityService;

    @Autowired
    private AdminTrainingRecordManager adminTrainingRecordManager;

    @Autowired
    private GroupValidator groupValidator;

    public IPage<TrainingVO> getTrainingList(Integer limit, Integer currentPage, Long gid) throws StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!isRoot && !groupValidator.isGroupMember(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        return groupTrainingEntityService.getTrainingList(limit, currentPage, gid);
    }

    public IPage<Training> getAdminTrainingList(Integer limit, Integer currentPage, Long gid) throws StatusNotFoundException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!isRoot && !groupValidator.isGroupAdmin(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;

        return groupTrainingEntityService.getAdminTrainingList(limit, currentPage, gid);
    }

    public TrainingDTO getTraining(Long tid) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Training training = trainingEntityService.getById(tid);

        if (training == null) {
            throw new StatusNotFoundException("该训练不存在！");
        }

        Long gid = training.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUsername().equals(training.getAuthor()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        TrainingDTO trainingDto = new TrainingDTO();
        trainingDto.setTraining(training);

        QueryWrapper<MappingTrainingCategory> mappingTrainingCategoryQueryWrapper = new QueryWrapper<>();
        mappingTrainingCategoryQueryWrapper.eq("tid", tid);

        MappingTrainingCategory mappingTrainingCategory = mappingTrainingCategoryEntityService.getOne(mappingTrainingCategoryQueryWrapper);
        TrainingCategory trainingCategory = null;

        if (mappingTrainingCategory != null) {
            trainingCategory = trainingCategoryEntityService.getById(mappingTrainingCategory.getCid());
        }
        trainingDto.setTrainingCategory(trainingCategory);
        return trainingDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addTraining(TrainingDTO trainingDto) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long gid = trainingDto.getTraining().getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!isRoot && !groupValidator.isGroupAdmin(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        trainingDto.getTraining().setIsGroup(true);

        Training training = trainingDto.getTraining();
        trainingEntityService.save(training);
        TrainingCategory trainingCategory = trainingDto.getTrainingCategory();

        if (trainingCategory.getGid() != null && trainingCategory.getGid() != gid) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (trainingCategory.getId() == null) {
            try {
                trainingCategory.setGid(gid);
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
    public void updateTraining(TrainingDTO trainingDto) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long tid = trainingDto.getTraining().getId();

        Training training = trainingEntityService.getById(tid);

        if (training == null) {
            throw new StatusNotFoundException("该训练不存在！");
        }

        Long gid = training.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUsername().equals(training.getAuthor()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        trainingDto.getTraining().setIsGroup(training.getIsGroup());

        trainingEntityService.updateById(trainingDto.getTraining());

        if (trainingDto.getTraining().getAuth().equals(Constants.Training.AUTH_PRIVATE.getValue())) {
            if (!Objects.equals(training.getPrivatePwd(), trainingDto.getTraining().getPrivatePwd())) {
                UpdateWrapper<TrainingRegister> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("tid", tid);
                trainingRegisterEntityService.remove(updateWrapper);
            }
        }

        TrainingCategory trainingCategory = trainingDto.getTrainingCategory();

        if (trainingCategory.getGid() != null && trainingCategory.getGid().longValue() != gid) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        if (trainingCategory.getId() == null) {
            try {
                trainingCategory.setGid(gid);
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

    public void deleteTraining(Long tid) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Training training = trainingEntityService.getById(tid);

        if (training == null) {
            throw new StatusNotFoundException("该训练不存在！");
        }

        Long gid = training.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUsername().equals(training.getAuthor()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        boolean isOk = trainingEntityService.removeById(tid);
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
    }

    public void changeTrainingStatus(Long tid, Boolean status) throws StatusForbiddenException, StatusNotFoundException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Training training = trainingEntityService.getById(tid);

        if (training == null) {
            throw new StatusNotFoundException("该训练不存在！");
        }

        Long gid = training.getGid();

        Group group = groupEntityService.getById(gid);

        if (group == null || group.getStatus() == 1 && !isRoot) {
            throw new StatusNotFoundException("该团队不存在或已被封禁！");
        }

        if (!userRolesVo.getUsername().equals(training.getAuthor()) && !isRoot
                && !groupValidator.isGroupRoot(userRolesVo.getUid(), gid)) {
            throw new StatusForbiddenException("对不起，您无权限操作！");
        }

        UpdateWrapper<Training> trainingUpdateWrapper = new UpdateWrapper<>();
        trainingUpdateWrapper.eq("id", tid).set("status", status);

        boolean isOk = trainingEntityService.update(trainingUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }
}

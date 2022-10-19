package top.hcode.hoj.manager.group.training;

import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.exception.StatusNotFoundException;
import top.hcode.hoj.dao.group.GroupEntityService;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.training.*;
import top.hcode.hoj.manager.admin.training.AdminTrainingProblemManager;
import top.hcode.hoj.manager.admin.training.AdminTrainingRecordManager;
import top.hcode.hoj.pojo.dto.TrainingProblemDTO;
import top.hcode.hoj.pojo.entity.group.Group;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.training.*;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.validator.GroupValidator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

/**
 * @Author: LengYun
 * @Date: 2022/3/11 13:36
 * @Description:
 */
@Component
public class GroupTrainingProblemManager {

    @Autowired
    private GroupEntityService groupEntityService;

    @Autowired
    private TrainingEntityService trainingEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private AdminTrainingProblemManager adminTrainingProblemManager;

    @Autowired
    private TrainingProblemEntityService trainingProblemEntityService;

    @Autowired
    private AdminTrainingRecordManager adminTrainingRecordManager;

    @Autowired
    private GroupValidator groupValidator;

    public HashMap<String, Object> getTrainingProblemList(Integer limit, Integer currentPage, String keyword, Boolean queryExisted, Long tid) throws StatusNotFoundException, StatusForbiddenException {

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

        return adminTrainingProblemManager.getProblemList(limit, currentPage, keyword, queryExisted, tid);
    }

    public void updateTrainingProblem(TrainingProblem trainingProblem) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Training training = trainingEntityService.getById(trainingProblem.getTid());

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

        boolean isOk = trainingProblemEntityService.updateById(trainingProblem);
        if (!isOk) {
            throw new StatusFailException("修改失败！");
        }
    }

    public void deleteTrainingProblem(Long pid, Long tid) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
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

        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        trainingProblemQueryWrapper.eq("tid", tid).eq("pid", pid);

        boolean isOk = trainingProblemEntityService.remove(trainingProblemQueryWrapper);
        if (!isOk) {
            throw new StatusFailException("删除失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addProblemFromPublic(TrainingProblemDTO trainingProblemDto) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVO userRolesVo = (UserRolesVO) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Long pid = trainingProblemDto.getPid();

        Problem problem = problemEntityService.getById(pid);

        if (problem == null || problem.getAuth() != 1 || problem.getIsGroup()) {
            throw new StatusNotFoundException("该题目不存在或已被隐藏！");
        }

        Long tid = trainingProblemDto.getTid();

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

        String displayId = trainingProblemDto.getDisplayId();

        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        trainingProblemQueryWrapper.eq("tid", tid)
                .and(wrapper -> wrapper.eq("pid", pid)
                        .or()
                        .eq("display_id", displayId));
        TrainingProblem trainingProblem = trainingProblemEntityService.getOne(trainingProblemQueryWrapper);
        if (trainingProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的训练展示ID已存在！");
        }

        TrainingProblem newTProblem = new TrainingProblem();

        boolean isOk = trainingProblemEntityService.saveOrUpdate(newTProblem
                .setTid(tid).setPid(pid).setDisplayId(displayId));
        if (isOk) { // 添加成功

            // 更新训练最近更新时间
            UpdateWrapper<Training> trainingUpdateWrapper = new UpdateWrapper<>();
            trainingUpdateWrapper.set("gmt_modified", new Date())
                    .eq("id", tid);
            trainingEntityService.update(trainingUpdateWrapper);

            adminTrainingRecordManager.syncAlreadyRegisterUserRecord(tid, pid, newTProblem.getId());
        } else {
            throw new StatusFailException("添加失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addProblemFromGroup(String problemId, Long tid) throws StatusNotFoundException, StatusForbiddenException, StatusFailException {
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

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
        problemQueryWrapper.eq("problem_id", problemId).eq("gid", gid);

        Problem problem = problemEntityService.getOne(problemQueryWrapper);

        if (problem == null) {
            throw new StatusNotFoundException("该题目不存在或不是团队题目！");
        }

        QueryWrapper<TrainingProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", tid)
                .and(wrapper -> wrapper.eq("pid", problem.getId())
                        .or()
                        .eq("display_id", problem.getProblemId()));

        TrainingProblem trainingProblem = trainingProblemEntityService.getOne(queryWrapper);

        if (trainingProblem != null) {
            throw new StatusFailException("添加失败，该题目已添加或者题目的训练展示ID已存在！");
        }

        TrainingProblem newTProblem = new TrainingProblem();
        boolean isOk = trainingProblemEntityService.save(newTProblem
                .setTid(tid).setPid(problem.getId()).setDisplayId(problem.getProblemId()));
        if (isOk) {
            UpdateWrapper<Training> trainingUpdateWrapper = new UpdateWrapper<>();
            trainingUpdateWrapper.set("gmt_modified", new Date())
                    .eq("id", tid);
            trainingEntityService.update(trainingUpdateWrapper);
            adminTrainingRecordManager.syncAlreadyRegisterUserRecord(tid, problem.getId(), newTProblem.getId());
        } else {
            throw new StatusFailException("添加失败！");
        }
    }
}

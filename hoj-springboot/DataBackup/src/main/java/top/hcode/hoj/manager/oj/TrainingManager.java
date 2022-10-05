package top.hcode.hoj.manager.oj;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusAccessDeniedException;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.group.GroupMemberEntityService;
import top.hcode.hoj.dao.training.*;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.manager.admin.training.AdminTrainingRecordManager;
import top.hcode.hoj.pojo.dto.RegisterTrainingDto;
import top.hcode.hoj.pojo.entity.training.*;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.validator.GroupValidator;
import top.hcode.hoj.validator.TrainingValidator;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 17:12
 * @Description:
 */
@Component
public class TrainingManager {

    @Resource
    private TrainingEntityService trainingEntityService;

    @Resource
    private TrainingRegisterEntityService trainingRegisterEntityService;

    @Resource
    private TrainingCategoryEntityService trainingCategoryEntityService;

    @Resource
    private TrainingProblemEntityService trainingProblemEntityService;

    @Resource
    private TrainingRecordEntityService trainingRecordEntityService;

    @Resource
    private UserInfoEntityService userInfoEntityService;

    @Resource
    private AdminTrainingRecordManager adminTrainingRecordManager;

    @Resource
    private GroupMemberEntityService groupMemberEntityService;

    @Autowired
    private GroupValidator groupValidator;

    @Resource
    private TrainingValidator trainingValidator;

    /**
     * @param limit
     * @param currentPage
     * @param keyword
     * @param categoryId
     * @param auth
     * @MethodName getTrainingList
     * @Description 获取训练题单列表，可根据关键词、类别、权限、类型过滤
     * @Return
     * @Since 2021/11/20
     */
    public IPage<TrainingVo> getTrainingList(Integer limit, Integer currentPage, String keyword, Long categoryId, String auth) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 20;

        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        String currentUid = null;
        if (userRolesVo != null) {
            currentUid = userRolesVo.getUid();
        }

        return trainingEntityService.getTrainingList(limit, currentPage, categoryId, auth, keyword, currentUid);
    }


    /**
     * @param tid
     * @MethodName getTraining
     * @Description 根据tid获取指定训练详情
     * @Return
     * @Since 2021/11/20
     */
    public TrainingVo getTraining(Long tid) throws StatusFailException, StatusAccessDeniedException, StatusForbiddenException {
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        boolean isRoot = SecurityUtils.getSubject().hasRole("root");

        Training training = trainingEntityService.getById(tid);
        if (training == null || !training.getStatus()) {
            throw new StatusFailException("该训练不存在或不允许显示！");
        }

        Long gid = training.getGid();
        if (training.getIsGroup()) {
            if (!isRoot && !groupValidator.isGroupMember(userRolesVo.getUid(), training.getGid())) {
                throw new StatusForbiddenException("对不起，您无权限操作！");
            }
        } else {
            gid = null;
        }

        TrainingVo trainingVo = BeanUtil.copyProperties(training, TrainingVo.class);
        TrainingCategory trainingCategory = trainingCategoryEntityService.getTrainingCategoryByTrainingId(training.getId());
        trainingVo.setCategoryName(trainingCategory.getName());
        trainingVo.setCategoryColor(trainingCategory.getColor());
        List<Long> trainingProblemIdList = trainingProblemEntityService.getTrainingProblemIdList(training.getId());
        trainingVo.setProblemCount(trainingProblemIdList.size());

        if (userRolesVo != null && trainingValidator.isInTrainingOrAdmin(training, userRolesVo)) {
            Integer count = trainingProblemEntityService.getUserTrainingACProblemCount(userRolesVo.getUid(), gid, trainingProblemIdList);
            trainingVo.setAcCount(count);
        } else {
            trainingVo.setAcCount(0);
        }

        return trainingVo;
    }

    /**
     * @param tid
     * @MethodName getTrainingProblemList
     * @Description 根据tid获取指定训练的题单题目列表
     * @Return
     * @Since 2021/11/20
     */
    public List<ProblemVo> getTrainingProblemList(Long tid) throws StatusAccessDeniedException,
            StatusForbiddenException, StatusFailException {
        Training training = trainingEntityService.getById(tid);
        if (training == null || !training.getStatus()) {
            throw new StatusFailException("该训练不存在或不允许显示！");
        }
        trainingValidator.validateTrainingAuth(training);

        return trainingProblemEntityService.getTrainingProblemList(tid);

    }

    /**
     * @param registerTrainingDto
     * @MethodName toRegisterTraining
     * @Description 注册校验私有权限的训练
     * @Return
     * @Since 2021/11/20
     */
    public void toRegisterTraining(RegisterTrainingDto registerTrainingDto) throws StatusFailException, StatusForbiddenException {

        Long tid = registerTrainingDto.getTid();
        String password = registerTrainingDto.getPassword();

        if (tid == null || StringUtils.isEmpty(password)) {
            throw new StatusFailException("请求参数不能为空！");
        }

        Training training = trainingEntityService.getById(tid);

        if (training == null || !training.getStatus()) {
            throw new StatusFailException("对不起，该训练不存在或不允许显示!");
        }

        if (!training.getPrivatePwd().equals(password)) { // 密码不对
            throw new StatusFailException("训练密码错误，请重新输入！");
        }

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        QueryWrapper<TrainingRegister> registerQueryWrapper = new QueryWrapper<>();
        registerQueryWrapper.eq("tid", tid).eq("uid", userRolesVo.getUid());
        if (trainingRegisterEntityService.count(registerQueryWrapper) > 0) {
            throw new StatusFailException("您已注册过该训练，请勿重复注册！");
        }

        boolean isOk = trainingRegisterEntityService.save(new TrainingRegister()
                .setTid(tid)
                .setUid(userRolesVo.getUid()));

        if (!isOk) {
            throw new StatusFailException("校验训练密码失败，请稍后再试");
        } else {
            adminTrainingRecordManager.syncUserSubmissionToRecordByTid(tid, userRolesVo.getUid());
        }
    }


    /**
     * @param tid
     * @MethodName getTrainingAccess
     * @Description 私有权限的训练需要获取当前用户是否有进入训练的权限
     * @Return
     * @Since 2021/11/20
     */
    public AccessVo getTrainingAccess(Long tid) throws StatusFailException {

        // 获取当前登录的用户
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        QueryWrapper<TrainingRegister> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", tid).eq("uid", userRolesVo.getUid());
        TrainingRegister trainingRegister = trainingRegisterEntityService.getOne(queryWrapper, false);
        boolean access = false;
        if (trainingRegister != null) {
            access = true;
            Training training = trainingEntityService.getById(tid);
            if (training == null || !training.getStatus()) {
                throw new StatusFailException("对不起，该训练不存在!");
            }
        }

        AccessVo accessVo = new AccessVo();
        accessVo.setAccess(access);

        return accessVo;
    }


    /**
     * @param tid
     * @param limit
     * @param currentPage
     * @MethodName getTrainingRnk
     * @Description 获取训练的排行榜分页
     * @Return
     * @Since 2021/11/22
     */
    public IPage<TrainingRankVo> getTrainingRank(Long tid, Integer limit, Integer currentPage) throws
            StatusAccessDeniedException, StatusForbiddenException, StatusFailException {

        Training training = trainingEntityService.getById(tid);
        if (training == null || !training.getStatus()) {
            throw new StatusFailException("该训练不存在或不允许显示！");
        }

        trainingValidator.validateTrainingAuth(training);

        // 页数，每页数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 30;

        return getTrainingRank(tid, training.getIsGroup() ? training.getGid() : null, training.getAuthor(), currentPage, limit);
    }

    private IPage<TrainingRankVo> getTrainingRank(Long tid, Long gid, String username, int currentPage, int limit) {

        Map<Long, String> tpIdMapDisplayId = getTPIdMapDisplayId(tid);
        List<TrainingRecordVo> trainingRecordVoList = trainingRecordEntityService.getTrainingRecord(tid);

        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();
        if (gid != null) {
            List<String> groupRootUidList = groupMemberEntityService.getGroupRootUidList(gid);
            superAdminUidList.addAll(groupRootUidList);
        }

        List<TrainingRankVo> result = new ArrayList<>();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();
        int pos = 0;
        for (TrainingRecordVo trainingRecordVo : trainingRecordVoList) {
            // 超级管理员和训练创建者的提交不入排行榜
            if (username.equals(trainingRecordVo.getUsername())
                    || superAdminUidList.contains(trainingRecordVo.getUid())) {
                continue;
            }

            TrainingRankVo trainingRankVo;
            Integer index = uidMapIndex.get(trainingRecordVo.getUid());
            if (index == null) {
                trainingRankVo = new TrainingRankVo();
                trainingRankVo.setRealname(trainingRecordVo.getRealname())
                        .setAvatar(trainingRecordVo.getAvatar())
                        .setSchool(trainingRecordVo.getSchool())
                        .setGender(trainingRecordVo.getGender())
                        .setUid(trainingRecordVo.getUid())
                        .setUsername(trainingRecordVo.getUsername())
                        .setNickname(trainingRecordVo.getNickname())
                        .setAc(0)
                        .setTotalRunTime(0);
                HashMap<String, HashMap<String, Object>> submissionInfo = new HashMap<>();
                trainingRankVo.setSubmissionInfo(submissionInfo);

                result.add(trainingRankVo);
                uidMapIndex.put(trainingRecordVo.getUid(), pos);
                pos++;
            } else {
                trainingRankVo = result.get(index);
            }
            String displayId = tpIdMapDisplayId.get(trainingRecordVo.getTpid());
            HashMap<String, Object> problemSubmissionInfo = trainingRankVo
                    .getSubmissionInfo()
                    .getOrDefault(displayId, new HashMap<>());

            // 如果该题目已经AC过了，只比较运行时间取最小
            if ((Boolean) problemSubmissionInfo.getOrDefault("isAC", false)) {
                if (trainingRecordVo.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                    int runTime = (int) problemSubmissionInfo.getOrDefault("runTime", 0);
                    if (runTime > trainingRecordVo.getUseTime()) {
                        trainingRankVo.setTotalRunTime(trainingRankVo.getTotalRunTime() - runTime + trainingRecordVo.getUseTime());
                        problemSubmissionInfo.put("runTime", trainingRecordVo.getUseTime());
                    }
                }
                continue;
            }

            problemSubmissionInfo.put("status", trainingRecordVo.getStatus());
            problemSubmissionInfo.put("score", trainingRecordVo.getScore());

            // 通过的话
            if (trainingRecordVo.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                // 总解决题目次数ac+1
                trainingRankVo.setAc(trainingRankVo.getAc() + 1);
                problemSubmissionInfo.put("isAC", true);
                problemSubmissionInfo.put("runTime", trainingRecordVo.getUseTime());
                trainingRankVo.setTotalRunTime(trainingRankVo.getTotalRunTime() + trainingRecordVo.getUseTime());
            }

            trainingRankVo.getSubmissionInfo().put(displayId, problemSubmissionInfo);
        }

        List<TrainingRankVo> orderResultList = result.stream().sorted(Comparator.comparing(TrainingRankVo::getAc, Comparator.reverseOrder()) // 先以总ac数降序
                .thenComparing(TrainingRankVo::getTotalRunTime) //再以总耗时升序
        ).collect(Collectors.toList());

        // 计算好排行榜，然后进行分页
        Page<TrainingRankVo> page = new Page<>(currentPage, limit);
        int count = orderResultList.size();
        List<TrainingRankVo> pageList = new ArrayList<>();
        //计算当前页第一条数据的下标
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            pageList.add(orderResultList.get(currId + i));
        }
        page.setSize(limit);
        page.setCurrent(currentPage);
        page.setTotal(count);
        page.setRecords(pageList);
        return page;
    }

    private Map<Long, String> getTPIdMapDisplayId(Long tid) {
        QueryWrapper<TrainingProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", tid);
        List<TrainingProblem> trainingProblemList = trainingProblemEntityService.list(queryWrapper);
        return trainingProblemList.stream().collect(Collectors.toMap(TrainingProblem::getId, TrainingProblem::getDisplayId));
    }

    /**
     * 未启用，该操作会导致公开训练也记录，但其实并不需要，会造成数据量无效增加
     */
    @Async
    public void checkAndSyncTrainingRecord(Long pid, Long submitId, String uid) {
        List<TrainingProblem> trainingProblemList = trainingProblemEntityService.getPrivateTrainingProblemListByPid(pid, uid);
        if (!CollectionUtils.isEmpty(trainingProblemList)) {
            List<TrainingRecord> trainingRecordList = new ArrayList<>();
            for (TrainingProblem trainingProblem : trainingProblemList) {
                TrainingRecord trainingRecord = new TrainingRecord();
                trainingRecord.setPid(pid)
                        .setTid(trainingProblem.getTid())
                        .setTpid(trainingProblem.getId())
                        .setSubmitId(submitId)
                        .setUid(uid);
                trainingRecordList.add(trainingRecord);
            }
            trainingRecordEntityService.saveBatch(trainingRecordList);
        }
    }
}
package top.hcode.hoj.manager.oj;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.dao.problem.ProblemEntityService;
import top.hcode.hoj.dao.user.*;
import top.hcode.hoj.manager.email.EmailManager;
import top.hcode.hoj.pojo.dto.ChangeEmailDTO;
import top.hcode.hoj.pojo.dto.ChangePasswordDTO;
import top.hcode.hoj.pojo.dto.CheckUsernameOrEmailDTO;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.user.Role;
import top.hcode.hoj.pojo.entity.user.Session;
import top.hcode.hoj.pojo.entity.user.UserAcproblem;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.shiro.AccountProfile;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;
import top.hcode.hoj.validator.CommonValidator;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 16:53
 * @Description:
 */
@Component
public class AccountManager {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Autowired
    private UserRoleEntityService userRoleEntityService;

    @Autowired
    private UserRecordEntityService userRecordEntityService;

    @Autowired
    private UserAcproblemEntityService userAcproblemEntityService;

    @Autowired
    private ProblemEntityService problemEntityService;

    @Autowired
    private SessionEntityService sessionEntityService;

    @Autowired
    private CommonValidator commonValidator;

    @Autowired
    private EmailManager emailManager;

    /**
     * @MethodName checkUsernameOrEmail
     * @Params * @param null
     * @Description 检验用户名和邮箱是否存在
     * @Return
     * @Since 2020/11/5
     */
    public CheckUsernameOrEmailVO checkUsernameOrEmail(CheckUsernameOrEmailDTO checkUsernameOrEmailDto) {

        String email = checkUsernameOrEmailDto.getEmail();

        String username = checkUsernameOrEmailDto.getUsername();

        boolean rightEmail = false;

        boolean rightUsername = false;

        if (!StringUtils.isEmpty(email)) {
            email = email.trim();
            boolean isEmail = Validator.isEmail(email);
            if (!isEmail) {
                rightEmail = false;
            } else {
                QueryWrapper<UserInfo> wrapper = new QueryWrapper<UserInfo>().eq("email", email);
                UserInfo user = userInfoEntityService.getOne(wrapper, false);
                if (user != null) {
                    rightEmail = true;
                } else {
                    rightEmail = false;
                }
            }
        }

        if (!StringUtils.isEmpty(username)) {
            username = username.trim();
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<UserInfo>().eq("username", username);
            UserInfo user = userInfoEntityService.getOne(wrapper, false);
            if (user != null) {
                rightUsername = true;
            } else {
                rightUsername = false;
            }
        }

        CheckUsernameOrEmailVO checkUsernameOrEmailVo = new CheckUsernameOrEmailVO();
        checkUsernameOrEmailVo.setEmail(rightEmail);
        checkUsernameOrEmailVo.setUsername(rightUsername);
        return checkUsernameOrEmailVo;
    }

    /**
     * @param uid
     * @MethodName getUserHomeInfo
     * @Description 前端userHome用户个人主页的数据请求，主要是返回解决题目数，AC的题目列表，提交总数，AC总数，Rating分，
     * @Since 2021/01/07
     */
    public UserHomeVO getUserHomeInfo(String uid, String username) throws StatusFailException {

        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 如果没有uid和username，默认查询当前登录用户的
        if (StringUtils.isEmpty(uid) && StringUtils.isEmpty(username)) {
            if (userRolesVo != null) {
                uid = userRolesVo.getUid();
            } else {
                throw new StatusFailException("请求参数错误：uid和username不能都为空！");
            }
        }

        UserHomeVO userHomeInfo = userRecordEntityService.getUserHomeInfo(uid, username);
        if (userHomeInfo == null) {
            throw new StatusFailException("用户不存在");
        }
        QueryWrapper<UserAcproblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid", userHomeInfo.getUid())
                .select("distinct pid", "submit_id")
                .orderByAsc("submit_id");

        List<UserAcproblem> acProblemList = userAcproblemEntityService.list(queryWrapper);
        List<Long> pidList = acProblemList.stream().map(UserAcproblem::getPid).collect(Collectors.toList());

        List<String> disPlayIdList = new LinkedList<>();

        if (pidList.size() > 0) {
            QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();
            problemQueryWrapper.select("id", "problem_id", "difficulty");
            problemQueryWrapper.in("id", pidList);
            List<Problem> problems = problemEntityService.list(problemQueryWrapper);
            Map<Integer, List<UserHomeProblemVO>> map = problems.stream()
                    .map(this::convertProblemVO)
                    .collect(Collectors.groupingBy(UserHomeProblemVO::getDifficulty));
            userHomeInfo.setSolvedGroupByDifficulty(map);
            disPlayIdList = problems.stream().map(Problem::getProblemId).collect(Collectors.toList());
        }
        userHomeInfo.setSolvedList(disPlayIdList);
        QueryWrapper<Session> sessionQueryWrapper = new QueryWrapper<>();
        sessionQueryWrapper.eq("uid", userHomeInfo.getUid())
                .orderByDesc("gmt_create")
                .last("limit 1");

        Session recentSession = sessionEntityService.getOne(sessionQueryWrapper, false);
        if (recentSession != null) {
            userHomeInfo.setRecentLoginTime(recentSession.getGmtCreate());
        }
        return userHomeInfo;
    }

    private UserHomeProblemVO convertProblemVO(Problem problem) {
        return UserHomeProblemVO.builder()
                .problemId(problem.getProblemId())
                .id(problem.getId())
                .difficulty(problem.getDifficulty())
                .build();
    }

    /**
     * @param uid
     * @param username
     * @return
     * @Description 获取用户最近一年的提交热力图数据
     */
    public UserCalendarHeatmapVO getUserCalendarHeatmap(String uid, String username) throws StatusFailException {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (StringUtils.isEmpty(uid) && StringUtils.isEmpty(username)) {
            if (userRolesVo != null) {
                uid = userRolesVo.getUid();
            } else {
                throw new StatusFailException("请求参数错误：uid和username不能都为空！");
            }
        }
        UserCalendarHeatmapVO userCalendarHeatmapVo = new UserCalendarHeatmapVO();
        userCalendarHeatmapVo.setEndDate(DateUtil.format(new Date(), "yyyy-MM-dd"));
        List<Judge> lastYearUserJudgeList = userRecordEntityService.getLastYearUserJudgeList(uid, username);
        if (CollectionUtils.isEmpty(lastYearUserJudgeList)) {
            userCalendarHeatmapVo.setDataList(new ArrayList<>());
            return userCalendarHeatmapVo;
        }
        HashMap<String, Integer> tmpRecordMap = new HashMap<>();
        for (Judge judge : lastYearUserJudgeList) {
            Date submitTime = judge.getSubmitTime();
            String dateStr = DateUtil.format(submitTime, "yyyy-MM-dd");
            tmpRecordMap.merge(dateStr, 1, Integer::sum);
        }
        List<HashMap<String, Object>> dataList = new ArrayList<>();
        for (Map.Entry<String, Integer> record : tmpRecordMap.entrySet()) {
            HashMap<String, Object> tmp = new HashMap<>(2);
            tmp.put("date", record.getKey());
            tmp.put("count", record.getValue());
            dataList.add(tmp);
        }
        userCalendarHeatmapVo.setDataList(dataList);
        return userCalendarHeatmapVo;
    }


    /**
     * @MethodName changePassword
     * @Description 修改密码的操作，连续半小时内修改密码错误5次，则需要半个小时后才可以再次尝试修改密码
     * @Return
     * @Since 2021/1/8
     */
    public ChangeAccountVO changePassword(ChangePasswordDTO changePasswordDto) throws StatusSystemErrorException, StatusFailException {
        String oldPassword = changePasswordDto.getOldPassword();
        String newPassword = changePasswordDto.getNewPassword();

        // 数据可用性判断
        if (StringUtils.isEmpty(oldPassword) || StringUtils.isEmpty(newPassword)) {
            throw new StatusFailException("错误：原始密码或新密码不能为空！");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new StatusFailException("新密码长度应该为6~20位！");
        }
        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        // 如果已经被锁定半小时，则不能修改
        String lockKey = Constants.Account.CODE_CHANGE_PASSWORD_LOCK + userRolesVo.getUid();
        // 统计失败的key
        String countKey = Constants.Account.CODE_CHANGE_PASSWORD_FAIL + userRolesVo.getUid();

        ChangeAccountVO resp = new ChangeAccountVO();
        if (redisUtils.hasKey(lockKey)) {
            long expire = redisUtils.getExpire(lockKey);
            Date now = new Date();
            long minute = expire / 60;
            long second = expire % 60;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resp.setCode(403);
            Date afterDate = new Date(now.getTime() + expire * 1000);
            String msg = "由于您多次修改密码失败，修改密码功能已锁定，请在" + minute + "分" + second + "秒后(" + formatter.format(afterDate) + ")再进行尝试！";
            resp.setMsg(msg);
            return resp;
        }
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.select("uuid", "password")
                .eq("uuid", userRolesVo.getUid());
        UserInfo userInfo = userInfoEntityService.getOne(userInfoQueryWrapper, false);
        // 与当前登录用户的密码进行比较判断
        if (userInfo.getPassword().equals(SecureUtil.md5(oldPassword))) { // 如果相同，则进行修改密码操作
            UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("password", SecureUtil.md5(newPassword))// 数据库用户密码全部用md5加密
                    .eq("uuid", userRolesVo.getUid());
            boolean isOk = userInfoEntityService.update(updateWrapper);
            if (isOk) {
                resp.setCode(200);
                resp.setMsg("修改密码成功！您将于5秒钟后退出进行重新登录操作！");
                // 清空记录
                redisUtils.del(countKey);
                return resp;
            } else {
                throw new StatusSystemErrorException("系统错误：修改密码失败！");
            }
        } else { // 如果不同，则进行记录，当失败次数达到5次，半个小时后才可重试
            Integer count = (Integer) redisUtils.get(countKey);
            if (count == null) {
                redisUtils.set(countKey, 1, 60 * 30); // 三十分钟不尝试，该限制会自动清空消失
                count = 0;
            } else if (count < 5) {
                redisUtils.incr(countKey, 1);
            }
            count++;
            if (count == 5) {
                redisUtils.del(countKey); // 清空统计
                redisUtils.set(lockKey, "lock", 60 * 30); // 设置锁定更改
            }
            resp.setCode(400);
            resp.setMsg("原始密码错误！您已累计修改密码失败" + count + "次...");
            return resp;
        }
    }


    public void getChangeEmailCode(String email) throws StatusFailException {

        String lockKey = Constants.Email.CHANGE_EMAIL_LOCK + email;
        if (redisUtils.hasKey(lockKey)) {
            throw new StatusFailException("对不起，您的操作频率过快，请在" + redisUtils.getExpire(lockKey) + "秒后再次发送修改邮件！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        QueryWrapper<UserInfo> emailUserInfoQueryWrapper = new QueryWrapper<>();
        emailUserInfoQueryWrapper.select("uuid", "email")
                .eq("email", email);
        UserInfo emailUserInfo = userInfoEntityService.getOne(emailUserInfoQueryWrapper, false);

        if (emailUserInfo != null) {
            if (Objects.equals(emailUserInfo.getUuid(), userRolesVo.getUid())) {
                throw new StatusFailException("新邮箱与当前邮箱一致，请不要重复设置！");
            } else {
                throw new StatusFailException("该邮箱已被他人使用，请重新设置其它邮箱！");
            }
        }

        String numbers = RandomUtil.randomNumbers(6); // 随机生成6位数字的组合
        redisUtils.set(Constants.Email.CHANGE_EMAIL_KEY_PREFIX.getValue() + email, numbers, 10 * 60); //默认验证码有效10分钟
        emailManager.sendChangeEmailCode(email, userRolesVo.getUsername(), numbers);
        redisUtils.set(lockKey, 0, 30);
    }


    /**
     * @MethodName changeEmail
     * @Description 修改邮箱的操作，连续半小时内密码错误5次，则需要半个小时后才可以再次尝试修改
     * @Return
     * @Since 2021/1/9
     */
    public ChangeAccountVO changeEmail(ChangeEmailDTO changeEmailDto) throws StatusSystemErrorException, StatusFailException {

        String password = changeEmailDto.getPassword();
        String newEmail = changeEmailDto.getNewEmail();
        String code = changeEmailDto.getCode();
        // 数据可用性判断
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(newEmail) || StringUtils.isEmpty(code)) {
            throw new StatusFailException("错误：密码、新邮箱或验证码不能为空！");
        }

        if (!Validator.isEmail(newEmail)) {
            throw new StatusFailException("邮箱格式错误！");
        }

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        // 如果已经被锁定半小时不能修改
        String lockKey = Constants.Account.CODE_CHANGE_EMAIL_LOCK + userRolesVo.getUid();
        // 统计失败的key
        String countKey = Constants.Account.CODE_CHANGE_EMAIL_FAIL + userRolesVo.getUid();

        ChangeAccountVO resp = new ChangeAccountVO();
        if (redisUtils.hasKey(lockKey)) {
            long expire = redisUtils.getExpire(lockKey);
            Date now = new Date();
            long minute = expire / 60;
            long second = expire % 60;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resp.setCode(403);
            Date afterDate = new Date(now.getTime() + expire * 1000);
            String msg = "由于您多次修改邮箱失败，修改邮箱功能已锁定，请在" + minute + "分" + second + "秒后(" + formatter.format(afterDate) + ")再进行尝试！";
            resp.setMsg(msg);
            return resp;
        }

        QueryWrapper<UserInfo> emailUserInfoQueryWrapper = new QueryWrapper<>();
        emailUserInfoQueryWrapper.select("uuid", "email")
                .eq("email", changeEmailDto.getNewEmail());
        UserInfo emailUserInfo = userInfoEntityService.getOne(emailUserInfoQueryWrapper, false);

        if (emailUserInfo != null) {
            if (Objects.equals(emailUserInfo.getUuid(), userRolesVo.getUid())) {
                throw new StatusFailException("新邮箱与当前邮箱一致，请不要重复设置！");
            } else {
                throw new StatusFailException("该邮箱已被他人使用，请重新设置其它邮箱！");
            }
        }

        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.select("uuid", "password")
                .eq("uuid", userRolesVo.getUid());
        UserInfo userInfo = userInfoEntityService.getOne(userInfoQueryWrapper, false);

        String cacheCodeKey = Constants.Email.CHANGE_EMAIL_KEY_PREFIX.getValue() + newEmail;
        String cacheCode = (String) redisUtils.get(cacheCodeKey);
        if (cacheCode == null) {
            throw new StatusFailException("修改邮箱验证码不存在或已过期，请重新发送！");
        }

        if (!Objects.equals(cacheCode, code)) {
            Integer count = (Integer) redisUtils.get(countKey);
            if (count == null) {
                redisUtils.set(countKey, 1, 60 * 30); // 三十分钟不尝试，该限制会自动清空消失
                count = 0;
            } else if (count < 5) {
                redisUtils.incr(countKey, 1);
            }
            count++;
            if (count == 5) {
                redisUtils.del(countKey); // 清空统计
                redisUtils.set(lockKey, "lock", 60 * 30); // 设置锁定更改
            }

            resp.setCode(400);
            resp.setMsg("验证码错误！您已累计修改邮箱失败" + count + "次...");
            return resp;
        }

        // 与当前登录用户的密码进行比较判断
        if (userInfo.getPassword().equals(SecureUtil.md5(password))) { // 如果相同，则进行修改操作
            UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("email", newEmail)
                    .eq("uuid", userRolesVo.getUid());

            boolean isOk = userInfoEntityService.update(updateWrapper);
            if (isOk) {

                UserRolesVO userRoles = userRoleEntityService.getUserRoles(userRolesVo.getUid(), null);
                UserInfoVO userInfoVo = new UserInfoVO();

                BeanUtil.copyProperties(userRoles, userInfoVo, "roles");
                userInfoVo.setRoleList(userRoles
                        .getRoles()
                        .stream()
                        .map(Role::getRole)
                        .collect(Collectors.toList()));
                resp.setCode(200);
                resp.setMsg("修改邮箱成功！");
                resp.setUserInfo(userInfoVo);
                // 清空记录
                redisUtils.del(countKey, cacheCodeKey);
                return resp;
            } else {
                throw new StatusSystemErrorException("系统错误：修改邮箱失败！");
            }
        } else { // 如果不同，则进行记录，当失败次数达到5次，半个小时后才可重试
            Integer count = (Integer) redisUtils.get(countKey);
            if (count == null) {
                redisUtils.set(countKey, 1, 60 * 30); // 三十分钟不尝试，该限制会自动清空消失
                count = 0;
            } else if (count < 5) {
                redisUtils.incr(countKey, 1);
            }
            count++;
            if (count == 5) {
                redisUtils.del(countKey); // 清空统计
                redisUtils.set(lockKey, "lock", 60 * 30); // 设置锁定更改
            }

            resp.setCode(400);
            resp.setMsg("密码错误！您已累计修改邮箱失败" + count + "次...");
            return resp;
        }
    }


    public UserInfoVO changeUserInfo(UserInfoVO userInfoVo) throws StatusFailException {

        commonValidator.validateContentLength(userInfoVo.getRealname(), "真实姓名",50);
        commonValidator.validateContentLength(userInfoVo.getNickname(), "昵称",20);
        commonValidator.validateContentLength(userInfoVo.getSignature(), "个性简介",65535);
        commonValidator.validateContentLength(userInfoVo.getBlog(), "博客", 255);
        commonValidator.validateContentLength(userInfoVo.getGithub(), "Github", 255);
        commonValidator.validateContentLength(userInfoVo.getSchool(), "学校", 100);
        commonValidator.validateContentLength(userInfoVo.getNumber(), "学号", 200);
        commonValidator.validateContentLength(userInfoVo.getCfUsername(), "Codeforces用户名", 255);

        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();

        UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uuid", userRolesVo.getUid())
                .set("cf_username", userInfoVo.getCfUsername())
                .set("realname", userInfoVo.getRealname())
                .set("nickname", userInfoVo.getNickname())
                .set("signature", userInfoVo.getSignature())
                .set("blog", userInfoVo.getBlog())
                .set("gender", userInfoVo.getGender())
                .set("github", userInfoVo.getGithub())
                .set("school", userInfoVo.getSchool())
                .set("number", userInfoVo.getNumber());

        boolean isOk = userInfoEntityService.update(updateWrapper);

        if (isOk) {
            UserRolesVO userRoles = userRoleEntityService.getUserRoles(userRolesVo.getUid(), null);
            // 更新session
            BeanUtil.copyProperties(userRoles, userRolesVo);
            UserInfoVO userInfoVO = new UserInfoVO();
            BeanUtil.copyProperties(userRoles, userInfoVO, "roles");
            userInfoVO.setRoleList(userRoles.getRoles().stream().map(Role::getRole).collect(Collectors.toList()));
            return userInfoVO;
        } else {
            throw new StatusFailException("更新个人信息失败！");
        }

    }

    public UserAuthInfoVO getUserAuthInfo(){
        // 获取当前登录的用户
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        //获取该用户角色所有的权限
        List<Role> roles = userRoleEntityService.getRolesByUid(userRolesVo.getUid());
        UserAuthInfoVO authInfoVO = new UserAuthInfoVO();
        authInfoVO.setRoles(roles.stream().map(Role::getRole).collect(Collectors.toList()));
        return authInfoVO;
    }

}
package top.hcode.hoj.manager.oj;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.exception.StatusAccessDeniedException;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.dao.user.SessionEntityService;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.dao.user.UserRecordEntityService;
import top.hcode.hoj.dao.user.UserRoleEntityService;
import top.hcode.hoj.manager.email.EmailManager;
import top.hcode.hoj.manager.msg.NoticeManager;
import top.hcode.hoj.pojo.bo.EmailRuleBO;
import top.hcode.hoj.pojo.dto.ApplyResetPasswordDTO;
import top.hcode.hoj.pojo.dto.LoginDTO;
import top.hcode.hoj.pojo.dto.RegisterDTO;
import top.hcode.hoj.pojo.dto.ResetPasswordDTO;
import top.hcode.hoj.pojo.entity.user.*;
import top.hcode.hoj.pojo.vo.ConfigVO;
import top.hcode.hoj.pojo.vo.RegisterCodeVO;
import top.hcode.hoj.pojo.vo.UserInfoVO;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.IpUtils;
import top.hcode.hoj.utils.JwtUtils;
import top.hcode.hoj.utils.RedisUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 16:46
 * @Description:
 */
@Component
public class PassportManager {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private ConfigVO configVo;

    @Resource
    private EmailRuleBO emailRuleBO;

    @Resource
    private UserInfoEntityService userInfoEntityService;

    @Resource
    private UserRoleEntityService userRoleEntityService;

    @Resource
    private UserRecordEntityService userRecordEntityService;

    @Resource
    private SessionEntityService sessionEntityService;


    @Resource
    private EmailManager emailManager;

    @Resource
    private NoticeManager noticeManager;

    public UserInfoVO login(LoginDTO loginDto, HttpServletResponse response, HttpServletRequest request) throws StatusFailException {
        // 去掉账号密码首尾的空格
        loginDto.setPassword(loginDto.getPassword().trim());
        loginDto.setUsername(loginDto.getUsername().trim());

        if (StringUtils.isEmpty(loginDto.getUsername()) || StringUtils.isEmpty(loginDto.getPassword())) {
            throw new StatusFailException("用户名或密码不能为空！");
        }

        if (loginDto.getPassword().length() < 6 || loginDto.getPassword().length() > 20) {
            throw new StatusFailException("密码长度应该为6~20位！");
        }
        if (loginDto.getUsername().length() > 20) {
            throw new StatusFailException("用户名长度不能超过20位!");
        }

        String userIpAddr = IpUtils.getUserIpAddr(request);
        String key = Constants.Account.TRY_LOGIN_NUM.getCode() + loginDto.getUsername() + "_" + userIpAddr;
        Integer tryLoginCount = (Integer) redisUtils.get(key);

        if (tryLoginCount != null && tryLoginCount >= 20) {
            throw new StatusFailException("对不起！登录失败次数过多！您的账号有风险，半个小时内暂时无法登录！");
        }

        UserRolesVO userRolesVo = userRoleEntityService.getUserRoles(null, loginDto.getUsername());

        if (userRolesVo == null) {
            throw new StatusFailException("用户名或密码错误！请注意大小写！");
        }

        if (!userRolesVo.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))) {
            if (tryLoginCount == null) {
                redisUtils.set(key, 1, 60 * 30); // 三十分钟不尝试，该限制会自动清空消失
            } else {
                redisUtils.set(key, tryLoginCount + 1, 60 * 30);
            }
            throw new StatusFailException("用户名或密码错误！请注意大小写！");
        }

        if (userRolesVo.getStatus() != 0) {
            throw new StatusFailException("该账户已被封禁，请联系管理员进行处理！");
        }

        String jwt = jwtUtils.generateToken(userRolesVo.getUid());
        response.setHeader("Authorization", jwt); //放到信息头部
        response.setHeader("Access-Control-Expose-Headers", "Authorization");

        // 会话记录
        sessionEntityService.save(new Session()
                .setUid(userRolesVo.getUid())
                .setIp(IpUtils.getUserIpAddr(request))
                .setUserAgent(request.getHeader("User-Agent")));

        // 登录成功，清除锁定限制
        if (tryLoginCount != null) {
            redisUtils.del(key);
        }

        // 异步检查是否异地登录
        sessionEntityService.checkRemoteLogin(userRolesVo.getUid());

        UserInfoVO userInfoVo = new UserInfoVO();
        BeanUtil.copyProperties(userRolesVo, userInfoVo, "roles");
        userInfoVo.setRoleList(userRolesVo.getRoles()
                .stream()
                .map(Role::getRole)
                .collect(Collectors.toList()));
        return userInfoVo;
    }


    public RegisterCodeVO getRegisterCode(String email) throws StatusAccessDeniedException, StatusFailException, StatusForbiddenException {

        if (!configVo.getRegister()) { // 需要判断一下网站是否开启注册
            throw new StatusAccessDeniedException("对不起！本站暂未开启注册功能！");
        }
        if (!emailManager.isOk()) {
            throw new StatusAccessDeniedException("对不起！本站邮箱系统未配置，暂不支持注册！");
        }

        email = email.trim();

        boolean isEmail = Validator.isEmail(email);
        if (!isEmail) {
            throw new StatusFailException("对不起！您的邮箱格式不正确！");
        }

        boolean isBlackEmail = emailRuleBO.getBlackList().stream().anyMatch(email::endsWith);
        if (isBlackEmail) {
            throw new StatusForbiddenException("对不起！您的邮箱无法注册本网站！");
        }

        String lockKey = Constants.Email.REGISTER_EMAIL_LOCK + email;
        if (redisUtils.hasKey(lockKey)) {
            throw new StatusFailException("对不起，您的操作频率过快，请在" + redisUtils.getExpire(lockKey) + "秒后再次发送注册邮件！");
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        UserInfo userInfo = userInfoEntityService.getOne(queryWrapper, false);
        if (userInfo != null) {
            throw new StatusFailException("对不起！该邮箱已被注册，请更换新的邮箱！");
        }

        String numbers = RandomUtil.randomNumbers(6); // 随机生成6位数字的组合
        redisUtils.set(Constants.Email.REGISTER_KEY_PREFIX.getValue() + email, numbers, 5 * 60);//默认验证码有效5分钟
        emailManager.sendCode(email, numbers);
        redisUtils.set(lockKey, 0, 60);

        RegisterCodeVO registerCodeVo = new RegisterCodeVO();
        registerCodeVo.setEmail(email);
        registerCodeVo.setExpire(5 * 60);

        return registerCodeVo;
    }


    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDto) throws StatusAccessDeniedException, StatusFailException {

        if (!configVo.getRegister()) { // 需要判断一下网站是否开启注册
            throw new StatusAccessDeniedException("对不起！本站暂未开启注册功能！");
        }

        String codeKey = Constants.Email.REGISTER_KEY_PREFIX.getValue() + registerDto.getEmail();
        if (!redisUtils.hasKey(codeKey)) {
            throw new StatusFailException("验证码不存在或已过期");
        }

        if (!redisUtils.get(codeKey).equals(registerDto.getCode())) { //验证码判断
            throw new StatusFailException("验证码不正确");
        }

        if (StringUtils.isEmpty(registerDto.getPassword())) {
            throw new StatusFailException("密码不能为空");
        }

        if (registerDto.getPassword().length() < 6 || registerDto.getPassword().length() > 20) {
            throw new StatusFailException("密码长度应该为6~20位！");
        }

        if (StringUtils.isEmpty(registerDto.getUsername())) {
            throw new StatusFailException("用户名不能为空");
        }

        if (registerDto.getUsername().length() > 20) {
            throw new StatusFailException("用户名长度不能超过20位!");
        }

        String uuid = IdUtil.simpleUUID();
        //为新用户设置uuid
        registerDto.setUuid(uuid);
        registerDto.setPassword(SecureUtil.md5(registerDto.getPassword().trim())); // 将密码MD5加密写入数据库
        registerDto.setUsername(registerDto.getUsername().trim());
        registerDto.setEmail(registerDto.getEmail().trim());

        //往user_info表插入数据
        boolean addUser = userInfoEntityService.addUser(registerDto);

        //往user_role表插入数据
        boolean addUserRole = userRoleEntityService.save(new UserRole().setRoleId(1002L).setUid(uuid));

        //往user_record表插入数据
        boolean addUserRecord = userRecordEntityService.save(new UserRecord().setUid(uuid));

        if (addUser && addUserRole && addUserRecord) {
            redisUtils.del(registerDto.getEmail());
            noticeManager.syncNoticeToNewRegisterUser(uuid);
        } else {
            throw new StatusFailException("注册失败，请稍后重新尝试！");
        }
    }


    public void applyResetPassword(ApplyResetPasswordDTO applyResetPasswordDto) throws StatusFailException {

        String captcha = applyResetPasswordDto.getCaptcha();
        String captchaKey = applyResetPasswordDto.getCaptchaKey();
        String email = applyResetPasswordDto.getEmail();

        if (StringUtils.isEmpty(captcha) || StringUtils.isEmpty(email) || StringUtils.isEmpty(captchaKey)) {
            throw new StatusFailException("邮箱或验证码不能为空");
        }

        if (!emailManager.isOk()) {
            throw new StatusFailException("对不起！本站邮箱系统未配置，暂不支持重置密码！");
        }

        String lockKey = Constants.Email.RESET_EMAIL_LOCK + email;
        if (redisUtils.hasKey(lockKey)) {
            throw new StatusFailException("对不起，您的操作频率过快，请在" + redisUtils.getExpire(lockKey) + "秒后再次发送重置邮件！");
        }

        // 获取redis中的验证码
        String redisCode = (String) redisUtils.get(captchaKey);
        if (!redisCode.equals(captcha.trim().toLowerCase())) {
            throw new StatusFailException("验证码不正确");
        }

        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("email", email.trim());
        UserInfo userInfo = userInfoEntityService.getOne(userInfoQueryWrapper, false);
        if (userInfo == null) {
            throw new StatusFailException("对不起，该邮箱无该用户，请重新检查！");
        }

        String code = IdUtil.simpleUUID().substring(0, 21); // 随机生成20位数字与字母的组合
        redisUtils.set(Constants.Email.RESET_PASSWORD_KEY_PREFIX.getValue() + userInfo.getUsername(), code, 10 * 60);//默认链接有效10分钟
        // 发送邮件
        emailManager.sendResetPassword(userInfo.getUsername(), code, email.trim());
        redisUtils.set(lockKey, 0, 90);
    }


    public void resetPassword(ResetPasswordDTO resetPasswordDto) throws StatusFailException {
        String username = resetPasswordDto.getUsername();
        String password = resetPasswordDto.getPassword();
        String code = resetPasswordDto.getCode();

        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(username) || StringUtils.isEmpty(code)) {
            throw new StatusFailException("用户名、新密码或验证码不能为空");
        }

        if (password.length() < 6 || password.length() > 20) {
            throw new StatusFailException("新密码长度应该为6~20位！");
        }

        String codeKey = Constants.Email.RESET_PASSWORD_KEY_PREFIX.getValue() + username;
        if (!redisUtils.hasKey(codeKey)) {
            throw new StatusFailException("重置密码链接不存在或已过期，请重新发送重置邮件");
        }

        if (!redisUtils.get(codeKey).equals(code)) { //验证码判断
            throw new StatusFailException("重置密码的验证码不正确，请重新输入");
        }

        UpdateWrapper<UserInfo> userInfoUpdateWrapper = new UpdateWrapper<>();
        userInfoUpdateWrapper.eq("username", username).set("password", SecureUtil.md5(password));
        boolean isOk = userInfoEntityService.update(userInfoUpdateWrapper);
        if (!isOk) {
            throw new StatusFailException("重置密码失败");
        }
        redisUtils.del(codeKey);
    }
}
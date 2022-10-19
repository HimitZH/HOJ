package top.hcode.hoj.manager.admin.account;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.hcode.hoj.common.exception.StatusAccessDeniedException;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.dao.user.SessionEntityService;
import top.hcode.hoj.dao.user.UserRoleEntityService;
import top.hcode.hoj.pojo.dto.LoginDTO;
import top.hcode.hoj.pojo.entity.user.Role;
import top.hcode.hoj.pojo.entity.user.Session;
import top.hcode.hoj.pojo.vo.UserInfoVO;
import top.hcode.hoj.pojo.vo.UserRolesVO;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.IpUtils;
import top.hcode.hoj.utils.JwtUtils;
import top.hcode.hoj.utils.RedisUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 10:32
 * @Description:
 */
@Component
public class AdminAccountManager {

    @Autowired
    private SessionEntityService sessionEntityService;

    @Autowired
    private JwtUtils jwtUtils;

    @Resource
    private RedisUtils redisUtils;

    @Autowired
    private UserRoleEntityService userRoleEntityService;

    public UserInfoVO login(LoginDTO loginDto) throws StatusFailException, StatusAccessDeniedException {

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        HttpServletResponse response = servletRequestAttributes.getResponse();

        String userIpAddr = IpUtils.getUserIpAddr(request);
        String key = Constants.Account.TRY_LOGIN_NUM.getCode() + loginDto.getUsername() + "_" + userIpAddr;
        Integer tryLoginCount = (Integer) redisUtils.get(key);

        if (tryLoginCount != null && tryLoginCount >= 20) {
            throw new StatusFailException("对不起！登录失败次数过多！您的账号有风险，半个小时内暂时无法登录！");
        }

        UserRolesVO userRolesVo = userRoleEntityService.getUserRoles(null, loginDto.getUsername());

        if (userRolesVo == null) {
            throw new StatusFailException("用户名或密码错误");
        }

        if (!userRolesVo.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))) {
            if (tryLoginCount == null) {
                redisUtils.set(key, 1, 60 * 30); // 三十分钟不尝试，该限制会自动清空消失
            } else {
                redisUtils.set(key, tryLoginCount + 1, 60 * 30);
            }
            throw new StatusFailException("用户名或密码错误");
        }

        if (userRolesVo.getStatus() != 0) {
            throw new StatusFailException("该账户已被封禁，请联系管理员进行处理！");
        }

        // 认证成功，清除锁定限制
        if (tryLoginCount != null) {
            redisUtils.del(key);
        }

        // 查询用户角色
        List<String> rolesList = new LinkedList<>();
        userRolesVo.getRoles().stream()
                .forEach(role -> rolesList.add(role.getRole()));


        if (rolesList.contains("admin") || rolesList.contains("root") || rolesList.contains("problem_admin")) { // 超级管理员或管理员、题目管理员
            String jwt = jwtUtils.generateToken(userRolesVo.getUid());

            response.setHeader("Authorization", jwt); //放到信息头部
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
            // 会话记录
            sessionEntityService.save(new Session().setUid(userRolesVo.getUid())
                    .setIp(IpUtils.getUserIpAddr(request)).setUserAgent(request.getHeader("User-Agent")));
            // 异步检查是否异地登录
            sessionEntityService.checkRemoteLogin(userRolesVo.getUid());

            UserInfoVO userInfoVo = new UserInfoVO();
            BeanUtil.copyProperties(userRolesVo, userInfoVo, "roles");
            userInfoVo.setRoleList(userRolesVo.getRoles()
                    .stream()
                    .map(Role::getRole)
                    .collect(Collectors.toList()));

            return userInfoVo;
        } else {
            throw new StatusAccessDeniedException("该账号并非管理员账号，无权登录！");
        }
    }

    public void logout(){
        SecurityUtils.getSubject().logout();
    }
}
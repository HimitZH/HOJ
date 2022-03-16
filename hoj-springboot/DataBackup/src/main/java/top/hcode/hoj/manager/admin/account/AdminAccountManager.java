package top.hcode.hoj.manager.admin.account;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.hcode.hoj.common.exception.StatusAccessDeniedException;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.pojo.dto.LoginDto;
import top.hcode.hoj.pojo.entity.user.Role;
import top.hcode.hoj.pojo.entity.user.Session;
import top.hcode.hoj.pojo.vo.UserInfoVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.dao.user.SessionEntityService;
import top.hcode.hoj.dao.user.UserRoleEntityService;
import top.hcode.hoj.utils.IpUtils;
import top.hcode.hoj.utils.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private UserRoleEntityService userRoleEntityService;

    public UserInfoVo login(LoginDto loginDto) throws StatusFailException, StatusAccessDeniedException {

        UserRolesVo userRolesVo = userRoleEntityService.getUserRoles(null, loginDto.getUsername());

        if (userRolesVo == null) {
            throw new StatusFailException("用户名不存在");
        }

        if (!userRolesVo.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))) {
            throw new StatusFailException("密码不正确");
        }

        if (userRolesVo.getStatus() != 0) {
            throw new StatusFailException("该账户已被封禁，请联系管理员进行处理！");
        }

        // 查询用户角色
        List<String> rolesList = new LinkedList<>();
        userRolesVo.getRoles().stream()
                .forEach(role -> rolesList.add(role.getRole()));


        if (rolesList.contains("admin") || rolesList.contains("root") || rolesList.contains("problem_admin")) { // 超级管理员或管理员、题目管理员
            String jwt = jwtUtils.generateToken(userRolesVo.getUid());

            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = servletRequestAttributes.getRequest();
            HttpServletResponse response = servletRequestAttributes.getResponse();

            response.setHeader("Authorization", jwt); //放到信息头部
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
            // 会话记录
            sessionEntityService.save(new Session().setUid(userRolesVo.getUid())
                    .setIp(IpUtils.getUserIpAddr(request)).setUserAgent(request.getHeader("User-Agent")));
            // 异步检查是否异地登录
            sessionEntityService.checkRemoteLogin(userRolesVo.getUid());

            UserInfoVo userInfoVo = new UserInfoVo();
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
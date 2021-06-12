package top.hcode.hoj.shiro;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import top.hcode.hoj.dao.RoleAuthMapper;
import top.hcode.hoj.dao.RoleMapper;
import top.hcode.hoj.dao.UserRoleMapper;
import top.hcode.hoj.pojo.entity.Auth;
import top.hcode.hoj.pojo.entity.Role;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.pojo.entity.UserRole;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.impl.UserInfoServiceImpl;
import top.hcode.hoj.utils.JwtUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/7/19 22:57
 * @Description:
 */
@Slf4j
@Component
public class AccountRealm extends AuthorizingRealm {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRoleMapper userRoleDao;
    @Autowired
    private RoleAuthMapper roleAuthDao;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        AccountProfile user = (AccountProfile) principals.getPrimaryPrincipal();
        //角色权限列表
        List<String> permissionsNameList = new LinkedList<>();
        //用户角色列表
        List<String> roleNameList = new LinkedList<>();
        //获取该用户角色所有的权限
        List<Role> roles = userRoleDao.getRolesByUid(user.getUid());
        // 角色变动，同时需要修改会话里面的数据
        Session session = SecurityUtils.getSubject().getSession();
        UserRolesVo userInfo = (UserRolesVo) session.getAttribute("userInfo");
        userInfo.setRoles(roles);
        session.setAttribute("userInfo",userInfo);
        for (Role role:roles) {
            roleNameList.add(role.getRole());
            for (Auth auth : roleAuthDao.getRoleAuths(role.getId()).getAuths()) {
                permissionsNameList.add(auth.getPermission());
            }
        }
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        authorizationInfo.addRoles(roleNameList);
        //添加权限
        authorizationInfo.addStringPermissions(permissionsNameList);
        return authorizationInfo;
    }
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        JwtToken jwt = (JwtToken) token;
        String userId = jwtUtils.getClaimByToken((String) jwt.getPrincipal()).getSubject();
        UserRolesVo userRoles = userRoleDao.getUserRoles(userId, null);
        if(userRoles == null) {
            throw new UnknownAccountException("账户不存在！");
        }
        if(userRoles.getStatus() == 1) {
            throw new LockedAccountException("该账户已被封禁，请联系管理员进行处理！");
        }
        AccountProfile profile = new AccountProfile();
        BeanUtil.copyProperties(userRoles, profile);
        // 写入会话，后续不必重复查询
        Session session = SecurityUtils.getSubject().getSession();
        session.setAttribute("userInfo", userRoles);
        return new SimpleAuthenticationInfo(profile, jwt.getCredentials(), getName());
    }
}
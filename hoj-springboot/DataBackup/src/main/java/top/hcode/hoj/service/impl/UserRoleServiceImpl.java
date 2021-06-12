package top.hcode.hoj.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.LogoutAware;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.entity.UserRole;
import top.hcode.hoj.dao.UserRoleMapper;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.UserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.shiro.AccountProfile;

import java.util.Collection;
import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    RedisSessionDAO redisSessionDAO;

    @Override
    public IPage<UserRolesVo> getUserList(int limit, int currentPage, String keyword,Boolean onlyAdmin) {
        //新建分页
        Page<UserRolesVo> page = new Page<>(currentPage, limit);
        if (onlyAdmin){
            return userRoleMapper.getAdminUserList(page,limit, currentPage,keyword);
        }else {
            return userRoleMapper.getUserList(page, limit, currentPage, keyword);
        }
    }

    /**
     * @MethodName deleteCache
     * @param uid 当前需要操作的用户id
     * @param isRemoveSession 如果为true则会强行删除该用户session，必须重新登陆，false的话 在访问受限接口时会重新授权
     * @Description TODO
     * @Return
     * @Since 2021/6/12
     */
    @Override
    public void deleteCache(String uid, boolean isRemoveSession){
        //从缓存中获取Session
        Session session = null;
        Collection<Session> sessions = redisSessionDAO.getActiveSessions();
        AccountProfile accountProfile;
        Object attribute = null;
        for(Session sessionInfo : sessions){
            //遍历Session,找到该用户名称对应的Session
            attribute = sessionInfo.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
            if (attribute == null) {
                continue;
            }
            accountProfile = (AccountProfile) ((SimplePrincipalCollection) attribute).getPrimaryPrincipal();
            if (accountProfile == null) {
                continue;
            }
            if (Objects.equals(accountProfile.getUid(), uid)) {
                session=sessionInfo;
                break;
            }
        }
        if (session == null||attribute == null) {
            return;
        }
        //删除session 会强制退出！主要是在禁用用户或角色时，强制用户退出的
        if (isRemoveSession) {
            redisSessionDAO.delete(session);
        }

        //删除Cache，在访问受限接口时会重新授权
        DefaultWebSecurityManager securityManager = (DefaultWebSecurityManager) SecurityUtils.getSecurityManager();
        Authenticator authc = securityManager.getAuthenticator();
        ((LogoutAware) authc).onLogout((SimplePrincipalCollection) attribute);
    }

}

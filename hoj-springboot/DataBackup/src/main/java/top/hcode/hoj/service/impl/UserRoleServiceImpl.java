package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.entity.UserRole;
import top.hcode.hoj.dao.UserRoleMapper;
import top.hcode.hoj.pojo.vo.JudgeVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.UserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

    @Override
    public IPage<UserRolesVo> getUserList(int limit, int currentPage, String keyword) {
        //新建分页
        Page<UserRolesVo> page = new Page<>(currentPage, limit);
        return userRoleMapper.getUserList(page,limit, currentPage,keyword);
    }
}

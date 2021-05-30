package top.hcode.hoj.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.pojo.entity.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.UserRolesVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface UserRoleService extends IService<UserRole> {
    IPage<UserRolesVo> getUserList(int limit, int currentPage, String keyword,Boolean onlyAdmin);
}

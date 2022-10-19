package top.hcode.hoj.dao.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.pojo.entity.user.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.vo.UserRolesVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface UserRoleEntityService extends IService<UserRole> {

    UserRolesVO getUserRoles(String uid, String username);

    IPage<UserRolesVO> getUserList(int limit, int currentPage, String keyword, Boolean onlyAdmin);

    void deleteCache(String uid, boolean isRemoveSession);

    String getAuthChangeContent(int oldType,int newType);
}

package top.hcode.hoj.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.user.Role;
import top.hcode.hoj.pojo.entity.user.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.hcode.hoj.pojo.vo.UserRolesVO;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface UserRoleMapper extends BaseMapper<UserRole> {

    UserRolesVO getUserRoles(@Param("uid") String uid, @Param("username") String username);

    List<Role> getRolesByUid(@Param("uid") String uid);

    IPage<UserRolesVO> getUserList(Page<UserRolesVO> page, @Param("limit") int limit,
                                   @Param("currentPage") int currentPage,
                                   @Param("keyword") String keyword);

    IPage<UserRolesVO> getAdminUserList(Page<UserRolesVO> page, @Param("limit") int limit,
                                        @Param("currentPage") int currentPage,
                                        @Param("keyword") String keyword);
}

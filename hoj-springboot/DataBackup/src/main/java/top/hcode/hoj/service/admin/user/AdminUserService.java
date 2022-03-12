package top.hcode.hoj.service.admin.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.AdminEditUserDto;
import top.hcode.hoj.pojo.vo.UserRolesVo;

import java.util.List;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 21:31
 * @Description:
 */
public interface AdminUserService {

    public CommonResult<IPage<UserRolesVo>> getUserList(Integer limit, Integer currentPage,Boolean onlyAdmin, String keyword);

    public CommonResult<Void> editUser(AdminEditUserDto adminEditUserDto);

    public CommonResult<Void> deleteUser(List<String> deleteUserIdList);

    public CommonResult<Void> insertBatchUser(List<List<String>> users);

    public CommonResult<Map<Object,Object>> generateUser(Map<String, Object> params);

}
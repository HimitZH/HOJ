package top.hcode.hoj.dao.user;

import top.hcode.hoj.pojo.dto.RegisterDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.user.UserInfo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface UserInfoEntityService extends IService<UserInfo> {

    public Boolean addUser(RegisterDTO registerDto);

    public List<String> getSuperAdminUidList();

    public List<String> getProblemAdminUidList();
}

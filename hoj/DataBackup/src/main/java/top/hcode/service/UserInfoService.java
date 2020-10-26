package top.hcode.service;

import top.hcode.common.dto.RegisterDto;
import top.hcode.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
public interface UserInfoService extends IService<UserInfo> {
    public Integer addUser(RegisterDto registerDto);
}

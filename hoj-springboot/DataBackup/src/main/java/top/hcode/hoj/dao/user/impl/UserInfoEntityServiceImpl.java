package top.hcode.hoj.dao.user.impl;

import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.dto.RegisterDto;
import top.hcode.hoj.pojo.entity.user.UserInfo;
import top.hcode.hoj.mapper.UserInfoMapper;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class UserInfoEntityServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoEntityService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public Boolean addUser(RegisterDto registerDto) {
        return userInfoMapper.addUser(registerDto) == 1;
    }

    @Override
    public List<UserInfo> getSuperAdminList() {
        return userInfoMapper.getSuperAdminList();
    }

}

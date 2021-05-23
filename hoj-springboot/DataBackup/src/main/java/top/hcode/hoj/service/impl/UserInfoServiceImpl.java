package top.hcode.hoj.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.dto.RegisterDto;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.dao.UserInfoMapper;
import top.hcode.hoj.service.UserInfoService;
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
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public Integer addUser(RegisterDto registerDto) {
        return userInfoMapper.addUser(registerDto);
    }
}

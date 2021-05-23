package top.hcode.hoj.service.impl;

import top.hcode.hoj.pojo.entity.Auth;
import top.hcode.hoj.dao.AuthMapper;
import top.hcode.hoj.service.AuthService;
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
public class AuthServiceImpl extends ServiceImpl<AuthMapper, Auth> implements AuthService {

}

package top.hcode.hoj.dao.user.impl;

import top.hcode.hoj.pojo.entity.user.Auth;
import top.hcode.hoj.mapper.AuthMapper;
import top.hcode.hoj.dao.user.AuthEntityService;
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
public class AuthEntityServiceImpl extends ServiceImpl<AuthMapper, Auth> implements AuthEntityService {

}

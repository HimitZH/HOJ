package top.hcode.hoj.service.user.impl;

import top.hcode.hoj.pojo.entity.user.Role;
import top.hcode.hoj.dao.RoleMapper;
import top.hcode.hoj.service.user.RoleService;
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
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

}

package top.hcode.hoj.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.hcode.hoj.pojo.entity.user.RoleAuth;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.hcode.hoj.pojo.vo.RoleAuthsVO;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Mapper
@Repository
public interface RoleAuthMapper extends BaseMapper<RoleAuth> {
    RoleAuthsVO getRoleAuths(@Param("rid") long rid);
}

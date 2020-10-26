package top.hcode.dao;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Repository;
import top.hcode.common.dto.RegisterDto;
import top.hcode.entity.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;


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
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    int addUser(RegisterDto registerDto);
}

package top.hcode;

import cn.hutool.core.util.IdUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.hcode.hoj.pojo.dto.RegisterDto;
import top.hcode.hoj.dao.UserInfoMapper;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/24 17:24
 * @Description:
 */
@SpringBootTest
public class DataBackupApplicationTests {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Test
    public void Test1(){
        int test = userInfoMapper.addUser(new RegisterDto().setUuid(IdUtil.simpleUUID()).setEmail("372347736@qq.com").setNickname("test")
                .setUsername("111").setPassword("123456").setNumber("2018030801054"));
        System.out.println(test);
    }
}
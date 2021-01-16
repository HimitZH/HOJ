package top.hcode.hoj;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.cj.protocol.PacketReceivedTimeHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.*;
import top.hcode.hoj.pojo.entity.Contest;
import top.hcode.hoj.pojo.entity.Role;
import top.hcode.hoj.pojo.entity.Session;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.pojo.vo.AnnouncementVo;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.pojo.vo.RoleAuthsVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.UserInfoService;
import top.hcode.hoj.service.UserRoleService;
import top.hcode.hoj.service.impl.AnnouncementServiceImpl;
import top.hcode.hoj.service.impl.UserInfoServiceImpl;
import top.hcode.hoj.service.impl.UserRoleServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.IpUtils;
import top.hcode.hoj.utils.JsoupUtils;
import top.hcode.hoj.utils.RedisUtils;

import java.io.IOException;
import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/24 17:24
 * @Description:
 */
@SpringBootTest
public class DataBackupApplicationTests {

    @Autowired
    private JudgeMapper judgeMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserRoleServiceImpl userRoleService;

    @Autowired
    private RoleAuthMapper roleAuthMapper;

    @Autowired
    private ContestMapper contestMapper;

    @Autowired
    private UserInfoServiceImpl userInfoService;


    @Autowired
    private AnnouncementServiceImpl announcementService;

    @Test
    public void Test1() {
//        UserRolesVo roles = userRoleMapper.getUserRoles("c5ddbe4b38d641bea7d87ae0e102260d",null);
//        System.out.println(roles);
//        IPage<UserRolesVo> admin = userRoleService.getUserList(10, 1, "admin");
//        System.out.println(admin.getPages());
//        System.out.println(admin.getSize());
//        System.out.println(admin.getRecords());
//        List<String> list = new LinkedList<>();
//        list.add("1");
//        list.add("2");
//        boolean b = userInfoService.removeByIds(list);
//        System.out.println(b);
//        UserInfo userInfo = new UserInfo().
//                setUsername("111")
//                .setPassword("1111")
//                .setEmail("11111");
//        boolean b = userInfoService.saveOrUpdate(userInfo);
//        if (b){
//            System.out.println(userInfo);
//        }

//        List<AnnouncementVo> contestAnnouncement = announcementService.getContestAnnouncement(1L);
//        System.out.println(contestAnnouncement.size());
    }

    @Test
    public void Test2() {
        RoleAuthsVo roleAuths = roleAuthMapper.getRoleAuths(1000L);
        System.out.println(roleAuths);
    }

    @Test
    public void Test3() {
        String serviceIp = IpUtils.getServiceIp();
        System.out.println(serviceIp);
    }

    @Test
    public void Test4() {
////        int todayJudgeNum = judgeMapper.getTodayJudgeNum();
//        List<ContestVo> withinNext14DaysContests = contestMapper.getWithinNext14DaysContests();
//        System.out.println(withinNext14DaysContests);

    }

    @Test
    public void Test5() throws IOException {

    }

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Test
    public void Test6() {
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.isNotNull("cf_username");
        List<UserInfo> userInfoList = userInfoMapper.selectList(userInfoQueryWrapper);
        for (UserInfo userInfo : userInfoList) {
            System.out.println(userInfo.getCfUsername());
        }
    }


}
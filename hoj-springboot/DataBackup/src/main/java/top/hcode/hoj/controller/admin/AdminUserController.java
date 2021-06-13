package top.hcode.hoj.controller.admin;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.pojo.entity.UserRecord;
import top.hcode.hoj.pojo.entity.UserRole;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.UserInfoService;
import top.hcode.hoj.service.UserRecordService;
import top.hcode.hoj.service.impl.UserRoleServiceImpl;
import top.hcode.hoj.utils.RedisUtils;
import top.hcode.hoj.utils.ShiroUtils;

import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/6 15:18
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private UserRoleServiceImpl userRoleService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserRecordService userRecordService;

    @Autowired
    private RedisUtils redisUtils;


    @GetMapping("/get-user-list")
    @RequiresAuthentication
    @RequiresPermissions("user_admin")
    public CommonResult getUserList(@RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                    @RequestParam(value = "onlyAdmin", defaultValue = "false") Boolean onlyAdmin,
                                    @RequestParam(value = "keyword", required = false) String keyword) {
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 10;
        if (keyword != null) {
            keyword = keyword.trim();
        }
        IPage<UserRolesVo> userList = userRoleService.getUserList(limit, currentPage, keyword, onlyAdmin);
        if (userList.getTotal() == 0) { // 未查询到一条数据
            return CommonResult.successResponse(userList, "暂无数据");
        } else {
            return CommonResult.successResponse(userList, "获取成功");
        }

    }

    @PutMapping("/edit-user")
    @RequiresPermissions("user_admin")
    @RequiresAuthentication
    @Transactional
    public CommonResult editUser(@RequestBody Map<String, Object> params) {
        String username = (String) params.get("username");
        String uid = (String) params.get("uid");
        String realname = (String) params.get("realname");
        String email = (String) params.get("email");
        String password = (String) params.get("password");
        int type = (int) params.get("type");
        int status = (int) params.get("status");
        boolean setNewPwd = (boolean) params.get("setNewPwd");
        UpdateWrapper<UserInfo> updateWrapper1 = new UpdateWrapper<>();

        updateWrapper1.eq("uuid", uid)
                .set("username", username)
                .set("realname", realname)
                .set("email", email)
                .set(setNewPwd, "password", SecureUtil.md5(password))
                .set("status", status);
        boolean result1 = userInfoService.update(updateWrapper1);

        UpdateWrapper<UserRole> updateWrapper2 = new UpdateWrapper<>();
        updateWrapper2.eq("uid", uid).set("role_id", type);
        boolean result2 = userRoleService.update(updateWrapper2);
        if (result1) {
            // 需要重新登录
            userRoleService.deleteCache(uid, true);
        } else if (result2) {
            // 需要重新授权
            userRoleService.deleteCache(uid, false);
        }

        if (result1 && result2) {
            return CommonResult.successResponse(null, "修改成功！");
        }
        return CommonResult.errorResponse("修改失败！");
    }

    @DeleteMapping("/delete-user")
    @RequiresPermissions("user_admin")
    @RequiresAuthentication
    public CommonResult deleteUser(@RequestBody Map<String, Object> params) {
        List<String> deleteUserIds = (List<String>) params.get("ids");
        boolean result = userInfoService.removeByIds(deleteUserIds);
        if (result) {
            return CommonResult.successResponse(null, "删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！");
        }
    }

    @PostMapping("/insert-batch-user")
    @RequiresPermissions("user_admin")
    @RequiresAuthentication
    @Transactional
    public CommonResult insertBatchUser(@RequestBody Map<String, Object> params) {
        List<List<String>> users = (List<List<String>>) params.get("users");
        List<UserInfo> userInfoList = new LinkedList<>();
        List<UserRole> userRoleList = new LinkedList<>();
        List<UserRecord> userRecordList = new LinkedList<>();
        if (users != null) {
            for (List<String> user : users) {
                String uuid = IdUtil.simpleUUID();
                userInfoList.add(new UserInfo()
                        .setUuid(uuid)
                        .setUsername(user.get(0))
                        .setPassword(SecureUtil.md5(user.get(1)))
                        .setEmail(user.get(2)));
                userRoleList.add(new UserRole()
                        .setRoleId(1002L)
                        .setUid(uuid));
                userRecordList.add(new UserRecord().setUid(uuid));
            }
            boolean result1 = userInfoService.saveBatch(userInfoList);
            boolean result2 = userRoleService.saveBatch(userRoleList);
            boolean result3 = userRecordService.saveBatch(userRecordList);
            if (result1 && result2 && result3) {
                return CommonResult.successResponse(null, "添加成功！");
            } else {
                return CommonResult.errorResponse("删除失败");
            }
        } else {
            return CommonResult.errorResponse("插入的用户数据不能为空！");
        }
    }

    @PostMapping("/generate-user")
    @RequiresPermissions("user_admin")
    @RequiresAuthentication
    @Transactional
    public CommonResult generateUser(@RequestBody Map<String, Object> params) {
        String prefix = (String) params.getOrDefault("prefix", "");
        String suffix = (String) params.getOrDefault("suffix", "");
        int numberFrom = (int) params.getOrDefault("number_from", 1);
        int numberTo = (int) params.getOrDefault("number_to", 10);
        int passwordLength = (int) params.getOrDefault("password_length", 6);

        List<UserInfo> userInfoList = new LinkedList<>();
        List<UserRole> userRoleList = new LinkedList<>();
        List<UserRecord> userRecordList = new LinkedList<>();

        HashMap<String, Object> userInfo = new HashMap<>(); // 存储账号密码放入redis中，等待导出excel
        for (int num = numberFrom; num <= numberTo; num++) {
            String uuid = IdUtil.simpleUUID();
            String password = RandomUtil.randomString(passwordLength);
            String username = prefix + num + suffix;
            userInfoList.add(new UserInfo()
                    .setUuid(uuid)
                    .setUsername(username)
                    .setPassword(SecureUtil.md5(password)));
            userInfo.put(username, password);
            userRoleList.add(new UserRole()
                    .setRoleId(1002L)
                    .setUid(uuid));
            userRecordList.add(new UserRecord().setUid(uuid));
        }
        boolean result1 = userInfoService.saveBatch(userInfoList);
        boolean result2 = userRoleService.saveBatch(userRoleList);
        boolean result3 = userRecordService.saveBatch(userRecordList);
        if (result1 && result2 && result3) {
            String key = IdUtil.simpleUUID();
            redisUtils.hmset(key, userInfo, 1800); // 存储半小时
            return CommonResult.successResponse(MapUtil.builder()
                    .put("key", key).map(), "生成指定用户成功！");
        } else {
            return CommonResult.errorResponse("生成指定用户失败！");
        }
    }

}
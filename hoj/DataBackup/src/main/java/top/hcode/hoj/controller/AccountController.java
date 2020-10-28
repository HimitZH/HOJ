package top.hcode.hoj.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.pojo.dto.LoginDto;
import top.hcode.hoj.pojo.dto.RegisterDto;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.UserRecordMapper;
import top.hcode.hoj.dao.UserRoleMapper;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.pojo.entity.UserRecord;
import top.hcode.hoj.pojo.entity.UserRole;
import top.hcode.hoj.service.UserInfoService;
import top.hcode.hoj.service.impl.EmailServiceImpl;
import top.hcode.hoj.utils.JwtUtils;
import top.hcode.hoj.utils.RedisUtils;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/23 12:00
 * @Description:账户处理控制类，负责处理登录请求和注册请求
 */
@RestController
@RequestMapping("/api")
public class AccountController {

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private UserInfoService userInfoDao;

    @Autowired
    private UserRoleMapper userRoleDao;

    @Autowired
    private UserRecordMapper userRecordDao;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * @MethodName getRegisterCode
     * @Params * @param null
     * @Description 调用邮件服务，发送注册流程的6位随机验证码
     * @Return
     * @Since 2020/10/26
     */
    @RequestMapping(value = "/get-register-code",method = RequestMethod.GET)
    public CommonResult getRegisterCode(@RequestParam(value = "username",required = true) String username,
                                        @RequestParam(value = "email",required = true) String email) throws MessagingException {
        String numbers = RandomUtil.randomNumbers(6); // 随机生成6位数字的组合
        redisUtils.set(username, numbers, 5 * 60);//默认验证码有效5分钟
        emailService.sendCode(email, username, numbers);
        return CommonResult.successResponse(MapUtil.builder()
                .put("username", username)
                .put("email", email)
                .put("expire", 5 * 60)
                .map(), "验证码已发送至指定邮箱");
    }

    /**
     * @MethodName register
     * @Params * @param RegisterDto
     * @Description 注册逻辑，具体参数请看RegisterDto类
     * @Return
     * @Since 2020/10/24
     */
    @PostMapping("/register")
    @Transactional
    public CommonResult register(@Validated @RequestBody RegisterDto registerDto) {

        if (!redisUtils.hasKey(registerDto.getUsername())) {
            return CommonResult.errorResponse("验证码不存在或已过期");
        }
        if (!redisUtils.get(registerDto.getUsername()).equals(registerDto.getCode())) { //验证码判断
            return CommonResult.errorResponse("验证码不正确");
        }
        String uuid = IdUtil.simpleUUID();
        //为新用户设置uuid
        registerDto.setUuid(uuid);
        //往user_info表插入数据
        int result1 = userInfoDao.addUser(registerDto);
        //往user_role表插入数据
        int result2 = userRoleDao.insert(new UserRole().setRoleId(1002L).setUid(uuid));
        //往user_record表插入数据
        int result3 = userRecordDao.insert(new UserRecord().setUid(uuid));
        if (result1 == 1 && result2 == 1 && result3 == 1) {
            return CommonResult.successResponse(null, "注册成功！");
        } else {
            return CommonResult.errorResponse("注册失败！", CommonResult.STATUS_ERROR); // 插入数据库失败，返回500
        }
    }


    /**
     * @MethodName login
     * @Params * @param LoginDto
     * @Description 处理登录逻辑
     * @Return CommonResult
     * @Since 2020/10/24
     */
    @PostMapping("/login")
    public CommonResult login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<UserInfo>().eq("username", loginDto.getUsername());
        UserInfo user = userInfoDao.getOne(wrapper);
        Assert.notNull(user, "用户不存在");
        if (user.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))) {
            return CommonResult.errorResponse("密码不正确");
        }
        if (user.getStatus() != 0) {
            return CommonResult.errorResponse("该账户已被封禁，请联系管理员进行处理。");
        }
        String jwt = jwtUtils.generateToken(user.getUuid());
        response.setHeader("Authorization", jwt); //放到信息头部
        response.setHeader("Access-Control-Expose-Headers", "Authorization");

        return CommonResult.successResponse(MapUtil.builder()
                .put("uid", user.getUuid())
                .put("username", user.getUsername())
                .put("nickname", user.getNickname())
                .put("avatar", user.getAvatar())
                .put("email", user.getEmail())
                .map(), "登录成功"
        );
    }


    /**
     * @MethodName logout
     * @Params * @param null
     * @Description 退出逻辑，将jwt在redis中清除，下次需要再次登录。
     * @Return CommonResult
     * @Since 2020/10/24
     */
    @GetMapping("/logout")
    @RequiresAuthentication
    public CommonResult logout() {
        SecurityUtils.getSubject().logout();
        return CommonResult.successResponse(null, "退出成功");
    }
}
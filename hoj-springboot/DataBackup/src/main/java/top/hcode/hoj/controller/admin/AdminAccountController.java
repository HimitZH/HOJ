package top.hcode.hoj.controller.admin;

import cn.hutool.core.map.MapUtil;
import cn.hutool.crypto.SecureUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.*;
import top.hcode.hoj.pojo.dto.LoginDto;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.utils.IpUtils;
import top.hcode.hoj.utils.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;


/**
 * @Author: Himit_ZH
 * @Date: 2020/12/2 21:23
 * @Description:
 */
@RestController
@RequestMapping("/admin")
public class AdminAccountController {

    @Autowired
    private UserRoleMapper userRoleDao;

    @Autowired
    private SessionMapper sessionDao;

    @Autowired
    private JwtUtils jwtUtils;


    @PostMapping("/login")
    public CommonResult login(@Validated @RequestBody LoginDto loginDto, HttpServletResponse response,HttpServletRequest request){
        UserRolesVo userRoles = userRoleDao.getUserRoles(null, loginDto.getUsername());

        Assert.notNull(userRoles, "用户名不存在");
        if (!userRoles.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))) {
            return CommonResult.errorResponse("密码不正确");
        }
        if (userRoles.getStatus() != 0) {
            return CommonResult.errorResponse("该账户已被封禁，请联系管理员进行处理！");
        }
        // 查询用户角色
        List<String> rolesList = new LinkedList<>();
        userRoles.getRoles().stream()
                .forEach(role -> rolesList.add(role.getRole()));


        if (rolesList.contains("admin") || rolesList.contains("root")){ // 超级管理员或管理员
            String jwt = jwtUtils.generateToken(userRoles.getUid());
            response.setHeader("Authorization", jwt); //放到信息头部
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
            // 会话记录
            sessionDao.insert(new Session().setUid(userRoles.getUid())
                    .setIp(IpUtils.getUserIpAddr(request)).setUserAgent(request.getHeader("User-Agent")));
            return CommonResult.successResponse(MapUtil.builder()
                    .put("uid", userRoles.getUid())
                    .put("username", userRoles.getUsername())
                    .put("nickname", userRoles.getNickname())
                    .put("avatar", userRoles.getAvatar())
                    .put("email", userRoles.getEmail())
                    .put("number",userRoles.getNumber())
                    .put("school",userRoles.getSchool())
                    .put("course",userRoles.getCourse())
                    .put("signature",userRoles.getSignature())
                    .put("realname", userRoles.getRealname())
                    .put("roleList", rolesList)
                    .map(), "登录成功！"
            );
        }else{
            return CommonResult.errorResponse("该账号并非管理员账号，无权登录！",CommonResult.STATUS_ACCESS_DENIED);
        }
    }

    @GetMapping("/logout")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin"},logical = Logical.OR)
    public CommonResult logout() {
        SecurityUtils.getSubject().logout();
        return CommonResult.successResponse(null, "登出成功！");
    }


}
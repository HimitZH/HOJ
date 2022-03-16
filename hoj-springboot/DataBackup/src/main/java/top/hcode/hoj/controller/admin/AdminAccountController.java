package top.hcode.hoj.controller.admin;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;

import top.hcode.hoj.pojo.dto.LoginDto;

import top.hcode.hoj.pojo.vo.UserInfoVo;
import top.hcode.hoj.service.admin.account.AdminAccountService;



/**
 * @Author: Himit_ZH
 * @Date: 2020/12/2 21:23
 * @Description:
 */
@RestController
@RequestMapping("/api/admin")
public class AdminAccountController {

    @Autowired
    private AdminAccountService adminAccountService;

    @PostMapping("/login")
    public CommonResult<UserInfoVo> login(@Validated @RequestBody LoginDto loginDto){
       return adminAccountService.login(loginDto);
    }

    @GetMapping("/logout")
    @RequiresAuthentication
    @RequiresRoles(value = {"root","admin","problem_admin"},logical = Logical.OR)
    public CommonResult<Void> logout() {
        return adminAccountService.logout();
    }

}
package top.hcode.hoj.service.oj;


import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ApplyResetPasswordDto;
import top.hcode.hoj.pojo.dto.LoginDto;
import top.hcode.hoj.pojo.dto.RegisterDto;
import top.hcode.hoj.pojo.dto.ResetPasswordDto;
import top.hcode.hoj.pojo.vo.RegisterCodeVo;
import top.hcode.hoj.pojo.vo.UserInfoVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 17:39
 * @Description:
 */
public interface PassportService {

    public CommonResult<UserInfoVo> login(LoginDto loginDto, HttpServletResponse response, HttpServletRequest request);

    public CommonResult<RegisterCodeVo> getRegisterCode(String email);

    public CommonResult<Void> register(RegisterDto registerDto);

    public CommonResult<Void> applyResetPassword(ApplyResetPasswordDto applyResetPasswordDto);

    public CommonResult<Void> resetPassword(ResetPasswordDto resetPasswordDto);
}
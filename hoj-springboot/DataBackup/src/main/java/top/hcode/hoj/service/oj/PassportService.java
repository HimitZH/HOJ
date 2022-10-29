package top.hcode.hoj.service.oj;


import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ApplyResetPasswordDTO;
import top.hcode.hoj.pojo.dto.LoginDTO;
import top.hcode.hoj.pojo.dto.RegisterDTO;
import top.hcode.hoj.pojo.dto.ResetPasswordDTO;
import top.hcode.hoj.pojo.vo.RegisterCodeVO;
import top.hcode.hoj.pojo.vo.UserInfoVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 17:39
 * @Description:
 */
public interface PassportService {

    public CommonResult<UserInfoVO> login(LoginDTO loginDto, HttpServletResponse response, HttpServletRequest request);

    public CommonResult<RegisterCodeVO> getRegisterCode(String email);

    public CommonResult<Void> register(RegisterDTO registerDto);

    public CommonResult<Void> applyResetPassword(ApplyResetPasswordDTO applyResetPasswordDto);

    public CommonResult<Void> resetPassword(ResetPasswordDTO resetPasswordDto);

    public CommonResult<Void> logout();
}
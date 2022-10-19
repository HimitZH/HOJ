package top.hcode.hoj.service.oj.impl;

import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusAccessDeniedException;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusForbiddenException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.PassportManager;
import top.hcode.hoj.pojo.dto.ApplyResetPasswordDTO;
import top.hcode.hoj.pojo.dto.LoginDTO;
import top.hcode.hoj.pojo.dto.RegisterDTO;
import top.hcode.hoj.pojo.dto.ResetPasswordDTO;
import top.hcode.hoj.pojo.vo.RegisterCodeVO;
import top.hcode.hoj.pojo.vo.UserInfoVO;
import top.hcode.hoj.service.oj.PassportService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 17:40
 * @Description:
 */
@Service
public class PassportServiceImpl implements PassportService {

    @Resource
    private PassportManager passportManager;

    @Override
    public CommonResult<UserInfoVO> login(LoginDTO loginDto, HttpServletResponse response, HttpServletRequest request) {
        try {
            return CommonResult.successResponse(passportManager.login(loginDto, response, request));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<RegisterCodeVO> getRegisterCode(String email) {
        try {
            return CommonResult.successResponse(passportManager.getRegisterCode(email));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusAccessDeniedException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.ACCESS_DENIED);
        } catch (StatusForbiddenException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.FORBIDDEN);
        }
    }

    @Override
    public CommonResult<Void> register(RegisterDTO registerDto) {
        try {
            passportManager.register(registerDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusAccessDeniedException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.ACCESS_DENIED);
        }
    }

    @Override
    public CommonResult<Void> applyResetPassword(ApplyResetPasswordDTO applyResetPasswordDto) {
        try {
            passportManager.applyResetPassword(applyResetPasswordDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> resetPassword(ResetPasswordDTO resetPasswordDto) {
        try {
            passportManager.resetPassword(resetPasswordDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
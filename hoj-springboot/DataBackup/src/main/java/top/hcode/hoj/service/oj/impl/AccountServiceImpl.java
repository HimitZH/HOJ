package top.hcode.hoj.service.oj.impl;

import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.AccountManager;
import top.hcode.hoj.pojo.dto.ChangeEmailDto;
import top.hcode.hoj.pojo.dto.ChangePasswordDto;
import top.hcode.hoj.pojo.dto.CheckUsernameOrEmailDto;
import top.hcode.hoj.pojo.vo.ChangeAccountVo;
import top.hcode.hoj.pojo.vo.CheckUsernameOrEmailVo;
import top.hcode.hoj.pojo.vo.UserHomeVo;
import top.hcode.hoj.pojo.vo.UserInfoVo;
import top.hcode.hoj.service.oj.AccountService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 19:35
 * @Description:
 */
@Service
public class AccountServiceImpl implements AccountService {

    @Resource
    private AccountManager accountManager;

    @Override
    public CommonResult<CheckUsernameOrEmailVo> checkUsernameOrEmail(CheckUsernameOrEmailDto checkUsernameOrEmailDto) {
        return CommonResult.successResponse(accountManager.checkUsernameOrEmail(checkUsernameOrEmailDto));
    }

    @Override
    public CommonResult<UserHomeVo> getUserHomeInfo(String uid, String username) {
        try {
            return CommonResult.successResponse(accountManager.getUserHomeInfo(uid, username));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<ChangeAccountVo> changePassword(ChangePasswordDto changePasswordDto) {
        try {
            return CommonResult.successResponse(accountManager.changePassword(changePasswordDto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusSystemErrorException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.SYSTEM_ERROR);
        }
    }

    @Override
    public CommonResult<ChangeAccountVo> changeEmail(ChangeEmailDto changeEmailDto) {
        try {
            return CommonResult.successResponse(accountManager.changeEmail(changeEmailDto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusSystemErrorException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.SYSTEM_ERROR);
        }
    }

    @Override
    public CommonResult<UserInfoVo> changeUserInfo(UserInfoVo userInfoVo) {
        try {
            return CommonResult.successResponse(accountManager.changeUserInfo(userInfoVo));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
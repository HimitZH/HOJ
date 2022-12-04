package top.hcode.hoj.service.oj.impl;

import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.exception.StatusSystemErrorException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.oj.AccountManager;
import top.hcode.hoj.pojo.dto.ChangeEmailDTO;
import top.hcode.hoj.pojo.dto.ChangePasswordDTO;
import top.hcode.hoj.pojo.dto.CheckUsernameOrEmailDTO;
import top.hcode.hoj.pojo.vo.*;
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
    public CommonResult<CheckUsernameOrEmailVO> checkUsernameOrEmail(CheckUsernameOrEmailDTO checkUsernameOrEmailDto) {
        return CommonResult.successResponse(accountManager.checkUsernameOrEmail(checkUsernameOrEmailDto));
    }

    @Override
    public CommonResult<UserHomeVO> getUserHomeInfo(String uid, String username) {
        try {
            return CommonResult.successResponse(accountManager.getUserHomeInfo(uid, username));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<UserCalendarHeatmapVO> getUserCalendarHeatmap(String uid, String username) {
        try {
            return CommonResult.successResponse(accountManager.getUserCalendarHeatmap(uid, username));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<ChangeAccountVO> changePassword(ChangePasswordDTO changePasswordDto) {
        try {
            return CommonResult.successResponse(accountManager.changePassword(changePasswordDto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusSystemErrorException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.SYSTEM_ERROR);
        }
    }

    @Override
    public CommonResult<Void> getChangeEmailCode(String email) {
        try {
            accountManager.getChangeEmailCode(email);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<ChangeAccountVO> changeEmail(ChangeEmailDTO changeEmailDto) {
        try {
            return CommonResult.successResponse(accountManager.changeEmail(changeEmailDto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusSystemErrorException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.SYSTEM_ERROR);
        }
    }

    @Override
    public CommonResult<UserInfoVO> changeUserInfo(UserInfoVO userInfoVo) {
        try {
            return CommonResult.successResponse(accountManager.changeUserInfo(userInfoVo));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<UserAuthInfoVO> getUserAuthInfo() {
        return CommonResult.successResponse(accountManager.getUserAuthInfo());
    }
}
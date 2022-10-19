package top.hcode.hoj.service.admin.account.impl;

import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusAccessDeniedException;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.manager.admin.account.AdminAccountManager;
import top.hcode.hoj.pojo.vo.UserInfoVO;
import top.hcode.hoj.service.admin.account.AdminAccountService;
import top.hcode.hoj.pojo.dto.LoginDTO;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 10:22
 * @Description:
 */

@Service
public class AdminAccountServiceImpl implements AdminAccountService {

    @Resource
    private AdminAccountManager adminAccountManager;

    @Override
    public CommonResult<UserInfoVO> login(LoginDTO loginDto) {
        try {
            return CommonResult.successResponse(adminAccountManager.login(loginDto));
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        } catch (StatusAccessDeniedException e) {
            return CommonResult.errorResponse(e.getMessage(), ResultStatus.ACCESS_DENIED);
        }
    }

    @Override
    public CommonResult<Void> logout() {
        adminAccountManager.logout();
        return CommonResult.successResponse("退出登录成功！");
    }
}
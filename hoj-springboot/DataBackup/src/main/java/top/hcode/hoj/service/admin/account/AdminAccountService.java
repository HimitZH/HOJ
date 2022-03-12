package top.hcode.hoj.service.admin.account;


import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.LoginDto;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 10:10
 * @Description:
 */
public interface AdminAccountService {

    public CommonResult<Void> login(LoginDto loginDto);

    public CommonResult<Void> logout();
}
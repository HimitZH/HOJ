package top.hcode.hoj.service.oj;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ChangeEmailDto;
import top.hcode.hoj.pojo.dto.ChangePasswordDto;
import top.hcode.hoj.pojo.dto.CheckUsernameOrEmailDto;
import top.hcode.hoj.pojo.vo.ChangeAccountVo;
import top.hcode.hoj.pojo.vo.CheckUsernameOrEmailVo;
import top.hcode.hoj.pojo.vo.UserHomeVo;
import top.hcode.hoj.pojo.vo.UserInfoVo;


public interface AccountService {

    public CommonResult<CheckUsernameOrEmailVo> checkUsernameOrEmail(CheckUsernameOrEmailDto checkUsernameOrEmailDto);

    public CommonResult<UserHomeVo> getUserHomeInfo(String uid, String username);

    public CommonResult<ChangeAccountVo> changePassword(ChangePasswordDto changePasswordDto);

    public CommonResult<ChangeAccountVo> changeEmail(ChangeEmailDto changeEmailDto);

    public CommonResult<UserInfoVo> changeUserInfo(UserInfoVo userInfoVo);

}

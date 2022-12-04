package top.hcode.hoj.service.oj;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ChangeEmailDTO;
import top.hcode.hoj.pojo.dto.ChangePasswordDTO;
import top.hcode.hoj.pojo.dto.CheckUsernameOrEmailDTO;
import top.hcode.hoj.pojo.vo.*;


public interface AccountService {

    public CommonResult<CheckUsernameOrEmailVO> checkUsernameOrEmail(CheckUsernameOrEmailDTO checkUsernameOrEmailDto);

    public CommonResult<UserHomeVO> getUserHomeInfo(String uid, String username);

    public CommonResult<UserCalendarHeatmapVO> getUserCalendarHeatmap(String uid, String username);

    public CommonResult<ChangeAccountVO> changePassword(ChangePasswordDTO changePasswordDto);

    public CommonResult<Void> getChangeEmailCode(String email);

    public CommonResult<ChangeAccountVO> changeEmail(ChangeEmailDTO changeEmailDto);

    public CommonResult<UserInfoVO> changeUserInfo(UserInfoVO userInfoVo);

    public CommonResult<UserAuthInfoVO> getUserAuthInfo();
}

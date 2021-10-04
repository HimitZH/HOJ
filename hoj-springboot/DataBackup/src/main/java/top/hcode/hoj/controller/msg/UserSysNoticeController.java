package top.hcode.hoj.controller.msg;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.impl.UserSysNoticeServiceImpl;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:42
 * @Description: 负责用户的 系统消息模块、我的消息模块
 */
@RestController
@RequestMapping("/api/msg")
public class UserSysNoticeController {

    @Resource
    private UserSysNoticeServiceImpl userSysNoticeService;

    @RequestMapping(value = "/sys", method = RequestMethod.GET)
    @RequiresAuthentication
    public CommonResult getSysNotice(@RequestParam(value = "limit", required = false) Integer limit,
                                     @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                     HttpServletRequest request) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 5;
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        return CommonResult.successResponse(userSysNoticeService.getSysNotice(limit, currentPage, userRolesVo.getUid()));
    }


    @RequestMapping(value = "/mine", method = RequestMethod.GET)
    @RequiresAuthentication
    public CommonResult getMineNotice(@RequestParam(value = "limit", required = false) Integer limit,
                                     @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                     HttpServletRequest request) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 5;
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        return CommonResult.successResponse(userSysNoticeService.getMineNotice(limit, currentPage, userRolesVo.getUid()));
    }
}
package top.hcode.hoj.controller.msg;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.pojo.vo.UserUnreadMsgCountVo;
import top.hcode.hoj.service.msg.impl.MsgRemindServiceImpl;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:40
 * @Description: 获取用户 评论我的、回复我的、收到的赞的消息
 */
@RestController
@RequestMapping("/api/msg")
public class MsgRemindController {

    @Resource
    private MsgRemindServiceImpl msgRemindService;

    /**
     * @MethodName getUnreadMsgCount
     * @Description 获取用户的未读消息数量，包括评论我的、回复我的、收到的赞、系统通知、我的消息
     * @Return
     * @Since 2021/10/1
     */
    @RequestMapping(value = "/unread", method = RequestMethod.GET)
    @RequiresAuthentication
    public CommonResult getUnreadMsgCount(HttpServletRequest request) {
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        UserUnreadMsgCountVo userUnreadMsgCount = msgRemindService.getUserUnreadMsgCount(userRolesVo.getUid());
        if (userUnreadMsgCount == null) {
            userUnreadMsgCount = new UserUnreadMsgCountVo(0, 0, 0, 0, 0);
        }
        return CommonResult.successResponse(userUnreadMsgCount);
    }


    /**
     * @param type    Discuss Reply Like Sys Mine
     * @param request
     * @MethodName cleanMsg
     * @Description 根据type，清空各个消息模块的消息或单个消息
     * @Return
     * @Since 2021/10/3
     */
    @RequestMapping(value = "/clean", method = RequestMethod.DELETE)
    @RequiresAuthentication
    public CommonResult cleanMsg(@RequestParam("type") String type,
                                 @RequestParam(value = "id", required = false) Long id,
                                 HttpServletRequest request) {

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");
        boolean isOk = msgRemindService.cleanMsgByType(type, id, userRolesVo.getUid());
        if (isOk) {
            return CommonResult.successResponse(null, "清空全部成功");
        } else {
            return CommonResult.errorResponse("清空失败");
        }
    }


    /**
     * @param limit
     * @param currentPage
     * @MethodName getCommentMsg
     * @Description 获取评论我的讨论贴的消息，按未读的在前、时间晚的在前进行排序
     * @Return
     * @Since 2021/10/1
     */
    @RequestMapping(value = "/comment", method = RequestMethod.GET)
    @RequiresAuthentication
    public CommonResult getCommentMsg(@RequestParam(value = "limit", required = false) Integer limit,
                                      @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                      HttpServletRequest request) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 5;
        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        return CommonResult.successResponse(msgRemindService.getUserMsgList(userRolesVo.getUid(), "Discuss", limit, currentPage));
    }

    /**
     * @param limit
     * @param currentPage
     * @MethodName getReplyMsg
     * @Description 获取回复我的评论的消息，按未读的在前、时间晚的在前进行排序
     * @Return
     * @Since 2021/10/1
     */
    @RequestMapping(value = "/reply", method = RequestMethod.GET)
    @RequiresAuthentication
    public CommonResult getReplyMsg(@RequestParam(value = "limit", required = false) Integer limit,
                                    @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                    HttpServletRequest request) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 5;

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        return CommonResult.successResponse(msgRemindService.getUserMsgList(userRolesVo.getUid(), "Reply", limit, currentPage));
    }


    /**
     * @param limit
     * @param currentPage
     * @MethodName getLikeMsg
     * @Description 获取点赞我的的消息，按未读的在前、时间晚的在前进行排序
     * @Return
     * @Since 2021/10/1
     */
    @RequestMapping(value = "/like", method = RequestMethod.GET)
    @RequiresAuthentication
    public CommonResult getLikeMsg(@RequestParam(value = "limit", required = false) Integer limit,
                                   @RequestParam(value = "currentPage", required = false) Integer currentPage,
                                   HttpServletRequest request) {

        // 页数，每页题数若为空，设置默认值
        if (currentPage == null || currentPage < 1) currentPage = 1;
        if (limit == null || limit < 1) limit = 5;

        // 获取当前登录的用户
        HttpSession session = request.getSession();
        UserRolesVo userRolesVo = (UserRolesVo) session.getAttribute("userInfo");

        return CommonResult.successResponse(msgRemindService.getUserMsgList(userRolesVo.getUid(), "Like", limit, currentPage));
    }

}
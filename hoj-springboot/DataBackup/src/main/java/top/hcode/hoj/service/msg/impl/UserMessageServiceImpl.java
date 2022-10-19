package top.hcode.hoj.service.msg.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.msg.UserMessageManager;
import top.hcode.hoj.pojo.vo.UserMsgVO;
import top.hcode.hoj.pojo.vo.UserUnreadMsgCountVO;
import top.hcode.hoj.service.msg.UserMessageService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 14:20
 * @Description:
 */
@Service
public class UserMessageServiceImpl implements UserMessageService {

    @Resource
    private UserMessageManager userMessageManager;

    @Override
    public CommonResult<UserUnreadMsgCountVO> getUnreadMsgCount() {
        return CommonResult.successResponse(userMessageManager.getUnreadMsgCount());
    }

    @Override
    public CommonResult<Void> cleanMsg(String type, Long id) {
        try {
            userMessageManager.cleanMsg(type, id);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<IPage<UserMsgVO>> getCommentMsg(Integer limit, Integer currentPage) {
        return CommonResult.successResponse(userMessageManager.getCommentMsg(limit, currentPage));
    }

    @Override
    public CommonResult<IPage<UserMsgVO>> getReplyMsg(Integer limit, Integer currentPage) {
        return CommonResult.successResponse(userMessageManager.getReplyMsg(limit, currentPage));
    }

    @Override
    public CommonResult<IPage<UserMsgVO>> getLikeMsg(Integer limit, Integer currentPage) {
        return CommonResult.successResponse(userMessageManager.getLikeMsg(limit, currentPage));
    }
}
package top.hcode.hoj.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.UserMsgVo;
import top.hcode.hoj.pojo.vo.UserUnreadMsgCountVo;

public interface UserMessageService {

    public CommonResult<UserUnreadMsgCountVo> getUnreadMsgCount();

    public CommonResult<Void> cleanMsg(String type, Long id);

    public CommonResult<IPage<UserMsgVo>> getCommentMsg( Integer limit,Integer currentPage);

    public CommonResult<IPage<UserMsgVo>> getReplyMsg(Integer limit, Integer currentPage);

    public CommonResult<IPage<UserMsgVo>> getLikeMsg(Integer limit, Integer currentPage);

}

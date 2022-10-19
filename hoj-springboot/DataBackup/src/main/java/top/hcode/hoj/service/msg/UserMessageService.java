package top.hcode.hoj.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.UserMsgVO;
import top.hcode.hoj.pojo.vo.UserUnreadMsgCountVO;

public interface UserMessageService {

    public CommonResult<UserUnreadMsgCountVO> getUnreadMsgCount();

    public CommonResult<Void> cleanMsg(String type, Long id);

    public CommonResult<IPage<UserMsgVO>> getCommentMsg(Integer limit, Integer currentPage);

    public CommonResult<IPage<UserMsgVO>> getReplyMsg(Integer limit, Integer currentPage);

    public CommonResult<IPage<UserMsgVO>> getLikeMsg(Integer limit, Integer currentPage);

}

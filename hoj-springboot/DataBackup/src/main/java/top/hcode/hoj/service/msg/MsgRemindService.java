package top.hcode.hoj.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.msg.MsgRemind;
import top.hcode.hoj.pojo.vo.UserMsgVo;
import top.hcode.hoj.pojo.vo.UserUnreadMsgCountVo;


/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:32
 * @Description:
 */
public interface MsgRemindService extends IService<MsgRemind> {
    UserUnreadMsgCountVo getUserUnreadMsgCount(String uid);

    boolean cleanMsgByType(String type, Long id, String uid);

    IPage<UserMsgVo> getUserMsgList(String uid, String action, int limit, int currentPage);
}
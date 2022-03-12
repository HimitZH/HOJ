package top.hcode.hoj.dao.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.msg.MsgRemind;
import top.hcode.hoj.pojo.vo.UserMsgVo;
import top.hcode.hoj.pojo.vo.UserUnreadMsgCountVo;


/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:32
 * @Description:
 */
public interface MsgRemindEntityService extends IService<MsgRemind> {

    UserUnreadMsgCountVo getUserUnreadMsgCount(String uid);

    IPage<UserMsgVo> getUserMsg(Page<UserMsgVo> page, String uid,String action);
}
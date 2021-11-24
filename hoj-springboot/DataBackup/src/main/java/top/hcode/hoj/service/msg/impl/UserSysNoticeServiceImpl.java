package top.hcode.hoj.service.msg.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.UserSysNoticeMapper;
import top.hcode.hoj.pojo.entity.msg.UserSysNotice;
import top.hcode.hoj.pojo.vo.SysMsgVo;
import top.hcode.hoj.service.msg.UserSysNoticeService;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:35
 * @Description:
 */
@Service
public class UserSysNoticeServiceImpl extends ServiceImpl<UserSysNoticeMapper, UserSysNotice> implements UserSysNoticeService {

    @Resource
    private UserSysNoticeMapper userSysNoticeMapper;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public IPage<SysMsgVo> getSysNotice(int limit, int currentPage, String uid) {
        Page<SysMsgVo> page = new Page<>(currentPage, limit);
        IPage<SysMsgVo> sysNotice = userSysNoticeMapper.getSysOrMineNotice(page, uid, "Sys");
        applicationContext.getBean(UserSysNoticeServiceImpl.class).updateSysOrMineMsgRead(sysNotice);
        return sysNotice;
    }

    @Override
    public IPage<SysMsgVo> getMineNotice(int limit, int currentPage, String uid) {
        Page<SysMsgVo> page = new Page<>(currentPage, limit);
        IPage<SysMsgVo> mineNotice = userSysNoticeMapper.getSysOrMineNotice(page, uid, "Mine");
        applicationContext.getBean(UserSysNoticeServiceImpl.class).updateSysOrMineMsgRead(mineNotice);
        return mineNotice;
    }

    @Async
    public void updateSysOrMineMsgRead(IPage<SysMsgVo> userMsgList) {
        List<Long> idList = userMsgList.getRecords().stream()
                .filter(userMsgVo -> !userMsgVo.getState())
                .map(SysMsgVo::getId)
                .collect(Collectors.toList());
        if (idList.size() == 0) {
            return;
        }
        UpdateWrapper<UserSysNotice> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", idList)
                .set("state", true);
        userSysNoticeMapper.update(null, updateWrapper);
    }
}
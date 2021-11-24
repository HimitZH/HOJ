package top.hcode.hoj.service.msg.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.dao.AdminSysNoticeMapper;

import top.hcode.hoj.pojo.entity.msg.AdminSysNotice;
import top.hcode.hoj.pojo.entity.msg.UserSysNotice;
import top.hcode.hoj.pojo.vo.AdminSysNoticeVo;
import top.hcode.hoj.service.msg.AdminSysNoticeService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:34
 * @Description:
 */
@Service
public class AdminSysNoticeServiceImpl extends ServiceImpl<AdminSysNoticeMapper, AdminSysNotice> implements AdminSysNoticeService {

    @Resource
    private AdminSysNoticeMapper adminSysNoticeMapper;

    @Resource
    private UserSysNoticeServiceImpl userSysNoticeService;

    @Override
    public IPage<AdminSysNoticeVo> getSysNotice(int limit, int currentPage, String type) {
        Page<AdminSysNoticeVo> page = new Page<>(currentPage, limit);
        return adminSysNoticeMapper.getAdminSysNotice(page, type);
    }

    @Override
    @Async
    public void syncNoticeToNewRegisterUser(String uid) {
        QueryWrapper<AdminSysNotice> adminSysNoticeQueryWrapper = new QueryWrapper<>();
        adminSysNoticeQueryWrapper
                .eq("type", "All")
                .le("gmt_create", new Date())
                .eq("state", true);
        List<AdminSysNotice> adminSysNotices = adminSysNoticeMapper.selectList(adminSysNoticeQueryWrapper);
        if (adminSysNotices.size() == 0) {
            return;
        }
        List<UserSysNotice> userSysNoticeList = new ArrayList<>();
        for (AdminSysNotice adminSysNotice : adminSysNotices) {
            UserSysNotice userSysNotice = new UserSysNotice();
            userSysNotice.setType("Sys")
                    .setSysNoticeId(adminSysNotice.getId())
                    .setRecipientId(uid);
            userSysNoticeList.add(userSysNotice);
        }
        userSysNoticeService.saveOrUpdateBatch(userSysNoticeList);
    }

    @Override
    @Async
    public void syncNoticeToNewRegisterBatchUser(List<String> uidList) {
        QueryWrapper<AdminSysNotice> adminSysNoticeQueryWrapper = new QueryWrapper<>();
        adminSysNoticeQueryWrapper
                .eq("type", "All")
                .le("gmt_create", new Date())
                .eq("state", true);
        List<AdminSysNotice> adminSysNotices = adminSysNoticeMapper.selectList(adminSysNoticeQueryWrapper);
        if (adminSysNotices.size() == 0) {
            return;
        }
        List<UserSysNotice> userSysNoticeList = new ArrayList<>();
        for (String uid : uidList) {
            for (AdminSysNotice adminSysNotice : adminSysNotices) {
                UserSysNotice userSysNotice = new UserSysNotice();
                userSysNotice.setType("Sys")
                        .setSysNoticeId(adminSysNotice.getId())
                        .setRecipientId(uid);
                userSysNoticeList.add(userSysNotice);
            }
        }
        userSysNoticeService.saveOrUpdateBatch(userSysNoticeList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    public void addSingleNoticeToUser(String adminId, String recipientId, String title, String content, String type) {
        AdminSysNotice adminSysNotice = new AdminSysNotice();
        adminSysNotice.setAdminId(adminId)
                .setType("Single")
                .setTitle(title)
                .setContent(content)
                .setState(true)
                .setRecipientId(recipientId);
        boolean isOk = adminSysNoticeMapper.insert(adminSysNotice) > 0;
        if (isOk) {
            UserSysNotice userSysNotice = new UserSysNotice();
            userSysNotice.setRecipientId(recipientId)
                    .setSysNoticeId(adminSysNotice.getId())
                    .setType(type);
            userSysNoticeService.save(userSysNotice);
        }
    }
}
package top.hcode.hoj.dao.msg.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.mapper.AdminSysNoticeMapper;

import top.hcode.hoj.pojo.entity.msg.AdminSysNotice;
import top.hcode.hoj.pojo.vo.AdminSysNoticeVO;
import top.hcode.hoj.dao.msg.AdminSysNoticeEntityService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2021/10/1 20:34
 * @Description:
 */
@Service
public class AdminSysNoticeEntityServiceImpl extends ServiceImpl<AdminSysNoticeMapper, AdminSysNotice> implements AdminSysNoticeEntityService {

    @Resource
    private AdminSysNoticeMapper adminSysNoticeMapper;

    @Override
    public IPage<AdminSysNoticeVO> getSysNotice(int limit, int currentPage, String type) {
        Page<AdminSysNoticeVO> page = new Page<>(currentPage, limit);
        return adminSysNoticeMapper.getAdminSysNotice(page, type);
    }
}
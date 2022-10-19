package top.hcode.hoj.service.msg.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.msg.AdminNoticeManager;
import top.hcode.hoj.pojo.entity.msg.AdminSysNotice;
import top.hcode.hoj.pojo.vo.AdminSysNoticeVO;
import top.hcode.hoj.service.msg.AdminNoticeService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 11:55
 * @Description:
 */
@Service
public class AdminNoticeServiceImpl implements AdminNoticeService {

    @Resource
    private AdminNoticeManager adminNoticeManager;

    @Override
    public CommonResult<IPage<AdminSysNoticeVO>> getSysNotice(Integer limit, Integer currentPage, String type) {

        return CommonResult.successResponse(adminNoticeManager.getSysNotice(limit, currentPage, type));
    }

    @Override
    public CommonResult<Void> addSysNotice(AdminSysNotice adminSysNotice) {
        try {
            adminNoticeManager.addSysNotice(adminSysNotice);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> deleteSysNotice(Long id) {
        try {
            adminNoticeManager.deleteSysNotice(id);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> updateSysNotice(AdminSysNotice adminSysNotice) {
        try {
            adminNoticeManager.updateSysNotice(adminSysNotice);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
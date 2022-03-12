package top.hcode.hoj.service.msg.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.msg.NoticeManager;
import top.hcode.hoj.pojo.vo.SysMsgVo;
import top.hcode.hoj.service.msg.NoticeService;

import javax.annotation.Resource;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 11:47
 * @Description:
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Resource
    private NoticeManager noticeManager;

    @Override
    public CommonResult<IPage<SysMsgVo>> getSysNotice(Integer limit, Integer currentPage) {
        return CommonResult.successResponse(noticeManager.getSysNotice(limit, currentPage));
    }

    @Override
    public CommonResult<IPage<SysMsgVo>> getMineNotice(Integer limit, Integer currentPage) {
        return CommonResult.successResponse(noticeManager.getMineNotice(limit, currentPage));
    }
}
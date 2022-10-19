package top.hcode.hoj.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.msg.AdminSysNotice;
import top.hcode.hoj.pojo.vo.AdminSysNoticeVO;

public interface AdminNoticeService {

    public CommonResult<IPage<AdminSysNoticeVO>> getSysNotice(Integer limit, Integer currentPage, String type);

    public CommonResult<Void> addSysNotice(AdminSysNotice adminSysNotice);

    public CommonResult<Void> deleteSysNotice(Long id);

    public CommonResult<Void> updateSysNotice(AdminSysNotice adminSysNotice);
}

package top.hcode.hoj.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.SysMsgVo;

public interface NoticeService {

    public CommonResult<IPage<SysMsgVo>> getSysNotice(Integer limit,Integer currentPage);

    public CommonResult<IPage<SysMsgVo>> getMineNotice(Integer limit, Integer currentPage);
}

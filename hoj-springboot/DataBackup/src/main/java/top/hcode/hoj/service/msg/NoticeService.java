package top.hcode.hoj.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.SysMsgVO;

public interface NoticeService {

    public CommonResult<IPage<SysMsgVO>> getSysNotice(Integer limit, Integer currentPage);

    public CommonResult<IPage<SysMsgVO>> getMineNotice(Integer limit, Integer currentPage);
}

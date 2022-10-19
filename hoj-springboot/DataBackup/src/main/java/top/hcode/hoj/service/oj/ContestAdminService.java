package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.CheckACDTO;
import top.hcode.hoj.pojo.entity.contest.ContestPrint;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;

public interface ContestAdminService {

    public CommonResult<IPage<ContestRecord>> getContestACInfo(Long cid, Integer currentPage, Integer limit);

    public CommonResult<Void> checkContestACInfo(CheckACDTO checkACDto);

    public CommonResult<IPage<ContestPrint>> getContestPrint(Long cid, Integer currentPage, Integer limit);

    public CommonResult<Void> checkContestPrintStatus(Long id, Long cid);
}

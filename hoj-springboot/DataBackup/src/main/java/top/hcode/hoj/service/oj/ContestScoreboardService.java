package top.hcode.hoj.service.oj;

import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ContestRankDto;
import top.hcode.hoj.pojo.vo.ContestOutsideInfo;

import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 22:18
 * @Description:
 */
public interface ContestScoreboardService {

    public CommonResult<ContestOutsideInfo> getContestOutsideInfo(Long cid);

    public CommonResult<List> getContestOutsideScoreboard(ContestRankDto contestRankDto);

}
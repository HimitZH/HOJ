package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.ContestRankDTO;
import top.hcode.hoj.pojo.vo.ContestOutsideInfoVO;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 22:18
 * @Description:
 */
public interface ContestScoreboardService {

    public CommonResult<ContestOutsideInfoVO> getContestOutsideInfo(Long cid);

    public CommonResult<IPage> getContestOutsideScoreboard(ContestRankDTO contestRankDto);

}
package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.vo.ACMRankVo;
import top.hcode.hoj.pojo.vo.AnnouncementVo;
import top.hcode.hoj.pojo.vo.ContestVo;
import top.hcode.hoj.pojo.vo.SubmissionStatisticsVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 21:06
 * @Description:
 */
public interface HomeService {

    public CommonResult<List<ContestVo>> getRecentContest();

    public CommonResult<List<HashMap<String, Object>>> getHomeCarousel();

    public CommonResult<List<ACMRankVo>> getRecentSevenACRank();

    @Deprecated
    public CommonResult<List<HashMap<String, Object>>> getRecentOtherContest();

    public CommonResult<IPage<AnnouncementVo>> getCommonAnnouncement(Integer limit, Integer currentPage);

    public CommonResult<Map<Object, Object>> getWebConfig();

    public CommonResult<List<Problem>> getRecentUpdatedProblemList();

    public CommonResult<SubmissionStatisticsVo> getLastWeekSubmissionStatistics(Boolean forceRefresh);
}
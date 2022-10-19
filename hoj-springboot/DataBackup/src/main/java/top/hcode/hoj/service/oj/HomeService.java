package top.hcode.hoj.service.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.vo.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 21:06
 * @Description:
 */
public interface HomeService {

    public CommonResult<List<ContestVO>> getRecentContest();

    public CommonResult<List<HashMap<String, Object>>> getHomeCarousel();

    public CommonResult<List<ACMRankVO>> getRecentSevenACRank();

    @Deprecated
    public CommonResult<List<HashMap<String, Object>>> getRecentOtherContest();

    public CommonResult<IPage<AnnouncementVO>> getCommonAnnouncement(Integer limit, Integer currentPage);

    public CommonResult<Map<Object, Object>> getWebConfig();

    public CommonResult<List<RecentUpdatedProblemVO>> getRecentUpdatedProblemList();

    public CommonResult<SubmissionStatisticsVO> getLastWeekSubmissionStatistics(Boolean forceRefresh);
}
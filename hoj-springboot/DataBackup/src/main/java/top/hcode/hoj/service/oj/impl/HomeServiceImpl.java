package top.hcode.hoj.service.oj.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.oj.HomeManager;
import top.hcode.hoj.pojo.vo.*;
import top.hcode.hoj.service.oj.HomeService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/10 21:08
 * @Description:
 */
@Service
public class HomeServiceImpl implements HomeService {

    @Resource
    private HomeManager homeManager;

    @Override
    public CommonResult<List<ContestVO>> getRecentContest() {
        return CommonResult.successResponse(homeManager.getRecentContest());
    }

    @Override
    public CommonResult<List<HashMap<String, Object>>> getHomeCarousel() {
        return CommonResult.successResponse(homeManager.getHomeCarousel());
    }

    @Override
    public CommonResult<List<ACMRankVO>> getRecentSevenACRank() {
        return CommonResult.successResponse(homeManager.getRecentSevenACRank());
    }

    @Override
    public CommonResult<List<HashMap<String, Object>>> getRecentOtherContest() {
        return CommonResult.successResponse(homeManager.getRecentOtherContest());
    }

    @Override
    public CommonResult<IPage<AnnouncementVO>> getCommonAnnouncement(Integer limit, Integer currentPage) {
        return CommonResult.successResponse(homeManager.getCommonAnnouncement(limit, currentPage));
    }

    @Override
    public CommonResult<Map<Object, Object>> getWebConfig() {
        return CommonResult.successResponse(homeManager.getWebConfig());
    }

    @Override
    public CommonResult<List<RecentUpdatedProblemVO>> getRecentUpdatedProblemList() {
        return CommonResult.successResponse(homeManager.getRecentUpdatedProblemList());
    }

    @Override
    public CommonResult<SubmissionStatisticsVO> getLastWeekSubmissionStatistics(Boolean forceRefresh) {
        return CommonResult.successResponse(homeManager.getLastWeekSubmissionStatistics(forceRefresh));
    }
}
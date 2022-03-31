package top.hcode.hoj.manager.oj;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.vo.ACMContestRankVo;
import top.hcode.hoj.pojo.vo.OIContestRankVo;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/11 20:30
 * @Description:
 */
@Component
public class ContestRankManager {


    @Resource
    private ContestCalculateRankManager contestCalculateRankManager;

    /**
     * @param isOpenSealRank
     * @param removeStar
     * @param currentUserId
     * @param concernedList
     * @param contest
     * @param currentPage
     * @param limit
     * @desc 获取ACM比赛排行榜，有分页
     */
    public IPage<ACMContestRankVo> getContestACMRankPage(Boolean isOpenSealRank,
                                                         Boolean removeStar,
                                                         String currentUserId,
                                                         List<String> concernedList,
                                                         Contest contest,
                                                         int currentPage,
                                                         int limit){

        // 进行排序计算
        List<ACMContestRankVo> orderResultList = contestCalculateRankManager.calcACMRank(isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList);

        // 计算好排行榜，然后进行分页
        Page<ACMContestRankVo> page = new Page<>(currentPage, limit);
        int count = orderResultList.size();
        List<ACMContestRankVo> pageList = new ArrayList<>();
        //计算当前页第一条数据的下标
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            pageList.add(orderResultList.get(currId + i));
        }
        page.setSize(limit);
        page.setCurrent(currentPage);
        page.setTotal(count);
        page.setRecords(pageList);

        return page;
    }


    /**
     * @param isOpenSealRank
     * @param removeStar
     * @param currentUserId
     * @param concernedList
     * @param contest
     * @param currentPage
     * @param limit
     * @desc 获取OI比赛排行榜，有分页
     */
    public IPage<OIContestRankVo> getContestOIRankPage(Boolean isOpenSealRank,
                                                       Boolean removeStar,
                                                       String currentUserId,
                                                       List<String> concernedList,
                                                       Contest contest,
                                                       int currentPage,
                                                       int limit) {

        List<OIContestRankVo> orderResultList = contestCalculateRankManager.calcOIRank(isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList);

        // 计算好排行榜，然后进行分页
        Page<OIContestRankVo> page = new Page<>(currentPage, limit);
        int count = orderResultList.size();
        List<OIContestRankVo> pageList = new ArrayList<>();
        //计算当前页第一条数据的下标
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            pageList.add(orderResultList.get(currId + i));
        }
        page.setSize(limit);
        page.setCurrent(currentPage);
        page.setTotal(count);
        page.setRecords(pageList);
        return page;
    }

    /**
     *
     * @param isOpenSealRank
     * @param removeStar
     * @param contest
     * @param currentUserId
     * @param concernedList
     * @param useCache
     * @param cacheTime
     * @desc  获取ACM比赛排行榜外榜
     */
    public List<ACMContestRankVo> getACMContestScoreboard(Boolean isOpenSealRank,
                                                          Boolean removeStar,
                                                          Contest contest,
                                                          String currentUserId,
                                                          List<String> concernedList,
                                                          Boolean useCache,
                                                          Long cacheTime) {

        return contestCalculateRankManager.calcACMRank(isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList,
                useCache,
                cacheTime);
    }

    /**
     *
     * @param isOpenSealRank
     * @param removeStar
     * @param contest
     * @param currentUserId
     * @param concernedList
     * @param useCache
     * @param cacheTime
     * @desc 获取OI比赛排行榜外榜
     */
    public List<OIContestRankVo> getOIContestScoreboard(Boolean isOpenSealRank,
                                                         Boolean removeStar,
                                                         Contest contest,
                                                         String currentUserId,
                                                         List<String> concernedList,
                                                         Boolean useCache,
                                                         Long cacheTime) {

        return contestCalculateRankManager.calcOIRank(isOpenSealRank,
                removeStar,
                contest,
                currentUserId,
                concernedList,
                useCache,
                cacheTime);
    }

}
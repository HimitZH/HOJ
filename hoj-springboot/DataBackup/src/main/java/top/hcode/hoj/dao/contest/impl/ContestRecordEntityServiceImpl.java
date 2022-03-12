package top.hcode.hoj.dao.contest.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestRecord;
import top.hcode.hoj.mapper.ContestRecordMapper;
import top.hcode.hoj.pojo.vo.ContestRecordVo;
import top.hcode.hoj.dao.contest.ContestRecordEntityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.dao.user.UserInfoEntityService;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class ContestRecordEntityServiceImpl extends ServiceImpl<ContestRecordMapper, ContestRecord> implements ContestRecordEntityService {

    @Autowired
    private ContestRecordMapper contestRecordMapper;

    @Autowired
    private UserInfoEntityService userInfoEntityService;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public IPage<ContestRecord> getACInfo(Integer currentPage, Integer limit, Integer status, Long cid, String contestCreatorId) {

        List<ContestRecord> acInfo = contestRecordMapper.getACInfo(status, cid);

        HashMap<Long, String> pidMapUidAndPid = new HashMap<>(12);
        HashMap<String, Long> UidAndPidMapTime = new HashMap<>(12);

        List<String> superAdminUidList = userInfoEntityService.getSuperAdminUidList();

        List<ContestRecord> userACInfo = new LinkedList<>();

        for (ContestRecord contestRecord : acInfo) {

            if (contestRecord.getUid().equals(contestCreatorId)
                    || superAdminUidList.contains(contestRecord.getUid())) { // 超级管理员和比赛创建者的提交跳过
                continue;
            }

            contestRecord.setFirstBlood(false);
            String uidAndPid = pidMapUidAndPid.get(contestRecord.getPid());
            if (uidAndPid == null) {
                pidMapUidAndPid.put(contestRecord.getPid(), contestRecord.getUid() + contestRecord.getPid());
                UidAndPidMapTime.put(contestRecord.getUid() + contestRecord.getPid(), contestRecord.getTime());
            } else {
                Long firstTime = UidAndPidMapTime.get(uidAndPid);
                Long tmpTime = contestRecord.getTime();
                if (tmpTime < firstTime) {
                    pidMapUidAndPid.put(contestRecord.getPid(), contestRecord.getUid() + contestRecord.getPid());
                    UidAndPidMapTime.put(contestRecord.getUid() + contestRecord.getPid(), tmpTime);
                }
            }
            userACInfo.add(contestRecord);
        }


        List<ContestRecord> pageList = new ArrayList<>();

        int count = userACInfo.size();

        //计算当前页第一条数据的下标
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            ContestRecord contestRecord = userACInfo.get(currId + i);
            if (pidMapUidAndPid.get(contestRecord.getPid()).equals(contestRecord.getUid() + contestRecord.getPid())) {
                contestRecord.setFirstBlood(true);
            }
            pageList.add(contestRecord);
        }


        Page<ContestRecord> page = new Page<>(currentPage, limit);
        page.setSize(limit);
        page.setCurrent(currentPage);
        page.setTotal(count);
        page.setRecords(pageList);

        return page;
    }


    @Override
    public List<ContestRecordVo> getOIContestRecord(Contest contest, Boolean isOpenSealRank) {

        String oiRankScoreType = contest.getOiRankScoreType();
        Long cid = contest.getId();
        String contestAuthor = contest.getAuthor();
        Date sealTime = contest.getSealRankTime();
        Date startTime = contest.getStartTime();
        Date endTime = contest.getEndTime();

        if (!isOpenSealRank) {
            // 封榜解除 获取最新数据
            // 获取每个用户每道题最近一次提交
            if (Objects.equals(Constants.Contest.OI_RANK_RECENT_SCORE.getName(), oiRankScoreType)) {
                return contestRecordMapper.getOIContestRecordByRecentSubmission(cid,
                        contestAuthor,
                        false,
                        sealTime,
                        startTime,
                        endTime);
            } else {
                return contestRecordMapper.getOIContestRecordByHighestSubmission(cid,
                        contestAuthor,
                        false,
                        sealTime,
                        startTime,
                        endTime);
            }

        } else {
            String key = Constants.Contest.OI_CONTEST_RANK_CACHE.getName() + "_" + oiRankScoreType + "_" + cid;
            List<ContestRecordVo> oiContestRecordList = (List<ContestRecordVo>) redisUtils.get(key);
            if (oiContestRecordList == null) {
                if (Objects.equals(Constants.Contest.OI_RANK_RECENT_SCORE.getName(), oiRankScoreType)) {
                    oiContestRecordList = contestRecordMapper.getOIContestRecordByRecentSubmission(cid,
                            contestAuthor,
                            true,
                            sealTime,
                            startTime,
                            endTime);
                } else {
                    oiContestRecordList = contestRecordMapper.getOIContestRecordByHighestSubmission(cid,
                            contestAuthor,
                            true,
                            sealTime,
                            startTime,
                            endTime);
                }
                redisUtils.set(key, oiContestRecordList, 2 * 3600);
            }
            return oiContestRecordList;
        }

    }

    @Override
    public List<ContestRecordVo> getACMContestRecord(String username, Long cid) {
        return contestRecordMapper.getACMContestRecord(username, cid);
    }

}

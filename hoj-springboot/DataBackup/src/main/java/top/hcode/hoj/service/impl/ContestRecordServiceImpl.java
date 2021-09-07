package top.hcode.hoj.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.dao.UserInfoMapper;
import top.hcode.hoj.pojo.entity.Contest;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.pojo.vo.ACMContestRankVo;
import top.hcode.hoj.pojo.entity.ContestRecord;
import top.hcode.hoj.dao.ContestRecordMapper;
import top.hcode.hoj.pojo.vo.OIContestRankVo;
import top.hcode.hoj.service.ContestRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class ContestRecordServiceImpl extends ServiceImpl<ContestRecordMapper, ContestRecord> implements ContestRecordService {


    @Autowired
    private ContestRecordMapper contestRecordMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisUtils redisUtils;


    @Override
    public IPage<ContestRecord> getACInfo(Integer currentPage, Integer limit, Integer status, Long cid, String contestCreatorId) {

        List<ContestRecord> acInfo = contestRecordMapper.getACInfo(status, cid);

        HashMap<Long, String> pidMapUidAndPid = new HashMap<>(12);
        HashMap<String, Long> UidAndPidMapTime = new HashMap<>(12);

        List<UserInfo> superAdminList = getSuperAdminList();
        List<String> superAdminUidList = superAdminList.stream().map(UserInfo::getUuid).collect(Collectors.toList());

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
    public List<UserInfo> getSuperAdminList() {
        return userInfoMapper.getSuperAdminList();
    }


    @Override
    public IPage<ACMContestRankVo> getContestACMRank(Boolean isOpenSealRank, Contest contest,
                                                     List<ContestRecord> contestRecordList,
                                                     int currentPage, int limit) {

        // 进行排序计算
        List<ACMContestRankVo> orderResultList = calcACMRank(isOpenSealRank, contest, contestRecordList);
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


    @Override
    public IPage<OIContestRankVo> getContestOIRank(Long cid, String contestAuthor, Boolean isOpenSealRank, Date sealTime,
                                                   Date startTime, Date endTime, int currentPage, int limit) {

        // 获取每个用户每道题最近一次提交
        if (!isOpenSealRank) {
            // 超级管理员和比赛管理员选择强制刷新 或者 比赛结束
            return getOIContestRank(cid, contestAuthor, false, sealTime, startTime, endTime, currentPage, limit);
        } else {
            String key = Constants.Contest.OI_CONTEST_RANK_CACHE.getName() + "_" + cid;
            Page<OIContestRankVo> page = (Page<OIContestRankVo>) redisUtils.get(key);
            if (page == null) {
                page = getOIContestRank(cid, contestAuthor, true, sealTime, startTime, endTime, currentPage, limit);
                redisUtils.set(key, page, 2 * 3600);
            }
            return page;
        }

    }


    public Page<OIContestRankVo> getOIContestRank(Long cid, String contestAuthor, Boolean isOpenSealRank, Date sealTime,
                                                   Date startTime, Date endTime, int currentPage, int limit) {

        List<ContestRecord> oiContestRecord = contestRecordMapper.getOIContestRecord(cid, contestAuthor, isOpenSealRank, sealTime, startTime, endTime);
        // 计算排名
        List<OIContestRankVo> orderResultList = calcOIRank(oiContestRecord);

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

    @Override
    public List<ContestRecord> getOIContestRecord(Long cid, String contestAuthor, Boolean isOpenSealRank, Date sealTime, Date startTime, Date endTime) {
        return contestRecordMapper.getOIContestRecord(cid, contestAuthor, isOpenSealRank, sealTime, startTime, endTime);
    }


    public List<ACMContestRankVo> calcACMRank(boolean isOpenSealRank, Contest contest, List<ContestRecord> contestRecordList) {

        List<UserInfo> superAdminList = getSuperAdminList();

        List<String> superAdminUidList = superAdminList.stream().map(UserInfo::getUuid).collect(Collectors.toList());

        List<ACMContestRankVo> result = new ArrayList<>();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();

        int index = 0;

        HashMap<String, Long> firstACMap = new HashMap<>();

        for (ContestRecord contestRecord : contestRecordList) {

            if (superAdminUidList.contains(contestRecord.getUid())) { // 超级管理员的提交不入排行榜
                continue;
            }

            ACMContestRankVo ACMContestRankVo;
            if (!uidMapIndex.containsKey(contestRecord.getUid())) { // 如果该用户信息没还记录

                // 初始化参数
                ACMContestRankVo = new ACMContestRankVo();
                ACMContestRankVo.setRealname(contestRecord.getRealname())
                        .setUid(contestRecord.getUid())
                        .setUsername(contestRecord.getUsername())
                        .setAc(0)
                        .setTotalTime(0L)
                        .setTotal(0);

                HashMap<String, HashMap<String, Object>> submissionInfo = new HashMap<>();
                ACMContestRankVo.setSubmissionInfo(submissionInfo);

                result.add(ACMContestRankVo);
                uidMapIndex.put(contestRecord.getUid(), index);
                index++;
            } else {
                ACMContestRankVo = result.get(uidMapIndex.get(contestRecord.getUid())); // 根据记录的index进行获取
            }

            HashMap<String, Object> problemSubmissionInfo = ACMContestRankVo.getSubmissionInfo().getOrDefault(contestRecord.getDisplayId(), new HashMap<>());

            ACMContestRankVo.setTotal(ACMContestRankVo.getTotal() + 1);

            // 如果是当前是开启封榜的时段和同时该提交是处于封榜时段 尝试次数+1
            if (isOpenSealRank && isInSealTimeSubmission(contest, contestRecord.getSubmitTime())) {

                int tryNum = (int) problemSubmissionInfo.getOrDefault("tryNum", 0);
                problemSubmissionInfo.put("tryNum", tryNum + 1);

            } else {

                // 如果该题目已经AC过了，其它都不记录了
                if ((Boolean) problemSubmissionInfo.getOrDefault("isAC", false)) {
                    continue;
                }

                // 记录已经按题目提交耗时time升序了

                // 通过的话
                if (contestRecord.getStatus().intValue() == Constants.Contest.RECORD_AC.getCode()) {
                    // 总解决题目次数ac+1
                    ACMContestRankVo.setAc(ACMContestRankVo.getAc() + 1);

                    // 判断是不是first AC
                    boolean isFirstAC = false;
                    Long time = firstACMap.getOrDefault(contestRecord.getDisplayId(), null);
                    if (time == null) {
                        isFirstAC = true;
                        firstACMap.put(contestRecord.getDisplayId(), contestRecord.getTime());
                    } else {
                        // 相同提交时间也是first AC
                        if (time.longValue() == contestRecord.getTime().longValue()) {
                            isFirstAC = true;
                        }
                    }

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("isAC", true);
                    problemSubmissionInfo.put("isFirstAC", isFirstAC);
                    problemSubmissionInfo.put("ACTime", contestRecord.getTime());
                    problemSubmissionInfo.put("errorNum", errorNumber);

                    // 同时计算总耗时，总耗时加上 该题目未AC前的错误次数*20*60+题目AC耗时
                    ACMContestRankVo.setTotalTime(ACMContestRankVo.getTotalTime() + errorNumber * 20 * 60 + contestRecord.getTime());

                    // 未通过同时需要记录罚时次数
                } else if (contestRecord.getStatus().intValue() == Constants.Contest.RECORD_NOT_AC_PENALTY.getCode()) {

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("errorNum", errorNumber + 1);
                } else {

                    int errorNumber = (int) problemSubmissionInfo.getOrDefault("errorNum", 0);
                    problemSubmissionInfo.put("errorNum", errorNumber);
                }
            }
            ACMContestRankVo.getSubmissionInfo().put(contestRecord.getDisplayId(), problemSubmissionInfo);
        }

        List<ACMContestRankVo> orderResultList = result.stream().sorted(Comparator.comparing(ACMContestRankVo::getAc, Comparator.reverseOrder()) // 先以总ac数降序
                .thenComparing(ACMContestRankVo::getTotalTime) //再以总耗时升序
        ).collect(Collectors.toList());

        return orderResultList;
    }

    public List<OIContestRankVo> calcOIRank(List<ContestRecord> oiContestRecord) {

        List<UserInfo> superAdminList = getSuperAdminList();

        List<String> superAdminUidList = superAdminList.stream().map(UserInfo::getUuid).collect(Collectors.toList());

        List<OIContestRankVo> result = new ArrayList<>();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> uidMapTime = new HashMap<>();

        int index = 0;
        for (ContestRecord contestRecord : oiContestRecord) {

            if (superAdminUidList.contains(contestRecord.getUid())) { // 超级管理员的提交不入排行榜
                continue;
            }

            if (contestRecord.getStatus() == 1) { // AC
                HashMap<String, Integer> pidMapTime = uidMapTime.get(contestRecord.getUid());
                if (pidMapTime != null) {
                    Integer useTime = pidMapTime.get(contestRecord.getDisplayId());
                    if (useTime != null) {
                        if (useTime > contestRecord.getUseTime()) {  // 如果时间消耗比原来的少
                            pidMapTime.put(contestRecord.getDisplayId(), contestRecord.getUseTime());
                        }
                    } else {
                        pidMapTime.put(contestRecord.getDisplayId(), contestRecord.getUseTime());
                    }
                } else {
                    HashMap<String, Integer> tmp = new HashMap<>();
                    tmp.put(contestRecord.getDisplayId(), contestRecord.getUseTime());
                    uidMapTime.put(contestRecord.getUid(), tmp);
                }
            }

            OIContestRankVo oiContestRankVo;
            if (!uidMapIndex.containsKey(contestRecord.getUid())) { // 如果该用户信息没还记录
                // 初始化参数
                oiContestRankVo = new OIContestRankVo();
                oiContestRankVo.setRealname(contestRecord.getRealname())
                        .setUid(contestRecord.getUid())
                        .setUsername(contestRecord.getUsername())
                        .setTotalScore(0);


                HashMap<String, Integer> submissionInfo = new HashMap<>();
                oiContestRankVo.setSubmissionInfo(submissionInfo);

                result.add(oiContestRankVo);
                uidMapIndex.put(contestRecord.getUid(), index);
                index++;
            } else {
                oiContestRankVo = result.get(uidMapIndex.get(contestRecord.getUid())); // 根据记录的index进行获取
            }

            // 记录已经是每道題最新的提交了
            oiContestRankVo.getSubmissionInfo().put(contestRecord.getDisplayId(), contestRecord.getScore());

            if (contestRecord.getScore() != null) { // 一般來说不可能出现，status已经筛掉没有评分的提交记录
                oiContestRankVo.setTotalScore(oiContestRankVo.getTotalScore() + contestRecord.getScore());
            }

        }


        for (OIContestRankVo oiContestRankVo : result) {
            HashMap<String, Integer> pidMapTime = uidMapTime.get(oiContestRankVo.getUid());
            int sumTime = 0;
            if (pidMapTime != null) {
                for (String key : pidMapTime.keySet()) {
                    Integer time = pidMapTime.get(key);
                    sumTime += time == null ? 0 : time;
                }
            }
            oiContestRankVo.setTotalTime(sumTime);
            oiContestRankVo.setTimeInfo(pidMapTime);
        }

        // 根据总得分进行降序,再根据总时耗升序排序
        List<OIContestRankVo> orderResultList = result.stream()
                .sorted(Comparator.comparing(OIContestRankVo::getTotalScore, Comparator.reverseOrder())
                        .thenComparing(OIContestRankVo::getTotalTime, Comparator.naturalOrder()))
                .collect(Collectors.toList());
        return orderResultList;
    }


    private boolean isInSealTimeSubmission(Contest contest, Date submissionDate) {
        return DateUtil.isIn(submissionDate, contest.getSealRankTime(), contest.getEndTime());
    }
}

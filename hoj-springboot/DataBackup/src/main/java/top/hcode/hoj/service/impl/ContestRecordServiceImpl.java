package top.hcode.hoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import top.hcode.hoj.dao.UserInfoMapper;
import top.hcode.hoj.pojo.entity.UserInfo;
import top.hcode.hoj.pojo.vo.ACMContestRankVo;
import top.hcode.hoj.pojo.entity.ContestRecord;
import top.hcode.hoj.dao.ContestRecordMapper;
import top.hcode.hoj.pojo.vo.OIContestRankVo;
import top.hcode.hoj.service.ContestRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.hcode.hoj.utils.Constants;

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


    @Override
    public IPage<ContestRecord> getACInfo(Integer currentPage, Integer limit, Integer status, Long cid) {

        List<ContestRecord> acInfo = contestRecordMapper.getACInfo(status, cid);

        HashMap<Long, Integer> pidMapIndex = new HashMap<>(20);
        HashMap<Integer, Long> indexMapTime = new HashMap<>(20);

        for (int i = 0; i < acInfo.size(); i++) {
            ContestRecord contestRecord = acInfo.get(i);
            contestRecord.setFirstBlood(false);
            Integer index = pidMapIndex.get(contestRecord.getPid());
            if (index == null) {
                pidMapIndex.put(contestRecord.getPid(), i);
                indexMapTime.put(i, contestRecord.getTime());
            } else {
                Long firstTime = indexMapTime.get(index);
                Long tmpTime = contestRecord.getTime();
                if (tmpTime < firstTime) {
                    pidMapIndex.put(contestRecord.getPid(), i);
                    indexMapTime.put(i, tmpTime);
                }
            }
        }


        List<ContestRecord> pageList = new ArrayList<>();

        int count = acInfo.size();

        //计算当前页第一条数据的下标
        int currId = currentPage > 1 ? (currentPage - 1) * limit : 0;
        for (int i = 0; i < limit && i < count - currId; i++) {
            ContestRecord contestRecord = acInfo.get(currId + i);
            if (pidMapIndex.get(contestRecord.getPid()) == currId + i){
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
    public IPage<ACMContestRankVo> getContestACMRank(List<ContestRecord> contestRecordList, int currentPage, int limit) {

        // 进行排序计算
        List<ACMContestRankVo> orderResultList = calcACMRank(contestRecordList);
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
    public IPage<OIContestRankVo> getContestOIRank(Long cid, String contestAuthor, Boolean isOpenSealRank, Date sealTime, Date startTime, Date endTime, int currentPage, int limit) {

        // 获取每个用户每道题最近一次提交
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


    public List<ACMContestRankVo> calcACMRank(List<ContestRecord> contestRecordList) {

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

            // 如果该题目已经AC过了，那么只记录提交次数，其它都不记录了
            ACMContestRankVo.setTotal(ACMContestRankVo.getTotal() + 1);
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
}

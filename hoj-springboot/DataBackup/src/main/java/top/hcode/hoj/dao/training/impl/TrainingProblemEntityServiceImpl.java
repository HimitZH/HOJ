package top.hcode.hoj.dao.training.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.mapper.TrainingProblemMapper;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.pojo.vo.ProblemVO;
import top.hcode.hoj.dao.training.TrainingProblemEntityService;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/20 12:25
 * @Description:
 */
@Service
public class TrainingProblemEntityServiceImpl extends ServiceImpl<TrainingProblemMapper, TrainingProblem> implements TrainingProblemEntityService {

    @Resource
    private TrainingProblemMapper trainingProblemMapper;

    @Resource
    private JudgeEntityService judgeEntityService;

    @Override
    public List<Long> getTrainingProblemIdList(Long tid) {
        return trainingProblemMapper.getTrainingProblemCount(tid);
    }

    @Override
    public List<ProblemVO> getTrainingProblemList(Long tid) {
        List<ProblemVO> trainingProblemList = trainingProblemMapper.getTrainingProblemList(tid);
        return trainingProblemList.stream().filter(distinctByKey(ProblemVO::getPid)).collect(Collectors.toList());
    }

    static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Override
    public Integer getUserTrainingACProblemCount(String uid, Long gid, List<Long> pidList) {
        if (CollectionUtils.isEmpty(pidList)) {
            return 0;
        }
        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.select("DISTINCT pid")
                .in("pid", pidList)
                .eq("cid", 0)
                .eq("uid", uid)
                .eq("status", 0);

        if (gid == null) {
            judgeQueryWrapper.isNull("gid");
        } else {
            judgeQueryWrapper.eq("gid", gid);
        }

        return judgeEntityService.count(judgeQueryWrapper);
    }

    @Override
    public List<TrainingProblem> getPrivateTrainingProblemListByPid(Long pid, String uid) {
        return trainingProblemMapper.getPrivateTrainingProblemListByPid(pid, uid);
    }

    @Override
    public List<TrainingProblem> getTrainingListAcceptedCountByUid(List<Long> tidList, String uid) {
        return trainingProblemMapper.getTrainingListAcceptedCountByUid(tidList, uid);
    }

    @Override
    public List<TrainingProblem> getGroupTrainingListAcceptedCountByUid(List<Long> tidList, Long gid, String uid) {
        return trainingProblemMapper.getGroupTrainingListAcceptedCountByUid(tidList, gid, uid);
    }

}
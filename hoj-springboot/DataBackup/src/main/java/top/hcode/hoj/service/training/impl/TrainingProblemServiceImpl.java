package top.hcode.hoj.service.training.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.TrainingProblemMapper;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.pojo.vo.ProblemVo;
import top.hcode.hoj.service.judge.impl.JudgeServiceImpl;
import top.hcode.hoj.service.problem.impl.ProblemServiceImpl;
import top.hcode.hoj.service.training.TrainingProblemService;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/20 12:25
 * @Description:
 */
@Service
public class TrainingProblemServiceImpl extends ServiceImpl<TrainingProblemMapper, TrainingProblem> implements TrainingProblemService {

    @Resource
    private TrainingProblemMapper trainingProblemMapper;

    @Resource
    private ProblemServiceImpl problemService;

    @Resource
    private JudgeServiceImpl judgeService;

    @Override
    public List<Long> getTrainingProblemIdList(Long tid) {
        return trainingProblemMapper.getTrainingProblemCount(tid);
    }

    @Override
    public List<ProblemVo> getTrainingProblemList(Long tid) {
        return trainingProblemMapper.getTrainingProblemList(tid);
    }

    @Override
    public Integer getUserTrainingACProblemCount(String uid, List<Long> pidList) {
        if (CollectionUtils.isEmpty(pidList)){
            return 0;
        }
        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.select("DISTINCT pid")
                .in("pid", pidList)
                .eq("cid", 0)
                .eq("uid", uid)
                .eq("status", 0);
        return judgeService.count(judgeQueryWrapper);
    }

    @Override
    public HashMap<String, Object> getAdminTrainingProblemList(int limit, int currentPage, String keyword,
                                                               Long tid, boolean queryExisted) {

        IPage<Problem> iPage = new Page<>(currentPage, limit);
        // 根据tid在TrainingProblem表中查询到对应pid集合
        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        trainingProblemQueryWrapper.eq("tid", tid).orderByAsc("display_id");
        List<Long> pidList = new LinkedList<>();
        List<TrainingProblem> trainingProblemList = trainingProblemMapper.selectList(trainingProblemQueryWrapper);
        HashMap<Long, TrainingProblem> trainingProblemMap = new HashMap<>();
        trainingProblemList.forEach(trainingProblem -> {
            trainingProblemMap.put(trainingProblem.getPid(), trainingProblem);
            pidList.add(trainingProblem.getPid());
        });

        HashMap<String, Object> trainingProblem = new HashMap<>();
        if (pidList.size() == 0 && queryExisted) { // 该训练原本就无题目数据
            trainingProblem.put("problemList", pidList);
            trainingProblem.put("contestProblemMap", trainingProblemMap);
            return trainingProblem;
        }

        QueryWrapper<Problem> problemQueryWrapper = new QueryWrapper<>();

        // 逻辑判断，如果是查询已有的就应该是in，如果是查询不要重复的，使用not in
        if (queryExisted) {
            problemQueryWrapper.in(pidList.size() > 0, "id", pidList);
        } else {
            // 权限需要是公开的（隐藏的，比赛中不可加入！）
            problemQueryWrapper.eq("auth", 1);
            problemQueryWrapper.notIn(pidList.size() > 0, "id", pidList);
        }

        if (!StringUtils.isEmpty(keyword)) {
            problemQueryWrapper.and(wrapper -> wrapper.like("title", keyword).or()
                    .like("problem_id", keyword).or()
                    .like("author", keyword));
        }

        IPage<Problem> problemListPager = problemService.page(iPage, problemQueryWrapper);

        if (queryExisted) {
            List<Problem> sortProblemList = problemListPager.getRecords()
                    .stream()
                    .sorted(Comparator.comparingInt(problem -> trainingProblemMap.get(problem.getId()).getRank()))
                    .collect(Collectors.toList());
            problemListPager.setRecords(sortProblemList);
        }
        trainingProblem.put("problemList", problemListPager);
        trainingProblem.put("trainingProblemMap", trainingProblemMap);
        return trainingProblem;
    }
}
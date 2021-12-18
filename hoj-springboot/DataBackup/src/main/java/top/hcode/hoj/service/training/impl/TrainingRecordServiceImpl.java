package top.hcode.hoj.service.training.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.dao.TrainingRecordMapper;
import top.hcode.hoj.pojo.dto.ToJudgeDto;
import top.hcode.hoj.pojo.entity.contest.Contest;
import top.hcode.hoj.pojo.entity.contest.ContestProblem;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.problem.Problem;
import top.hcode.hoj.pojo.entity.training.Training;
import top.hcode.hoj.pojo.entity.training.TrainingProblem;
import top.hcode.hoj.pojo.entity.training.TrainingRecord;
import top.hcode.hoj.pojo.vo.TrainingRankVo;
import top.hcode.hoj.pojo.vo.TrainingRecordVo;
import top.hcode.hoj.pojo.vo.UserRolesVo;
import top.hcode.hoj.service.judge.impl.JudgeServiceImpl;
import top.hcode.hoj.service.problem.impl.ProblemServiceImpl;
import top.hcode.hoj.service.training.TrainingRecordService;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2021/11/21 23:39
 * @Description:
 */
@Service
public class TrainingRecordServiceImpl extends ServiceImpl<TrainingRecordMapper, TrainingRecord> implements TrainingRecordService {


    @Resource
    private TrainingProblemServiceImpl trainingProblemService;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private TrainingRecordMapper trainingRecordMapper;

    @Resource
    private TrainingServiceImpl trainingService;

    @Resource
    private TrainingRegisterServiceImpl trainingRegisterService;

    @Resource
    private ProblemServiceImpl problemService;

    @Resource
    private JudgeServiceImpl judgeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult submitTrainingProblem(ToJudgeDto judgeDto, UserRolesVo userRolesVo, Judge judge) {

        Training training = trainingService.getById(judgeDto.getTid());
        if (training == null || !training.getStatus()) {
            return CommonResult.errorResponse("该训练不存在或不允许显示！");
        }


        CommonResult result = trainingRegisterService.checkTrainingAuth(training, userRolesVo);
        if (result != null) {
            return result;
        }

        // 查询获取对应的pid和cpid
        QueryWrapper<TrainingProblem> trainingProblemQueryWrapper = new QueryWrapper<>();
        trainingProblemQueryWrapper.eq("tid", judgeDto.getTid())
                .eq("display_id", judgeDto.getPid());
        TrainingProblem trainingProblem = trainingProblemService.getOne(trainingProblemQueryWrapper);
        judge.setPid(trainingProblem.getPid());

        Problem problem = problemService.getById(trainingProblem.getPid());
        if (problem.getAuth() == 2) {
            return CommonResult.errorResponse("错误！当前题目不可提交！", CommonResult.STATUS_FORBIDDEN);
        }
        judge.setDisplayPid(problem.getProblemId());

        // 将新提交数据插入数据库
        judgeService.saveOrUpdate(judge);

        // 非私有训练不记录
        if (!training.getAuth().equals(Constants.Training.AUTH_PRIVATE.getValue())){
            return null;
        }

        TrainingRecord trainingRecord = new TrainingRecord();
        trainingRecord.setPid(problem.getId())
                .setTid(judge.getTid())
                .setTpid(trainingProblem.getId())
                .setSubmitId(judge.getSubmitId())
                .setUid(userRolesVo.getUid());
        trainingRecordMapper.insert(trainingRecord);
        return null;
    }

    @Override
    public IPage<TrainingRankVo> getTrainingRank(Long tid, int currentPage, int limit) {

        Map<Long, String> tpIdMapDisplayId = getTPIdMapDisplayId(tid);
        List<TrainingRecordVo> trainingRecordVoList = trainingRecordMapper.getTrainingRecord(tid);

        List<TrainingRankVo> result = new ArrayList<>();

        HashMap<String, Integer> uidMapIndex = new HashMap<>();
        int pos = 0;
        for (TrainingRecordVo trainingRecordVo : trainingRecordVoList) {
            TrainingRankVo trainingRankVo;
            Integer index = uidMapIndex.get(trainingRecordVo.getUid());
            if (index == null) {
                trainingRankVo = new TrainingRankVo();
                trainingRankVo.setRealname(trainingRecordVo.getRealname())
                        .setAvatar(trainingRecordVo.getAvatar())
                        .setSchool(trainingRecordVo.getSchool())
                        .setGender(trainingRecordVo.getGender())
                        .setUid(trainingRecordVo.getUid())
                        .setUsername(trainingRecordVo.getUsername())
                        .setNickname(trainingRecordVo.getNickname())
                        .setAc(0)
                        .setTotalRunTime(0)
                        .setTotal(0);
                HashMap<String, HashMap<String, Object>> submissionInfo = new HashMap<>();
                trainingRankVo.setSubmissionInfo(submissionInfo);

                result.add(trainingRankVo);
                uidMapIndex.put(trainingRecordVo.getUid(), pos);
                pos++;
            } else {
                trainingRankVo = result.get(index);
            }
            String displayId = tpIdMapDisplayId.get(trainingRecordVo.getTpid());
            HashMap<String, Object> problemSubmissionInfo = trainingRankVo
                    .getSubmissionInfo()
                    .getOrDefault(displayId, new HashMap<>());
            trainingRankVo.setTotal(trainingRankVo.getTotal() + 1);

            // 如果该题目已经AC过了，只比较运行时间取最小
            if ((Boolean) problemSubmissionInfo.getOrDefault("isAC", false)) {
                int runTime = (int) problemSubmissionInfo.getOrDefault("runTime", 0);
                if (runTime > trainingRecordVo.getUseTime()) {
                    trainingRankVo.setTotalRunTime(trainingRankVo.getTotalRunTime() - runTime + trainingRecordVo.getUseTime());
                    problemSubmissionInfo.put("runTime", trainingRecordVo.getUseTime());
                }
                continue;
            }

            problemSubmissionInfo.put("status", trainingRecordVo.getStatus());
            problemSubmissionInfo.put("score", trainingRecordVo.getScore());

            // 通过的话
            if (trainingRecordVo.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                // 总解决题目次数ac+1
                trainingRankVo.setAc(trainingRankVo.getAc() + 1);
                problemSubmissionInfo.put("isAC", true);
                problemSubmissionInfo.put("runTime", trainingRecordVo.getUseTime());
                trainingRankVo.setTotalRunTime(trainingRankVo.getTotalRunTime() + trainingRecordVo.getUseTime());
            }

            trainingRankVo.getSubmissionInfo().put(displayId, problemSubmissionInfo);
        }

        List<TrainingRankVo> orderResultList = result.stream().sorted(Comparator.comparing(TrainingRankVo::getAc, Comparator.reverseOrder()) // 先以总ac数降序
                .thenComparing(TrainingRankVo::getTotalRunTime) //再以总耗时升序
        ).collect(Collectors.toList());

        // 计算好排行榜，然后进行分页
        Page<TrainingRankVo> page = new Page<>(currentPage, limit);
        int count = orderResultList.size();
        List<TrainingRankVo> pageList = new ArrayList<>();
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

    @SuppressWarnings("unchecked")
    private Map<Long, String> getTPIdMapDisplayId(Long tid) {
        String key = Constants.Training.MAP_REDIS_KEY_PRE.getValue() + tid;
        Map<Long, String> res = (Map<Long, String>) redisUtils.get(key);
        if (res == null) {
            QueryWrapper<TrainingProblem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("tid", tid);
            List<TrainingProblem> trainingProblemList = trainingProblemService.list(queryWrapper);
            res = trainingProblemList.stream().collect(Collectors.toMap(TrainingProblem::getId, TrainingProblem::getDisplayId));
            redisUtils.set(key, res, 12 * 3600);
        }
        return res;
    }

}
package top.hcode.hoj.service.impl;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.dao.ContestRecordMapper;
import top.hcode.hoj.pojo.entity.ContestRecord;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.service.ContestRecordService;
import top.hcode.hoj.util.Constants;

import java.util.Arrays;
import java.util.List;

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

    private static List<Integer> penaltyStatus = Arrays.asList(Constants.Judge.STATUS_PRESENTATION_ERROR.getStatus(),
            Constants.Judge.STATUS_WRONG_ANSWER.getStatus(),
            Constants.Judge.STATUS_CPU_TIME_LIMIT_EXCEEDED.getStatus(),
            Constants.Judge.STATUS_REAL_TIME_LIMIT_EXCEEDED.getStatus(),
            Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus(),
            Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());


    @Override
    @Async
    @Transactional
    public void UpdateContestRecord(Judge judge) {
        UpdateWrapper<ContestRecord> updateWrapper = new UpdateWrapper<>();
        // 如果是AC
        if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
            updateWrapper.set("status", Constants.Contest.RECORD_AC.getCode());
            // 部分通过
        } else if (judge.getStatus().intValue() == Constants.Judge.STATUS_PARTIAL_ACCEPTED.getStatus()) {
            updateWrapper.set("first_blood", false)
                    .set("status", Constants.Contest.RECORD_NOT_AC_NOT_PENALTY.getCode());
            // 需要被罚时的状态
        } else if (penaltyStatus.contains(judge.getStatus())) {
            updateWrapper.set("status", Constants.Contest.RECORD_NOT_AC_PENALTY.getCode())
                    .set("first_blood", false);
        }

        if (judge.getScore()!=null){
            updateWrapper.set("score", judge.getScore());
        }

        updateWrapper.eq("submit_id", judge.getSubmitId()) // submit_id一定只有一个
                .eq("uid", judge.getUid())
                .eq("cid", judge.getCid());
        boolean result = contestRecordMapper.update(null, updateWrapper) > 0;
        if (!result) {
            tryAgainUpdate(updateWrapper);
        }
    }

    public void tryAgainUpdate(UpdateWrapper<ContestRecord> updateWrapper) {
        boolean retryable;
        int attemptNumber = 0;
        do {
            boolean result = contestRecordMapper.update(null, updateWrapper) > 0;
            if (result) {
                return;
            } else {
                attemptNumber++;
                retryable = attemptNumber < 8;
                if (attemptNumber == 8) {
                    log.error("更新contest_record表超过最大重试次数");
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        } while (retryable);
    }
}

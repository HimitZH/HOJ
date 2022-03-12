package top.hcode.hoj.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.dao.JudgeEntityService;
import top.hcode.hoj.mapper.UserRecordMapper;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.user.UserRecord;

import top.hcode.hoj.dao.UserRecordEntityService;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Himit_ZH
 * @since 2020-10-23
 */
@Service
public class UserRecordEntityServiceImpl extends ServiceImpl<UserRecordMapper, UserRecord> implements UserRecordEntityService {

    @Autowired
    private UserRecordMapper userRecordMapper;

    @Autowired
    private JudgeEntityService judgeEntityService;

    /**
     * @MethodNameupdateRecord
     * @Params  * @param null
     * @Description 本方法不启用，不适合数据一致性
     * @Return
     * @Since 2021/6/2
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    @Async
    @Override
    @Deprecated
    public void updateRecord(String uid, Long submitId, Long pid, Integer score) {
        QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
        judgeQueryWrapper.isNotNull("score")
                .eq("cid", 0)// 非比赛提交
                .eq("pid", pid)
                .eq("uid", uid)
                .ne("submit_id", submitId)
                .orderByDesc("score")
                .last("limit 1");
        Judge lastHighScoreJudge = judgeEntityService.getOne(judgeQueryWrapper);
        // 之前没有提交过，那么就需要修改
        boolean result = true;
        if (lastHighScoreJudge == null) {
            UpdateWrapper<UserRecord> userRecordUpdateWrapper = new UpdateWrapper<>();
            userRecordUpdateWrapper.setSql("total_score=total_score+" + score).eq("uid", uid);
            result = userRecordMapper.update(null, userRecordUpdateWrapper) == 1;
        } else if (lastHighScoreJudge.getScore() < score) {
            //如果之前该题目最高得分的提交比现在得分低,也需要修改
            int addValue = score - lastHighScoreJudge.getScore();
            UpdateWrapper<UserRecord> userRecordUpdateWrapper = new UpdateWrapper<>();
            userRecordUpdateWrapper.setSql("total_score=total_score+" + addValue).eq("uid", uid);
            result = userRecordMapper.update(null, userRecordUpdateWrapper) == 1;
        }
        if (result) {
            return;
        } else { // 失败则开始尝试
            tryAgainUpdate(uid, score);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean tryAgainUpdate(String uid, Integer score) {
        boolean retryable;
        int attemptNumber = 0;
        do {
            // 查询最新版本号
            QueryWrapper<Judge> judgeQueryWrapper = new QueryWrapper<>();
            judgeQueryWrapper.isNotNull("score").orderByDesc("score").isNull("cid") // 非比赛提交
                    .last("limit 1");
            Judge lastHighScoreJudge = judgeEntityService.getOne(judgeQueryWrapper);

            // 更新
            boolean success = true;
            if (lastHighScoreJudge == null) {
                UpdateWrapper<UserRecord> userRecordUpdateWrapper = new UpdateWrapper<>();
                userRecordUpdateWrapper.set("total_score", score).eq("uid", uid);
                success = userRecordMapper.update(null, userRecordUpdateWrapper) == 1;
            } else if (lastHighScoreJudge.getScore() < score) {
                //如果之前该题目最高得分的提交比现在得分低,也需要修改
                int addValue = score - lastHighScoreJudge.getScore();
                UpdateWrapper<UserRecord> userRecordUpdateWrapper = new UpdateWrapper<>();
                userRecordUpdateWrapper.setSql("total_score=total_score+" + addValue).eq("uid", uid);
                success = userRecordMapper.update(null, userRecordUpdateWrapper) == 1;
            }

            if (success) {
                return true;
            } else {
                attemptNumber++;
                retryable = attemptNumber < 8;
                if (attemptNumber == 8) {
                    log.error("更新user_record表超过最大重试次数");
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
        } while (retryable);

        return false;
    }
}

package top.hcode.hoj.remoteJudge;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.pojo.entity.Judge;

import top.hcode.hoj.pojo.entity.RemoteJudgeAccount;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeFactory;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.service.impl.RemoteJudgeAccountServiceImpl;
import top.hcode.hoj.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j(topic = "hoj")
@Component
public class RemoteJudgeGetResult {

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private RemoteJudgeAccountServiceImpl remoteJudgeAccountService;

    @Transactional
    @Async
    public void sendTask(String remoteJudge, String username, String password, Long submitId, String uid,
                         Long cid, Long pid, Long resultSubmitId, String cookies) {

        RemoteJudgeStrategy remoteJudgeStrategy = RemoteJudgeFactory.selectJudge(remoteJudge);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        AtomicInteger count = new AtomicInteger(0);
        Runnable getResultTask = new Runnable() {
            @Override
            @Transactional
            public void run() {

                if (count.get() > 60) { // 超过60次失败则判为提交失败
                    // 更新此次提交状态为提交失败！
                    UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
                    judgeUpdateWrapper.set("status", Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                            .eq("submit_id", submitId);
                    judgeService.update(judgeUpdateWrapper);

                    changeAccountStatus(remoteJudge, username, password);

                    scheduler.shutdown();

                    return;
                }

                count.getAndIncrement();
                try {
                    Map<String, Object> result = remoteJudgeStrategy.result(resultSubmitId, username, password, cookies);
                    Integer status = (Integer) result.getOrDefault("status", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
                    if (status.intValue() != Constants.Judge.STATUS_PENDING.getStatus() &&
                            status.intValue() != Constants.Judge.STATUS_JUDGING.getStatus() &&
                            status.intValue() != Constants.Judge.STATUS_COMPILING.getStatus()) {

                        // 由于POJ特殊 之前获取提交ID未释放账号，所以在此需要将账号变为可用
                        changeAccountStatus(remoteJudge, username, password);

                        Integer time = (Integer) result.getOrDefault("time", null);
                        Integer memory = (Integer) result.getOrDefault("memory", null);
                        String CEInfo = (String) result.getOrDefault("CEInfo", null);
                        Judge judge = new Judge();

                        judge.setSubmitId(submitId)
                                .setStatus(status)
                                .setTime(time)
                                .setMemory(memory);

                        if (status.intValue() == Constants.Judge.STATUS_COMPILE_ERROR.getStatus()) {
                            judge.setErrorMessage(CEInfo);
                        } else if (status.intValue() == Constants.Judge.STATUS_SYSTEM_ERROR.getStatus()) {
                            judge.setErrorMessage("There is something wrong with the " + remoteJudge + ", please try again later");
                        }

                        // 如果是比赛题目，需要特别适配OI比赛的得分 除AC给100 其它结果给0分
                        if (cid != 0) {
                            int score = 0;

                            if (judge.getStatus().intValue() == Constants.Judge.STATUS_ACCEPTED.getStatus()) {
                                score = 100;
                            }

                            judge.setScore(score);
                            // 写回数据库
                            judgeService.updateById(judge);
                            // 同步其它表
                            judgeService.updateOtherTable(submitId, status, cid, uid, pid, score, judge.getTime());

                        } else {
                            judgeService.updateById(judge);
                            // 同步其它表
                            judgeService.updateOtherTable(submitId, status, cid, uid, pid, null, null);
                        }

                        scheduler.shutdown();
                    }else {

                        Judge judge = new Judge();
                        judge.setSubmitId(submitId)
                                .setStatus(status);
                        // 写回数据库
                        judgeService.updateById(judge);
                    }

                } catch (Exception ignored) {

                }

            }
        };
        final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(
                getResultTask, 2, 2, TimeUnit.SECONDS);

    }

    public void changeAccountStatus(String remoteJudge, String username, String password) {
        // 由于POJ特殊 之前获取提交ID未释放账号，所以在此需要将账号变为可用
        if (remoteJudge.equals(Constants.RemoteJudge.POJ_JUDGE.getName())) {
            UpdateWrapper<RemoteJudgeAccount> remoteJudgeAccountUpdateWrapper = new UpdateWrapper<>();
            remoteJudgeAccountUpdateWrapper.set("status", true)
                    .eq("oj", remoteJudge)
                    .eq("username", username);
            boolean isOk = remoteJudgeAccountService.update(remoteJudgeAccountUpdateWrapper);
            if (!isOk) {
                log.error("远程判题：修正账号为可用状态失败----------->{}", "username:" + username + ",password:" + password);
            }
        }
    }

}

package top.hcode.hoj.remoteJudge;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeFactory;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;

import top.hcode.hoj.service.impl.JudgeServiceImpl;

import top.hcode.hoj.service.impl.RemoteJudgeServiceImpl;
import top.hcode.hoj.util.Constants;

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
    private RemoteJudgeServiceImpl remoteJudgeService;

    @Async
    public void sendTask(String remoteJudge, String username, String password,
                         Long submitId, String uid,
                         Long cid, Long pid,
                         Long resultSubmitId, String cookies,
                         String ip, Integer port) {

        RemoteJudgeStrategy remoteJudgeStrategy = RemoteJudgeFactory.selectJudge(remoteJudge);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        AtomicInteger count = new AtomicInteger(0);
        Runnable getResultTask = new Runnable() {
            @Override
            public void run() {

                if (count.get() > 60) { // 超过60次失败则判为提交失败
                    // 更新此次提交状态为提交失败！
                    UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
                    judgeUpdateWrapper.set("status", Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                            .eq("submit_id", submitId);
                    judgeService.update(judgeUpdateWrapper);

                    log.error("[{}] Get Result Failed!", remoteJudge);
                    changeRemoteJudgeLock(remoteJudge, username, ip, port, resultSubmitId);

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
                        log.info("[{}] Get Result Successfully! Status:[{}]", remoteJudge, status);

                        changeRemoteJudgeLock(remoteJudge, username, ip, port, resultSubmitId);

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
                    } else {

                        Judge judge = new Judge();
                        judge.setSubmitId(submitId)
                                .setStatus(status);
                        // 写回数据库
                        judgeService.updateById(judge);
                    }

                } catch (Exception e) {
                    log.error("The Error of getting the `remote judge` result:", e);
                }

            }
        };
        final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(
                getResultTask, 0, 3, TimeUnit.SECONDS);

    }


    private void changeRemoteJudgeLock(String remoteJudge, String username, String ip, Integer port, Long resultSubmitId) {
        log.info("After Get Result,remote_judge:[{}],submit_id: [{}]! Begin to return the account to other task!",
                remoteJudge, resultSubmitId);
        // 将账号变为可用
        remoteJudgeService.changeAccountStatus(remoteJudge, username);
        if (remoteJudge.equals(Constants.RemoteJudge.GYM_JUDGE.getName())
                || remoteJudge.equals(Constants.RemoteJudge.CF_JUDGE.getName())) {
            log.info("After Get Result,remote_judge:[{}],submit_id: [{}] !Begin to return the Server Status to other task!",
                    remoteJudge, resultSubmitId);
            remoteJudgeService.changeServerSubmitCFStatus(ip, port);
        }
    }


}

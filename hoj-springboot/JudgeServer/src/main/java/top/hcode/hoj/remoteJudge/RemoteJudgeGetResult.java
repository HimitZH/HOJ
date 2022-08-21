package top.hcode.hoj.remoteJudge;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.dao.JudgeCaseEntityService;
import top.hcode.hoj.dao.JudgeEntityService;
import top.hcode.hoj.judge.JudgeContext;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeDTO;
import top.hcode.hoj.remoteJudge.entity.RemoteJudgeRes;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.service.RemoteJudgeService;
import top.hcode.hoj.util.Constants;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j(topic = "hoj")
@Component
public class RemoteJudgeGetResult {

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private JudgeContext judgeContext;

    @Autowired
    private RemoteJudgeService remoteJudgeService;

    @Resource
    private JudgeCaseEntityService judgeCaseEntityService;

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private final static Map<String, Future> futureTaskMap = new ConcurrentHashMap<>(Runtime.getRuntime().availableProcessors() * 2);

    public void process(RemoteJudgeStrategy remoteJudgeStrategy) {

        RemoteJudgeDTO remoteJudgeDTO = remoteJudgeStrategy.getRemoteJudgeDTO();
        String key = UUID.randomUUID().toString() + remoteJudgeDTO.getSubmitId();
        AtomicInteger count = new AtomicInteger(0);
        Runnable getResultTask = new Runnable() {
            @Override
            public void run() {

                if (count.get() >= 60) { // 超过60次失败则判为提交失败
                    // 更新此次提交状态为提交失败！
                    UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
                    judgeUpdateWrapper.set("status", Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                            .set("error_message", "Waiting for remote judge result exceeds the maximum number of times, please try submitting again!")
                            .eq("submit_id", remoteJudgeDTO.getJudgeId());
                    judgeEntityService.update(judgeUpdateWrapper);

                    log.error("[{}] Get Result Failed!", remoteJudgeDTO.getOj());
                    changeRemoteJudgeLock(remoteJudgeDTO.getOj(),
                            remoteJudgeDTO.getUsername(),
                            remoteJudgeDTO.getServerIp(),
                            remoteJudgeDTO.getServerPort(),
                            remoteJudgeDTO.getSubmitId());

                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        boolean isCanceled = future.cancel(true);
                        if (isCanceled) {
                            futureTaskMap.remove(key);
                        }
                    }
                    return;
                }

                count.getAndIncrement();

                RemoteJudgeRes remoteJudgeRes;
                try {
                    remoteJudgeRes = remoteJudgeStrategy.result();
                } catch (Exception e) {
                    if (count.get() == 60) {
                        log.error("The Error of getting the `remote judge` result:", e);
                    }
                    return;
                }

                // 保留各个测试点的结果数据
                if (!CollectionUtils.isEmpty(remoteJudgeRes.getJudgeCaseList())) {
                    judgeCaseEntityService.saveBatch(remoteJudgeRes.getJudgeCaseList());
                }

                Integer status = remoteJudgeRes.getStatus();
                if (status.intValue() != Constants.Judge.STATUS_PENDING.getStatus() &&
                        status.intValue() != Constants.Judge.STATUS_JUDGING.getStatus() &&
                        status.intValue() != Constants.Judge.STATUS_COMPILING.getStatus()) {
                    log.info("[{}] Get Result Successfully! Status:[{}]", remoteJudgeDTO.getOj(), status);

                    changeRemoteJudgeLock(remoteJudgeDTO.getOj(),
                            remoteJudgeDTO.getUsername(),
                            remoteJudgeDTO.getServerIp(),
                            remoteJudgeDTO.getServerPort(),
                            remoteJudgeDTO.getSubmitId());

                    Integer time = remoteJudgeRes.getTime();
                    Integer memory = remoteJudgeRes.getMemory();
                    String errorInfo = remoteJudgeRes.getErrorInfo();
                    Judge finalJudgeRes = new Judge();

                    finalJudgeRes.setSubmitId(remoteJudgeDTO.getJudgeId())
                            .setStatus(status)
                            .setTime(time)
                            .setMemory(memory);

                    if (status.intValue() == Constants.Judge.STATUS_COMPILE_ERROR.getStatus()) {
                        finalJudgeRes.setErrorMessage(errorInfo);
                    } else if (status.intValue() == Constants.Judge.STATUS_SYSTEM_ERROR.getStatus()) {
                        finalJudgeRes.setErrorMessage("There is something wrong with the " + remoteJudgeDTO.getOj() + ", please try again later");
                    }

                    // 如果是比赛题目，需要特别适配OI比赛的得分 除AC给100 其它结果给0分
                    if (remoteJudgeDTO.getCid() != 0) {
                        int score = 0;

                        if (Objects.equals(finalJudgeRes.getStatus(), Constants.Judge.STATUS_ACCEPTED.getStatus())) {
                            score = 100;
                        }

                        finalJudgeRes.setScore(score);
                        // 写回数据库
                        judgeEntityService.updateById(finalJudgeRes);
                        // 同步其它表
                        judgeContext.updateOtherTable(remoteJudgeDTO.getJudgeId(),
                                status,
                                remoteJudgeDTO.getCid(),
                                remoteJudgeDTO.getUid(),
                                remoteJudgeDTO.getPid(),
                                remoteJudgeDTO.getGid(),
                                score,
                                finalJudgeRes.getTime());

                    } else {
                        judgeEntityService.updateById(finalJudgeRes);
                        // 同步其它表
                        judgeContext.updateOtherTable(remoteJudgeDTO.getJudgeId(),
                                status,
                                remoteJudgeDTO.getCid(),
                                remoteJudgeDTO.getUid(),
                                remoteJudgeDTO.getPid(),
                                remoteJudgeDTO.getGid(),
                                null,
                                null);
                    }

                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        future.cancel(true);
                        futureTaskMap.remove(key);
                    }
                } else {

                    Judge judge = new Judge();
                    judge.setSubmitId(remoteJudgeDTO.getJudgeId())
                            .setStatus(status);
                    // 写回数据库
                    judgeEntityService.updateById(judge);
                }
            }
        };
        ScheduledFuture<?> beeperHandle = scheduler.scheduleWithFixedDelay(
                getResultTask, 0, 2500, TimeUnit.MILLISECONDS);
        futureTaskMap.put(key, beeperHandle);
    }


    private void changeRemoteJudgeLock(String remoteJudge, String username, String ip, Integer port, Long resultSubmitId) {
        log.info("After Get Result,remote_judge:[{}],submit_id: [{}]! Begin to return the account to other task!",
                remoteJudge, resultSubmitId);
        // 将账号变为可用
        remoteJudgeService.changeAccountStatus(remoteJudge, username);
        if (RemoteJudgeContext.openCodeforcesFixServer) {
            if (remoteJudge.equals(Constants.RemoteJudge.GYM_JUDGE.getName())
                    || remoteJudge.equals(Constants.RemoteJudge.CF_JUDGE.getName())) {
                log.info("After Get Result,remote_judge:[{}],submit_id: [{}] !Begin to return the Server Status to other task!",
                        remoteJudge, resultSubmitId);
                remoteJudgeService.changeServerSubmitCFStatus(ip, port);
            }
        }
    }


}

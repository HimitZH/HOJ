package top.hcode.hoj.remoteJudge;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.pojo.entity.Judge;

import top.hcode.hoj.remoteJudge.task.RemoteJudgeFactory;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
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

    @Transactional
    public void sendTask(String remoteJudge, String username, Long submitId, String uid,
                         Long cid, Long pid, Long resultSubmitId, String token,
                         HashMap<String, String> cookies) {

        RemoteJudgeStrategy remoteJudgeStrategy = RemoteJudgeFactory.selectJudge(remoteJudge);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        AtomicInteger count = new AtomicInteger(0);

        Runnable getResultTask = new Runnable() {
            @Override
            @Transactional
            public void run() {
                count.getAndIncrement();
                try {
                    Map<String, Object> result = remoteJudgeStrategy.result(resultSubmitId, username, token, cookies);
                    Integer status = (Integer) result.getOrDefault("status", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
                    if (status.intValue() != Constants.Judge.STATUS_PENDING.getStatus() &&
                            status.intValue() != Constants.Judge.STATUS_JUDGING.getStatus()) {
                        Judge judge = new Judge();
                        judge.setSubmitId(submitId);
                        Integer time = (Integer) result.getOrDefault("time", null);
                        Integer memory = (Integer) result.getOrDefault("memory", null);
                        String CEInfo = (String) result.getOrDefault("CEInfo", null);
                        judge.setStatus(status)
                                .setTime(time)
                                .setMemory(memory);

                        if (status.intValue() == Constants.Judge.STATUS_COMPILE_ERROR.getStatus()) {
                            judge.setErrorMessage(CEInfo);
                        } else if (status.intValue() == Constants.Judge.STATUS_SYSTEM_ERROR.getStatus()) {
                            judge.setErrorMessage("There is something wrong with the " + remoteJudge + ", please try again later");
                        }
                        // 写回数据库
                        judgeService.updateById(judge);
                        // 同步其它表
                        judgeService.updateOtherTable(submitId, status, cid, uid, pid, null);
                        scheduler.shutdown();
                    }

                } catch (Exception ignored) {

                } finally {
                    if (count.get() == 60) { // 60次失败则判为提交失败
                        // 更新此次提交状态为提交失败！
                        UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
                        judgeUpdateWrapper.set("status", Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                                .eq("submit_id", submitId);
                        judgeService.update(judgeUpdateWrapper);
                        scheduler.shutdown();
                    }
                }

            }
        };
        final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(
                getResultTask, 2, 2, TimeUnit.SECONDS);

    }

}

package top.hcode.hoj.judge.remote;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.judge.RemoteJudgeAccountEntityService;
import top.hcode.hoj.judge.AbstractReceiver;
import top.hcode.hoj.judge.ChooseUtils;
import top.hcode.hoj.judge.Dispatcher;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.RemoteJudgeAccount;
import top.hcode.hoj.pojo.entity.judge.ToJudge;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RemoteJudgeReceiver extends AbstractReceiver {

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private Dispatcher dispatcher;

    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private ChooseUtils chooseUtils;

    @Autowired
    private RemoteJudgeAccountEntityService remoteJudgeAccountEntityService;

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private final static Map<String, Future> futureTaskMap = new ConcurrentHashMap<>(10);

    @Async("judgeTaskAsyncPool")
    public void processWaitingTask() {
        // 优先处理比赛的提交
        // 其次处理普通提交的提交
        handleWaitingTask(Constants.Queue.CONTEST_REMOTE_JUDGE_WAITING_HANDLE.getName(),
                Constants.Queue.GENERAL_REMOTE_JUDGE_WAITING_HANDLE.getName());
    }

    @Override
    public String getTaskByRedis(String queue) {
        if (redisUtils.lGetListSize(queue) > 0) {
            return (String) redisUtils.lrPop(queue);
        } else {
            return null;
        }
    }

    @Override
    public void handleJudgeMsg(String taskJsonStr) {
        JSONObject task = JSONUtil.parseObj(taskJsonStr);

        Judge judge = task.get("judge", Judge.class);
        String token = task.getStr("token");
        String remoteJudgeProblem = task.getStr("remoteJudgeProblem");
        Boolean isHasSubmitIdRemoteReJudge = task.getBool("isHasSubmitIdRemoteReJudge");
        String remoteOJName = remoteJudgeProblem.split("-")[0].toUpperCase();

        dispatchRemoteJudge(judge,
                token,
                remoteJudgeProblem,
                isHasSubmitIdRemoteReJudge,
                remoteOJName);
    }

    private void dispatchRemoteJudge(Judge judge, String token, String remoteJudgeProblem,
                                     Boolean isHasSubmitIdRemoteReJudge, String remoteOJName) {

        ToJudge toJudge = new ToJudge();
        toJudge.setJudge(judge)
                .setToken(token)
                .setRemoteJudgeProblem(remoteJudgeProblem);

        if (remoteOJName.equals(Constants.RemoteOJ.CODEFORCES.getName())
                || remoteOJName.equals(Constants.RemoteOJ.GYM.getName())) { // GYM与CF共用账号
            cfJudge(isHasSubmitIdRemoteReJudge, toJudge, judge);
        } else if (remoteOJName.equals(Constants.RemoteOJ.POJ.getName())) {
            pojJudge(isHasSubmitIdRemoteReJudge, toJudge, judge);
        } else {
            commonJudge(remoteOJName, isHasSubmitIdRemoteReJudge, toJudge, judge);
        }
        // 如果队列中还有任务，则继续处理
        processWaitingTask();
    }


    private void commonJudge(String OJName, Boolean isHasSubmitIdRemoteReJudge, ToJudge toJudge, Judge judge) {

        if (isHasSubmitIdRemoteReJudge) {
            toJudge.setIsHasSubmitIdRemoteReJudge(true);
            toJudge.setUsername(judge.getVjudgeUsername());
            toJudge.setPassword(judge.getVjudgePassword());
            // 调用判题服务
            dispatcher.dispatcher("judge", "/remote-judge", toJudge);
            return;
        }

        // 尝试600s
        AtomicInteger tryNum = new AtomicInteger(0);
        String key = UUID.randomUUID().toString() + toJudge.getJudge().getSubmitId();
        Runnable getResultTask = new Runnable() {
            @Override
            public void run() {
                if (tryNum.get() > 200) {
                    // 获取调用多次失败可能为系统忙碌，判为提交失败
                    judge.setStatus(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus());
                    judge.setErrorMessage("Submission failed! Please resubmit this submission again!" +
                            "Cause: Waiting for account scheduling timeout");
                    judgeEntityService.updateById(judge);
                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        boolean isCanceled = future.cancel(true);
                        if (isCanceled) {
                            futureTaskMap.remove(key);
                        }
                    }
                    return;
                }
                tryNum.getAndIncrement();
                RemoteJudgeAccount account = chooseUtils.chooseRemoteAccount(OJName, judge.getVjudgeUsername(), false);
                if (account != null) {
                    toJudge.setUsername(account.getUsername())
                            .setPassword(account.getPassword());
                    toJudge.setIsHasSubmitIdRemoteReJudge(false);
                    // 调用判题服务
                    dispatcher.dispatcher("judge", "/remote-judge", toJudge);
                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        future.cancel(true);
                        futureTaskMap.remove(key);
                    }
                }
            }
        };
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(getResultTask, 0, 3, TimeUnit.SECONDS);
        futureTaskMap.put(key, scheduledFuture);
    }


    private void pojJudge(Boolean isHasSubmitIdRemoteReJudge, ToJudge toJudge, Judge judge) {


        if (StringUtils.isEmpty(judge.getVjudgeUsername())) {
            isHasSubmitIdRemoteReJudge = false;
        }

        if (isHasSubmitIdRemoteReJudge) {
            QueryWrapper<RemoteJudgeAccount> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("oj", Constants.RemoteOJ.POJ.getName())
                    .eq("username", judge.getVjudgeUsername());
            int count = remoteJudgeAccountEntityService.count(queryWrapper);
            if (count == 0) {
                // poj以往的账号丢失了，那么只能重新从头到尾提交
                isHasSubmitIdRemoteReJudge = false;
            }
        }

        // 尝试600s
        AtomicInteger tryNum = new AtomicInteger(0);
        String key = UUID.randomUUID().toString() + toJudge.getJudge().getSubmitId();
        boolean finalIsHasSubmitIdRemoteReJudge = isHasSubmitIdRemoteReJudge;
        Runnable getResultTask = new Runnable() {
            @Override
            public void run() {
                if (tryNum.get() > 200) {
                    // 获取调用多次失败可能为系统忙碌，判为提交失败
                    judge.setStatus(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus());
                    judge.setErrorMessage("Submission failed! Please resubmit this submission again!" +
                            "Cause: Waiting for account scheduling timeout");
                    judgeEntityService.updateById(judge);
                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        boolean isCanceled = future.cancel(true);
                        if (isCanceled) {
                            futureTaskMap.remove(key);
                        }
                    }
                    return;
                }
                tryNum.getAndIncrement();
                RemoteJudgeAccount account = chooseUtils.chooseRemoteAccount(Constants.RemoteOJ.POJ.getName()
                        , judge.getVjudgeUsername(), finalIsHasSubmitIdRemoteReJudge);
                if (account != null) {
                    toJudge.setUsername(account.getUsername())
                            .setPassword(account.getPassword());
                    toJudge.setIsHasSubmitIdRemoteReJudge(finalIsHasSubmitIdRemoteReJudge);
                    // 调用判题服务
                    dispatcher.dispatcher("judge", "/remote-judge", toJudge);
                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        future.cancel(true);
                        futureTaskMap.remove(key);
                    }
                }
            }
        };
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(getResultTask, 0, 3, TimeUnit.SECONDS);
        futureTaskMap.put(key, scheduledFuture);
    }

    private void cfJudge(Boolean isHasSubmitIdRemoteReJudge, ToJudge toJudge, Judge judge) {

        if (isHasSubmitIdRemoteReJudge) {
            toJudge.setIsHasSubmitIdRemoteReJudge(true);
            toJudge.setUsername(judge.getVjudgeUsername());
            toJudge.setPassword(judge.getVjudgePassword());
            // 调用判题服务
            dispatcher.dispatcher("judge", "/remote-judge", toJudge);
            return;
        }

        // 尝试600s
        String key = UUID.randomUUID().toString() + toJudge.getJudge().getSubmitId();
        AtomicInteger tryNum = new AtomicInteger(0);
        Runnable getResultTask = new Runnable() {
            @Override
            public void run() {
                if (tryNum.get() > 200) {
                    // 获取调用多次失败可能为系统忙碌，判为提交失败
                    judge.setStatus(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus());
                    judge.setErrorMessage("Submission failed! Please resubmit this submission again!" +
                            "Cause: Waiting for account scheduling timeout");
                    judgeEntityService.updateById(judge);
                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        boolean isCanceled = future.cancel(true);
                        if (isCanceled) {
                            futureTaskMap.remove(key);
                        }
                    }
                    return;
                }
                tryNum.getAndIncrement();
                HashMap<String, Object> result = chooseUtils.chooseFixedAccount(Constants.RemoteOJ.CODEFORCES.getName());
                if (result != null) {
                    RemoteJudgeAccount account = (RemoteJudgeAccount) result.get("account");
                    int index = (int) result.get("index");
                    int size = (int) result.get("size");
                    toJudge.setUsername(account.getUsername())
                            .setPassword(account.getPassword());
                    toJudge.setIsHasSubmitIdRemoteReJudge(false);
                    toJudge.setIndex(index);
                    toJudge.setSize(size);
                    // 调用判题服务
                    dispatcher.dispatcher("judge", "/remote-judge", toJudge);
                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        future.cancel(true);
                        futureTaskMap.remove(key);
                    }
                }
            }
        };
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(getResultTask, 0, 3, TimeUnit.SECONDS);
        futureTaskMap.put(key, scheduledFuture);
    }
}

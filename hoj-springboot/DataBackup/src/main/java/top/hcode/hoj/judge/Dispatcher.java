package top.hcode.hoj.judge;


import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.common.result.ResultStatus;
import top.hcode.hoj.dao.judge.JudgeEntityService;
import top.hcode.hoj.dao.judge.JudgeServerEntityService;
import top.hcode.hoj.dao.judge.impl.RemoteJudgeAccountEntityServiceImpl;
import top.hcode.hoj.pojo.dto.CompileDTO;
import top.hcode.hoj.pojo.dto.TestJudgeReq;
import top.hcode.hoj.pojo.dto.TestJudgeRes;
import top.hcode.hoj.pojo.dto.ToJudgeDTO;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.pojo.entity.judge.JudgeServer;
import top.hcode.hoj.pojo.entity.judge.RemoteJudgeAccount;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Himit_ZH
 * @Date: 2021/4/15 17:29
 * @Description: 分发调用判题机执行任务
 */
@Component
@Slf4j(topic = "hoj")
public class Dispatcher {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JudgeServerEntityService judgeServerEntityService;

    @Autowired
    private JudgeEntityService judgeEntityService;

    @Autowired
    private ChooseUtils chooseUtils;

    @Autowired
    private RedisUtils redisUtils;

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

    private final static Map<String, Future> futureTaskMap = new ConcurrentHashMap<>(20);

    // 每个提交任务尝试300次失败则判为提交失败
    protected final static Integer maxTryNum = 300;

    @Autowired
    private RemoteJudgeAccountEntityServiceImpl remoteJudgeAccountService;

    public CommonResult dispatch(Constants.TaskType taskType, Object data) {
        switch (taskType) {
            case JUDGE:
                defaultJudge((ToJudgeDTO) data, taskType.getPath());
                break;
            case REMOTE_JUDGE:
                remoteJudge((ToJudgeDTO) data, taskType.getPath());
                break;
            case TEST_JUDGE:
                testJudge((TestJudgeReq) data, taskType.getPath());
                break;
            case COMPILE_SPJ:
            case COMPILE_INTERACTIVE:
                return compile((CompileDTO) data, taskType.getPath());
            default:
                throw new IllegalArgumentException("判题机不支持此调用类型");
        }
        return null;
    }

    /**
     * 普通评测
     * @param data
     * @param path
     */
    public void defaultJudge(ToJudgeDTO data, String path) {

        Long submitId = data.getJudge().getSubmitId();
        AtomicInteger count = new AtomicInteger(0);
        String taskKey = UUID.randomUUID().toString() + submitId;

        Runnable getResultTask = () -> {
            if (count.get() > maxTryNum) {
                checkResult(null, submitId);
                releaseTaskThread(taskKey);
                return;
            }
            count.getAndIncrement();
            JudgeServer judgeServer = chooseUtils.chooseServer(false);
            if (judgeServer != null) { // 获取到判题机资源
                CommonResult result = null;
                try {
                    result = restTemplate.postForObject("http://" + judgeServer.getUrl() + path, data, CommonResult.class);
                } catch (Exception e) {
                    log.error("[Self Judge] Request the judge server [" + judgeServer.getUrl() + "] error -------------->", e);
                } finally {
                    checkResult(result, submitId);
                    releaseJudgeServer(judgeServer.getId());
                    releaseTaskThread(taskKey);
                }
            }
        };
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(getResultTask, 0, 2, TimeUnit.SECONDS);
        futureTaskMap.put(taskKey, scheduledFuture);
    }

    /**
     * 远程评测
     * @param data
     * @param path
     */
    public void remoteJudge(ToJudgeDTO data, String path) {

        String oj = data.getRemoteJudgeProblem().split("-")[0];
        if (oj.equals("GYM")) {
            oj = "CF";
        }

        // 如果是vj判題，同时不是已有提交id的获取结果操作，归属于CF的判題，需要控制判题机的权限，一机一题
        boolean isCFFixServerJudge = ChooseUtils.openCodeforcesFixServer
                && !data.getIsHasSubmitIdRemoteReJudge()
                && Constants.RemoteOJ.CODEFORCES.getName().equals(oj);

        Long submitId = data.getJudge().getSubmitId();
        AtomicInteger count = new AtomicInteger(0);
        String taskKey = UUID.randomUUID().toString() + submitId;
        final String finalOj = oj;

        Runnable getResultTask = () -> {
            if (count.get() > maxTryNum) { // 300次失败则判为提交失败
                // 远程判题需要将账号归为可用
                changeRemoteJudgeStatus(finalOj, data.getUsername(), null);
                checkResult(null, submitId);
                releaseTaskThread(taskKey);
                return;
            }
            count.getAndIncrement();
            JudgeServer judgeServer = null;
            if (!isCFFixServerJudge) {
                judgeServer = chooseUtils.chooseServer(true);
            } else {
                judgeServer = chooseUtils.chooseFixedServer(true, "cf_submittable", data.getIndex(), data.getSize());
            }
            if (judgeServer != null) { // 获取到判题机资源
                data.setJudgeServerIp(judgeServer.getIp());
                data.setJudgeServerPort(judgeServer.getPort());
                CommonResult result = null;
                try {
                    result = restTemplate.postForObject("http://" + judgeServer.getUrl() + path, data, CommonResult.class);
                } catch (Exception e) {
                    log.error("[Remote Judge] Request the judge server [" + judgeServer.getUrl() + "] error-------------->", e);
                    changeRemoteJudgeStatus(finalOj, data.getUsername(), judgeServer);
                } finally {
                    checkResult(result, submitId);
                    if (!isCFFixServerJudge) {
                        // 无论成功与否，都要将对应的当前判题机当前判题数减1
                        releaseJudgeServer(judgeServer.getId());
                    }
                    releaseTaskThread(taskKey);
                }
            }
        };
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(getResultTask, 0, 2, TimeUnit.SECONDS);
        futureTaskMap.put(taskKey, scheduledFuture);
    }

    /**
     * 在线调试
     * @param testJudgeReq
     * @param path
     */
    public void testJudge(TestJudgeReq testJudgeReq, String path) {
        AtomicInteger count = new AtomicInteger(0);
        String taskKey = testJudgeReq.getUniqueKey();
        Runnable getResultTask = () -> {
            if (count.get() > maxTryNum) { // 300次失败则判为提交失败
                releaseTaskThread(taskKey);
                return;
            }
            count.getAndIncrement();
            JudgeServer judgeServer = chooseUtils.chooseServer(false);
            if (judgeServer != null) { // 获取到判题机资源
                try {
                    String url = "http://" + judgeServer.getUrl() + path;
                    JSONObject resultJson = restTemplate.postForObject(url, testJudgeReq, JSONObject.class);
                    if (resultJson != null) {
                        if (resultJson.getInt("status") == ResultStatus.SUCCESS.getStatus()) {
                            TestJudgeRes testJudgeRes = resultJson.getBean("data", TestJudgeRes.class);
                            testJudgeRes.setInput(testJudgeReq.getTestCaseInput());
                            testJudgeRes.setExpectedOutput(testJudgeReq.getExpectedOutput());
                            testJudgeRes.setProblemJudgeMode(testJudgeReq.getProblemJudgeMode());
                            redisUtils.set(testJudgeReq.getUniqueKey(), testJudgeRes, 60);
                        } else {
                            TestJudgeRes testJudgeRes = TestJudgeRes.builder()
                                    .status(Constants.Judge.STATUS_SYSTEM_ERROR.getStatus())
                                    .time(0L)
                                    .memory(0L)
                                    .stderr(resultJson.getStr("msg"))
                                    .build();
                            redisUtils.set(testJudgeReq.getUniqueKey(), testJudgeRes, 60);
                        }
                    }
                } catch (Exception e) {
                    log.error("[Test Judge] Request the judge server [" + judgeServer.getUrl() + "] error-------------->", e);
                    TestJudgeRes testJudgeRes = TestJudgeRes.builder()
                            .status(Constants.Judge.STATUS_SYSTEM_ERROR.getStatus())
                            .time(0L)
                            .memory(0L)
                            .stderr("Failed to connect the judgeServer. Please resubmit this submission again!")
                            .build();
                    redisUtils.set(testJudgeReq.getUniqueKey(), testJudgeRes, 60);
                } finally {
                    releaseJudgeServer(judgeServer.getId());
                    releaseTaskThread(taskKey);
                }
            }
        };
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(getResultTask, 0, 1, TimeUnit.SECONDS);
        futureTaskMap.put(taskKey, scheduledFuture);
    }


    /**
     * 编译特殊判题程序或交互程序
     * @param data
     * @param path
     */
    public CommonResult compile(CompileDTO data, String path) {
        CommonResult result = CommonResult.errorResponse("没有可用的判题服务器，请重新尝试！");
        JudgeServer judgeServer = chooseUtils.chooseServer(false);
        if (judgeServer != null) {
            try {
                result = restTemplate.postForObject("http://" + judgeServer.getUrl() + path, data, CommonResult.class);
            } catch (Exception e) {
                log.error("[Compile] Request the judge server [" + judgeServer.getUrl() + "] error-------------->", e.getMessage());
            } finally {
                // 无论成功与否，都要将对应的当前判题机当前判题数减1
                releaseJudgeServer(judgeServer.getId());
            }
        }
        return result;
    }


    private void checkResult(CommonResult<Void> result, Long submitId) {

        Judge judge = new Judge();
        if (result == null) { // 调用失败
            judge.setSubmitId(submitId);
            judge.setStatus(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus());
            judge.setErrorMessage("Failed to connect the judgeServer. Please resubmit this submission again!");
            judgeEntityService.updateById(judge);
        } else {
            if (result.getStatus() != ResultStatus.SUCCESS.getStatus()) { // 如果是结果码不是200 说明调用有错误
                // 判为系统错误
                judge.setStatus(Constants.Judge.STATUS_SYSTEM_ERROR.getStatus())
                        .setErrorMessage(result.getMsg());
                judgeEntityService.updateById(judge);
            }
        }

    }

    /**
     * 成功与否，皆需移除任务，释放线程
     *
     * @param taskKey
     */
    private void releaseTaskThread(String taskKey) {
        Future future = futureTaskMap.get(taskKey);
        if (future != null) {
            boolean isCanceled = future.cancel(true);
            if (isCanceled) {
                futureTaskMap.remove(taskKey);
            }
        }
    }

    /**
     * 释放评测机资源
     *
     * @param judgeServerId
     */
    public void releaseJudgeServer(Integer judgeServerId) {
        UpdateWrapper<JudgeServer> judgeServerUpdateWrapper = new UpdateWrapper<>();
        judgeServerUpdateWrapper.setSql("task_number = task_number-1")
                .eq("id", judgeServerId);
        boolean isOk = judgeServerEntityService.update(judgeServerUpdateWrapper);
        if (!isOk) { // 重试八次
            tryAgainUpdateJudge(judgeServerUpdateWrapper);
        }
    }

    public void tryAgainUpdateJudge(UpdateWrapper<JudgeServer> updateWrapper) {
        boolean retryable;
        int attemptNumber = 0;
        do {
            boolean success = judgeServerEntityService.update(updateWrapper);
            if (success) {
                return;
            } else {
                attemptNumber++;
                retryable = attemptNumber < 8;
                if (attemptNumber == 8) {
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (retryable);
    }


    /**
     * 将远程评测的账号变为可用
     * @param oj
     * @param username
     * @param judgeServer
     */
    public void changeRemoteJudgeStatus(String oj, String username, JudgeServer judgeServer) {
        changeAccountStatus(oj, username);
        if (ChooseUtils.openCodeforcesFixServer) {
            if (oj.equals(Constants.RemoteOJ.CODEFORCES.getName())
                    || oj.equals(Constants.RemoteOJ.GYM.getName())) {
                if (judgeServer != null) {
                    changeServerSubmitCFStatus(judgeServer.getIp(), judgeServer.getPort());
                }
            }
        }
    }

    /**
     * 将远程评测的账号变为可用
     * @param remoteJudge
     * @param username
     */
    public void changeAccountStatus(String remoteJudge, String username) {

        UpdateWrapper<RemoteJudgeAccount> remoteJudgeAccountUpdateWrapper = new UpdateWrapper<>();
        remoteJudgeAccountUpdateWrapper.set("status", true)
                .eq("status", false)
                .eq("username", username);
        if (remoteJudge.equals("GYM")) {
            remoteJudge = "CF";
        }
        remoteJudgeAccountUpdateWrapper.eq("oj", remoteJudge);

        boolean isOk = remoteJudgeAccountService.update(remoteJudgeAccountUpdateWrapper);

        if (!isOk) { // 重试8次
            tryAgainUpdateAccount(remoteJudgeAccountUpdateWrapper, remoteJudge, username);
        }
    }


    private void tryAgainUpdateAccount(UpdateWrapper<RemoteJudgeAccount> updateWrapper, String remoteJudge, String username) {
        boolean retryable;
        int attemptNumber = 0;
        do {
            boolean success = remoteJudgeAccountService.update(updateWrapper);
            if (success) {
                return;
            } else {
                attemptNumber++;
                retryable = attemptNumber < 8;
                if (attemptNumber == 8) {
                    log.error("[Remote Judge] Failed to change the account to be available----------->{}", "oj:" + remoteJudge + ",username:" + username);
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (retryable);
    }

    @Deprecated
    public void changeServerSubmitCFStatus(String ip, Integer port) {

        if (StringUtils.isEmpty(ip) || port == null) {
            return;
        }
        UpdateWrapper<JudgeServer> judgeServerUpdateWrapper = new UpdateWrapper<>();
        judgeServerUpdateWrapper.set("cf_submittable", true)
                .eq("ip", ip)
                .eq("is_remote", true)
                .eq("port", port);
        boolean isOk = judgeServerEntityService.update(judgeServerUpdateWrapper);

        if (!isOk) { // 重试8次
            tryAgainUpdateServer(judgeServerUpdateWrapper, ip, port);
        }
    }

    @Deprecated
    private void tryAgainUpdateServer(UpdateWrapper<JudgeServer> updateWrapper, String ip, Integer port) {
        boolean retryable;
        int attemptNumber = 0;
        do {
            boolean success = judgeServerEntityService.update(updateWrapper);
            if (success) {
                return;
            } else {
                attemptNumber++;
                retryable = attemptNumber < 8;
                if (attemptNumber == 8) {
                    log.error("[Remote Judge] Change Codeforces Judge Server Status to `true` Failed! =======>{}", "ip:" + ip + ",port:" + port);
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (retryable);
    }
}
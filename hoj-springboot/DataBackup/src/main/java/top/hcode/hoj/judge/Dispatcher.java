package top.hcode.hoj.judge;


import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.judge.*;
import top.hcode.hoj.service.judge.impl.JudgeServerServiceImpl;
import top.hcode.hoj.service.judge.impl.JudgeServiceImpl;
import top.hcode.hoj.service.judge.impl.RemoteJudgeAccountServiceImpl;
import top.hcode.hoj.utils.Constants;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Himit_ZH
 * @Date: 2021/4/15 17:29
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class Dispatcher {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JudgeServerServiceImpl judgeServerService;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private ChooseUtils chooseUtils;

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

    private final static Map<String, Future> futureTaskMap = new ConcurrentHashMap<>(20);

    @Autowired
    private RemoteJudgeAccountServiceImpl remoteJudgeAccountService;

    public CommonResult dispatcher(String type, String path, Object data) {
        switch (type) {
            case "judge":
                ToJudge judgeData = (ToJudge) data;
                toJudge(path, judgeData, judgeData.getJudge().getSubmitId(), judgeData.getRemoteJudgeProblem() != null);
                break;
            case "compile":
                CompileSpj compileSpj = (CompileSpj) data;
                return toCompile(path, compileSpj);
            default:
                throw new NullPointerException("判题机不支持此调用类型");
        }
        return null;
    }


    public void toJudge(String path, ToJudge data, Long submitId, Boolean isRemote) {

        String oj = null;
        if (isRemote) {
            oj = data.getRemoteJudgeProblem().split("-")[0];
            if (oj.equals("GYM")) {
                oj = "CF";
            }
        }

        // 如果是vj判題，同时不是已有提交id的获取结果操作，归属于CF的判題，需要控制判题机的权限，一机一题
        boolean isCFFirstSubmit = isRemote && !data.getIsHasSubmitIdRemoteReJudge()
                && Constants.RemoteOJ.CODEFORCES.getName().equals(oj);

        // 尝试600s
        AtomicInteger count = new AtomicInteger(0);
        String key = UUID.randomUUID().toString() + data.getJudge().getSubmitId();
        final String finalOj = oj;
        Runnable getResultTask = new Runnable() {
            @Override
            public void run() {
                count.getAndIncrement();
                JudgeServer judgeServer = null;
                if (!isCFFirstSubmit) {
                    judgeServer = chooseUtils.chooseServer(isRemote);
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
                        log.error("调用判题服务器[" + judgeServer.getUrl() + "]发送异常-------------->", e);
                        if (isRemote) {
                            changeRemoteJudgeStatus(finalOj, data.getUsername(), judgeServer);
                        }
                    } finally {
                        checkResult(result, submitId);
                        if (!isCFFirstSubmit) {
                            // 无论成功与否，都要将对应的当前判题机当前判题数减1
                            reduceCurrentTaskNum(judgeServer.getId());
                        }
                        Future future = futureTaskMap.get(key);
                        if (future != null) {
                            future.cancel(true);
                            futureTaskMap.remove(key);
                        }
                    }
                    return;
                }

                if (count.get() == 300) { // 300次失败则判为提交失败
                    if (isRemote) { // 远程判题需要将账号归为可用
                        changeRemoteJudgeStatus(finalOj, data.getUsername(), null);
                    }
                    checkResult(null, submitId);
                    Future future = futureTaskMap.get(key);
                    if (future != null) {
                        future.cancel(true);
                        futureTaskMap.remove(key);
                    }
                }
            }
        };
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(getResultTask, 0, 2, TimeUnit.SECONDS);
        futureTaskMap.put(key, scheduledFuture);
    }


    public CommonResult toCompile(String path, CompileSpj data) {
        CommonResult result = CommonResult.errorResponse("没有可用的判题服务器，请重新尝试！");
        JudgeServer judgeServer = chooseUtils.chooseServer(false);
        if (judgeServer != null) {
            try {
                result = restTemplate.postForObject("http://" + judgeServer.getUrl() + path, data, CommonResult.class);
            } catch (Exception e) {
                log.error("调用判题服务器[" + judgeServer.getUrl() + "]发送异常-------------->", e.getMessage());
            } finally {
                // 无论成功与否，都要将对应的当前判题机当前判题数减1
                reduceCurrentTaskNum(judgeServer.getId());
            }
        }
        return result;
    }


    private void checkResult(CommonResult result, Long submitId) {

        Judge judge = new Judge();
        if (result == null) { // 调用失败
            judge.setSubmitId(submitId);
            judge.setStatus(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus());
            judge.setErrorMessage("Failed to connect the judgeServer. Please resubmit this submission again!");
            judgeService.updateById(judge);
        } else {
            if (result.getStatus().intValue() != CommonResult.STATUS_SUCCESS) { // 如果是结果码不是200 说明调用有错误
                // 判为系统错误
                judge.setStatus(Constants.Judge.STATUS_SYSTEM_ERROR.getStatus())
                        .setErrorMessage(result.getMsg());
                judgeService.updateById(judge);
            }
        }

    }

    public void reduceCurrentTaskNum(Integer id) {
        UpdateWrapper<JudgeServer> judgeServerUpdateWrapper = new UpdateWrapper<>();
        judgeServerUpdateWrapper.setSql("task_number = task_number-1").eq("id", id);
        boolean isOk = judgeServerService.update(judgeServerUpdateWrapper);
        if (!isOk) { // 重试八次
            tryAgainUpdateJudge(judgeServerUpdateWrapper);
        }
    }

    public void tryAgainUpdateJudge(UpdateWrapper<JudgeServer> updateWrapper) {
        boolean retryable;
        int attemptNumber = 0;
        do {
            boolean success = judgeServerService.update(updateWrapper);
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


    public void changeRemoteJudgeStatus(String oj, String username, JudgeServer judgeServer) {
        changeAccountStatus(oj, username);
        if (oj.equals(Constants.RemoteOJ.CODEFORCES.getName())
                || oj.equals(Constants.RemoteOJ.GYM.getName())) {
            if (judgeServer != null) {
                changeServerSubmitCFStatus(judgeServer.getIp(), judgeServer.getPort());
            }
        }
    }


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
                    log.error("远程判题：修正账号为可用状态失败----------->{}", "oj:" + remoteJudge + ",username:" + username);
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


    public void changeServerSubmitCFStatus(String ip, Integer port) {

        if (StringUtils.isEmpty(ip) || port == null) {
            return;
        }
        UpdateWrapper<JudgeServer> judgeServerUpdateWrapper = new UpdateWrapper<>();
        judgeServerUpdateWrapper.set("cf_submittable", true)
                .eq("ip", ip)
                .eq("is_remote", true)
                .eq("port", port);
        boolean isOk = judgeServerService.update(judgeServerUpdateWrapper);

        if (!isOk) { // 重试8次
            tryAgainUpdateServer(judgeServerUpdateWrapper, ip, port);
        }
    }

    private void tryAgainUpdateServer(UpdateWrapper<JudgeServer> updateWrapper, String ip, Integer port) {
        boolean retryable;
        int attemptNumber = 0;
        do {
            boolean success = judgeServerService.update(updateWrapper);
            if (success) {
                return;
            } else {
                attemptNumber++;
                retryable = attemptNumber < 8;
                if (attemptNumber == 8) {
                    log.error("Remote Judge：Change CF Judge Server Status to `true` Failed! =======>{}", "ip:" + ip + ",port:" + port);
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
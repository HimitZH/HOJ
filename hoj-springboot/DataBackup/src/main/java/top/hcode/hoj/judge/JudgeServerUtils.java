package top.hcode.hoj.judge;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.*;
import top.hcode.hoj.service.impl.JudgeServerServiceImpl;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.service.impl.RemoteJudgeAccountServiceImpl;
import top.hcode.hoj.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Himit_ZH
 * @Date: 2021/4/15 17:29
 * @Description:
 */
@Component
@Slf4j(topic = "hoj")
public class JudgeServerUtils {

    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    @Value("${service-url.name}")
    private String JudgeServiceName;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JudgeServerServiceImpl judgeServerService;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private RemoteJudgeAccountServiceImpl remoteJudgeAccountService;

    public CommonResult dispatcher(String type, String path, Object data) {
        switch (type) {
            case "judge":
                ToJudge judgeData = (ToJudge) data;
                toJudge(path, (ToJudge) data, judgeData.getJudge().getSubmitId(), judgeData.getRemoteJudge() != null);
                break;
            case "compile":
                CompileSpj compileSpj = (CompileSpj) data;
                return toCompile(path, compileSpj);
            default:
                throw new NullPointerException("判题机不支持此调用类型");
        }
        return null;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void toJudge(String path, ToJudge data, Long submitId, Boolean isRemote) {
        // 尝试30s
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        AtomicInteger count = new AtomicInteger(0);
        Runnable getResultTask = new Runnable() {
            @Override
            public void run() {
                count.getAndIncrement();
                JudgeServer judgeServer = chooseServer(isRemote);
                if (count.get() == 30) { // 30次失败则判为提交失败
                    if (isRemote) { // 远程判题需要将账号归为可用
                        UpdateWrapper<RemoteJudgeAccount> remoteJudgeAccountUpdateWrapper = new UpdateWrapper<>();
                        remoteJudgeAccountUpdateWrapper
                                .eq("username", data.getUsername())
                                .eq("password", data.getPassword())
                                .set("status", true);
                        remoteJudgeAccountService.update(remoteJudgeAccountUpdateWrapper);
                    }
                    checkResult(null, submitId);
                    scheduler.shutdown();
                }
                if (judgeServer != null) { // 获取到判题机资源
                    CommonResult result = null;
                    try {
                        result = restTemplate.postForObject("http://" + judgeServer.getUrl() + path, data, CommonResult.class);
                    } catch (Exception e) {
                        log.error("调用判题服务器[" + judgeServer.getUrl() + "]发送异常-------------->{}", e.getMessage());
                    } finally {
                        checkResult(result, submitId);
                        // 无论成功与否，都要将对应的当前判题机当前判题数减1
                        reduceCurrentTaskNum(judgeServer.getId());
                        scheduler.shutdown();
                    }
                }
            }
        };
        scheduler.scheduleAtFixedRate(getResultTask, 0, 1, TimeUnit.SECONDS);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CommonResult toCompile(String path, CompileSpj data) {
        CommonResult result = CommonResult.errorResponse("没有可用的判题服务器，请重新尝试！");
        JudgeServer judgeServer = chooseServer(false);
        if (judgeServer != null) {
            try {
                result = restTemplate.postForObject("http://" + judgeServer.getUrl() + path, data, CommonResult.class);
            } catch (Exception e) {
                log.error("调用判题服务器[" + judgeServer.getUrl() + "]发送异常-------------->{}", e.getMessage());
            } finally {
                // 无论成功与否，都要将对应的当前判题机当前判题数减1
                reduceCurrentTaskNum(judgeServer.getId());
            }
        }
        return result;
    }

    /**
     * @param
     * @MethodName chooseServer
     * @Description 选择可以用调用判题的判题服务器
     * @Return
     * @Since 2021/4/15
     */
    public JudgeServer chooseServer(Boolean isRemote) {
        // 获取该微服务的所有健康实例
        List<Instance> instances = getInstances(JudgeServiceName);
        if (instances.size() <= 0) {
            return null;
        }
        List<String> keyList = new ArrayList<>();
        // 获取当前健康实例取出ip和port拼接
        for (Instance instance : instances) {
            keyList.add(instance.getIp() + ":" + instance.getPort());
        }
        // 过滤出小于或等于规定最大并发判题任务数的服务实例且健康的判题机
        QueryWrapper<JudgeServer> judgeServerQueryWrapper = new QueryWrapper<>();
        judgeServerQueryWrapper
                .in("url", keyList)
                .eq("is_remote", isRemote)
                .orderByAsc("task_number");
        List<JudgeServer> judgeServerList = judgeServerService.list(judgeServerQueryWrapper);
        // 使用乐观锁获取可用判题机
        for (JudgeServer judgeServer : judgeServerList) {
            if (judgeServer.getTaskNumber() < judgeServer.getMaxTaskNumber()) {
                judgeServer.setTaskNumber(judgeServer.getTaskNumber() + 1);
                boolean isOk = judgeServerService.updateById(judgeServer);
                if (isOk) {
                    return judgeServer;
                }
            }
        }
        return null;
    }


    /**
     * @param serviceId
     * @MethodName getInstances
     * @Description 根据服务id获取对应的健康实例列表
     * @Return
     * @Since 2021/4/15
     */
    private List<Instance> getInstances(String serviceId) {
        // 获取服务发现的相关API
        NamingService namingService = discoveryProperties.namingServiceInstance();
        try {
            // 获取该微服务的所有健康实例
            return namingService.selectInstances(serviceId, true);
        } catch (NacosException e) {
            log.error("获取微服务健康实例发生异常--------->{}", e);
            return Collections.emptyList();
        }
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

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void reduceCurrentTaskNum(Integer id) {
        UpdateWrapper<JudgeServer> judgeServerUpdateWrapper = new UpdateWrapper<>();
        judgeServerUpdateWrapper.setSql("task_number = task_number-1").eq("id", id);
        boolean isOk = judgeServerService.update(judgeServerUpdateWrapper);
        if (!isOk) { // 重试八次
            tryAgainUpdate(judgeServerUpdateWrapper);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void tryAgainUpdate(UpdateWrapper<JudgeServer> updateWrapper) {
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
}
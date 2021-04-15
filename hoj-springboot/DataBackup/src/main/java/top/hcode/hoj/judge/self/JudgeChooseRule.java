package top.hcode.hoj.judge.self;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.ribbon.ExtendBalancer;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.hcode.hoj.pojo.entity.JudgeServer;
import top.hcode.hoj.service.impl.JudgeServerServiceImpl;
import top.hcode.hoj.utils.RedisUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/4 16:44
 * @Description: 任务调度的自定义负载均衡策略
 */
@Slf4j

public class JudgeChooseRule extends AbstractLoadBalancerRule {

    @Autowired
    private NacosDiscoveryProperties discoveryProperties;


    private JudgeServerServiceImpl judgeServerService ;


    @Autowired
    public void setJudgeServerService (JudgeServerServiceImpl judgeServerService){
        this.judgeServerService =  judgeServerService;
    }


    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object key) {
//        // 获取配置文件中所配置的集群名称
//        String clusterName = discoveryProperties.getClusterName();
        DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
        // 需要请求的微服务名称
        String serviceId = loadBalancer.getName();
        // 获取该微服务的所有健康实例
        List<Instance> instances = getInstances(serviceId);
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
                .orderByAsc("task_num");
        List<JudgeServer> judgeServerList = judgeServerService.list(judgeServerQueryWrapper);
        System.out.println(judgeServerList);
        // 使用乐观锁获取可用判题机
        for (JudgeServer judgeServer : judgeServerList) {
            if (judgeServer.getTaskNumber() <= judgeServer.getMaxTaskNumber()) {
                judgeServer.setTaskNumber(judgeServer.getTaskNumber() + 1);
                boolean isOk = judgeServerService.updateById(judgeServer);
                if (isOk) {
                    int instanceIndex = keyList.indexOf(judgeServer.getIp() + ":" + judgeServer.getPort());
                    if (instanceIndex != -1) {
                        return new NacosServer(instances.get(instanceIndex));
                    }
                }
            }
        }

        return null;
    }


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

}
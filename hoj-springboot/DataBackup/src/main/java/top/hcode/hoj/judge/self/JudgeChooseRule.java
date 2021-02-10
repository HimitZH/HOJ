package top.hcode.hoj.judge.self;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.ribbon.ExtendBalancer;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
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


    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object key) {
        // 获取配置文件中所配置的集群名称
        String clusterName = discoveryProperties.getClusterName();

        DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
        // 需要请求的微服务名称
        String serviceId = loadBalancer.getName();
        // 获取该微服务的所有健康实例
        List<Instance> instances = getInstances(serviceId);
        System.out.println(instances);
        // 进行匹配筛选的实例列表
        List<Instance> metadataMatchInstances;
        // 过滤出小于或等于规定最大并发判题任务数的服务实例
        metadataMatchInstances = instances.stream()
                .filter(instance ->
                    Integer.parseInt(instance.getMetadata().getOrDefault("currentTaskNum", "0"))<=
                            Integer.parseInt(instance.getMetadata().getOrDefault("maxTaskNum","4"))
                ).collect(Collectors.toList());
        // 如果为空闲判题服务器的数量为空，则该判题请求重新进入等待队列
        if (CollectionUtils.isEmpty(metadataMatchInstances)) {
            return null;
        }

        // 基于随机权重的负载均衡算法，选取其中一个实例
        Instance instance = ExtendBalancer.getHostByRandomWeight2(metadataMatchInstances);
        return new NacosServer(instance);
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
package top.hcode.hoj.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/29 22:49
 * @Description:
 */
@Configuration
public class CloudConfig {
    @Bean
    @LoadBalanced //设置实现负载均衡，Ribbon
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}

package top.hcode.hoj.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provide a minimal NacosDiscoveryProperties bean when discovery is disabled,
 * so components that autowire it won't fail during local development.
 */
@Configuration
@ConditionalOnClass(NacosDiscoveryProperties.class)
@ConditionalOnMissingBean(NacosDiscoveryProperties.class)
@ConditionalOnProperty(prefix = "spring.cloud.nacos.discovery", name = "enabled", havingValue = "false", matchIfMissing = false)
public class LocalNacosDiscoveryStubConfig {

    @Bean
    public NacosDiscoveryProperties nacosDiscoveryProperties() {
        // Use default instance; fields can remain unset in local/no-nacos mode.
        return new NacosDiscoveryProperties();
    }
}

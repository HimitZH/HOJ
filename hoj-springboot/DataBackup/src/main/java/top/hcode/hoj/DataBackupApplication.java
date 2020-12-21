package top.hcode.hoj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/22 23:25
 * @Description:
 */
@EnableDiscoveryClient // 开启注册发现
@SpringBootApplication
@EnableFeignClients // 开启feign
@EnableAsync(proxyTargetClass=true) //开启异步注解
public class DataBackupApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataBackupApplication.class,args);
    }
}
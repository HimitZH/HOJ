package top.hcode.hoj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/29 22:12
 * @Description: 判题机服务系统启动类
 */
@EnableDiscoveryClient
@SpringBootApplication
public class JudgeServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(JudgeServerApplication.class,args);
    }
}
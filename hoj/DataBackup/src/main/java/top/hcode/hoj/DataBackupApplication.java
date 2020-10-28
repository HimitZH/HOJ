package top.hcode.hoj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Author: Himit_ZH
 * @Date: 2020/10/22 23:25
 * @Description:
 */
//@EnableDiscoveryClient
@SpringBootApplication
//@EnableFeignClients
@EnableAsync //开启异步注解
public class DataBackupApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataBackupApplication.class,args);
    }
}
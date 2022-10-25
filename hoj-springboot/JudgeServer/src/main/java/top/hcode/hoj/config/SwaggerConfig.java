package top.hcode.hoj.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;

/**
 * @Author: Himit_ZH
 * @Date: 2020/5/29 22:28
 * @Description:
 */
@Configuration
@EnableSwagger2 // 开启swagger2
@Profile({"dev", "test"}) // 只允许开发环境访问
public class SwaggerConfig {
    @Bean //配置swagger的docket的bean势力
    public Docket docket(Environment environment) {
        //设置要显示的swagger环境
        Profiles profiles = Profiles.of("dev", "test"); //线下环境
        //通过环境判断是否在自己所设定的环境当中
        boolean flag = environment.acceptsProfiles(profiles);
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .groupName("hoj") //分组
                .enable(flag) //开启
                .select()
                //RequestHandlerSelectors扫描方式
                //any()全部
                //none 都不扫描
                //path 过滤什么路径
                .apis(RequestHandlerSelectors.basePackage("top.hcode"))
                .build();
    }

    //配置swagger信息
    private ApiInfo apiInfo() {
        //作者信息
        Contact contact = new Contact("Himit_ZH",
                "https://docs.hdoi.cn",
                "372347736@qq.com");
        return new ApiInfo(
                "HOJ-JudgeServer的API文档",
                "HCODE ONLINE JUDGE(HOJ)的判题端接口文档",
                "v4.4",
                "https://docs.hdoi.cn",
                contact,
                "MIT",
                "http://www.apache.org/licenses/LICENSE-2.0",
                new ArrayList());
    }
}
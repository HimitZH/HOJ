package top.hcode.hoj.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author Himit_ZH
 * @Date 2022/10/26
 */
@Data
@Configuration
@Slf4j(topic = "hoj")
public class NacosSwitchConfig {

    @Value("${spring.cloud.nacos.url}")
    private String NACOS_URL;

    @Value("${spring.cloud.nacos.config.username}")
    private String nacosUsername;

    @Value("${spring.cloud.nacos.config.password}")
    private String nacosPassword;

    @Value("${nacos-switch-config}")
    private String switchConfigFileName;

    @Value("${nacos-web-config}")
    private String webConfigFileName;

    @Value("${spring.cloud.nacos.config.group}")
    private String group;

    private static AtomicBoolean init = new AtomicBoolean(false);

    private static ConfigService configService;

    private static SwitchConfig switchConfig;

    private static WebConfig webConfig;

    @PostConstruct
    public void init() throws NacosException {
        if (init.compareAndSet(false, true)) {
            Properties properties = new Properties();
            properties.put("serverAddr", NACOS_URL);
            // if need username and password to login
            properties.put("username", nacosUsername);
            properties.put("password", nacosPassword);

            configService = NacosFactory.createConfigService(properties);

            refreshSwitchConfig(configService.getConfig(switchConfigFileName, group, 6000));

            configService.addListener(switchConfigFileName, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    refreshSwitchConfig(configInfo);
                }
            });
            log.info("[Switch Config] [Init Succeeded] [{}]", getSwitchConfig());


            refreshWebConfig(configService.getConfig(webConfigFileName, group, 6000));
            configService.addListener(webConfigFileName, group, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    refreshWebConfig(configInfo);
                }
            });
            log.info("[Web Config] [Init Succeeded] [{}]", getWebConfig());
        }
    }

    private static void refreshSwitchConfig(String config) {
        if (StrUtil.isBlank(config)) {
            switchConfig = new SwitchConfig();
        } else {
            try {
                Yaml yaml = new Yaml();
                switchConfig = yaml.loadAs(config, SwitchConfig.class);
                switchConfig.convertUnicodeRemoteAccount2Str();
            } catch (Exception e) {
                log.error("[Nacos Config] refresh switch config error:{}, config:{}", e, config);
            }
        }
    }

    public SwitchConfig getSwitchConfig() {
        return switchConfig;
    }

    private static void refreshWebConfig(String config) {
        if (StrUtil.isBlank(config)) {
            webConfig = new WebConfig();
        } else {
            try {
                Yaml yaml = new Yaml();
                webConfig = yaml.loadAs(config, WebConfig.class);
            } catch (Exception e) {
                log.error("[Nacos Config] refresh web config error:{}, config:{}", e, config);
            }
        }
    }

    public WebConfig getWebConfig() {
        return webConfig;
    }

    public boolean publishSwitchConfig() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        Yaml yaml = new Yaml(options);
        SwitchConfig switchConfig = BeanUtil.copyProperties(getSwitchConfig(), SwitchConfig.class);
        switchConfig.formatStrRemoteAccount2Unicode();
        String content = yaml.dumpAsMap(switchConfig);
        try {
            return configService.publishConfig(switchConfigFileName, group, content, ConfigType.YAML.getType());
        } catch (NacosException e) {
            log.error("[Nacos Config] publish switch config error:{}, config:{}", e, content);
            return false;
        }
    }

    public boolean publishWebConfig() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        Yaml yaml = new Yaml(options);
        String content = yaml.dumpAsMap(getWebConfig());
        try {
            return configService.publishConfig(webConfigFileName, group, content, ConfigType.YAML.getType());
        } catch (NacosException e) {
            log.error("[Nacos Config] publish web config error:{}, config:{}", e, content);
            return false;
        }
    }

}

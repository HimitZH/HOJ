package top.hcode.hoj.service.impl;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.pojo.vo.ConfigVo;
import top.hcode.hoj.service.ConfigService;
import top.hcode.hoj.utils.ConfigUtils;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/3 11:03
 * @Description: 动态修改网站配置，获取后台服务状态及判题服务器的状态
 */
@Service
@Slf4j(topic = "hoj")
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigUtils configUtils;

    @Autowired
    private ConfigVo configVo;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${service-url.name}")
    private String judgeServiceName;

    @Value("${spring.application.name}")
    private String currentServiceName;

    @Value("${spring.cloud.nacos.url}")
    private String NACOS_URL;

    @Value("${spring.cloud.nacos.config.prefix}")
    private String prefix;

    @Value("${spring.profiles.active}")
    private String active;

    @Value("${spring.cloud.nacos.config.file-extension}")
    private String fileExtension;

    @Value("${spring.cloud.nacos.config.group}")
    private String GROUP;

    @Value("${spring.cloud.nacos.config.type}")
    private String TYPE;

    @Value("${spring.cloud.nacos.config.username}")
    private String nacosUsername;

    @Value("${spring.cloud.nacos.config.password}")
    private String nacosPassword;

    private OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    @Override
    public JSONObject getServiceInfo() {

        JSONObject result = new JSONObject();

        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(currentServiceName);

        // 获取nacos中心配置所在的机器环境
        String response = restTemplate.getForObject(NACOS_URL + "/nacos/v1/ns/operator/metrics", String.class);

        JSONObject jsonObject = JSONUtil.parseObj(response);
        // 获取当前数据后台所在机器环境
        int cores = Runtime.getRuntime().availableProcessors(); // 当前机器的cpu核数
        double cpuLoad = osmxb.getSystemCpuLoad();
        String percentCpuLoad = String.format("%.2f", cpuLoad * 100) + "%"; // 当前服务所在机器cpu使用率

        double totalVirtualMemory = osmxb.getTotalPhysicalMemorySize(); // 当前服务所在机器总内存
        double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize(); // 当前服务所在机器空闲内存
        double value = freePhysicalMemorySize / totalVirtualMemory;
        String percentMemoryLoad = String.format("%.2f", (1 - value) * 100) + "%"; // 当前服务所在机器内存使用率

        result.put("nacos", jsonObject);
        result.put("backupCores", cores);
        result.put("backupService", serviceInstances);
        result.put("backupPercentCpuLoad", percentCpuLoad);
        result.put("backupPercentMemoryLoad", percentMemoryLoad);
        return result;
    }

    @Override
    public List<JSONObject> getJudgeServiceInfo() {
        List<JSONObject> serviceInfoList = new LinkedList<>();
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(judgeServiceName);
        for (ServiceInstance serviceInstance : serviceInstances) {
            String result = restTemplate.getForObject(serviceInstance.getUri() + "/get-sys-config", String.class);
            JSONObject jsonObject = JSONUtil.parseObj(result);
            jsonObject.put("service", serviceInstance);
            serviceInfoList.add(jsonObject);
        }
        return serviceInfoList;
    }

    @Override
    public boolean setEmailConfig(HashMap<String, Object> params) {
        if (!StringUtils.isEmpty(params.get("emailHost"))) {
            configVo.setEmailHost((String) params.get("emailHost"));
        }
        if (!StringUtils.isEmpty(params.get("emailPassword"))) {
            configVo.setEmailPassword((String) params.get("emailPassword"));
        }
        if (!StringUtils.isEmpty(params.get("emailPort"))) {
            configVo.setEmailPort((Integer) params.get("emailPort"));
        }

        if (!StringUtils.isEmpty(params.get("emailUsername"))) {
            configVo.setEmailUsername((String) params.get("emailUsername"));
        }

        if (!StringUtils.isEmpty(params.get("emailBGImg"))) {
            configVo.setEmailBGImg((String) params.get("emailBGImg"));
        }

        if (params.get("emailSsl") != null) {
            configVo.setEmailSsl((Boolean) params.get("emailSsl"));
        }
        return sendNewConfigToNacos();
    }

    @Override
    public boolean setWebConfig(HashMap<String, Object> params) {
        if (!StringUtils.isEmpty(params.get("baseUrl"))) {
            configVo.setBaseUrl((String) params.get("baseUrl"));
        }
        if (!StringUtils.isEmpty(params.get("name"))) {
            configVo.setName((String) params.get("name"));
        }
        if (!StringUtils.isEmpty(params.get("shortName"))) {
            configVo.setShortName((String) params.get("shortName"));
        }
        if (params.get("register") != null) {
            configVo.setRegister((Boolean) params.get("register"));
        }
        if (!StringUtils.isEmpty(params.get("recordName"))) {
            configVo.setRecordName((String) params.get("recordName"));
        }
        if (!StringUtils.isEmpty(params.get("recordUrl"))) {
            configVo.setRecordUrl((String) params.get("recordUrl"));
        }
        if (!StringUtils.isEmpty(params.get("projectName"))) {
            configVo.setProjectName((String) params.get("projectName"));
        }
        if (!StringUtils.isEmpty(params.get("projectUrl"))) {
            configVo.setProjectUrl((String) params.get("projectUrl"));
        }
        return sendNewConfigToNacos();
    }

    @Override
    public boolean setDBAndRedisConfig(HashMap<String, Object> params) {
        if (!StringUtils.isEmpty(params.get("dbName"))) {
            configVo.setMysqlDBName((String) params.get("dbName"));
        }
        if (!StringUtils.isEmpty(params.get("dbName"))) {
            configVo.setMysqlHost((String) params.get("dbHost"));
        }
        if (params.get("dbPort") != null) {
            configVo.setMysqlPort((Integer) params.get("dbPort"));
        }
        if (!StringUtils.isEmpty(params.get("dbUsername"))) {
            configVo.setMysqlUsername((String) params.get("dbUsername"));
        }
        if (!StringUtils.isEmpty(params.get("dbPassword"))) {
            configVo.setMysqlPassword((String) params.get("dbPassword"));
        }
        if (!StringUtils.isEmpty(params.get("redisHost"))) {
            configVo.setRedisHost((String) params.get("redisHost"));
        }
        if (params.get("redisPort") != null) {
            configVo.setRedisPort((Integer) params.get("redisPort"));
        }
        if (params.get("redisPassword") != null) {
            configVo.setRedisPassword((String) params.get("redisPassword"));
        }
        return sendNewConfigToNacos();
    }


    public boolean sendNewConfigToNacos() {

        Properties properties = new Properties();
        properties.put("serverAddr", NACOS_URL);

        // if need username and password to login
        properties.put("username", nacosUsername);
        properties.put("password", nacosPassword);

        com.alibaba.nacos.api.config.ConfigService configService = null;
        boolean isOK = false;
        try {
            configService = NacosFactory.createConfigService(properties);
            isOK = configService.publishConfig(prefix + "-" + active + "." + fileExtension, GROUP, configUtils.getConfigContent(), TYPE);
        } catch (NacosException e) {
            log.error("通过nacos修改网站配置异常--------------->{}", e.getMessage());
        }
        return isOK;
    }
}
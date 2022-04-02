package top.hcode.hoj.manager.admin.system;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.oshi.OshiUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.manager.email.EmailManager;
import top.hcode.hoj.pojo.entity.admin.DBAndRedisConfig;
import top.hcode.hoj.pojo.entity.admin.EmailConfig;
import top.hcode.hoj.pojo.entity.admin.WebConfig;
import top.hcode.hoj.pojo.entity.common.File;
import top.hcode.hoj.pojo.vo.ConfigVo;
import top.hcode.hoj.dao.common.FileEntityService;
import top.hcode.hoj.utils.ConfigUtils;

import java.util.*;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 21:50
 * @Description: 动态修改网站配置，获取后台服务状态及判题服务器的状态
 */

@Component
@Slf4j(topic = "hoj")
public class ConfigManager {
    @Autowired
    private ConfigVo configVo;

    @Autowired
    private EmailManager emailManager;

    @Autowired
    private FileEntityService fileEntityService;

    @Autowired
    private ConfigUtils configUtils;

    @Autowired
    private RestTemplate restTemplate;

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

    /**
     * @MethodName getServiceInfo
     * @Params * @param null
     * @Description 获取当前服务的相关信息以及当前系统的cpu情况，内存使用情况
     * @Return CommonResult
     * @Since 2020/12/3
     */

    public JSONObject getServiceInfo() {

        JSONObject result = new JSONObject();

        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(currentServiceName);

        // 获取nacos中心配置所在的机器环境
        String response = restTemplate.getForObject(NACOS_URL + "/nacos/v1/ns/operator/metrics", String.class);

        JSONObject jsonObject = JSONUtil.parseObj(response);
        // 获取当前数据后台所在机器环境
        int cores = OshiUtil.getCpuInfo().getCpuNum(); // 当前机器的cpu核数
        double cpuLoad = 100 - OshiUtil.getCpuInfo().getFree();
        String percentCpuLoad = String.format("%.2f", cpuLoad) + "%"; // 当前服务所在机器cpu使用率

        double totalVirtualMemory = OshiUtil.getMemory().getTotal(); // 当前服务所在机器总内存
        double freePhysicalMemorySize = OshiUtil.getMemory().getAvailable(); // 当前服务所在机器空闲内存
        double value = freePhysicalMemorySize / totalVirtualMemory;
        String percentMemoryLoad = String.format("%.2f", (1 - value) * 100) + "%"; // 当前服务所在机器内存使用率

        result.put("nacos", jsonObject);
        result.put("backupCores", cores);
        result.put("backupService", serviceInstances);
        result.put("backupPercentCpuLoad", percentCpuLoad);
        result.put("backupPercentMemoryLoad", percentMemoryLoad);
        return result;
    }

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


    public Map<Object, Object> getWebConfig() {
        return MapUtil.builder().put("baseUrl", UnicodeUtil.toString(configVo.getBaseUrl()))
                .put("name", UnicodeUtil.toString(configVo.getName()))
                .put("shortName", UnicodeUtil.toString(configVo.getShortName()))
                .put("description", UnicodeUtil.toString(configVo.getDescription()))
                .put("register", configVo.getRegister())
                .put("recordName", UnicodeUtil.toString(configVo.getRecordName()))
                .put("recordUrl", UnicodeUtil.toString(configVo.getRecordUrl()))
                .put("projectName", UnicodeUtil.toString(configVo.getProjectName()))
                .put("projectUrl", UnicodeUtil.toString(configVo.getProjectUrl())).map();
    }


    public void deleteHomeCarousel(Long id) throws StatusFailException {

        File imgFile = fileEntityService.getById(id);
        if (imgFile == null) {
            throw new StatusFailException("文件id错误，图片不存在");
        }
        boolean isOk = fileEntityService.removeById(id);
        if (isOk) {
            FileUtil.del(imgFile.getFilePath());
        } else {
            throw new StatusFailException("删除失败！");
        }
    }

    public void setWebConfig(WebConfig config) throws StatusFailException {

        if (!StringUtils.isEmpty(config.getBaseUrl())) {
            configVo.setBaseUrl(config.getBaseUrl());
        }
        if (!StringUtils.isEmpty(config.getName())) {
            configVo.setName(config.getName());
        }
        if (!StringUtils.isEmpty(config.getShortName())) {
            configVo.setShortName(config.getShortName());
        }
        if (!StringUtils.isEmpty(config.getDescription())) {
            configVo.setDescription(config.getDescription());
        }
        if (config.getRegister() != null) {
            configVo.setRegister(config.getRegister());
        }
        if (!StringUtils.isEmpty(config.getRecordName())) {
            configVo.setRecordName(config.getRecordName());
        }
        if (!StringUtils.isEmpty(config.getRecordUrl())) {
            configVo.setRecordUrl(config.getRecordUrl());
        }
        if (!StringUtils.isEmpty(config.getProjectName())) {
            configVo.setProjectName(config.getProjectName());
        }
        if (!StringUtils.isEmpty(config.getProjectUrl())) {
            configVo.setProjectUrl(config.getProjectUrl());
        }
        boolean isOk = sendNewConfigToNacos();
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    public Map<Object, Object> getEmailConfig() {
        return MapUtil.builder().put("emailUsername", configVo.getEmailUsername())
                .put("emailPassword", configVo.getEmailPassword())
                .put("emailHost", configVo.getEmailHost())
                .put("emailPort", configVo.getEmailPort())
                .put("emailBGImg", configVo.getEmailBGImg())
                .put("emailSsl", configVo.getEmailSsl()).map();
    }

    public void setEmailConfig(EmailConfig config) throws StatusFailException {

        if (!StringUtils.isEmpty(config.getEmailHost())) {
            configVo.setEmailHost(config.getEmailHost());
        }
        if (!StringUtils.isEmpty(config.getEmailPassword())) {
            configVo.setEmailPassword(config.getEmailPassword());
        }
        if (config.getEmailPort() != null && config.getEmailPort() >= 0) {
            configVo.setEmailPort(config.getEmailPort());
        }

        if (!StringUtils.isEmpty(config.getEmailUsername())) {
            configVo.setEmailUsername(config.getEmailUsername());
        }

        if (!StringUtils.isEmpty(config.getEmailBGImg())) {
            configVo.setEmailBGImg(config.getEmailBGImg());
        }

        if (config.getEmailSsl() != null) {
            configVo.setEmailSsl(config.getEmailSsl());
        }

        boolean isOk = sendNewConfigToNacos();
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }


    public void testEmail(String email) throws StatusFailException {
        boolean isEmail = Validator.isEmail(email);
        if (isEmail) {
            emailManager.testEmail(email);
        } else {
            throw new StatusFailException("测试的邮箱格式不正确！");
        }
    }

    public Map<Object, Object> getDBAndRedisConfig() {
        return MapUtil.builder().put("dbName", configVo.getMysqlDBName())
                .put("dbHost", configVo.getMysqlHost())
                .put("dbPost", configVo.getMysqlPort())
                .put("dbUsername", configVo.getMysqlUsername())
                .put("dbPassword", configVo.getMysqlPassword())
                .put("redisHost", configVo.getRedisHost())
                .put("redisPort", configVo.getRedisPort())
                .put("redisPassword", configVo.getRedisPassword())
                .map();
    }


    public void setDBAndRedisConfig(DBAndRedisConfig config) throws StatusFailException {

        if (!StringUtils.isEmpty(config.getDbName())) {
            configVo.setMysqlDBName(config.getDbName());
        }
        if (!StringUtils.isEmpty(config.getDbHost())) {
            configVo.setMysqlHost(config.getDbHost());
        }
        if (config.getDbPort() != null && config.getDbPort() >= 0) {
            configVo.setMysqlPort(config.getDbPort());
        }
        if (!StringUtils.isEmpty(config.getDbUsername())) {
            configVo.setMysqlUsername(config.getDbUsername());
        }
        if (!StringUtils.isEmpty(config.getDbPassword())) {
            configVo.setMysqlPassword(config.getDbPassword());
        }

        if (!StringUtils.isEmpty(config.getRedisHost())) {
            configVo.setRedisHost(config.getRedisHost());
        }
        if (config.getRedisPort() != null && config.getRedisPort() >= 0) {
            configVo.setRedisPort(config.getRedisPort());
        }
        if (!StringUtils.isEmpty(config.getRedisPassword())) {
            configVo.setRedisPassword(config.getRedisPassword());
        }

        boolean isOk = sendNewConfigToNacos();

        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
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
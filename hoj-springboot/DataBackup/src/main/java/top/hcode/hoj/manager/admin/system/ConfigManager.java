package top.hcode.hoj.manager.admin.system;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.NumberUtil;
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

    public void setWebConfig(HashMap<String, Object> params) throws StatusFailException {

        if (!StringUtils.isEmpty(params.get("baseUrl"))) {
            configVo.setBaseUrl((String) params.get("baseUrl"));
        }
        if (!StringUtils.isEmpty(params.get("name"))) {
            configVo.setName((String) params.get("name"));
        }
        if (!StringUtils.isEmpty(params.get("shortName"))) {
            configVo.setShortName((String) params.get("shortName"));
        }
        if (!StringUtils.isEmpty(params.get("description"))) {
            configVo.setDescription((String) params.get("description"));
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


    public void setEmailConfig(HashMap<String, Object> params) throws StatusFailException {

        if (!StringUtils.isEmpty(params.get("emailHost"))) {
            configVo.setEmailHost((String) params.get("emailHost"));
        }
        if (!StringUtils.isEmpty(params.get("emailPassword"))) {
            configVo.setEmailPassword((String) params.get("emailPassword"));
        }
        Object emailPort = params.get("emailPort");
        if (emailPort != null) {
            if (emailPort.getClass().equals(String.class)) {
                String emailPortStr = (String) emailPort;
                if (NumberUtil.isInteger(emailPortStr)) {
                    configVo.setEmailPort(Integer.parseInt(emailPortStr));
                } else {
                    throw new StatusFailException("email port must be number!");
                }
            } else {
                configVo.setEmailPort((Integer) emailPort);
            }
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
        boolean isOk = sendNewConfigToNacos();
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }


    public void testEmail(HashMap<String, Object> params) throws StatusFailException {
        String email = (String) params.get("email");
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


    public void setDBAndRedisConfig(HashMap<String, Object> params) throws StatusFailException {

        if (!StringUtils.isEmpty(params.get("dbName"))) {
            configVo.setMysqlDBName((String) params.get("dbName"));
        }
        if (!StringUtils.isEmpty(params.get("dbHost"))) {
            configVo.setMysqlHost((String) params.get("dbHost"));
        }
        Object dbPort = params.get("dbPort");
        if (dbPort != null) {
            if (dbPort.getClass().equals(String.class)) {
                String dbPortStr = (String) dbPort;
                if (NumberUtil.isInteger(dbPortStr)) {
                    configVo.setMysqlPort(Integer.parseInt(dbPortStr));
                } else {
                    throw new StatusFailException("db port must be number!");
                }
            } else {
                configVo.setMysqlPort((Integer) dbPort);
            }
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
        Object redisPort = params.get("redisPort");
        if (redisPort != null) {
            if (redisPort.getClass().equals(String.class)) {
                String redisPortStr = (String) redisPort;
                if (NumberUtil.isInteger(redisPortStr)) {
                    configVo.setRedisPort(Integer.parseInt(redisPortStr));
                } else {
                    throw new StatusFailException("redis port must be number!");
                }
            } else {
                configVo.setRedisPort((Integer) redisPort);
            }
        }
        if (params.get("redisPassword") != null) {
            configVo.setRedisPassword((String) params.get("redisPassword"));
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
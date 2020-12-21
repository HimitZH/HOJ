package top.hcode.hoj.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.pojo.vo.ConfigVo;
import top.hcode.hoj.service.ConfigService;
import top.hcode.hoj.utils.ConfigUtils;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/3 11:03
 * @Description:
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigUtils configUtils;

    @Autowired
    private ConfigVo configVo;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    private static final String NACOS_URL = "http://localhost:8848";
    private static final String DATA_ID = "hoj-data-backup-dev.yml";
    private static final String GROUP = "DEFAULT_GROUP";
    private static final String TYPE = "yaml";
    private static OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    @Override
    public JSONObject getServiceInfo() {

        JSONObject result = new JSONObject();

        List<ServiceInstance> serviceInstances = discoveryClient.getInstances("hoj-data-backup");

        // 获取nacos中心配置所在的机器环境
        String response = restTemplate.getForObject(NACOS_URL+"/nacos/v1/ns/operator/metrics", String.class);

        JSONObject jsonObject = JSONObject.parseObject(response);
        // 获取当前数据后台所在机器环境
        int cores = Runtime.getRuntime().availableProcessors(); // 当前机器的cpu核数
        double cpuLoad = osmxb.getSystemCpuLoad();
        String percentCpuLoad = String.format("%.2f", cpuLoad * 100)+"%"; // 当前服务所在机器cpu使用率

        double totalVirtualMemory = osmxb.getTotalPhysicalMemorySize(); // 当前服务所在机器总内存
        double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize(); // 当前服务所在机器空闲内存
        double value = freePhysicalMemorySize/totalVirtualMemory;
        String percentMemoryLoad = String.format("%.2f", (1-value)*100)+"%"; // 当前服务所在机器内存使用率

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
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances("hoj-judge-server");
        for (ServiceInstance serviceInstance:serviceInstances){
            String result = restTemplate.getForObject(serviceInstance.getUri() + "/get-sys-config", String.class);
            JSONObject jsonObject = JSONObject.parseObject(result);
            jsonObject.put("service",serviceInstance);
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
        if (!StringUtils.isEmpty(params.get("emailHost"))) {
            configVo.setEmailHost((String) params.get("emailHost"));
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
        return sendNewConfigToNacos();
    }


    public boolean sendNewConfigToNacos() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("dataId", DATA_ID);
        map.add("content", configUtils.getConfigContent());
        map.add("group", GROUP);
        map.add("type", TYPE);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        String result = restTemplate.postForObject(NACOS_URL + "/nacos/v1/cs/configs", request, String.class);

        if (result.equals("true")) {
            return true;
        } else {
            return false;
        }
    }
}
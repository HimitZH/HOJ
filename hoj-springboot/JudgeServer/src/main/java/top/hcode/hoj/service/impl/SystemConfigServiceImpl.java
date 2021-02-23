package top.hcode.hoj.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sun.management.OperatingSystemMXBean;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.service.SystemConfigService;
import top.hcode.hoj.util.IpUtils;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/3 20:15
 * @Description:
 */
@Service
@Data
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final int cpuNum = Runtime.getRuntime().availableProcessors();

    @Value("${hoj-judger.task-num}")
    private Integer taskNum;

    @Value("${hoj-judger.max-task-num}")
    private Integer maxTaskNum;

    @Value("${spring.cloud.nacos.url}")
    private String nacosUrl;

    @Value("${hoj-judger.ip}")
    private String ip;

    @Value("${hoj-judger.port}")
    private Integer port;

    @Value("${hoj-judger.name}")
    private String name;

    @Value("${spring.application.name}")
    private String judgeServiceName;

    @Autowired
    private RestTemplate restTemplate;

    private static OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public HashMap<String, Object> getSystemConfig() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        int cpuCores = Runtime.getRuntime().availableProcessors(); // cpu核数

        double cpuLoad = osmxb.getSystemCpuLoad();
        String percentCpuLoad = String.format("%.2f", cpuLoad * 100) + "%"; // cpu使用率

        double totalVirtualMemory = osmxb.getTotalPhysicalMemorySize(); // 总内存
        double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize(); // 空闲内存
        double value = freePhysicalMemorySize / totalVirtualMemory;
        String percentMemoryLoad = String.format("%.2f", (1 - value) * 100) + "%"; // 内存使用率

        result.put("cpuCores", cpuCores);
        result.put("percentCpuLoad", percentCpuLoad);
        result.put("percentMemoryLoad", percentMemoryLoad);
        return result;
    }

    public synchronized void reduceCurrentTaskNum() {
        int currentTaskNum = getTaskNum();
        if (currentTaskNum >= 1) {
            setTaskNum(currentTaskNum - 1);
        }
    }

    public synchronized void addCurrentTaskNum() {
        int currentTaskNum = getTaskNum();
        setTaskNum(currentTaskNum + 1);
    }

    @Override
    public void updateJudgeTaskNum(Boolean add) {
        int max = cpuNum;
        if (maxTaskNum != -1) {
            max = getMaxTaskNum();
        }
        String useIP = ip.equals("-1") ? IpUtils.getLocalIpv4Address() : ip;
        if (add) {
            addCurrentTaskNum();
        } else {
            reduceCurrentTaskNum();
        }

        JSONObject metaData = new JSONObject();
        metaData.set("currentTaskNum", getTaskNum());
        metaData.set("maxTaskNum", max);
        metaData.set("judgeName", name);
        String url = nacosUrl + "/nacos/v1/ns/instance?ip=" + useIP + "&port=" + getPort() +
                "&serviceName=" + getJudgeServiceName() + "&metadata=";
        try {
            String encodeMeta = URLEncoder.encode(JSONUtil.toJsonStr(metaData), "utf-8");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> result = restTemplate.exchange(url + encodeMeta, HttpMethod.PUT, entity, String.class);
            if (result.getBody() == null || !result.getBody().equals("ok")) {
                tryAgainUpdate(max,useIP);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void tryAgainUpdate(int max,String useIP) {
        boolean retryable;
        int attemptNumber = 0;
        do {
            JSONObject metaData = new JSONObject();
            metaData.set("currentTaskNum", getTaskNum());
            metaData.set("maxTaskNum", max);
            metaData.set("judgeName", name);
            String url = nacosUrl + "/nacos/v1/ns/instance?ip=" + useIP + "&port=" + getPort() +
                    "&serviceName=" + getJudgeServiceName() + "&metadata=" + JSONUtil.toJsonStr(metaData);
            String encodeUrl;
            boolean success = false;
            try {
                encodeUrl = URLEncoder.encode(url, "utf-8");
                String result = restTemplate.getForObject(encodeUrl, String.class);
                if (result == null || result.equals("ok")) {
                    success = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (success) {
                return;
            } else {
                attemptNumber++;
                retryable = attemptNumber < 3;
                if (attemptNumber == 3) {
                    break;
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (retryable);
    }
}
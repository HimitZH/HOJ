package top.hcode.hoj.service.impl;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Service;
import top.hcode.hoj.service.SystemConfigService;

import java.lang.management.ManagementFactory;
import java.util.HashMap;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/3 20:15
 * @Description:
 */
@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    private static OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public HashMap<String, Object> getSystemConfig() {
        HashMap<String, Object> result = new HashMap<String, Object>();
        int cpuCores = Runtime.getRuntime().availableProcessors(); // cpu核数

        double cpuLoad = osmxb.getSystemCpuLoad();
        String percentCpuLoad = String.format("%.2f", cpuLoad * 100)+"%"; // cpu使用率

        double totalVirtualMemory = osmxb.getTotalPhysicalMemorySize(); // 总内存
        double freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize(); // 空闲内存
        double value = freePhysicalMemorySize/totalVirtualMemory;
        String percentMemoryLoad = String.format("%.2f", (1-value)*100)+"%"; // 内存使用率

        result.put("cpuCores", cpuCores);
        result.put("percentCpuLoad",percentCpuLoad);
        result.put("percentMemoryLoad",percentMemoryLoad);
        return result;
    }

}
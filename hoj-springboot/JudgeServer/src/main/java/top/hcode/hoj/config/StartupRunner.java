package top.hcode.hoj.config;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.pojo.entity.JudgeServer;
import top.hcode.hoj.service.impl.JudgeServerServiceImpl;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.IpUtils;
import top.hcode.hoj.util.RedisUtils;

import java.util.HashMap;


/**
 * @Author: Himit_ZH
 * @Date: 2021/2/19 22:11
 * @Description:项目启动加载类，启动完毕将该判题机加入到redis里面
 */
@Component
@Slf4j
public class StartupRunner implements CommandLineRunner {

    @Value("${hoj-judge-server.max-task-num}")
    private Integer maxTaskNum;

    @Value("${hoj-judge-server.remote-judge.max-task-num}")
    private Integer maxRemoteTaskNum;

    @Value("${hoj-judge-server.remote-judge.open}")
    private Boolean openRemoteJudge;

    @Value("${hoj-judge-server.name}")
    private String name;

    @Value("${hoj-judge-server.ip}")
    private String ip;

    @Value("${hoj-judge-server.port}")
    private Integer port;

    private static final int cpuNum = Runtime.getRuntime().availableProcessors();

    @Autowired
    private JudgeServerServiceImpl judgeServerService;

    @Override
    @Transactional
    public void run(String... args) {

        if (maxTaskNum == -1) {
            maxTaskNum = cpuNum * 2;
        }
        if (ip.equals("-1")) {
            ip = IpUtils.getLocalIpv4Address();
        }
        UpdateWrapper<JudgeServer> judgeServerQueryWrapper = new UpdateWrapper<>();
        judgeServerQueryWrapper.eq("ip", ip).eq("port", port);
        judgeServerService.remove(judgeServerQueryWrapper);
        boolean isOk1 = judgeServerService.save(new JudgeServer()
                .setCpuCore(cpuNum)
                .setIp(ip)
                .setPort(port)
                .setVersion(0L)
                .setUrl(ip + ":" + port)
                .setMaxTaskNumber(maxTaskNum)
                .setIsRemote(false)
                .setName(name));
        boolean isOk2 = true;
        if (openRemoteJudge) {
            if (maxRemoteTaskNum == -1) {
                maxRemoteTaskNum = (cpuNum * 2 ) * 2;
            }
            isOk2 = judgeServerService.save(new JudgeServer()
                    .setCpuCore(cpuNum)
                    .setIp(ip)
                    .setPort(port)
                    .setVersion(0L)
                    .setUrl(ip + ":" + port)
                    .setMaxTaskNumber(maxRemoteTaskNum)
                    .setIsRemote(true)
                    .setName(name));
        }

        if (!isOk1 || !isOk2) {
            log.error("初始化判题机信息到数据库失败，请重新启动试试！");
        }

    }

}
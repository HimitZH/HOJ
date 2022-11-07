package top.hcode.hoj.manager.admin.system;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.oshi.OshiUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.config.NacosSwitchConfig;
import top.hcode.hoj.config.SwitchConfig;
import top.hcode.hoj.config.WebConfig;
import top.hcode.hoj.dao.common.FileEntityService;
import top.hcode.hoj.dao.judge.RemoteJudgeAccountEntityService;
import top.hcode.hoj.manager.email.EmailManager;
import top.hcode.hoj.pojo.dto.*;
import top.hcode.hoj.pojo.entity.common.File;
import top.hcode.hoj.pojo.entity.judge.RemoteJudgeAccount;
import top.hcode.hoj.pojo.vo.ConfigVO;
import top.hcode.hoj.utils.ConfigUtils;
import top.hcode.hoj.utils.Constants;

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
    private ConfigVO configVo;

    @Autowired
    private EmailManager emailManager;

    @Autowired
    private FileEntityService fileEntityService;

    @Autowired
    private RemoteJudgeAccountEntityService remoteJudgeAccountEntityService;

    @Autowired
    private ConfigUtils configUtils;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private NacosSwitchConfig nacosSwitchConfig;

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
            try {
                String result = restTemplate.getForObject(serviceInstance.getUri() + "/get-sys-config", String.class);
                JSONObject jsonObject = JSONUtil.parseObj(result);
                jsonObject.put("service", serviceInstance);
                serviceInfoList.add(jsonObject);
            } catch (Exception e) {
                log.error("[Admin Dashboard] get judge service info error, uri={}, error={}", serviceInstance.getUri(), e);
            }
        }
        return serviceInfoList;
    }


    public WebConfigDTO getWebConfig() {
        WebConfig webConfig = nacosSwitchConfig.getWebConfig();
        return WebConfigDTO.builder()
                .baseUrl(UnicodeUtil.toString(webConfig.getBaseUrl()))
                .name(UnicodeUtil.toString(webConfig.getName()))
                .shortName(UnicodeUtil.toString(webConfig.getShortName()))
                .description(UnicodeUtil.toString(webConfig.getDescription()))
                .register(webConfig.getRegister())
                .recordName(UnicodeUtil.toString(webConfig.getRecordName()))
                .recordUrl(UnicodeUtil.toString(webConfig.getRecordUrl()))
                .projectName(UnicodeUtil.toString(webConfig.getProjectName()))
                .projectUrl(UnicodeUtil.toString(webConfig.getProjectUrl()))
                .build();
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

    public void setWebConfig(WebConfigDTO config) throws StatusFailException {

        WebConfig webConfig = nacosSwitchConfig.getWebConfig();

        if (!StringUtils.isEmpty(config.getBaseUrl())) {
            webConfig.setBaseUrl(config.getBaseUrl());
        }
        if (!StringUtils.isEmpty(config.getName())) {
            webConfig.setName(config.getName());
        }
        if (!StringUtils.isEmpty(config.getShortName())) {
            webConfig.setShortName(config.getShortName());
        }
        if (!StringUtils.isEmpty(config.getDescription())) {
            webConfig.setDescription(config.getDescription());
        }
        if (config.getRegister() != null) {
            webConfig.setRegister(config.getRegister());
        }
        if (!StringUtils.isEmpty(config.getRecordName())) {
            webConfig.setRecordName(config.getRecordName());
        }
        if (!StringUtils.isEmpty(config.getRecordUrl())) {
            webConfig.setRecordUrl(config.getRecordUrl());
        }
        if (!StringUtils.isEmpty(config.getProjectName())) {
            webConfig.setProjectName(config.getProjectName());
        }
        if (!StringUtils.isEmpty(config.getProjectUrl())) {
            webConfig.setProjectUrl(config.getProjectUrl());
        }
        boolean isOk = nacosSwitchConfig.publishWebConfig();
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    public EmailConfigDTO getEmailConfig() {
        WebConfig webConfig = nacosSwitchConfig.getWebConfig();
        return EmailConfigDTO.builder()
                .emailUsername(webConfig.getEmailUsername())
                .emailPassword(webConfig.getEmailPassword())
                .emailHost(webConfig.getEmailHost())
                .emailPort(webConfig.getEmailPort())
                .emailBGImg(webConfig.getEmailBGImg())
                .emailSsl(webConfig.getEmailSsl())
                .build();
    }


    public void setEmailConfig(EmailConfigDTO config) throws StatusFailException {
        WebConfig webConfig = nacosSwitchConfig.getWebConfig();
        if (!StringUtils.isEmpty(config.getEmailHost())) {
            webConfig.setEmailHost(config.getEmailHost());
        }
        if (!StringUtils.isEmpty(config.getEmailPassword())) {
            webConfig.setEmailPassword(config.getEmailPassword());
        }

        if (config.getEmailPort() != null) {
            webConfig.setEmailPort(config.getEmailPort());
        }

        if (!StringUtils.isEmpty(config.getEmailUsername())) {
            webConfig.setEmailUsername(config.getEmailUsername());
        }

        if (!StringUtils.isEmpty(config.getEmailBGImg())) {
            webConfig.setEmailBGImg(config.getEmailBGImg());
        }

        if (config.getEmailSsl() != null) {
            webConfig.setEmailSsl(config.getEmailSsl());
        }

        boolean isOk = nacosSwitchConfig.publishWebConfig();
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }


    public void testEmail(TestEmailDTO testEmailDto) throws StatusFailException {
        String email = testEmailDto.getEmail();
        if (StringUtils.isEmpty(email)) {
            throw new StatusFailException("测试的邮箱不能为空！");
        }
        boolean isEmail = Validator.isEmail(email);
        if (isEmail) {
            emailManager.testEmail(email);
        } else {
            throw new StatusFailException("测试的邮箱格式不正确！");
        }
    }

    public DBAndRedisConfigDTO getDBAndRedisConfig() {
        return DBAndRedisConfigDTO.builder()
                .dbName(configVo.getMysqlDBName())
                .dbHost(configVo.getMysqlHost())
                .dbPort(configVo.getMysqlPort())
                .dbUsername(configVo.getMysqlUsername())
                .dbPassword(configVo.getMysqlPassword())
                .redisHost(configVo.getRedisHost())
                .redisPort(configVo.getRedisPort())
                .redisPassword(configVo.getRedisPassword())
                .build();
    }


    public void setDBAndRedisConfig(DBAndRedisConfigDTO config) throws StatusFailException {

        if (!StringUtils.isEmpty(config.getDbName())) {
            configVo.setMysqlDBName(config.getDbName());
        }

        if (!StringUtils.isEmpty(config.getDbHost())) {
            configVo.setMysqlHost(config.getDbHost());
        }
        if (config.getDbPort() != null) {
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

        if (config.getRedisPort() != null) {
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

    public SwitchConfigDTO getSwitchConfig() {
        SwitchConfig switchConfig = nacosSwitchConfig.getSwitchConfig();
        SwitchConfigDTO switchConfigDTO = new SwitchConfigDTO();
        BeanUtil.copyProperties(switchConfig, switchConfigDTO);
        return switchConfigDTO;
    }

    public void setSwitchConfig(SwitchConfigDTO config) throws StatusFailException {

        SwitchConfig switchConfig = nacosSwitchConfig.getSwitchConfig();

        if (config.getOpenPublicDiscussion() != null) {
            switchConfig.setOpenPublicDiscussion(config.getOpenPublicDiscussion());
        }
        if (config.getOpenGroupDiscussion() != null) {
            switchConfig.setOpenGroupDiscussion(config.getOpenGroupDiscussion());
        }
        if (config.getOpenContestComment() != null) {
            switchConfig.setOpenContestComment(config.getOpenContestComment());
        }
        if (config.getOpenPublicJudge() != null) {
            switchConfig.setOpenPublicJudge(config.getOpenPublicJudge());
        }
        if (config.getOpenGroupJudge() != null) {
            switchConfig.setOpenGroupJudge(config.getOpenGroupJudge());
        }
        if (config.getOpenContestJudge() != null) {
            switchConfig.setOpenContestJudge(config.getOpenContestJudge());
        }

        if (config.getHideNonContestSubmissionCode() != null) {
            switchConfig.setHideNonContestSubmissionCode(config.getHideNonContestSubmissionCode());
        }

        if (config.getDefaultCreateDiscussionACInitValue() != null) {
            switchConfig.setDefaultCreateDiscussionACInitValue(config.getDefaultCreateDiscussionACInitValue());
        }

        if (config.getDefaultCreateDiscussionDailyLimit() != null) {
            switchConfig.setDefaultCreateDiscussionDailyLimit(config.getDefaultCreateDiscussionDailyLimit());
        }

        if (config.getDefaultCreateCommentACInitValue() != null) {
            switchConfig.setDefaultCreateCommentACInitValue(config.getDefaultCreateCommentACInitValue());
        }

        if (config.getDefaultSubmitInterval() != null) {
            if (config.getDefaultSubmitInterval() >= 0) {
                switchConfig.setDefaultSubmitInterval(config.getDefaultSubmitInterval());
            } else {
                switchConfig.setDefaultSubmitInterval(0);
            }
        }

        if (config.getDefaultCreateGroupACInitValue() != null) {
            switchConfig.setDefaultCreateGroupACInitValue(config.getDefaultCreateGroupACInitValue());
        }

        if (config.getDefaultCreateGroupDailyLimit() != null) {
            switchConfig.setDefaultCreateGroupDailyLimit(config.getDefaultCreateGroupDailyLimit());
        }

        if (config.getDefaultCreateGroupLimit() != null) {
            switchConfig.setDefaultCreateGroupLimit(config.getDefaultCreateGroupLimit());
        }

        if (checkListDiff(config.getCfUsernameList(), switchConfig.getCfUsernameList()) ||
                checkListDiff(config.getCfPasswordList(), switchConfig.getCfPasswordList())) {
            switchConfig.setCfUsernameList(config.getCfUsernameList());
            switchConfig.setCfPasswordList(config.getCfPasswordList());
            changeRemoteJudgeAccount(config.getCfUsernameList(),
                    config.getCfPasswordList(),
                    Constants.RemoteOJ.CODEFORCES.getName());
        }

        if (checkListDiff(config.getHduUsernameList(), switchConfig.getHduUsernameList()) ||
                checkListDiff(config.getHduPasswordList(), switchConfig.getHduPasswordList())) {
            switchConfig.setHduUsernameList(config.getHduUsernameList());
            switchConfig.setHduPasswordList(config.getHduPasswordList());
            changeRemoteJudgeAccount(config.getHduUsernameList(),
                    config.getHduPasswordList(),
                    Constants.RemoteOJ.HDU.getName());
        }

        if (checkListDiff(config.getPojUsernameList(), switchConfig.getPojUsernameList()) ||
                checkListDiff(config.getPojPasswordList(), switchConfig.getPojPasswordList())) {
            switchConfig.setPojUsernameList(config.getPojUsernameList());
            switchConfig.setPojPasswordList(config.getPojPasswordList());
            changeRemoteJudgeAccount(config.getPojUsernameList(),
                    config.getPojPasswordList(),
                    Constants.RemoteOJ.POJ.getName());
        }

        if (checkListDiff(config.getSpojUsernameList(), switchConfig.getSpojUsernameList()) ||
                checkListDiff(config.getSpojPasswordList(), switchConfig.getSpojPasswordList())) {
            switchConfig.setSpojUsernameList(config.getSpojUsernameList());
            switchConfig.setSpojPasswordList(config.getSpojPasswordList());
            changeRemoteJudgeAccount(config.getSpojUsernameList(),
                    config.getSpojPasswordList(),
                    Constants.RemoteOJ.SPOJ.getName());
        }

        if (checkListDiff(config.getAtcoderUsernameList(), switchConfig.getAtcoderUsernameList()) ||
                checkListDiff(config.getAtcoderPasswordList(), switchConfig.getAtcoderPasswordList())) {
            switchConfig.setAtcoderUsernameList(config.getAtcoderUsernameList());
            switchConfig.setAtcoderPasswordList(config.getAtcoderPasswordList());
            changeRemoteJudgeAccount(config.getAtcoderUsernameList(),
                    config.getAtcoderPasswordList(),
                    Constants.RemoteOJ.ATCODER.getName());
        }

        boolean isOk = nacosSwitchConfig.publishSwitchConfig();
        if (!isOk) {
            throw new StatusFailException("修改失败");
        }
    }

    private boolean checkListDiff(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size()) {
            return true;
        }
        return !list1.toString().equals(list2.toString());
    }

    private void changeRemoteJudgeAccount(List<String> usernameList,
                                          List<String> passwordList,
                                          String oj) {

        if (CollectionUtils.isEmpty(usernameList) || CollectionUtils.isEmpty(passwordList) || usernameList.size() != passwordList.size()) {
            log.error("[Change by Switch] [{}]: There is no account or password configured for remote judge, " +
                            "username list:{}, password list:{}", oj, Arrays.toString(usernameList.toArray()),
                    Arrays.toString(passwordList.toArray()));
        }

        QueryWrapper<RemoteJudgeAccount> remoteJudgeAccountQueryWrapper = new QueryWrapper<>();
        remoteJudgeAccountQueryWrapper.eq("oj", oj);
        remoteJudgeAccountEntityService.remove(remoteJudgeAccountQueryWrapper);

        List<RemoteJudgeAccount> newRemoteJudgeAccountList = new ArrayList<>();

        for (int i = 0; i < usernameList.size(); i++) {
            newRemoteJudgeAccountList.add(new RemoteJudgeAccount()
                    .setUsername(usernameList.get(i))
                    .setPassword(passwordList.get(i))
                    .setStatus(true)
                    .setVersion(0L)
                    .setOj(oj));
        }

        if (newRemoteJudgeAccountList.size() > 0) {
            boolean addOk = remoteJudgeAccountEntityService.saveOrUpdateBatch(newRemoteJudgeAccountList);
            if (!addOk) {
                log.error("Remote judge initialization failed. Failed to add account for: [{}]. Please check the configuration file and restart!", oj);
            }
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
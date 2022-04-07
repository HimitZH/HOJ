package top.hcode.hoj.service.admin.system.impl;

import cn.hutool.json.JSONObject;
import org.springframework.stereotype.Service;
import top.hcode.hoj.common.exception.StatusFailException;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.manager.admin.system.ConfigManager;
import top.hcode.hoj.pojo.dto.DBAndRedisConfigDto;
import top.hcode.hoj.pojo.dto.EmailConfigDto;
import top.hcode.hoj.pojo.dto.TestEmailDto;
import top.hcode.hoj.pojo.dto.WebConfigDto;
import top.hcode.hoj.service.admin.system.ConfigService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 22:23
 * @Description:
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    @Resource
    private ConfigManager configManager;

    @Override
    public CommonResult<JSONObject> getServiceInfo() {
        return CommonResult.successResponse(configManager.getServiceInfo());
    }

    @Override
    public CommonResult<List<JSONObject>> getJudgeServiceInfo() {
        return CommonResult.successResponse(configManager.getJudgeServiceInfo());
    }

    @Override
    public CommonResult<Void> deleteHomeCarousel(Long id) {
        try {
            configManager.deleteHomeCarousel(id);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<WebConfigDto> getWebConfig() {
        return CommonResult.successResponse(configManager.getWebConfig());
    }

    @Override
    public CommonResult<Void> setWebConfig(WebConfigDto config) {
        try {
            configManager.setWebConfig(config);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<EmailConfigDto> getEmailConfig() {
        return CommonResult.successResponse(configManager.getEmailConfig());
    }

    @Override
    public CommonResult<Void> setEmailConfig(EmailConfigDto config) {
        try {
            configManager.setEmailConfig(config);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<Void> testEmail(TestEmailDto testEmailDto) {
        try {
            configManager.testEmail(testEmailDto);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }

    @Override
    public CommonResult<DBAndRedisConfigDto> getDBAndRedisConfig() {
        return CommonResult.successResponse(configManager.getDBAndRedisConfig());
    }

    @Override
    public CommonResult<Void> setDBAndRedisConfig(DBAndRedisConfigDto config) {
        try {
            configManager.setDBAndRedisConfig(config);
            return CommonResult.successResponse();
        } catch (StatusFailException e) {
            return CommonResult.errorResponse(e.getMessage());
        }
    }
}
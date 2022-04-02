package top.hcode.hoj.service.admin.system;

import cn.hutool.json.JSONObject;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.admin.DBAndRedisConfig;
import top.hcode.hoj.pojo.entity.admin.EmailConfig;
import top.hcode.hoj.pojo.entity.admin.WebConfig;

import java.util.List;
import java.util.Map;

public interface ConfigService {

    public CommonResult<JSONObject> getServiceInfo();

    public CommonResult<List<JSONObject>> getJudgeServiceInfo();

    public CommonResult<Void> deleteHomeCarousel(Long id);

    public CommonResult<Map<Object,Object>> getWebConfig();

    public CommonResult<Void> setWebConfig(WebConfig config);

    public CommonResult<Map<Object,Object>> getEmailConfig();

    public CommonResult<Void> setEmailConfig(EmailConfig config);

    public CommonResult<Void> testEmail(String params);

    public CommonResult<Map<Object,Object>> getDBAndRedisConfig();

    public CommonResult<Void> setDBAndRedisConfig(DBAndRedisConfig config);

}

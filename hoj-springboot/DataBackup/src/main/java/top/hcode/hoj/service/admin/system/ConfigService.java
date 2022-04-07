package top.hcode.hoj.service.admin.system;

import cn.hutool.json.JSONObject;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.DBAndRedisConfigDto;
import top.hcode.hoj.pojo.dto.EmailConfigDto;
import top.hcode.hoj.pojo.dto.TestEmailDto;
import top.hcode.hoj.pojo.dto.WebConfigDto;

import java.util.List;
import java.util.Map;

public interface ConfigService {

    public CommonResult<JSONObject> getServiceInfo();

    public CommonResult<List<JSONObject>> getJudgeServiceInfo();

    public CommonResult<Void> deleteHomeCarousel(Long id);

    public CommonResult<WebConfigDto> getWebConfig();

    public CommonResult<Void> setWebConfig(WebConfigDto config);

    public CommonResult<EmailConfigDto> getEmailConfig();

    public CommonResult<Void> setEmailConfig(EmailConfigDto config);

    public CommonResult<Void> testEmail(TestEmailDto testEmailDto);

    public CommonResult<DBAndRedisConfigDto> getDBAndRedisConfig();

    public CommonResult<Void> setDBAndRedisConfig(DBAndRedisConfigDto config);

}

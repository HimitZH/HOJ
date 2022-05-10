package top.hcode.hoj.service.admin.system;

import cn.hutool.json.JSONObject;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.*;

import java.util.List;

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

    public CommonResult<SwitchConfigDto> getSwitchConfig();

    public CommonResult<Void> setSwitchConfig(SwitchConfigDto config);

}

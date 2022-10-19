package top.hcode.hoj.service.admin.system;

import cn.hutool.json.JSONObject;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.*;

import java.util.List;

public interface ConfigService {

    public CommonResult<JSONObject> getServiceInfo();

    public CommonResult<List<JSONObject>> getJudgeServiceInfo();

    public CommonResult<Void> deleteHomeCarousel(Long id);

    public CommonResult<WebConfigDTO> getWebConfig();

    public CommonResult<Void> setWebConfig(WebConfigDTO config);

    public CommonResult<EmailConfigDTO> getEmailConfig();

    public CommonResult<Void> setEmailConfig(EmailConfigDTO config);

    public CommonResult<Void> testEmail(TestEmailDTO testEmailDto);

    public CommonResult<DBAndRedisConfigDTO> getDBAndRedisConfig();

    public CommonResult<Void> setDBAndRedisConfig(DBAndRedisConfigDTO config);

    public CommonResult<SwitchConfigDTO> getSwitchConfig();

    public CommonResult<Void> setSwitchConfig(SwitchConfigDTO config);

}

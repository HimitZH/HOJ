package top.hcode.hoj.controller.admin;


import cn.hutool.json.JSONObject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.dto.DBAndRedisConfigDTO;
import top.hcode.hoj.pojo.dto.EmailConfigDTO;
import top.hcode.hoj.pojo.dto.TestEmailDTO;
import top.hcode.hoj.pojo.dto.WebConfigDTO;
import top.hcode.hoj.service.admin.system.ConfigService;


import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/2 21:42
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    /**
     * @MethodName getServiceInfo
     * @Params * @param null
     * @Description 获取当前服务的相关信息以及当前系统的cpu情况，内存使用情况
     * @Return CommonResult
     * @Since 2020/12/3
     */
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @RequestMapping("/get-service-info")
    public CommonResult<JSONObject> getServiceInfo() {
        return configService.getServiceInfo();
    }

    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @RequestMapping("/get-judge-service-info")
    public CommonResult<List<JSONObject>> getJudgeServiceInfo() {
        return configService.getJudgeServiceInfo();
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/get-web-config")
    public CommonResult<WebConfigDTO> getWebConfig() {
        return configService.getWebConfig();
    }


    @RequiresPermissions("system_info_admin")
    @DeleteMapping("/home-carousel")
    public CommonResult<Void> deleteHomeCarousel(@RequestParam("id") Long id) {

        return configService.deleteHomeCarousel(id);
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping(value = "/set-web-config", method = RequestMethod.PUT)
    public CommonResult<Void> setWebConfig(@RequestBody WebConfigDTO config) {

        return configService.setWebConfig(config);
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/get-email-config")
    public CommonResult<EmailConfigDTO> getEmailConfig() {

        return configService.getEmailConfig();
    }

    @RequiresPermissions("system_info_admin")
    @PutMapping("/set-email-config")
    public CommonResult<Void> setEmailConfig(@RequestBody EmailConfigDTO config) {
        return configService.setEmailConfig(config);
    }

    @RequiresPermissions("system_info_admin")
    @PostMapping("/test-email")
    public CommonResult<Void> testEmail(@RequestBody TestEmailDTO testEmailDto) {
        return configService.testEmail(testEmailDto);
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/get-db-and-redis-config")
    public CommonResult<DBAndRedisConfigDTO> getDBAndRedisConfig() {
        return configService.getDBAndRedisConfig();
    }

    @RequiresPermissions("system_info_admin")
    @PutMapping("/set-db-and-redis-config")
    public CommonResult<Void> setDBAndRedisConfig(@RequestBody DBAndRedisConfigDTO config) {
        return configService.setDBAndRedisConfig(config);
    }

}
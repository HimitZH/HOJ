package top.hcode.hoj.controller.admin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.UnicodeUtil;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.hcode.hoj.common.result.CommonResult;
import top.hcode.hoj.pojo.entity.common.File;
import top.hcode.hoj.pojo.vo.ConfigVo;
import top.hcode.hoj.service.common.impl.ConfigServiceImpl;
import top.hcode.hoj.service.common.impl.EmailServiceImpl;
import top.hcode.hoj.service.common.impl.FileServiceImpl;

import javax.mail.MessagingException;
import java.util.HashMap;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/2 21:42
 * @Description:
 */
@RestController
@RequestMapping("/api/admin/config")
public class ConfigController {

    @Autowired
    private ConfigVo configVo;

    @Autowired
    private ConfigServiceImpl configService;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private FileServiceImpl fileService;

    /**
     * @MethodName getServiceInfo
     * @Params * @param null
     * @Description 获取当前服务的相关信息以及当前系统的cpu情况，内存使用情况
     * @Return CommonResult
     * @Since 2020/12/3
     */

    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @RequestMapping("/get-service-info")
    public CommonResult getServiceInfo() {

        return CommonResult.successResponse(configService.getServiceInfo());
    }

    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    @RequestMapping("/get-judge-service-info")
    public CommonResult getJudgeServiceInfo() {
        return CommonResult.successResponse(configService.getJudgeServiceInfo());
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/get-web-config")
    public CommonResult getWebConfig() {

        return CommonResult.successResponse(
                MapUtil.builder().put("baseUrl", UnicodeUtil.toString(configVo.getBaseUrl()))
                        .put("name", UnicodeUtil.toString(configVo.getName()))
                        .put("shortName", UnicodeUtil.toString(configVo.getShortName()))
                        .put("description", UnicodeUtil.toString(configVo.getDescription()))
                        .put("register", configVo.getRegister())
                        .put("recordName", UnicodeUtil.toString(configVo.getRecordName()))
                        .put("recordUrl", UnicodeUtil.toString(configVo.getRecordUrl()))
                        .put("projectName", UnicodeUtil.toString(configVo.getProjectName()))
                        .put("projectUrl", UnicodeUtil.toString(configVo.getProjectUrl())).map()
        );
    }


    @RequiresPermissions("system_info_admin")
    @DeleteMapping("/home-carousel")
    public CommonResult deleteHomeCarousel(@RequestParam("id") Long id) {

        File imgFile = fileService.getById(id);
        if (imgFile == null) {
            return CommonResult.errorResponse("文件id错误，图片不存在");
        }
        boolean isOk = fileService.removeById(id);
        if (isOk) {
            FileUtil.del(imgFile.getFilePath());
            return CommonResult.successResponse("删除成功！");
        } else {
            return CommonResult.errorResponse("删除失败！");
        }
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping(value = "/set-web-config", method = RequestMethod.PUT)
    public CommonResult setWebConfig(@RequestBody HashMap<String, Object> params) {

        boolean result = configService.setWebConfig(params);
        if (result) {
            return CommonResult.successResponse(null, "修改网站前端配置成功！");
        } else {
            return CommonResult.errorResponse("修改失败！");
        }
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/get-email-config")
    public CommonResult getEmailConfig() {
        return CommonResult.successResponse(
                MapUtil.builder().put("emailUsername", configVo.getEmailUsername())
                        .put("emailPassword", configVo.getEmailPassword())
                        .put("emailHost", configVo.getEmailHost())
                        .put("emailPort", configVo.getEmailPort())
                        .put("emailBGImg", configVo.getEmailBGImg())
                        .put("emailSsl", configVo.getEmailSsl()).map()
        );
    }

    @RequiresPermissions("system_info_admin")
    @PutMapping("/set-email-config")
    public CommonResult setEmailConfig(@RequestBody HashMap<String, Object> params) {

        boolean result = configService.setEmailConfig(params);
        if (result) {
            return CommonResult.successResponse(null, "修改邮箱配置成功！");
        } else {
            return CommonResult.errorResponse("修改失败！");
        }
    }

    @RequiresPermissions("system_info_admin")
    @PostMapping("/test-email")
    public CommonResult testEmail(@RequestBody HashMap<String, Object> params) throws MessagingException {
        String email = (String) params.get("email");
        boolean isEmail = Validator.isEmail(email);
        if (isEmail) {
            emailService.testEmail(email);
            return CommonResult.successResponse(null, "测试邮件已发送至指定邮箱！");
        } else {
            return CommonResult.errorResponse("测试的邮箱不正确！");
        }
    }

    @RequiresPermissions("system_info_admin")
    @RequestMapping("/get-db-and-redis-config")
    public CommonResult getDBAndRedisConfig() {

        return CommonResult.successResponse(
                MapUtil.builder().put("dbName", configVo.getMysqlDBName())
                        .put("dbHost", configVo.getMysqlHost())
                        .put("dbPost", configVo.getMysqlPort())
                        .put("dbUsername", configVo.getMysqlUsername())
                        .put("dbPassword", configVo.getMysqlPassword())
                        .put("redisHost", configVo.getRedisHost())
                        .put("redisPort", configVo.getRedisPort())
                        .put("redisPassword", configVo.getRedisPassword())
                        .map()
        );
    }

    @RequiresPermissions("system_info_admin")
    @PutMapping("/set-db-and-redis-config")
    public CommonResult setDBAndRedisConfig(@RequestBody HashMap<String, Object> params) {
        boolean result = configService.setDBAndRedisConfig(params);
        if (result) {
            return CommonResult.successResponse(null, "修改数据库配置成功！");
        } else {
            return CommonResult.errorResponse("修改失败！");
        }
    }

}
package top.hcode.hoj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.hcode.hoj.service.impl.SystemConfigServiceImpl;

import java.util.HashMap;

/**
 * @Author: Himit_ZH
 * @Date: 2020/12/3 20:12
 * @Description:
 */
@RestController
public class SystemConfigController {

    @Autowired
    private SystemConfigServiceImpl systemConfigService;

    @RequestMapping("/get-sys-config")
    public HashMap<String,Object> getSystemConfig(){
        return systemConfigService.getSystemConfig();
    }
}
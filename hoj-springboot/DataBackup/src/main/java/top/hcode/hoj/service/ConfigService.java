package top.hcode.hoj.service;


import cn.hutool.json.JSONObject;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.HashMap;
import java.util.List;

public interface ConfigService {
    boolean setEmailConfig(HashMap<String,Object> params) throws NacosException;
    boolean setWebConfig(HashMap<String,Object> params) throws NacosException;
    boolean setDBAndRedisConfig(HashMap<String,Object> params) throws NacosException;
    JSONObject getServiceInfo();
    List<JSONObject> getJudgeServiceInfo();
}

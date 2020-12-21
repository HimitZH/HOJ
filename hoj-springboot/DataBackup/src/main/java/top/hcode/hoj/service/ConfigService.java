package top.hcode.hoj.service;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;

public interface ConfigService {
    boolean setEmailConfig(HashMap<String,Object> params);
    boolean setWebConfig(HashMap<String,Object> params);
    boolean setDBAndRedisConfig(HashMap<String,Object> params);
    JSONObject getServiceInfo();
    List<JSONObject> getJudgeServiceInfo();
}

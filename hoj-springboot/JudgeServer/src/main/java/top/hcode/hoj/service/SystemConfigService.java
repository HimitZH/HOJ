package top.hcode.hoj.service;

import java.util.HashMap;

public interface SystemConfigService {
    public HashMap<String,Object> getSystemConfig();

    public void updateJudgeTaskNum(Boolean add);
}

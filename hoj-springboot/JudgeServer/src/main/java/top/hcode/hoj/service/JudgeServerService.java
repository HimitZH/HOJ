package top.hcode.hoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.common.CommonResult;
import top.hcode.hoj.pojo.entity.judge.JudgeServer;

import java.util.HashMap;

public interface JudgeServerService extends IService<JudgeServer> {

    public HashMap<String, Object> getJudgeServerInfo();
}

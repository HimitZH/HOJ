package top.hcode.hoj.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hcode.hoj.pojo.entity.judge.JudgeServer;

import java.util.HashMap;

public interface JudgeServerEntityService extends IService<JudgeServer> {

    public HashMap<String, Object> getJudgeServerInfo();
}

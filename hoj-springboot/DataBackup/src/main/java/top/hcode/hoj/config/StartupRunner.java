package top.hcode.hoj.config;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.vo.ConfigVo;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: Himit_ZH
 * @Date: 2021/2/19 22:11
 * @Description:项目启动加载类，启动完毕将远程判题的账号列表存到redis里面
 */
@Component
@Slf4j
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private ConfigVo configVo;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void run(String... args) throws Exception {
        redisUtils.del(Constants.Judge.STATUS_HDU_REMOTE_JUDGE_ACCOUNT.getName());
        List<String> hduUsernameList = configVo.getHduUsernameList();
        List<String> hduPasswordList = configVo.getHduPasswordList();
        for (int i = 0; i < hduUsernameList.size(); i++) {
            HashMap<String, Object> tmp = new HashMap<>();
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("username", hduUsernameList.get(i));
            jsonObject.set("password", hduPasswordList.get(i));
            redisUtils.llPush(Constants.Judge.STATUS_HDU_REMOTE_JUDGE_ACCOUNT.getName(), JSONUtil.toJsonStr(jsonObject));
        }
        if (redisUtils.lGetListSize(Constants.Judge.STATUS_HDU_REMOTE_JUDGE_ACCOUNT.getName()) != hduUsernameList.size()) {
            log.error("HDU判题账号注入Redis的List异常------------>{}", "请检查配置文件，然后重新启动！");
        }

    }
}
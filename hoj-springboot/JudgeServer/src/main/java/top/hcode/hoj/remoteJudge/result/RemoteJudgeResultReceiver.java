package top.hcode.hoj.remoteJudge.result;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeFactory;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.IpUtils;
import top.hcode.hoj.util.RedisUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RemoteJudgeResultReceiver implements MessageListener {
    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RemoteJudgeResultDispatcher remoteJudgeResultDispatcher;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Override
    @Transactional
    public void onMessage(Message message, byte[] bytes) {
        String source = (String) redisUtils.lrPop(Constants.RemoteJudge.JUDGE_WAITING_RESULT_QUEUE.getName());
        // 如果竞争不到查询队列，结束
        if (source == null) {
            return;
        }
        JSONObject task = JSONUtil.parseObj(source);
        Long resultSubmitId = task.getLong("resultSubmitId");
        String username = task.getStr("username");  // HDU不需要，但是CF需要
        Long submitId = task.getLong("submitId");
        String uid = task.getStr("uid");
        Long cid = task.getLong("cid");
        Long pid = task.getLong("pid");
        String remoteJudge = task.getStr("remoteJudge");
        String token = task.getStr("token");
        HashMap<String, String> cookies = JsonObjectToHashMap(JSONUtil.parseObj(task.getStr("cookies")));
        RemoteJudgeStrategy remoteJudgeStrategy = RemoteJudgeFactory.selectJudge(remoteJudge);
        // 获取不到对应的题库或者题库写错了
        if (remoteJudgeStrategy == null) {
            log.error("暂不支持该{}题库---------------->请求失败", remoteJudge);
            return;
        }

        // TODO 获取对应的result，修改到数据库
        try {
            Map<String, Object> result = remoteJudgeStrategy.result(resultSubmitId, username, token, cookies);
            Judge judge = new Judge();
            judge.setSubmitId(submitId);
            Integer status = (Integer) result.getOrDefault("status", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
            // TODO 如果结果没出来，重新放入队列并更新状态为Waiting
            if (status.intValue() == Constants.Judge.STATUS_PENDING.getStatus() ||
                    status.intValue() == Constants.Judge.STATUS_JUDGING.getStatus()) {
                try {
                    TimeUnit.SECONDS.sleep(2);
                    remoteJudgeResultDispatcher.sendTask(remoteJudge, username, submitId, uid, cid, pid, resultSubmitId, token, cookies);
                } catch (Exception e) {
                    log.error("重新查询结果任务出错------{}", e.getMessage());
                }
                return;
            }
            Integer time = (Integer) result.getOrDefault("time", null);
            Integer memory = (Integer) result.getOrDefault("memory", null);
            String CEInfo = (String) result.getOrDefault("CEInfo", null);
            judge.setStatus(status)
                    .setTime(time)
                    .setMemory(memory);

            // 写入当前远程判题OJ名字
            judge.setJudger(remoteJudge);

            if (status.intValue() == Constants.Judge.STATUS_COMPILE_ERROR.getStatus()) {
                judge.setErrorMessage(CEInfo);
            } else if (status.intValue() == Constants.Judge.STATUS_SYSTEM_ERROR.getStatus()) {
                judge.setErrorMessage("There is something wrong with the " + remoteJudge + ", please try again later");
            }
            // 写回数据库
            judgeService.updateById(judge);

            /**
             * @Description TODO 注意！ 如果是OI题目肯定score得分，若不是则请传入null，该方法是为了更新关联表，保持数据一致！
             * @Since 2021/2/12
             **/
            judgeService.updateOtherTable(submitId, status, cid, uid, pid, null);

        } catch (Exception e) {
            log.error("获取结果出错------------>{}", e.getLocalizedMessage());
            // 更新此次提交状态为提交失败！
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.set("status", Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                    .eq("submit_id", submitId);
            judgeService.update(judgeUpdateWrapper);
        }

    }

    public static HashMap<String, String> JsonObjectToHashMap(JSONObject jsonObj) {
        HashMap<String, String> data = new HashMap<String, String>();
        for (String key : jsonObj.keySet()) {
            data.put(key, jsonObj.getStr(key));
        }
        return data;
    }

}

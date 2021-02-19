package top.hcode.hoj.remoteJudge.submit;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.remoteJudge.result.RemoteJudgeResultDispatcher;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeFactory;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.RedisUtils;

import java.util.HashMap;

@Component
@Slf4j
public class RemoteJudgeSubmitReceiver implements MessageListener {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RemoteJudgeResultDispatcher remoteJudgeResultDispatcher;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Override
    public void onMessage(Message message, byte[] bytes) {
//        log.debug("RemoteJudgeSubmitReceiver获取到消息{}", Arrays.toString(message.getBody()));
        String source = (String) redisUtils.lrPop(Constants.RemoteJudge.JUDGE_WAITING_SUBMIT_QUEUE.getName());
        // 如果竞争不到提交队列，结束
        if (source == null) {
            return;
        }
        JSONObject task = JSONUtil.parseObj(source);
        Long remotePid = task.getLong("remotePid");
        Long submitId = task.getLong("submitId");
        String uid = task.getStr("uid");
        Long cid = task.getLong("cid");
        Long pid = task.getLong("pid");
        String remoteJudge = task.getStr("remoteJudge");
        String language = task.getStr("language");
        String userCode = task.getStr("userCode");
        String username = task.getStr("username");
        String password = task.getStr("password");
        RemoteJudgeStrategy remoteJudgeStrategy = RemoteJudgeFactory.selectJudge(remoteJudge);
        // 获取不到对应的题库或者题库写错了
        if (remoteJudgeStrategy == null) {
            log.error("暂不支持该{}题库---------------->请求失败", remoteJudge);
            return;
        }
        Long resultSubmitId = -1L;
        try {
            resultSubmitId = remoteJudgeStrategy.submit(username, password, remotePid, language, userCode);
        } catch (Exception e) {
            log.error("网络错误，提交失败");
        }

        // 提交成功与失败都要把账号放回list
        JSONObject account = new JSONObject();
        account.set("username", username);
        account.set("password", password);
        redisUtils.llPush(Constants.RemoteJudge.getListNameByOJName(remoteJudge), JSONUtil.toJsonStr(account));

        // TODO 提交失败 前端手动按按钮再次提交 修改状态 STATUS_SUBMITTED_FAILED
        if (resultSubmitId < 0) {
            // 更新此次提交状态为提交失败！
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.set("status", Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                    .eq("submit_id", submitId);
            judgeService.update(judgeUpdateWrapper);
            log.error("网络错误---------------->获取不到提交ID");
            return;
        }


        // 提交成功顺便更新状态为-->STATUS_JUDGING 判题中...
        judgeService.updateById(new Judge().setSubmitId(submitId).setStatus(Constants.Judge.STATUS_JUDGING.getStatus()));
        try {
            remoteJudgeResultDispatcher.sendTask(remoteJudge, submitId, uid, cid, pid, resultSubmitId);
        } catch (Exception e) {
            log.error("调用redis消息发布异常,此次远程查询结果任务判为系统错误--------------->{}", e.getMessage());
        }

    }
}

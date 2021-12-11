package top.hcode.hoj.remoteJudge;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.entity.judge.Judge;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeFactory;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.service.impl.RemoteJudgeServiceImpl;
import top.hcode.hoj.util.Constants;

import java.util.Map;

@Component
@Slf4j(topic = "hoj")
@RefreshScope
public class RemoteJudgeToSubmit {

    @Autowired
    private RemoteJudgeGetResult remoteJudgeGetResult;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private RemoteJudgeServiceImpl remoteJudgeService;


    @Value("${hoj-judge-server.name}")
    private String name;

    @Async
    public void sendTask(String username, String password, String remoteJudge,
                         String remotePid, Long submitId,
                         String uid, Long cid, Long pid,
                         String language, String userCode,
                         String serverIp, Integer serverPort) {
        log.info("Ready Send Task to RemoteJudge[{}] => submit_id: [{}], uid: [{}]," +
                        " pid: [{}], vjudge_username: [{}], vjudge_password: [{}]",
                remoteJudge, submitId, uid, pid, username, password);

        RemoteJudgeStrategy remoteJudgeStrategy = RemoteJudgeFactory.selectJudge(remoteJudge);
        // 获取不到对应的题库或者题库写错了
        if (remoteJudgeStrategy == null) {
            log.error("Remote Judge 暂不支持该题库---------------->[{}]", remoteJudge);
            // 更新此次提交状态为系统失败！
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.set("status", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus())
                    .set("error_message", "The judge server does not support this oj:" + remoteJudge)
                    .eq("submit_id", submitId);
            judgeService.update(judgeUpdateWrapper);
            return;
        }

        Map<String, Object> submitResult = null;
        String errLog = null;
        try {
            submitResult = remoteJudgeStrategy.submit(username, password, remotePid, language, userCode);
        } catch (Exception e) {
            log.error("Submit Failed! Error:", e);
            errLog = e.getMessage();
        }

        // 提交失败 前端手动按按钮再次提交 修改状态 STATUS_SUBMITTED_FAILED
        if (submitResult == null || (Long) submitResult.getOrDefault("runId", -1L) == -1L) {
            // 将使用的账号放回对应列表
            log.error("[{}] Submit Failed! Begin to return the account to other task!", remoteJudge);
            remoteJudgeService.changeAccountStatus(remoteJudge, username);
            if (remoteJudge.equals(Constants.RemoteJudge.GYM_JUDGE.getName())
                    || remoteJudge.equals(Constants.RemoteJudge.CF_JUDGE.getName())) {
                // 对CF特殊，归还账号及判题机权限
                log.error("[{}] Submit Failed! Begin to return the Server Status to other task!", remoteJudge);
                remoteJudgeService.changeServerSubmitCFStatus(serverIp, serverPort);
            }

            // 更新此次提交状态为提交失败！
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.set("status", Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
                    .set("error_message", errLog)
                    .eq("submit_id", submitId);
            judgeService.update(judgeUpdateWrapper);
            // 更新其它表
            judgeService.updateOtherTable(submitId,
                    Constants.Judge.STATUS_SYSTEM_ERROR.getStatus(),
                    cid,
                    uid,
                    pid,
                    null,
                    null);
            return;
        }

        Long vjudgeSubmitId = (Long) submitResult.get("runId");

        // 提交成功顺便更新状态为-->STATUS_PENDING 判题中...
        judgeService.updateById(new Judge()
                .setSubmitId(submitId)
                .setStatus(Constants.Judge.STATUS_PENDING.getStatus())
                .setVjudgeSubmitId(vjudgeSubmitId)
                .setVjudgeUsername(username)
                .setVjudgePassword(password)
                .setJudger(name)
        );

        log.info("[{}] Submit Successfully! The submit_id of remote judge is [{}]. Waiting the result of the task!",
                vjudgeSubmitId, remoteJudge);

        // 调用获取远程判题结果
        remoteJudgeGetResult.sendTask(remoteJudge, username, password, submitId, uid, cid, pid,
                vjudgeSubmitId, (String) submitResult.get("cookies"), serverIp, serverPort);

    }
}

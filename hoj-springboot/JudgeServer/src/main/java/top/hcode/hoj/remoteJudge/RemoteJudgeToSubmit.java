package top.hcode.hoj.remoteJudge;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.RemoteJudgeAccount;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeFactory;
import top.hcode.hoj.remoteJudge.task.RemoteJudgeStrategy;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.service.impl.RemoteJudgeAccountServiceImpl;
import top.hcode.hoj.util.Constants;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j(topic = "hoj")
public class RemoteJudgeToSubmit {

    @Autowired
    private RemoteJudgeAccountServiceImpl remoteJudgeAccountService;

    @Autowired
    private RemoteJudgeGetResult remoteJudgeGetResult;

    @Autowired
    private JudgeServiceImpl judgeService;


    @Value("${hoj-judge-server.name}")
    private String name;


    public void sendTask(String username, String password, String remoteJudge, String remotePid, Long submitId,
                         String uid, Long cid, Long pid, String language, String userCode) {

        RemoteJudgeStrategy remoteJudgeStrategy = RemoteJudgeFactory.selectJudge(remoteJudge);
        // 获取不到对应的题库或者题库写错了
        if (remoteJudgeStrategy == null) {
            log.error("暂不支持该{}题库---------------->请求失败", remoteJudge);
            // 更新此次提交状态为系统失败！
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.set("status", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus())
                    .set("error_message", "The judge server does not support this oj:" + remoteJudge)
                    .eq("submit_id", submitId);
            judgeService.update(judgeUpdateWrapper);
            return;
        }

        Map<String, Object> submitResult = null;
        try {
            submitResult = remoteJudgeStrategy.submit(username, password, remotePid, language, userCode);
        } catch (Exception e) {
            log.error(remoteJudge + "的远程提交发生异常---------->{}", e.getMessage());
        } finally {
            // 将使用的账号放回对应列表

            UpdateWrapper<RemoteJudgeAccount> remoteJudgeAccountUpdateWrapper = new UpdateWrapper<>();
            remoteJudgeAccountUpdateWrapper.set("status", true)
                    .eq("oj", remoteJudge)
                    .eq("username", username)
                    .eq("password", password);
            boolean isOk = remoteJudgeAccountService.update(remoteJudgeAccountUpdateWrapper);
            if (!isOk) {
                log.error("远程判题：修正账号为可用状态失败----------->{}", "username:" + username + ",password:" + password);
            }

        }

        // TODO 提交失败 前端手动按按钮再次提交 修改状态 STATUS_SUBMITTED_FAILED
        if (submitResult == null || (Long) submitResult.getOrDefault("runId", -1L) == -1L) {
            // 更新此次提交状态为提交失败！
            UpdateWrapper<Judge> judgeUpdateWrapper = new UpdateWrapper<>();
            judgeUpdateWrapper.set("status", Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus())
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
            log.error("网络错误---------------->获取不到提交ID");
            return;
        }

        // 提交成功顺便更新状态为-->STATUS_JUDGING 判题中...
        judgeService.updateById(new Judge()
                .setSubmitId(submitId)
                .setStatus(Constants.Judge.STATUS_JUDGING.getStatus())
                .setJudger(name)
        );

        // 调用获取远程判题结果
        remoteJudgeGetResult.sendTask(remoteJudge, username, submitId, uid, cid, pid,
                (Long) submitResult.get("runId"), (String) submitResult.get("cookies"));

    }
}

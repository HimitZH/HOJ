package top.hcode.hoj.judge.remote;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.hcode.hoj.judge.JudgeServerUtils;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.JudgeServer;
import top.hcode.hoj.pojo.entity.RemoteJudgeAccount;
import top.hcode.hoj.pojo.entity.ToJudge;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.service.impl.RemoteJudgeAccountServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Async
public class RemoteJudgeReceiver {


    @Autowired
    private RemoteJudgeDispatcher remoteJudgeDispatcher;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private JudgeServerUtils judgeServerUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RemoteJudgeAccountServiceImpl remoteJudgeAccountService;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void processWaitingTask() {
        // 如果队列中还有任务，则继续处理
        if (redisUtils.lGetListSize(Constants.Judge.STATUS_REMOTE_JUDGE_WAITING_HANDLE.getName()) > 0) {
            String taskJsonStr = (String) redisUtils.lrPop(Constants.Judge.STATUS_REMOTE_JUDGE_WAITING_HANDLE.getName());
            // 再次检查
            if (taskJsonStr != null) {
                handleJudgeMsg(taskJsonStr);
            }
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void handleJudgeMsg(String taskJsonStr) {

        JSONObject task = JSONUtil.parseObj(taskJsonStr);

        Long submitId = task.getLong("submitId");
        String token = task.getStr("token");
        Long pid = task.getLong("pid");
        String remoteJudge = task.getStr("remoteJudge");
        Boolean isContest = task.getBool("isContest");
        Integer tryAgainNum = task.getInt("tryAgainNum");

        String remoteOJName = remoteJudge.split("-")[0].toUpperCase();

        // 过滤出当前远程oj可用的账号列表
        QueryWrapper<RemoteJudgeAccount> remoteJudgeAccountQueryWrapper = new QueryWrapper<>();
        remoteJudgeAccountQueryWrapper
                .eq("status", true)
                .eq("oj", remoteOJName);

        List<RemoteJudgeAccount> remoteJudgeAccountList = remoteJudgeAccountService.list(remoteJudgeAccountQueryWrapper);

        if (remoteJudgeAccountList.size() > 0) {
            RemoteJudgeAccount account = null;
            for (RemoteJudgeAccount remoteJudgeAccount : remoteJudgeAccountList) {
                remoteJudgeAccount.setStatus(false);
                boolean isOk = remoteJudgeAccountService.updateById(remoteJudgeAccount);
                if (isOk) {
                    account = remoteJudgeAccount;
                    break;
                }
            }

            if (account != null) { // 如果获取到账号

                Judge judge = judgeService.getById(submitId);
                // 调用判题服务
                judgeServerUtils.dispatcher("judge", "/remote-judge", new ToJudge()
                        .setJudge(judge)
                        .setToken(token)
                        .setRemoteJudge(remoteJudge)
                        .setUsername(account.getUsername())
                        .setPassword(account.getPassword())
                        .setTryAgainNum(tryAgainNum));
                // 如果队列中还有任务，则继续处理
                processWaitingTask();

            } else {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                remoteJudgeDispatcher.sendTask(submitId, pid, token, remoteJudge, isContest, tryAgainNum + 1);
            }
        } else {
            if (tryAgainNum >= 30) {
                // 获取调用多次失败可能为系统忙碌，判为提交失败
                Judge judge = new Judge();
                judge.setSubmitId(submitId);
                judge.setStatus(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus());
                judge.setErrorMessage("Failed to connect the judgeServer. Please resubmit this submission again!");
                judgeService.updateById(judge);
            } else {

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                remoteJudgeDispatcher.sendTask(submitId, pid, token, remoteJudge, isContest, tryAgainNum + 1);
            }
        }

    }
}

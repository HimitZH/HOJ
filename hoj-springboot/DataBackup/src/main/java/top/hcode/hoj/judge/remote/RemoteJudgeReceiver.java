package top.hcode.hoj.judge.remote;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.hcode.hoj.judge.Dispatcher;
import top.hcode.hoj.pojo.entity.Judge;
import top.hcode.hoj.pojo.entity.RemoteJudgeAccount;
import top.hcode.hoj.pojo.entity.ToJudge;
import top.hcode.hoj.service.impl.JudgeServiceImpl;
import top.hcode.hoj.service.impl.ProblemServiceImpl;
import top.hcode.hoj.service.impl.RemoteJudgeAccountServiceImpl;
import top.hcode.hoj.utils.Constants;
import top.hcode.hoj.utils.RedisUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RemoteJudgeReceiver {


    @Autowired
    private RemoteJudgeDispatcher remoteJudgeDispatcher;

    @Autowired
    private JudgeServiceImpl judgeService;

    @Autowired
    private Dispatcher dispatcher;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RemoteJudgeAccountServiceImpl remoteJudgeAccountService;

    @Resource
    private ApplicationContext applicationContext;

    @Async("judgeTaskAsyncPool")
    public void processWaitingTask() {
        // 如果队列中还有任务，则继续处理
        if (redisUtils.lGetListSize(Constants.Judge.STATUS_REMOTE_JUDGE_WAITING_HANDLE.getName()) > 0) {
            String taskJsonStr = (String) redisUtils.lrPop(Constants.Judge.STATUS_REMOTE_JUDGE_WAITING_HANDLE.getName());
            // 再次检查
            if (taskJsonStr != null) {

                JSONObject task = JSONUtil.parseObj(taskJsonStr);

                Judge judge = task.get("judge", Judge.class);
                String token = task.getStr("token");
                String remoteJudgeProblem = task.getStr("remoteJudgeProblem");
                Boolean isContest = task.getBool("isContest");
                Integer tryAgainNum = task.getInt("tryAgainNum");
                Boolean isHasSubmitIdRemoteReJudge = task.getBool("isHasSubmitIdRemoteReJudge");

                String remoteOJName = remoteJudgeProblem.split("-")[0].toUpperCase();

                applicationContext.getBean(RemoteJudgeReceiver.class)
                        .handleJudgeMsg(judge,
                                token,
                                remoteJudgeProblem,
                                isContest,
                                tryAgainNum,
                                isHasSubmitIdRemoteReJudge,
                                remoteOJName);
            }
        }
    }

    @Transactional
    public void handleJudgeMsg(Judge judge, String token, String remoteJudgeProblem, Boolean isContest, Integer tryAgainNum,
                               Boolean isHasSubmitIdRemoteReJudge, String remoteOJName) {

        // 过滤出当前远程oj可用的账号列表
        QueryWrapper<RemoteJudgeAccount> remoteJudgeAccountQueryWrapper = new QueryWrapper<>();
        remoteJudgeAccountQueryWrapper
                .eq("status", true)
                .eq("oj", remoteOJName)
                .last("for update"); // 开启悲观锁

        List<RemoteJudgeAccount> remoteJudgeAccountList = remoteJudgeAccountService.list(remoteJudgeAccountQueryWrapper);

        boolean isNeedAccountRejudge = remoteOJName.equals(Constants.RemoteOJ.POJ.getName());

        boolean isHasAccountRejudge = false;

        if (remoteJudgeAccountList.size() > 0) {
            RemoteJudgeAccount account = null;
            for (RemoteJudgeAccount remoteJudgeAccount : remoteJudgeAccountList) {
                // 已有submitId的重判不需要在获取账号（除POJ外）
                if (isHasSubmitIdRemoteReJudge) {
                    // POJ已有submitId的重判需要使用原来的账号获取结果
                    if (isNeedAccountRejudge) {
                        if (remoteJudgeAccount.getUsername().equals(judge.getVjudgeUsername())) {
                            judge.setVjudgePassword(remoteJudgeAccount.getPassword()); // 避免账号改密码
                            isHasAccountRejudge = true;
                            remoteJudgeAccount.setStatus(false);
                            boolean isOk = remoteJudgeAccountService.updateById(remoteJudgeAccount);
                            if (isOk) {
                                account = remoteJudgeAccount;
                                break;
                            }
                        }
                    }

                } else {
                    remoteJudgeAccount.setStatus(false);
                    boolean isOk = remoteJudgeAccountService.updateById(remoteJudgeAccount);
                    if (isOk) {
                        account = remoteJudgeAccount;
                        break;
                    }
                }
            }

            if (isHasSubmitIdRemoteReJudge) {
                ToJudge toJudge = new ToJudge();
                toJudge.setJudge(judge)
                        .setToken(token)
                        .setRemoteJudgeProblem(remoteJudgeProblem)
                        .setIsHasSubmitIdRemoteReJudge(true)
                        .setTryAgainNum(tryAgainNum);
                if (isNeedAccountRejudge) {
                    if (account != null) {
                        toJudge.setUsername(account.getUsername())
                                .setPassword(account.getPassword());
                    } else if (!isHasAccountRejudge) { // poj以往的账号丢失了，那么只能重新从头到尾提交
                        remoteJudgeDispatcher.sendTask(judge, token, remoteJudgeProblem, isContest,
                                tryAgainNum + 1, false);
                        return;
                    }
                }
                // 调用判题服务
                dispatcher.dispatcher("judge", "/remote-judge", toJudge);
                // 如果队列中还有任务，则继续处理
                processWaitingTask();
            } else if (account != null) { // 如果获取到账号
                // 调用判题服务
                dispatcher.dispatcher("judge", "/remote-judge", new ToJudge()
                        .setJudge(judge)
                        .setToken(token)
                        .setRemoteJudgeProblem(remoteJudgeProblem)
                        .setIsHasSubmitIdRemoteReJudge(false)
                        .setUsername(account.getUsername())
                        .setPassword(account.getPassword())
                        .setTryAgainNum(tryAgainNum));
                // 如果队列中还有任务，则继续处理
                processWaitingTask();

            } else {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                remoteJudgeDispatcher.sendTask(judge, token, remoteJudgeProblem, isContest,
                        tryAgainNum + 1, false);
            }
        } else {
            if (tryAgainNum >= 30) {
                // 获取调用多次失败可能为系统忙碌，判为提交失败
                judge.setStatus(Constants.Judge.STATUS_SUBMITTED_FAILED.getStatus());
                judge.setErrorMessage("Failed to connect the judgeServer. Please resubmit this submission again!");
                judgeService.updateById(judge);
            } else {

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                remoteJudgeDispatcher.sendTask(judge, token, remoteJudgeProblem, isContest,
                        tryAgainNum + 1, isHasSubmitIdRemoteReJudge);
            }
        }

    }
}

package top.hcode.hoj.remoteJudge.result;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.hcode.hoj.util.Constants;
import top.hcode.hoj.util.RedisUtils;

@Component
@Slf4j
public class RemoteJudgeResultDispatcher {

    private final RedisUtils redisUtils;

    public RemoteJudgeResultDispatcher(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    /**
     * @param remoteJudge    远程判题oj名字
     * @param resultSubmitId 远程判题提交后返回的提交ID
     * @param submitId       此次提交在HOJ的judge表中的主键，主要用于更新提交结果
     * @MethodName sendTask
     * @Description TODO
     * @Return
     * @Since 2021/2/12
     */
    public void sendTask(String remoteJudge, Long submitId, String uid, Long cid, Long pid, Long resultSubmitId) throws Exception {
        JSONObject task = new JSONObject();
        task.set("submitId", submitId);
        task.set("uid", uid);
        task.set("cid", cid);
        task.set("pid", pid);
        task.set("resultSubmitId", resultSubmitId);
        task.set("remoteJudge", remoteJudge);

        // 提醒判题机有待查询结果题目
        redisUtils.sendMessage(Constants.RemoteJudge.JUDGE_RESULT_HANDLER.getName(), "New Result Query Added");
        // 队列中插入待判ID和题库名称
        redisUtils.lrPush(Constants.RemoteJudge.JUDGE_WAITING_RESULT_QUEUE.getName(), JSONUtil.toJsonStr(task));
    }
}

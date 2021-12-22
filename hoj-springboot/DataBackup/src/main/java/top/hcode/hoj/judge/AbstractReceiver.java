package top.hcode.hoj.judge;

/**
 * @Author: Himit_ZH
 * @Date: 2021/12/22 12:40
 * @Description:
 */

public abstract class AbstractReceiver {

    public void handleWaitingTask(String... queues) {
        for (String queue : queues) {
            String taskJsonStr = getTaskByRedis(queue);
            if (taskJsonStr != null) {
                handleJudgeMsg(taskJsonStr);
            }
        }
    }

    public abstract String getTaskByRedis(String queue);

    public abstract void handleJudgeMsg(String taskJsonStr);
}
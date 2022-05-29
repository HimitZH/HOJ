package top.hcode.hoj.judge;

/**
 * @Author: Himit_ZH
 * @Date: 2021/12/22 12:40
 * @Description:
 */

public abstract class AbstractReceiver {

    public void handleWaitingTask(String... queues) {
        for (String queue : queues) {
            String taskStr = getTaskByRedis(queue);
            if (taskStr != null) {
                handleJudgeMsg(taskStr, queue);
            }
        }
    }

    public abstract String getTaskByRedis(String queue);

    public abstract void handleJudgeMsg(String taskStr, String queueName);

}
package top.hcode.hoj.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: Himit_ZH
 * @Date: 2020/11/6 23:36
 * @Description:
 */
@Configuration
public class AsyncTaskConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 线程池维护线程的最少数量
        taskExecutor.setCorePoolSize(10);
        // 线程池维护线程的最大数量
        taskExecutor.setMaxPoolSize(50);
        // 缓存队列
        taskExecutor.setQueueCapacity(200);
        //活跃时间
        taskExecutor.setKeepAliveSeconds(10);
        // 对拒绝task的处理策略
        //(1) 默认的ThreadPoolExecutor.AbortPolicy   处理程序遭到拒绝将抛出运行时RejectedExecutionException;
        //(2) ThreadPoolExecutor.CallerRunsPolicy 线程调用运行该任务的 execute 本身。此策略提供简单的反馈控制机制，能够减缓新任务的提交速度
        //(3) ThreadPoolExecutor.DiscardPolicy  不能执行的任务将被删除;
        //(4) ThreadPoolExecutor.DiscardOldestPolicy  如果执行程序尚未关闭，则位于工作队列头部的任务将被删除，然后重试执行程序（如果再次失败，则重复此过程）
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程名前缀,方便排查问题
        taskExecutor.setThreadNamePrefix("order-send-thread-");
        // 注意一定要初始化
        taskExecutor.initialize();

        return taskExecutor;

    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
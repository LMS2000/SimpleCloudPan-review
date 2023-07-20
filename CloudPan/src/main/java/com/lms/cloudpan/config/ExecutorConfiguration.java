package com.lms.cloudpan.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 大忽悠
 * @create 2023/3/8 9:12
 */
//自定义线程池
@Configuration
public class ExecutorConfiguration {

    @Bean("asyncExecutor")
    public Executor asyncExecutor() {
        return new ThreadPoolExecutor(2, 20, 60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100), new ThreadFactory() {
            private final AtomicInteger adder = new AtomicInteger(1);

            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r,"async-pool-thread-" + adder.getAndIncrement());
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }
}

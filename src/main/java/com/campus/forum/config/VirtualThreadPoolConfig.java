package com.campus.forum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 虚拟线程池配置类
 * 专门用于高并发预约模块（图书馆和体育馆）
 */
@Configuration
@EnableAsync
public class VirtualThreadPoolConfig {

    /**
     * 虚拟线程池（Platform + Virtual 结合）
     */
    @Bean("virtualThreadPool")
    public Executor virtualThreadPool() {
        // 创建平台线程池作为载体
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("platform-");
        executor.initialize();

        // 包装为虚拟线程执行器
        return Executors.newThreadPerTaskExecutor(new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                return Thread.ofVirtual()
                        .name("virtual-reservation-" + threadNumber.getAndIncrement())
                        .unstarted(r);
            }
        });
    }
}

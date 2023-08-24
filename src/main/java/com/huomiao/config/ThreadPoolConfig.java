package com.huomiao.config;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

/**
 * Copyright: Copyright (C) 2022, Inc. All rights reserved.
 *
 * @author: zixuan.yang
 * @since: 2022/6/22 14:40
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    final int core = Runtime.getRuntime().availableProcessors();
    /**
     * ttl 线程池
     * 局部变量在线程池应用的问题
     * 生命周期随Spring容器销毁  禁止手动销毁
     * @return
     */
    @Bean(name = "ttlExecutorService")
    public Executor ttlExecutorService() {

        ExecutorService executorService =
                new ThreadPoolExecutor(core*2, core*10, 2, TimeUnit.SECONDS, new LinkedTransferQueue<>(),
                        new ThreadFactoryBuilder().setNamePrefix("HUOMIAO-TTL-").build(), new ThreadPoolExecutor.CallerRunsPolicy());
        return (executorService);

    }

    @Bean(name = "cutTaskExecutor")
    public Executor cutTaskExecutor() {
        ExecutorService executorService =
                new ThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS, new LinkedTransferQueue<>(),
                        new ThreadFactoryBuilder().setNamePrefix("HM-TASK-").build(), new ThreadPoolExecutor.CallerRunsPolicy());
        return (executorService);

    }

}

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

    /**
     * ttl 线程池
     * 局部变量在线程池应用的问题
     * 生命周期随Spring容器销毁  禁止手动销毁
     * @return
     */
    @Bean(name = "ttlExecutorService")
    public Executor ttlExecutorService() {
        int core = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService =
                new ThreadPoolExecutor(core*2, core*8, 2, TimeUnit.SECONDS, new LinkedBlockingQueue(50000),
                        new ThreadFactoryBuilder().setNamePrefix("HUOMIAO-THREAD"+"-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy());
        return (executorService);

    }

}

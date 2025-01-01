//package com.example.SS2_Backend.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Executor;
//import java.util.concurrent.ThreadPoolExecutor;
//
//@EnableAsync
//@Configuration
//public class AsyncConfig {
//    @Bean(name = "taskExecutor")
//    public Executor taskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(10 * Runtime.getRuntime().availableProcessors());
//        executor.setMaxPoolSize(10 * Runtime.getRuntime().availableProcessors());
//        executor.setQueueCapacity(5);
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
//        executor.setThreadNamePrefix("Async-");
//        executor.initialize();
//        return executor;
//    }
//}

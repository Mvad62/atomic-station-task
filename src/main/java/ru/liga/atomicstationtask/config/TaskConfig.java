package ru.liga.atomicstationtask.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableAsync
@EnableScheduling
public class TaskConfig {

    @Bean
    public ThreadPoolTaskExecutor regulatorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Минимальное количество потоков
        executor.setMaxPoolSize(20);  // Максимальное количество потоков
        executor.setQueueCapacity(500); // Размер очереди
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

    // Настройка пула потоков для шедулеров
    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);  // Количество потоков для шедулеров
        scheduler.setThreadNamePrefix("Scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}

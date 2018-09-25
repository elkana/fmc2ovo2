package com.ppu.fmc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadConfig {
	@Bean(name = "specificTaskExecutor")
	public TaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(100);// bisa menjalankan 100 thread sekaligus
		executor.setMaxPoolSize(1000);// batas reuse thread
		executor.setThreadNamePrefix("rclkn");
		executor.initialize();
		return executor;
	}
}

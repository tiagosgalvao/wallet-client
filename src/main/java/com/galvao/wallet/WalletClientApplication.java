package com.galvao.wallet;

import com.galvao.wallet.service.ConcurrentLoadingService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.atomic.AtomicReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class WalletClientApplication {

	private static final String KEY_USERS = "users";
	private static final String KEY_CONCURRENT_THREADS = "concurrent_threads_per_user";
	private static final String KEY_ROUNDS_PER_THREAD = "rounds_per_thread";

	public static void main(String[] args) {
		SpringApplication.run(WalletClientApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(ConcurrentLoadingService concurrentLoadingService) {
		log.info("START runner");
		AtomicReference<Long> users = new AtomicReference<>();
		AtomicReference<Long> concurrentThreadsPerUser = new AtomicReference<>();
		AtomicReference<Long> roundsPerThread = new AtomicReference<>();
		return args -> {
			for (String commandLine : args) {
				String[] params = commandLine.split(",");
				for (String param : params) {
					String[] keyValue = param.split("=");
					switch (keyValue[0]) {
						case KEY_USERS:
							users.getAndSet(Long.valueOf(keyValue[1]));
							break;
						case KEY_CONCURRENT_THREADS:
							concurrentThreadsPerUser.getAndSet(Long.valueOf(keyValue[1]));
							break;
						case KEY_ROUNDS_PER_THREAD:
							roundsPerThread.getAndSet(Long.valueOf(keyValue[1]));
							break;
						default:
							throw new RuntimeException("Error receiving args. Expected users, concurrent_threads_per_user and rounds_per_thread");
					}
				}
			}
			concurrentLoadingService.executor(users.get(), concurrentThreadsPerUser.get(), roundsPerThread.get());
		};
	}
}
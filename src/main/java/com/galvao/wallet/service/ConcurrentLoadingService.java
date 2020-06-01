package com.galvao.wallet.service;

import com.galvao.wallet.grpc.BalanceRequest;
import com.galvao.wallet.grpc.BalanceResponse;
import com.galvao.wallet.grpc.TransactionRequest;
import com.galvao.wallet.grpc.TransactionServiceGrpc;
import com.galvao.wallet.infrastructure.impl.UserAccountEntity;
import com.galvao.wallet.infrastructure.repository.UserAccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConcurrentLoadingService {

	private static final Double ONE_HUNDRED = 100d;
	private static final Double TWO_HUNDRED = 200d;
	private static final Double THREE_HUNDRED = 300d;

	private static final String STEP_1 = "Step 1";
	private static final String STEP_2 = "Step 2";
	private static final String STEP_3 = "Step 3";
	private static final String STEP_4 = "Step 4";
	private static final String STEP_5 = "Step 5";
	private static final String STEP_6 = "Step 6";
	private static final String STEP_7 = "Step 7";
	private static final String STEP_8 = "Step 8";

	@Value("${grpc-call.server.url}")
	private String grpcCalLUrl;

	@Value("${grpc-call.server.port}")
	private int grpcCallPort;

	@Autowired
	private UserAccountRepository userAccountRepository;

	private static void doWithdraw(TransactionServiceGrpc.TransactionServiceBlockingStub stub, String roundName, String step, Long userId, double amount,
	                               TransactionRequest.Currency currency) {
		String message = "threadName:{} - round:{} step:{} - withdraw user:{} amount:{} currency:{} - result:{}";
		String status = "Ok";
		try {
			TransactionRequest request = TransactionRequest.newBuilder().setUserId(userId).setAmount(amount).setCurrency(currency).build();
			stub.withdraw(request);
		} catch (StatusRuntimeException e) {
			status = e.getStatus().getDescription();
			log.debug(Status.INTERNAL.withCause(e).withDescription(e.getMessage()).asRuntimeException().toString(), e);
		}
		log.info(message, Thread.currentThread().getName(), roundName, step, userId, amount, currency, status);
	}

	private static void doDeposit(TransactionServiceGrpc.TransactionServiceBlockingStub stub, String roundName, String step, Long userId, double amount,
	                              TransactionRequest.Currency currency) {
		String message = "threadName:{} - round:{} step:{} - deposit user:{} amount:{} currency:{} - result:{}";
		String status = "OK";
		try {
			TransactionRequest request = TransactionRequest.newBuilder().setUserId(userId).setAmount(amount).setCurrency(currency).build();
			stub.deposit(request);
		} catch (StatusRuntimeException e) {
			status = e.getStatus().getDescription();
			log.debug(Status.INTERNAL.withCause(e).withDescription(e.getMessage()).asRuntimeException().toString(), e);
		}
		log.info(message, Thread.currentThread().getName(), roundName, step, userId, amount, currency, status);
	}

	private static BalanceResponse getBalance(TransactionServiceGrpc.TransactionServiceBlockingStub stub, String roundName, String step, Long userId) {
		String message = "threadName:{} - round:{} step:{} - balance user:{} - result:{}";
		String status = "OK";
		try {
			return stub.balance(BalanceRequest.newBuilder().setUserId(userId).build());
		} catch (StatusRuntimeException e) {
			status = e.getStatus().getDescription();
			log.debug(Status.INTERNAL.withCause(e).withDescription(e.getMessage()).asRuntimeException().toString(), e);
		}
		log.info(message, Thread.currentThread().getName(), roundName, step, userId, status);
		return BalanceResponse.newBuilder().build();
	}

	public void executor(Long concurrentUsers, Long concurrentThreadsPerUser, Long roundsPerThread) throws InterruptedException {
		ManagedChannel channel = ManagedChannelBuilder.forAddress(grpcCalLUrl, grpcCallPort).usePlaintext().build();
		TransactionServiceGrpc.TransactionServiceBlockingStub stub = TransactionServiceGrpc.newBlockingStub(channel);
		Iterable<UserAccountEntity> usersIt = userAccountRepository.findAll();
		List<UserAccountEntity> users = new ArrayList<>();
		usersIt.forEach(users::add);

		log.info(String.format("Creating Executor Service with thread pool Size of: %s, users: %s, threads: %s",
				concurrentUsers * concurrentThreadsPerUser,
				concurrentUsers,
				concurrentThreadsPerUser
		));
		ExecutorService executor = Executors.newFixedThreadPool(concurrentThreadsPerUser.intValue());
		List<Callable<Object>> todo = new ArrayList<>((int) (concurrentUsers * concurrentThreadsPerUser));
		for (int i = 0; i < concurrentUsers; i++) {
			List<Runnable> runnables = new ArrayList<>();
			for (int j = 0; j < concurrentThreadsPerUser; j++) {
				for (int k = 0; k < roundsPerThread; k++) {
					log.info("Submitting the tasks for execution...");
					int finalI = i;
					runnables.add(() -> getRound(stub, users.get(finalI).getId()));
				}
			}
			runnables.forEach(r -> todo.add(Executors.callable(r)));
		}
		executor.invokeAll(todo); // gives return ignoring
		executor.shutdown();
		channel.shutdown();
		System.exit(0);
	}

	private void getRound(TransactionServiceGrpc.TransactionServiceBlockingStub stub, Long userId) {
		int randInt = new Random().nextInt(2);
		switch (randInt) {
			case (0):
				log.debug("Picked roundA user:{}", userId);
				roundA(stub, userId);
				break;
			case (1):
				log.debug("Picked roundB user:{}", userId);
				roundB(stub, userId);
				break;
			case (2):
				log.debug("Picked roundC user:{}", userId);
				roundC(stub, userId);
				break;
			default:
				throw new RuntimeException("Error defining round.");
		}
	}

	private void roundA(TransactionServiceGrpc.TransactionServiceBlockingStub stub, Long userId) {
		final String ROUND_A = "A";
		doDeposit(stub, ROUND_A, STEP_1, userId, ONE_HUNDRED, TransactionRequest.Currency.USD);
		doWithdraw(stub, ROUND_A, STEP_2, userId, TWO_HUNDRED, TransactionRequest.Currency.USD);
		doDeposit(stub, ROUND_A, STEP_3, userId, ONE_HUNDRED, TransactionRequest.Currency.EUR);
		getBalance(stub, ROUND_A, STEP_4, userId);
		doWithdraw(stub, ROUND_A, STEP_5, userId, ONE_HUNDRED, TransactionRequest.Currency.USD);
		getBalance(stub, ROUND_A, STEP_6, userId);
		doWithdraw(stub, ROUND_A, STEP_7, userId, ONE_HUNDRED, TransactionRequest.Currency.USD);
	}

	private void roundB(TransactionServiceGrpc.TransactionServiceBlockingStub stub, Long userId) {
		final String ROUND_B = "B";
		doWithdraw(stub, ROUND_B, STEP_1, userId, ONE_HUNDRED, TransactionRequest.Currency.GBP);
		doDeposit(stub, ROUND_B, STEP_2, userId, THREE_HUNDRED, TransactionRequest.Currency.GBP);
		doWithdraw(stub, ROUND_B, STEP_3, userId, ONE_HUNDRED, TransactionRequest.Currency.GBP);
		doWithdraw(stub, ROUND_B, STEP_4, userId, ONE_HUNDRED, TransactionRequest.Currency.GBP);
		doWithdraw(stub, ROUND_B, STEP_5, userId, ONE_HUNDRED, TransactionRequest.Currency.GBP);
	}

	private void roundC(TransactionServiceGrpc.TransactionServiceBlockingStub stub, Long userId) {
		final String ROUND_C = "C";
		getBalance(stub, ROUND_C, STEP_1, userId);
		doDeposit(stub, ROUND_C, STEP_2, userId, ONE_HUNDRED, TransactionRequest.Currency.USD);
		doDeposit(stub, ROUND_C, STEP_3, userId, ONE_HUNDRED, TransactionRequest.Currency.USD);
		doWithdraw(stub, ROUND_C, STEP_4, userId, ONE_HUNDRED, TransactionRequest.Currency.USD);
		doDeposit(stub, ROUND_C, STEP_5, userId, ONE_HUNDRED, TransactionRequest.Currency.USD);
		getBalance(stub, ROUND_C, STEP_6, userId);
		doWithdraw(stub, ROUND_C, STEP_7, userId, TWO_HUNDRED, TransactionRequest.Currency.USD);
		getBalance(stub, ROUND_C, STEP_8, userId);
	}

}
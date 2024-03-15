package net.lecousin.commons.executors;

import static net.lecousin.commons.test.AssertTimeout.assertTimeout;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.lecousin.commons.events.Cancellable;

public abstract class AbstractLcExecutorTest {

	protected abstract LcExecutor getExecutor();
	
	private static class Task implements Runnable {
		private List<Long> callTimes = new LinkedList<>();
		private boolean error;
		
		public Task(boolean error) {
			this.error = error;
		}
		
		public Task() {
			this(false);
		}
		
		@Override
		public void run() {
			synchronized (callTimes) {
				callTimes.add(System.currentTimeMillis());
			}
			if (error) throw new RuntimeException("test error");
		}
	}
	
	@Test
	void test() {
		long start = System.currentTimeMillis();
		List<Cancellable> toCancel = new LinkedList<>();

		try {
			Task taskImmediate = new Task();
			getExecutor().execute(taskImmediate);
			
			Task taskScheduledOnce = new Task();
			getExecutor().schedule(taskScheduledOnce, Duration.ofSeconds(2));
			
			Task taskScheduledOnceCancelled = new Task();
			Cancellable ctaskScheduledOnceCancelled = getExecutor().schedule(taskScheduledOnce, Duration.ofSeconds(5));
			
			Task taskAtFixedRate = new Task();
			Cancellable ctaskAtFixedRate = getExecutor().scheduleAtFixedRate(taskAtFixedRate, Duration.ofSeconds(10));
			toCancel.add(ctaskAtFixedRate);
			
			Task taskAtFixedRateError = new Task(true);
			Cancellable ctaskAtFixedRateError = getExecutor().scheduleAtFixedRate(taskAtFixedRateError, Duration.ofSeconds(10));
			toCancel.add(ctaskAtFixedRateError);
		
			Task taskWithFixedDelay = new Task();
			Cancellable ctaskWithFixedDelay = getExecutor().scheduleWithFixedDelay(taskWithFixedDelay, Duration.ofSeconds(3));
			toCancel.add(ctaskWithFixedDelay);
			
			Task taskWithFixedDelayError = new Task(true);
			Cancellable ctaskWithFixedDelayError = getExecutor().scheduleWithFixedDelay(taskWithFixedDelayError, Duration.ofSeconds(3));
			toCancel.add(ctaskWithFixedDelayError);
			
			
			assertThat(ctaskScheduledOnceCancelled.cancel()).isTrue();
			
			// first, check the immediate task
			assertTimeout(() -> {
				assertThat(taskImmediate.callTimes).hasSize(1);
			}, 15000, 100);
			
			// check the scheduled once
			assertTimeout(() -> {
				assertThat(taskScheduledOnce.callTimes).hasSize(1);
			}, 15000, 500);
			
			// check task at fixed rate executed at least 2 times
			assertTimeout(() -> {
				assertThat(taskAtFixedRate.callTimes).hasSize(2);
				ctaskAtFixedRate.cancel();
			}, 60000, 1000);
			assertTimeout(() -> {
				assertThat(taskAtFixedRateError.callTimes.size()).isGreaterThanOrEqualTo(2);
				ctaskAtFixedRateError.cancel();
			}, 30000, 1000);
			
			// check task with fixed delay
			ctaskWithFixedDelay.cancel();
			ctaskWithFixedDelayError.cancel();
			assertThat(taskWithFixedDelay.callTimes.size()).isGreaterThan(4);
			assertThat(taskWithFixedDelay.callTimes.get(0)).isGreaterThanOrEqualTo(start + 3000);
			assertThat(taskWithFixedDelay.callTimes.get(1)).isGreaterThanOrEqualTo(taskWithFixedDelay.callTimes.get(0) + 3000);
			assertThat(taskWithFixedDelay.callTimes.get(2)).isGreaterThanOrEqualTo(taskWithFixedDelay.callTimes.get(1) + 3000);
			assertThat(taskWithFixedDelayError.callTimes.size()).isGreaterThan(4);
			assertThat(taskWithFixedDelayError.callTimes.get(0)).isGreaterThanOrEqualTo(start + 3000);
			assertThat(taskWithFixedDelayError.callTimes.get(1)).isGreaterThanOrEqualTo(taskWithFixedDelayError.callTimes.get(0) + 3000);
			assertThat(taskWithFixedDelayError.callTimes.get(2)).isGreaterThanOrEqualTo(taskWithFixedDelayError.callTimes.get(1) + 3000);
			
			// check again immediate task
			assertThat(taskImmediate.callTimes).hasSize(1).allMatch(c -> c >= start);
			
			// check again the scheduled once
			assertThat(taskScheduledOnce.callTimes).hasSize(1).allMatch(c -> c >= start + 2000);
			
			// check again the fixed rate task
			assertThat(taskAtFixedRate.callTimes.size()).isGreaterThanOrEqualTo(2).isLessThanOrEqualTo(3);
			
			// check again the cancelled task
			assertThat(taskScheduledOnceCancelled.callTimes).isEmpty();
		} finally {
			toCancel.forEach(c -> c.cancel());
		}
	}
	
}

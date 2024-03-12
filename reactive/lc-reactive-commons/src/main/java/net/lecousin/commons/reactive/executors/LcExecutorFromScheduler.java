package net.lecousin.commons.reactive.executors;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lecousin.commons.events.Cancellable;
import net.lecousin.commons.events.Cancellation;
import net.lecousin.commons.executors.LcExecutor;
import reactor.core.Disposable;
import reactor.core.scheduler.Scheduler;

/** LcExecutor delegating scheduling to a projectreactor Scheduler. */
@RequiredArgsConstructor
@Slf4j
public class LcExecutorFromScheduler implements LcExecutor {

	private final Scheduler scheduler;
	
	@Override
	public void execute(Runnable task) {
		scheduler.schedule(task);
	}
	
	@Override
	public Cancellable schedule(Runnable task, long delay) {
		Disposable d = scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
		return () -> {
			d.dispose();
			return d.isDisposed();
		};
	}
	
	@Override
	public Cancellable scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period) {
		Disposable d = scheduler.schedulePeriodically(task, initialDelay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
		return () -> {
			d.dispose();
			return d.isDisposed();
		};
	}
	
	@Override
	public Cancellable scheduleWithFixedDelay(Runnable task, Duration initialDelay, Duration delay) {
		Cancellation c = new Cancellation();
		c.setCancellation(
			schedule(() -> {
				if (c.isCancelled()) return;
				c.setCancellation(() -> true);
				try {
					task.run();
				} catch (Throwable t) {
					log.error("Uncaught exception in scheduled task {}", task, t);
				}
				if (c.isCancelled()) return;
				c.setCancellation(scheduleWithFixedDelay(task, delay, delay));
			}, initialDelay)
		);
		return c;
	}
	
}

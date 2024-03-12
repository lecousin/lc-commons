package net.lecousin.commons.executors;

import java.time.Duration;
import java.util.ServiceLoader;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lecousin.commons.events.Cancellable;

/**
 * There are 2 executors: one for CPU only tasks, and one for non-CPU tasks (using disk, network...).<br/>
 * By default it uses ExecutorService, but can be replaced using a LcExecutorInitializer.
 */
// CHECKSTYLE DISABLE: MagicNumber
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class LcExecutors {

	private static LcExecutor cpuExecutor = null;
	private static LcExecutor nonCpuExecutor = null;
	private static final Object CPU_LOCK = new Object();
	private static final Object NON_CPU_LOCK = new Object();
	
	static {
		ServiceLoader.load(LcExecutorInitializer.class).forEach(init -> { /* nothing */ });
	}
	
	/** Change the default CPU executor.
	 * 
	 * @param executor new executor
	 */
	public static void setCpuExecutor(LcExecutor executor) {
		synchronized (CPU_LOCK) {
			cpuExecutor = executor;
		}
	}
	
	/** Change the default non-CPU executor.
	 * 
	 * @param executor new executor
	 */
	public static void setNonCpuExecutor(LcExecutor executor) {
		synchronized (NON_CPU_LOCK) {
			nonCpuExecutor = executor;
		}
	}
	
	/** @return the executor to execute tasks using only the CPU. */
	public static LcExecutor getCpu() {
		if (cpuExecutor != null) return cpuExecutor;
		synchronized (CPU_LOCK) {
			if (cpuExecutor != null) return cpuExecutor;
			cpuExecutor = createDefaultCpuExecutor();
		}
		return cpuExecutor;
	}
	
	/** @return the executor to execute tasks not only using CPU (disk, network...). */
	public static LcExecutor getNonCpu() {
		if (nonCpuExecutor != null) return nonCpuExecutor;
		synchronized (NON_CPU_LOCK) {
			if (nonCpuExecutor != null) return nonCpuExecutor;
			nonCpuExecutor = createDefaultNonCpuExecutor();
		}
		return nonCpuExecutor;
	}
	
	private static LcExecutor createDefaultCpuExecutor() {
		ScheduledThreadPoolExecutor service = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
		service.setMaximumPoolSize(Runtime.getRuntime().availableProcessors());
		log.info("CPU Executor initialized with {} thread(s)", Runtime.getRuntime().availableProcessors());
		return createJavaExecutor(service);
	}

	private static LcExecutor createDefaultNonCpuExecutor() {
		ScheduledThreadPoolExecutor service = new ScheduledThreadPoolExecutor(1);
		service.setMaximumPoolSize(100);
		log.info("Non-CPU Executor initialized with 1 to 100 threads");
		return createJavaExecutor(service);
	}
	
	/** Create an LcExecutor from a ScheduledExecutorService.
	 * 
	 * @param service the service to wrap
	 * @return the LcExecutor wrapping the service
	 */
	public static LcExecutor createJavaExecutor(ScheduledExecutorService service) {
		return new LcExecutor() {
			@Override
			public void execute(Runnable task) {
				service.execute(task);
			}
			
			@Override
			public Cancellable schedule(Runnable task, long delay) {
				ScheduledFuture<?> future = service.schedule(task, delay, TimeUnit.MILLISECONDS);
				return () -> future.cancel(false);
			}
			
			@Override
			public Cancellable scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period) {
				Runnable nonFailableTask = () -> {
					try {
						task.run();
					} catch (Throwable t) {
						log.error("Uncaught exception in scheduled task {}", task, t);
					}
				};
				ScheduledFuture<?> future = service.scheduleAtFixedRate(nonFailableTask, initialDelay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
				return () -> future.cancel(false);
			}
			
			@Override
			public Cancellable scheduleWithFixedDelay(Runnable task, Duration initialDelay, Duration delay) {
				Runnable nonFailableTask = () -> {
					try {
						task.run();
					} catch (Throwable t) {
						log.error("Uncaught exception in scheduled task {}", task, t);
					}
				};
				ScheduledFuture<?> future = service.scheduleWithFixedDelay(nonFailableTask, initialDelay.toMillis(), delay.toMillis(), TimeUnit.MILLISECONDS);
				return () -> future.cancel(false);
			}
		};
	}
	
}

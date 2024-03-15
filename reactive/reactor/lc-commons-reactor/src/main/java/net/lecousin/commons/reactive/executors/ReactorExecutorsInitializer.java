package net.lecousin.commons.reactive.executors;

import lombok.NoArgsConstructor;
import net.lecousin.commons.executors.LcExecutorInitializer;
import net.lecousin.commons.executors.LcExecutors;
import reactor.core.scheduler.Schedulers;

/**
 * Set LcExecutors to use parallel scheduler for CPU, and boundedElastic for non-CPU.
 */
@NoArgsConstructor
@SuppressWarnings("java:S1118")
public final class ReactorExecutorsInitializer implements LcExecutorInitializer {

	static {
		LcExecutors.setCpuExecutor(new LcExecutorFromScheduler(Schedulers.parallel()));
		LcExecutors.setNonCpuExecutor(new LcExecutorFromScheduler(Schedulers.boundedElastic()));
	}
	
}

package net.lecousin.commons.reactive.executors;

import static org.assertj.core.api.Assertions.assertThat;

import net.lecousin.commons.executors.AbstractLcExecutorTest;
import net.lecousin.commons.executors.LcExecutor;
import net.lecousin.commons.executors.LcExecutors;

public class TestLcExecutorWithReactor {

	public static class TestCPUExecutor extends AbstractLcExecutorTest {
		@Override
		protected LcExecutor getExecutor() {
			LcExecutor executor = LcExecutors.getCpu();
			assertThat(executor).isInstanceOf(LcExecutorFromScheduler.class);
			return executor;
		}
	}

	public static class TestNonCPUExecutor extends AbstractLcExecutorTest {
		@Override
		protected LcExecutor getExecutor() {
			LcExecutor executor = LcExecutors.getNonCpu();
			assertThat(executor).isInstanceOf(LcExecutorFromScheduler.class);
			return executor;
		}
	}
}

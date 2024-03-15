package net.lecousin.commons.executors;

public class TestDefaultLcExecutors {

	public static class TestCPUExecutor extends AbstractLcExecutorTest {
		@Override
		protected LcExecutor getExecutor() {
			return LcExecutors.getCpu();
		}
	}

	public static class TestNonCPUExecutor extends AbstractLcExecutorTest {
		@Override
		protected LcExecutor getExecutor() {
			return LcExecutors.getNonCpu();
		}
	}
	
}

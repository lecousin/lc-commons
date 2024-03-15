package net.lecousin.commons.test;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Utilities to check an assertion with timeout. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AssertTimeout {

	/** Check the assertions are fulfilled in the given timeout.
	 * @param assertions assertions
	 * @param timeout timeout in milliseconds
	 * @param checkInterval interval to check in milliseconds
	 */
	@SuppressWarnings("java:S1181")
	public static void assertTimeout(Runnable assertions, long timeout, long checkInterval) {
		Throwable lastException = null;
		long start = System.currentTimeMillis();
		do {
			try {
				assertions.run();
				return;
			} catch (Throwable t) {
				lastException = t;
			}
			try {
				Thread.sleep(checkInterval);
			} catch (InterruptedException e) {
				lastException = e;
				Thread.currentThread().interrupt();
				break;
			}
		} while (System.currentTimeMillis() - start < timeout);
		throw new AssertionError("Assertions not fulfilled in " + timeout + "ms.", lastException);
	}
	
	/** Wait for a delay, before to check the assertions.
	 * @param assertions assertions
	 * @param delay delay in milliseconds
	 */
	@SuppressWarnings("java:S112")
	public static void assertIn(Runnable assertions, long delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		assertions.run();
	}
	
}

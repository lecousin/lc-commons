package net.lecousin.commons.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import lombok.extern.slf4j.Slf4j;

/**
 * Log failing tests with display name.
 */
@Slf4j
public class LcTestWatcher implements TestWatcher {

	@Override
	public void testFailed(ExtensionContext context, Throwable cause) {
		if (cause != null)
			log.error("Test failed: {} {}", context.getClass(), context.getDisplayName(), cause);
	}
	
}

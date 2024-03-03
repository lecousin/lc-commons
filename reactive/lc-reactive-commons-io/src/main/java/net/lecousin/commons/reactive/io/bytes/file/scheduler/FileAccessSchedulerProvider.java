package net.lecousin.commons.reactive.io.bytes.file.scheduler;

import java.nio.file.Path;
import java.util.Optional;

import reactor.core.scheduler.Scheduler;

/**
 * Provide the Scheduler to use to perform file access operations.
 */
public interface FileAccessSchedulerProvider {

	/**
	 * @param path file
	 * @return the scheduler to use
	 */
	Scheduler getFileAccessScheduler(Path path);
	
	/**
	 * @return the singleton instance configured
	 */
	static FileAccessSchedulerProvider get() {
		return Initializer.INSTANCE;
	}
	
	/** Instantiate the provider. */
	@SuppressWarnings("java:S112")
	final class Initializer {
		
		/** Environment variable name to use to configure the class to instantiate. */
		public static final String ENV_PROVIDER_CLASS = "LC_REACTIVE_IO_FILE_ACCESS_SCHEDULER_PROVIDER";
		/** Property to use to configure the class to instantiate, if not configured through an environment variable. */
		public static final String PROPERTY_PROVIDER_CLASS = "lc.reactive.io.FileAccessSchedulerProvider";
		
		private static final FileAccessSchedulerProvider INSTANCE;
		
		private Initializer() {
			// no instance
		}
		
		static {
			String className = Optional.ofNullable(System.getenv(ENV_PROVIDER_CLASS))
				.or(() -> Optional.ofNullable(System.getProperty(PROPERTY_PROVIDER_CLASS)))
				.orElse(DefaultFileAccessSchedulerProvider.class.getName());
			try {
				INSTANCE = (FileAccessSchedulerProvider) Initializer.class.getClassLoader().loadClass(className).getConstructor().newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Unable to instantiate FileAccessSchedulerProvider", e);
			} 
		}
		
	}
	
}

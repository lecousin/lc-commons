package net.lecousin.commons.reactive.io.bytes.file.scheduler;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Default implementation, creating one single thread Scheduler by root directory
 * returned by {@link FileSystem#getRootDirectories()} on {@link FileSystems#getDefault()}.
 * <p>
 * An additional default scheduler, with a single thread, is created in case a Path which
 * is not under one of the root directories is accessed.
 * </p>
 */
public class DefaultFileAccessSchedulerProvider implements FileAccessSchedulerProvider {

	private final Map<Path, Scheduler> schedulers = new HashMap<>();
	private final Scheduler defaultScheduler = Schedulers.newSingle("default file access scheduler");
	
	/** Constructor. */
	public DefaultFileAccessSchedulerProvider() {
		FileSystems.getDefault().getRootDirectories().forEach(root -> schedulers.put(root, Schedulers.newSingle("File Access on " + root.toString())));
	}
	
	@Override
	public Scheduler getFileAccessScheduler(Path path) {
		for (Map.Entry<Path, Scheduler> e : schedulers.entrySet())
			if (path.startsWith(e.getKey()))
				return e.getValue();
		return defaultScheduler;
	}
}

package net.lecousin.commons.reactive.io.files;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * A FileTreeWalker, the reactive way.<br/>
 * Every file or directory is emitted through the WalkerEvent class.
 * Note that a sub-directory is emitted before going through its content. 
 */
public final class ReactiveFileTreeWalker {
	
	private ReactiveFileTreeWalker() {
		// no instance
	}
	
	/** Event. */
	@Data
	@AllArgsConstructor
	public static final class WalkerEvent {
		private Path path;
		private BasicFileAttributes attributes;
		private IOException error;
	}
	
	/** Start walking through the given directory, using the boundedElastic scheduler.
	 * 
	 * @param fromDir root directory
	 * @param visitor visitor
	 * @return events
	 * @see Files#walkFileTree(Path, FileVisitor)
	 */
	public static Flux<WalkerEvent> start(Path fromDir, FileVisitor<Path> visitor) {
		return start(fromDir, visitor, Schedulers.boundedElastic());
	}
	
	/** Start walking through the given directory, using the given scheduler to execute it.
	 * 
	 * @param fromDir root directory
	 * @param visitor visitor
	 * @param executeIn scheduler to use to execute {@link Files#walkFileTree(Path, FileVisitor)}
	 * @return events
	 * @see Files#walkFileTree(Path, FileVisitor)
	 */
	public static Flux<WalkerEvent> start(Path fromDir, FileVisitor<Path> visitor, Scheduler executeIn) {
		return Flux.create(sink -> executeIn.schedule(() -> {
			try {
				Files.walkFileTree(fromDir, new FileVisitor<Path>() {
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						FileVisitResult result = visitor.preVisitDirectory(dir, attrs);
						if (result != FileVisitResult.TERMINATE)
							sink.next(new WalkerEvent(dir, attrs, null));
						return result;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						FileVisitResult result = visitor.visitFile(file, attrs);
						if (result != FileVisitResult.TERMINATE) {
							sink.next(new WalkerEvent(file, attrs, null));
							return result;
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
						FileVisitResult result = visitor.visitFileFailed(file, exc);
						if (result != FileVisitResult.TERMINATE)
							sink.next(new WalkerEvent(file, null, exc));
						return result;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						return visitor.postVisitDirectory(fromDir, exc);
					}
				});
				sink.complete();
			} catch (IOException e) {
				sink.error(e);
			}
		}));
	}

}

package net.lecousin.commons.reactive.io.files;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lecousin.commons.reactive.io.files.scheduler.FileAccessSchedulerProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A FileTreeWalker, that emit an event by directory, with all its content.
 */
public final class ReactiveFileTreeWalkerByDirectory {
	
	private ReactiveFileTreeWalkerByDirectory() {
		// no instance
	}

	/** Event corresponding to a visited directory. */
	@Getter
	public static class DirectoryContent {
		private Path realPath;
		private Path relativePath;
		private Map<String, DirectoryElement> subDirectories = new HashMap<>();
		private Map<String, DirectoryElement> files = new HashMap<>();
		private List<Error> errors = new LinkedList<>();
	}
	
	/** File or directory. */
	@Getter
	public static class DirectoryElement {
		private Path realPath;
		private Path relativePath;
		private BasicFileAttributes attrs;
	}
	
	/** Error listing elements. */
	@Getter
	@AllArgsConstructor
	public static class Error {
		private Path realPath;
		private Path relativePath;
		private Throwable error;
	}
	
	/** Launch walking the given directory.
	 * 
	 * @param root root directory
	 * @return the flux of events
	 */
	public static Flux<DirectoryContent> walk(Path root) {
		return Flux.create(sink -> enterDirectory(root, Paths.get(""), sink).doFinally(s -> sink.complete()).subscribe());
	}
	
	private static Mono<Void> enterDirectory(Path realPath, Path relativePath, FluxSink<DirectoryContent> sink) {
		return listDirectoryContent(realPath, relativePath)
		.flatMapMany(content -> {
			sink.next(content);
			return Flux.fromIterable(content.subDirectories.values())
			.flatMap(d -> enterDirectory(d.realPath, d.relativePath, sink));
		}).then();
	}
	
	private static Mono<DirectoryContent> listDirectoryContent(Path realPath, Path relativePath) {
		return Mono.fromSupplier(() -> listDirectoryContentInDedicatedScheduler(realPath, relativePath))
			.subscribeOn(FileAccessSchedulerProvider.get().getFileAccessScheduler(realPath))
			.publishOn(Schedulers.parallel());
	}
	
	private static DirectoryContent listDirectoryContentInDedicatedScheduler(Path realPath, Path relativePath) {
		DirectoryContent result = new DirectoryContent();
		result.realPath = realPath;
		result.relativePath = relativePath;
		try (var stream = Files.newDirectoryStream(realPath)) {
			stream.forEach(child -> {
				String name = child.getFileName().toString();
				DirectoryElement element = new DirectoryElement();
				element.realPath = child;
				element.relativePath = relativePath.resolve(name);
				try {
					element.attrs = Files.readAttributes(child, BasicFileAttributes.class);
					if (element.attrs.isDirectory())
						result.subDirectories.put(name, element);
					else
						result.files.put(name, element);
				} catch (Exception error) {
					result.errors.add(new Error(element.realPath, element.relativePath, error));
				}
			});
		} catch (Exception e) {
			result.errors.add(new Error(realPath, relativePath, e));
		}
		return result;
	}
	
}

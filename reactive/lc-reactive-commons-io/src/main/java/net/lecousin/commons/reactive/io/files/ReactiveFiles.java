package net.lecousin.commons.reactive.io.files;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;

import net.lecousin.commons.reactive.io.files.scheduler.FileAccessSchedulerProvider;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/** Utilities for files, the reactive way. */
public final class ReactiveFiles {
	
	private ReactiveFiles() {
		// no instance
	}
	
	/**
	 * Create a directory if it does not exist.
	 * 
	 * @param path directory to create
	 * @param attrs an optional list of file attributes to set atomically when creating the directory
	 * @return the directory
	 * @see Files#createDirectory(Path, FileAttribute...)
	 */
	public static Mono<Path> createDirectory(Path path, FileAttribute<?>... attrs) {
		return Mono.just(path)
			.publishOn(FileAccessSchedulerProvider.get().getFileAccessScheduler(path))
			.flatMap(p -> Mono.fromCallable(() -> Files.createDirectory(path, attrs)))
			.publishOn(Schedulers.parallel());
	}
	
	/**
	 * Ensure a directory exists, creating any missing parent.
	 * @param path directory
	 * @return directory
	 */
	public static Mono<Path> ensureDirectoryExists(Path path) {
		return Mono.just(path)
			.publishOn(FileAccessSchedulerProvider.get().getFileAccessScheduler(path))
			.flatMap(p -> {
				if (Files.exists(path)) {
					if (!Files.isDirectory(path))
						return Mono.error(new FileAlreadyExistsException(path.toString()));
					return Mono.just(p);
				}
				Path parent = path.getParent();
				Mono<Path> create = Mono.fromCallable(() -> Files.createDirectory(p));
				if (parent == null)
					return create;
				return ensureDirectoryExists(parent)
					.publishOn(FileAccessSchedulerProvider.get().getFileAccessScheduler(path))
					.flatMap(pp -> create);
			}).publishOn(Schedulers.parallel());
	}
	
	/** List all files and directories inside the given directory.
	 * 
	 * @param dir directory to list
	 * @return list of elements
	 */
	public static Mono<List<Path>> listDirectoryContent(Path dir) {
		return Mono.just(dir)
			.publishOn(FileAccessSchedulerProvider.get().getFileAccessScheduler(dir))
			.flatMap(path -> Mono.fromCallable(() -> Files.list(path).toList()))
			.publishOn(Schedulers.parallel());
	}
	
	/**
	 * List all files and directories inside the given directory, attaching their attributes.
	 * 
	 * @param dir directory to list
	 * @return list of elements
	 */
	public static Mono<List<Tuple2<Path, BasicFileAttributes>>> listDirectoryContentWithAttributes(Path dir) {
		return Mono.just(dir)
			.publishOn(FileAccessSchedulerProvider.get().getFileAccessScheduler(dir))
			.flatMap(path -> Mono.fromCallable(() -> {
				List<Path> list = Files.list(path).toList();
				List<Tuple2<Path, BasicFileAttributes>> result = new ArrayList<>(list.size());
				for (Path f : list)
					result.add(Tuples.of(f, Files.readAttributes(f, BasicFileAttributes.class)));
				return result;
			}))
			.publishOn(Schedulers.parallel());
	}
	
	/**
	 * Delete a file.
	 * @param file file to delete
	 * @return empty on success
	 */
	public static Mono<Void> deleteFile(Path file) {
		return Mono.just(file)
		.publishOn(FileAccessSchedulerProvider.get().getFileAccessScheduler(file))
		.flatMap(p -> Mono.fromCallable(() -> {
			Files.delete(p);
			return p;
		}))
		.publishOn(Schedulers.parallel())
		.then();
	}
	
	/** Delete a directory with all its content.
	 * 
	 * @param dir directory to delete
	 * @return empty on success
	 */
	public static Mono<Void> deleteDirectoryWithContent(Path dir) {
		return Mono.just(dir)
		.publishOn(FileAccessSchedulerProvider.get().getFileAccessScheduler(dir))
		.flatMap(p -> Mono.fromCallable(() -> Files.walkFileTree(p, new DirectoryCleaner())))
		.then();
	}
	
	/**
	 * FileVisitor to remove content of a directory.
	 */
	public static class DirectoryCleaner extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            Files.delete(path);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}

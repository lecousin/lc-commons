package net.lecousin.commons.reactive.io.bytes.file;

import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.lecousin.commons.io.bytes.file.FileIO;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIOFromNonReactive;
import net.lecousin.commons.reactive.io.bytes.file.scheduler.FileAccessSchedulerProvider;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/** Builder for a reactive file I/O. */
public final class ReactiveFileIO {

	private ReactiveFileIO() {
		// no instance
	}
	
	/**
	 * Create a Readable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Readable> openReadable(Path path, Set<OpenOption> options) {
		options.removeAll(Set.of(StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING));
		options.add(StandardOpenOption.READ);
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromReadable(new FileIO.Readable((FileChannel) channel).asReadableBytesIO(), scheduler));
	}
	
	/**
	 * Create a Readable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Readable> openReadable(Path path, OpenOption... options) {
		return openReadable(path, new HashSet<>(Arrays.asList(options)));
	}
	
	
	/**
	 * Create a Readable and Seekable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Readable.Seekable> openReadableSeekable(Path path, Set<OpenOption> options) {
		options.removeAll(Set.of(StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING));
		options.add(StandardOpenOption.READ);
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromReadableSeekable(new FileIO.Readable((FileChannel) channel), scheduler));
	}
	
	/**
	 * Create a Readable and Seekable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Readable.Seekable> openReadableSeekable(Path path, OpenOption... options) {
		return openReadableSeekable(path, new HashSet<>(Arrays.asList(options)));
	}

	
	/**
	 * Create a Writable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable> openWritable(Path path, Set<OpenOption> options) {
		options.removeAll(Set.of(StandardOpenOption.READ, StandardOpenOption.APPEND));
		options.add(StandardOpenOption.WRITE);
		
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromWritable(new FileIO.Writable((FileChannel) channel).asWritableBytesIO(), scheduler));
	}
	
	/**
	 * Create a Writable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable> openWritable(Path path, OpenOption... options) {
		return openWritable(path, new HashSet<>(Arrays.asList(options)));
	}

	
	/**
	 * Create a Writable and Appendable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @param attrs an optional list of file attributes to set atomically when creating the file
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable> openWritableAppendable(Path path, Set<OpenOption> options, FileAttribute<?>... attrs) {
		options.removeAll(Set.of(StandardOpenOption.READ, StandardOpenOption.APPEND));
		options.add(StandardOpenOption.WRITE);
		
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options, attrs))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromWritable(new FileIO.Writable.Appendable((FileChannel) channel).asWritableBytesIO(), scheduler));
	}
	
	/**
	 * Create a Writable and Appendable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable> openWritableAppendable(Path path, OpenOption... options) {
		return openWritableAppendable(path, new HashSet<>(Arrays.asList(options)));
	}

	
	/**
	 * Create a Writable and Seekable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable.Seekable> openWritableSeekable(Path path, Set<OpenOption> options) {
		options.removeAll(Set.of(StandardOpenOption.READ, StandardOpenOption.APPEND));
		options.add(StandardOpenOption.WRITE);
		
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromWritableSeekable(new FileIO.Writable((FileChannel) channel), scheduler));
	}
	
	/**
	 * Create a Writable and Seekable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable.Seekable> openWritableSeekable(Path path, OpenOption... options) {
		return openWritableSeekable(path, new HashSet<>(Arrays.asList(options)));
	}
	
	
	/**
	 * Create a Writable Seekable and Appendable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @param attrs an optional list of file attributes to set atomically when creating the file
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable.Seekable> openWritableSeekableAppendable(Path path, Set<OpenOption> options, FileAttribute<?>... attrs) {
		options.removeAll(Set.of(StandardOpenOption.READ, StandardOpenOption.APPEND));
		options.add(StandardOpenOption.WRITE);
		
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options, attrs))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromWritableSeekable(new FileIO.Writable.Appendable((FileChannel) channel), scheduler));
	}
	
	/**
	 * Create a Writable Seekable and Appendable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable.Seekable> openWritableSeekableAppendable(Path path, OpenOption... options) {
		return openWritableSeekableAppendable(path, new HashSet<>(Arrays.asList(options)));
	}
	

	/**
	 * Create a Writable Seekable and Resizable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @param attrs an optional list of file attributes to set atomically when creating the file
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable.Seekable.Resizable> openWritableSeekableResizable(Path path, Set<OpenOption> options, FileAttribute<?>... attrs) {
		options.removeAll(Set.of(StandardOpenOption.READ, StandardOpenOption.APPEND));
		options.add(StandardOpenOption.WRITE);
		
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options, attrs))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromWritableSeekableResizable(new FileIO.Writable.Resizable((FileChannel) channel), scheduler));
	}
	
	/**
	 * Create a Writable Seekable and Resizable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable.Seekable.Resizable> openWritableSeekableResizable(Path path, OpenOption... options) {
		return openWritableSeekableResizable(path, new HashSet<>(Arrays.asList(options)));
	}
	

	/**
	 * Create a Writable Seekable Appendable and Resizable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @param attrs an optional list of file attributes to set atomically when creating the file
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable.Seekable.Resizable> openWritableSeekableAppendableResizable(Path path, Set<OpenOption> options, FileAttribute<?>... attrs) {
		options.removeAll(Set.of(StandardOpenOption.READ, StandardOpenOption.APPEND));
		options.add(StandardOpenOption.WRITE);
		
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options, attrs))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromWritableSeekableResizable(new FileIO.Writable.AppendableResizable((FileChannel) channel), scheduler));
	}
	
	/**
	 * Create a Writable Seekable Appendable and Resizable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.Writable.Seekable.Resizable> openWritableSeekableAppendableResizable(Path path, OpenOption... options) {
		return openWritableSeekableAppendableResizable(path, new HashSet<>(Arrays.asList(options)));
	}
	
	
	/**
	 * Create a Read-Write reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.ReadWrite> openReadWrite(Path path, Set<OpenOption> options) {
		options.remove(StandardOpenOption.APPEND);
		options.add(StandardOpenOption.READ);
		options.add(StandardOpenOption.WRITE);
		
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromReadWrite(new FileIO.ReadWrite((FileChannel) channel), scheduler));
	}
	
	/**
	 * Create a Read-Write reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.ReadWrite> openReadWrite(Path path, OpenOption... options) {
		return openReadWrite(path, new HashSet<>(Arrays.asList(options)));
	}

	
	/**
	 * Create a Read-Write Appendable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @param attrs an optional list of file attributes to set atomically when creating the file
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.ReadWrite> openReadWriteAppendable(Path path, Set<OpenOption> options, FileAttribute<?>... attrs) {
		options.remove(StandardOpenOption.APPEND);
		options.add(StandardOpenOption.READ);
		options.add(StandardOpenOption.WRITE);
		
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options, attrs))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromReadWrite(new FileIO.ReadWrite.Appendable((FileChannel) channel), scheduler));
	}
	
	/**
	 * Create a Read-Write Appendable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.ReadWrite> openReadWriteAppendable(Path path, OpenOption... options) {
		return openReadWriteAppendable(path, new HashSet<>(Arrays.asList(options)));
	}
	
	
	/**
	 * Create a Read-Write Resizable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @param attrs an optional list of file attributes to set atomically when creating the file
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.ReadWrite.Resizable> openReadWriteResizable(Path path, Set<OpenOption> options, FileAttribute<?>... attrs) {
		options.remove(StandardOpenOption.APPEND);
		options.add(StandardOpenOption.READ);
		options.add(StandardOpenOption.WRITE);
		
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options, attrs))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromReadWriteResizable(new FileIO.ReadWrite.Resizable((FileChannel) channel), scheduler));
	}
	
	/**
	 * Create a Read-Write Resizable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.ReadWrite.Resizable> openReadWriteResizable(Path path, OpenOption... options) {
		return openReadWriteResizable(path, new HashSet<>(Arrays.asList(options)));
	}

	
	/**
	 * Create a Read-Write Appendable Resizable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @param attrs an optional list of file attributes to set atomically when creating the file
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.ReadWrite.Resizable> openReadWriteAppendableResizable(Path path, Set<OpenOption> options, FileAttribute<?>... attrs) {
		options.remove(StandardOpenOption.APPEND);
		options.add(StandardOpenOption.READ);
		options.add(StandardOpenOption.WRITE);
		
		Scheduler scheduler = FileAccessSchedulerProvider.get().getFileAccessScheduler(path);
		return Mono.fromCallable(() -> Files.newByteChannel(path, options, attrs))
				.subscribeOn(scheduler).publishOn(Schedulers.parallel())
				.map(channel -> ReactiveBytesIOFromNonReactive.fromReadWriteResizable(new FileIO.ReadWrite.AppendableResizable((FileChannel) channel), scheduler));
	}
	
	/**
	 * Create a Read-Write Appendable Resizable reactive file I/O.
	 * @param path file
	 * @param options options specifying how the file is opened
	 * @return I/O
	 */
	public static Mono<ReactiveBytesIO.ReadWrite.Resizable> openReadWriteAppendableResizable(Path path, OpenOption... options) {
		return openReadWriteAppendableResizable(path, new HashSet<>(Arrays.asList(options)));
	}
}

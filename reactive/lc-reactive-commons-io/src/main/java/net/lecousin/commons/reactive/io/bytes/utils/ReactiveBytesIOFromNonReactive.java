package net.lecousin.commons.reactive.io.bytes.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.function.FailableRunnable;

import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.reactive.MonoUtils;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIOView;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/** Build a Reactive I/O from a non reactive one. */
public interface ReactiveBytesIOFromNonReactive {

	/**
	 * Create a Read-Write and Resizable reactive IO from a non reactive IO.<br/>
	 * If the given IO is Appendable, the returned reactive IO is also Appendable.
	 * 
	 * @param <T> type of non reactive IO
	 * @param io non reactive IO
	 * @param scheduler scheduler to use
	 * @return reactive IO
	 */
	static <T extends BytesIO.Readable.Seekable & BytesIO.Writable.Seekable & IO.Writable.Resizable> ReactiveBytesIOFromNonReactive.ReadWrite fromReadWriteResizable(T io, Scheduler scheduler) {
		if (io instanceof IO.Writable.Appendable)
			return new ReadWrite.Appendable(io, scheduler);
		return new ReadWrite(io, scheduler);
	}
	
	/**
	 * Create a Read-Write reactive IO from a non reactive IO.<br/>
	 * If the given IO is Appendable, the returned reactive IO is also Appendable.
	 * 
	 * @param <T> type of non reactive IO
	 * @param io non reactive IO
	 * @param scheduler scheduler to use
	 * @return reactive IO
	 */
	static <T extends BytesIO.Readable.Seekable & BytesIO.Writable.Seekable> ReactiveBytesIO.ReadWrite fromReadWrite(T io, Scheduler scheduler) {
		ReactiveBytesIO.ReadWrite reactive;
		if (io instanceof IO.Writable.Appendable)
			reactive = new ReadWrite.Appendable(io, scheduler);
		else
			reactive = new ReadWrite(io, scheduler);
		// non resizable view
		return ReactiveBytesIOView.ReadWrite.of(reactive);
	}
	
	/**
	 * Create a Readable reactive IO from a non reactive IO.
	 * 
	 * @param io non reactive IO
	 * @param scheduler scheduler to use
	 * @return reactive IO
	 */
	static ReactiveBytesIO.Readable fromReadable(BytesIO.Readable io, Scheduler scheduler) {
		return new ReadWrite(io, scheduler).asReadableBytesIO();
	}
	
	/**
	 * Create a Readable and Seekable reactive IO from a non reactive IO.
	 * 
	 * @param io non reactive IO
	 * @param scheduler scheduler to use
	 * @return reactive IO
	 */
	static ReactiveBytesIO.Readable.Seekable fromReadableSeekable(BytesIO.Readable.Seekable io, Scheduler scheduler) {
		return new ReadWrite(io, scheduler).asReadableSeekableBytesIO();
	}
	
	/**
	 * Create a Writable reactive IO from a non reactive IO.<br/>
	 * If the given IO is Appendable, the returned reactive IO is also Appendable.
	 * 
	 * @param io non reactive IO
	 * @param scheduler scheduler to use
	 * @return reactive IO
	 */
	static ReactiveBytesIO.Writable fromWritable(BytesIO.Writable io, Scheduler scheduler) {
		if (io instanceof IO.Writable.Appendable)
			return new ReadWrite.Appendable(io, scheduler).asWritableBytesIO();
		return new ReadWrite(io, scheduler).asWritableBytesIO();
	}
	
	/**
	 * Create a Writable and Seekable reactive IO from a non reactive IO.<br/>
	 * If the given IO is Appendable, the returned reactive IO is also Appendable.
	 * 
	 * @param io non reactive IO
	 * @param scheduler scheduler to use
	 * @return reactive IO
	 */
	static ReactiveBytesIO.Writable.Seekable fromWritableSeekable(BytesIO.Writable.Seekable io, Scheduler scheduler) {
		if (io instanceof IO.Writable.Appendable)
			return new ReadWrite.Appendable(io, scheduler).asWritableSeekableBytesIO();
		return new ReadWrite(io, scheduler).asWritableSeekableBytesIO();
	}
	
	/**
	 * Create a Writable Seekable and Resizable reactive IO from a non reactive IO.<br/>
	 * If the given IO is Appendable, the returned reactive IO is also Appendable.
	 * 
	 * @param <T> type of non reactive IO
	 * @param io non reactive IO
	 * @param scheduler scheduler to use
	 * @return reactive IO
	 */
	static <T extends BytesIO.Writable.Seekable & IO.Writable.Resizable> ReactiveBytesIO.Writable.Seekable.Resizable fromWritableSeekableResizable(T io, Scheduler scheduler) {
		if (io instanceof IO.Writable.Appendable)
			return new ReadWrite.Appendable(io, scheduler).asWritableSeekableResizableBytesIO();
		return new ReadWrite(io, scheduler).asWritableSeekableResizableBytesIO();
	}

	/** Read-Write and Resizable implementation. */
	class ReadWrite implements ReactiveBytesIO.ReadWrite.Resizable {
		
		private static final class Appendable extends ReactiveBytesIOFromNonReactive.ReadWrite implements ReactiveIO.Writable.Appendable {
			private Appendable(BytesIO io, Scheduler scheduler) {
				super(io, scheduler);
			}
		}
		
		
		private BytesIO io;
		private Scheduler scheduler;
		
		protected ReadWrite(BytesIO io, Scheduler scheduler) {
			this.io = io;
			this.scheduler = scheduler;
		}
		
		private Mono<Void> delegateVoid(FailableRunnable<IOException> runnable) {
			return MonoUtils.fromFailableRunnable(runnable).subscribeOn(scheduler).publishOn(Schedulers.parallel());
		}
		
		private <T> Mono<T> delegate(Callable<T> operation) {
			return Mono.fromCallable(operation).subscribeOn(scheduler).publishOn(Schedulers.parallel());
		}
		
		@Override
		public Scheduler getScheduler() {
			return scheduler;
		}
		
		@Override
		public boolean isClosed() {
			return io.isClosed();
		}
		
		@Override
		public Mono<Void> close() {
			// do close on bounded elastic so listeners can be blocking
			return MonoUtils.fromFailableRunnable(io::close).subscribeOn(Schedulers.boundedElastic()).publishOn(Schedulers.parallel());
		}
		
		@Override
		public void onClose(Mono<Void> listener) {
			io.onClose(listener::block);
		}
		
		@Override
		public Mono<Void> flush() {
			return delegateVoid(((BytesIO.Writable) io)::flush);
		}

		@Override
		public Mono<Long> size() {
			return delegate(((IO.KnownSize) io)::size);
		}

		@Override
		public Mono<Long> position() {
			return delegate(((IO.Seekable) io)::position);
		}

		@Override
		public Mono<Long> seek(SeekFrom from, long offset) {
			return delegate(() -> ((IO.Seekable) io).seek(from, offset));
		}
		
		@Override
		public Mono<Void> setSize(long newSize) {
			return delegateVoid(() -> ((IO.Writable.Resizable) io).setSize(newSize));
		}

		@Override
		public Mono<Byte> readByte() {
			return delegate(((BytesIO.Readable) io)::readByte);
		}

		@Override
		public Mono<Integer> readBytes(ByteBuffer buffer) {
			return delegate(() -> ((BytesIO.Readable) io).readBytes(buffer));
		}

		@Override
		public Mono<Integer> readBytes(byte[] buf, int off, int len) {
			return delegate(() -> ((BytesIO.Readable) io).readBytes(buf, off, len));
		}

		@Override
		public Mono<Integer> readBytes(byte[] buf) {
			return delegate(() -> ((BytesIO.Readable) io).readBytes(buf));
		}

		@Override
		public Mono<ByteBuffer> readBuffer() {
			return Mono.defer(() -> {
				try {
					var buf = ((BytesIO.Readable) io).readBuffer();
					if (buf.isEmpty()) return Mono.empty();
					return Mono.just(buf.get());
				} catch (IOException e) {
					return Mono.error(e);
				}
			});
		}

		@Override
		public Mono<ByteBuffer> readBytesFully(ByteBuffer buffer) {
			return delegateVoid(() -> ((BytesIO.Readable) io).readBytesFully(buffer)).then(Mono.defer(() -> Mono.just(buffer)));
		}

		@Override
		public Mono<byte[]> readBytesFully(byte[] buf, int off, int len) {
			return delegateVoid(() -> ((BytesIO.Readable) io).readBytesFully(buf, off, len)).then(Mono.defer(() -> Mono.just(buf)));
		}

		@Override
		public Mono<byte[]> readBytesFully(byte[] buf) {
			return delegateVoid(() -> ((BytesIO.Readable) io).readBytesFully(buf)).then(Mono.defer(() -> Mono.just(buf)));
		}

		@Override
		public Mono<Long> skipUpTo(long toSkip) {
			return delegate(() -> ((BytesIO.Readable) io).skipUpTo(toSkip));
		}

		@Override
		public Mono<Void> skipFully(long toSkip) {
			return delegateVoid(() -> ((BytesIO.Readable) io).skipFully(toSkip));
		}

		@Override
		public Mono<Byte> readByteAt(long pos) {
			return delegate(() -> ((BytesIO.Readable.Seekable) io).readByteAt(pos));
		}

		@Override
		public Mono<Integer> readBytesAt(long pos, ByteBuffer buffer) {
			return delegate(() -> ((BytesIO.Readable.Seekable) io).readBytesAt(pos, buffer));
		}

		@Override
		public Mono<Integer> readBytesAt(long pos, byte[] buf, int off, int len) {
			return delegate(() -> ((BytesIO.Readable.Seekable) io).readBytesAt(pos, buf, off, len));
		}

		@Override
		public Mono<Integer> readBytesAt(long pos, byte[] buf) {
			return delegate(() -> ((BytesIO.Readable.Seekable) io).readBytesAt(pos, buf));
		}

		@Override
		public Mono<ByteBuffer> readBytesFullyAt(long pos, ByteBuffer buffer) {
			return delegateVoid(() -> ((BytesIO.Readable.Seekable) io).readBytesFullyAt(pos, buffer)).then(Mono.defer(() -> Mono.just(buffer)));
		}

		@Override
		public Mono<byte[]> readBytesFullyAt(long pos, byte[] buf, int off, int len) {
			return delegateVoid(() -> ((BytesIO.Readable.Seekable) io).readBytesFullyAt(pos, buf, off, len)).then(Mono.defer(() -> Mono.just(buf)));
		}

		@Override
		public Mono<byte[]> readBytesFullyAt(long pos, byte[] buf) {
			return delegateVoid(() -> ((BytesIO.Readable.Seekable) io).readBytesFullyAt(pos, buf)).then(Mono.defer(() -> Mono.just(buf)));
		}

		@Override
		public Mono<Void> writeByte(byte value) {
			return delegateVoid(() -> ((BytesIO.Writable) io).writeByte(value));
		}

		@Override
		public Mono<Integer> writeBytes(ByteBuffer buffer) {
			return delegate(() -> ((BytesIO.Writable) io).writeBytes(buffer));
		}

		@Override
		public Mono<Integer> writeBytes(byte[] buf, int off, int len) {
			return delegate(() -> ((BytesIO.Writable) io).writeBytes(buf, off, len));
		}

		@Override
		public Mono<Integer> writeBytes(byte[] buf) {
			return delegate(() -> ((BytesIO.Writable) io).writeBytes(buf));
		}

		@Override
		public Mono<Void> writeBytesFully(ByteBuffer buffer) {
			return delegateVoid(() -> ((BytesIO.Writable) io).writeBytesFully(buffer));
		}

		@Override
		public Mono<Void> writeBytesFully(List<ByteBuffer> buffers) {
			return delegateVoid(() -> ((BytesIO.Writable) io).writeBytesFully(buffers));
		}

		@Override
		public Mono<Void> writeBytesFully(byte[] buf, int off, int len) {
			return delegateVoid(() -> ((BytesIO.Writable) io).writeBytesFully(buf, off, len));
		}

		@Override
		public Mono<Void> writeBytesFully(byte[] buf) {
			return delegateVoid(() -> ((BytesIO.Writable) io).writeBytesFully(buf));
		}

		@Override
		public Mono<Void> writeByteAt(long pos, byte value) {
			return delegateVoid(() -> ((BytesIO.Writable.Seekable) io).writeByteAt(pos, value));
		}

		@Override
		public Mono<Integer> writeBytesAt(long pos, ByteBuffer buffer) {
			return delegate(() -> ((BytesIO.Writable.Seekable) io).writeBytesAt(pos, buffer));
		}

		@Override
		public Mono<Integer> writeBytesAt(long pos, byte[] buf, int off, int len) {
			return delegate(() -> ((BytesIO.Writable.Seekable) io).writeBytesAt(pos, buf, off, len));
		}

		@Override
		public Mono<Integer> writeBytesAt(long pos, byte[] buf) {
			return delegate(() -> ((BytesIO.Writable.Seekable) io).writeBytesAt(pos, buf));
		}

		@Override
		public Mono<Void> writeBytesFullyAt(long pos, ByteBuffer buffer) {
			return delegateVoid(() -> ((BytesIO.Writable.Seekable) io).writeBytesFullyAt(pos, buffer));
		}

		@Override
		public Mono<Void> writeBytesFullyAt(long pos, byte[] buf, int off, int len) {
			return delegateVoid(() -> ((BytesIO.Writable.Seekable) io).writeBytesFullyAt(pos, buf, off, len));
		}

		@Override
		public Mono<Void> writeBytesFullyAt(long pos, byte[] buf) {
			return delegateVoid(() -> ((BytesIO.Writable.Seekable) io).writeBytesFullyAt(pos, buf));
		}

		@Override
		public Mono<Void> writeBytesFullyAt(long pos, List<ByteBuffer> buffers) {
			return delegateVoid(() -> ((BytesIO.Writable.Seekable) io).writeBytesFullyAt(pos, buffers));
		}

	}
	
}

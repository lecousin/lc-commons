package net.lecousin.commons.reactive.io.bytes.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

/**
 * Convert a reactive I/O into a non-reactive one.<br/>
 * As it uses the block method, it must be used in a thread allowing blocking operations.
 */
@SuppressWarnings("resource")
public interface ReactiveBytesIOToNonReactive {

	/**
	 * Create a non-reactive IO from a reactive one.
	 * @param reactive reactive IO
	 * @return non-reactive IO
	 */
	static BytesIO.Readable fromReadable(ReactiveBytesIO.Readable reactive) {
		return new Impl(reactive).asReadableBytesIO();
	}

	/**
	 * Create a non-reactive IO from a reactive one.
	 * @param reactive reactive IO
	 * @return non-reactive IO
	 */
	static BytesIO.Readable.Seekable fromReadableSeekable(ReactiveBytesIO.Readable.Seekable reactive) {
		return new Impl(reactive).asReadableSeekableBytesIO();
	}

	/**
	 * Create a non-reactive IO from a reactive one.
	 * @param reactive reactive IO
	 * @return non-reactive IO
	 */
	static BytesIO.Writable fromWritable(ReactiveBytesIO.Writable reactive) {
		if (reactive instanceof ReactiveIO.Writable.Appendable)
			return new Impl.Appendable(reactive).asWritableBytesIO();
		return new Impl(reactive).asWritableBytesIO();
	}

	/**
	 * Create a non-reactive IO from a reactive one.
	 * @param reactive reactive IO
	 * @return non-reactive IO
	 */
	static BytesIO.Writable.Seekable fromWritableSeekable(ReactiveBytesIO.Writable.Seekable reactive) {
		if (reactive instanceof ReactiveIO.Writable.Appendable)
			return new Impl.Appendable(reactive).asWritableSeekableBytesIO();
		return new Impl(reactive).asWritableSeekableBytesIO();
	}

	/**
	 * Create a non-reactive IO from a reactive one.
	 * @param reactive reactive IO
	 * @return non-reactive IO
	 */
	static BytesIO.Writable.Seekable.Resizable fromWritableSeekableResizable(ReactiveBytesIO.Writable.Seekable.Resizable reactive) {
		if (reactive instanceof ReactiveIO.Writable.Appendable)
			return new Impl.Appendable(reactive).asWritableSeekableResizableBytesIO();
		return new Impl(reactive).asWritableSeekableResizableBytesIO();
	}
	
	/**
	 * Create a non-reactive IO from a reactive one.
	 * @param reactive reactive IO
	 * @param <T> type of IO
	 * @return non-reactive IO
	 */
	static <T extends ReactiveBytesIO.Writable.Seekable & ReactiveBytesIO.Readable.Seekable> BytesIO.ReadWrite fromReadWrite(T reactive) {
		if (reactive instanceof ReactiveIO.Writable.Appendable)
			return new Impl.Appendable(reactive).asNonResizableReadWriteBytesIO();
		return new Impl(reactive).asNonResizableReadWriteBytesIO();
	}
	
	/**
	 * Create a non-reactive IO from a reactive one.
	 * @param reactive reactive IO
	 * @param <T> type of IO
	 * @return non-reactive IO
	 */
	static <T extends ReactiveBytesIO.Writable.Seekable & ReactiveBytesIO.Readable.Seekable & ReactiveIO.Writable.Resizable> BytesIO.ReadWrite.Resizable fromReadWriteResizable(T reactive) {
		if (reactive instanceof ReactiveIO.Writable.Appendable)
			return new Impl.Appendable(reactive);
		return new Impl(reactive);
	}

	
	/** Implementation. */
	@SuppressWarnings({"java:S2259"})
	class Impl implements BytesIO.ReadWrite.Resizable {
		
		private static final class Appendable extends Impl implements IO.Writable.Appendable {
			private Appendable(ReactiveBytesIO io) {
				super(io);
			}
		}
		
		private ReactiveBytesIO io;
		
		private Impl(ReactiveBytesIO io) {
			this.io = io;
		}


		@SuppressWarnings({"java:S1181"})
		private static <T> T nonReactive(Mono<T> mono) throws IOException {
			try {
				return mono.block();
			} catch (Throwable t) {
				Throwable t2 = Exceptions.unwrap(t);
				if (t2 instanceof IOException ioe) {
					throw ioe;
				}
				throw Exceptions.propagate(t);
			}
		}
		
		// -- IO --
		
		@Override
		public boolean isClosed() {
			return io.isClosed();
		}

		@Override
		public void onClose(Runnable listener) {
			io.onClose(Mono.fromRunnable(listener));
		}

		@Override
		public void close() throws IOException {
			nonReactive(io.close());
		}


		// -- Seekable - Resizable - KnownSize --

		@Override
		public long position() throws IOException {
			return nonReactive(((ReactiveIO.Seekable) io).position());
		}

		@Override
		public long seek(SeekFrom from, long offset) throws IOException {
			return nonReactive(((ReactiveIO.Seekable) io).seek(from, offset));
		}

		@Override
		public void setSize(long newSize) throws IOException {
			nonReactive(((ReactiveIO.Writable.Resizable) io).setSize(newSize));
		}
		
		@Override
		public long size() throws IOException {
			return nonReactive(((ReactiveIO.KnownSize) io).size());
		}

		// -- Readable --
		
		@Override
		public byte readByte() throws IOException {
			return nonReactive(((ReactiveBytesIO.Readable) io).readByte());
		}

		@Override
		public int readBytes(ByteBuffer buffer) throws IOException {
			return nonReactive(((ReactiveBytesIO.Readable) io).readBytes(buffer));
		}

		@Override
		public int readBytes(byte[] buf, int off, int len) throws IOException {
			return nonReactive(((ReactiveBytesIO.Readable) io).readBytes(buf, off, len));
		}

		@Override
		public int readBytes(byte[] buf) throws IOException {
			return nonReactive(((ReactiveBytesIO.Readable) io).readBytes(buf));
		}

		@Override
		public Optional<ByteBuffer> readBuffer() throws IOException {
			return Optional.ofNullable(nonReactive(((ReactiveBytesIO.Readable) io).readBuffer()));
		}

		@Override
		public void readBytesFully(ByteBuffer buffer) throws IOException {
			nonReactive(((ReactiveBytesIO.Readable) io).readBytesFully(buffer));
		}

		@Override
		public void readBytesFully(byte[] buf, int off, int len) throws IOException {
			nonReactive(((ReactiveBytesIO.Readable) io).readBytesFully(buf, off, len));
		}

		@Override
		public void readBytesFully(byte[] buf) throws IOException {
			nonReactive(((ReactiveBytesIO.Readable) io).readBytesFully(buf));
		}
		
		@Override
		public long skipUpTo(long toSkip) throws IOException {
			return nonReactive(((ReactiveBytesIO.Readable) io).skipUpTo(toSkip));
		}

		@Override
		public void skipFully(long toSkip) throws IOException {
			nonReactive(((ReactiveBytesIO.Readable) io).skipFully(toSkip));
		}


		@Override
		public byte readByteAt(long pos) throws IOException {
			return nonReactive(((ReactiveBytesIO.Readable.Seekable) io).readByteAt(pos));
		}

		@Override
		public int readBytesAt(long pos, ByteBuffer buffer) throws IOException {
			return nonReactive(((ReactiveBytesIO.Readable.Seekable) io).readBytesAt(pos, buffer));
		}

		@Override
		public int readBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			return nonReactive(((ReactiveBytesIO.Readable.Seekable) io).readBytesAt(pos, buf, off, len));
		}

		@Override
		public int readBytesAt(long pos, byte[] buf) throws IOException {
			return nonReactive(((ReactiveBytesIO.Readable.Seekable) io).readBytesAt(pos, buf));
		}

		@Override
		public void readBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
			nonReactive(((ReactiveBytesIO.Readable.Seekable) io).readBytesFullyAt(pos, buffer));
		}

		@Override
		public void readBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
			nonReactive(((ReactiveBytesIO.Readable.Seekable) io).readBytesFullyAt(pos, buf, off, len));
		}

		@Override
		public void readBytesFullyAt(long pos, byte[] buf) throws IOException {
			nonReactive(((ReactiveBytesIO.Readable.Seekable) io).readBytesFullyAt(pos, buf));
		}

		// -- Writable --
		
		@Override
		public void writeByteAt(long pos, byte value) throws IOException {
			nonReactive(((ReactiveBytesIO.Writable.Seekable) io).writeByteAt(pos, value));
		}

		@Override
		public int writeBytesAt(long pos, ByteBuffer buffer) throws IOException {
			return nonReactive(((ReactiveBytesIO.Writable.Seekable) io).writeBytesAt(pos, buffer));
		}

		@Override
		public int writeBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			return nonReactive(((ReactiveBytesIO.Writable.Seekable) io).writeBytesAt(pos, buf, off, len));
		}

		@Override
		public int writeBytesAt(long pos, byte[] buf) throws IOException {
			return nonReactive(((ReactiveBytesIO.Writable.Seekable) io).writeBytesAt(pos, buf));
		}

		@Override
		public void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
			nonReactive(((ReactiveBytesIO.Writable.Seekable) io).writeBytesFullyAt(pos, buffer));
		}

		@Override
		public void writeBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
			nonReactive(((ReactiveBytesIO.Writable.Seekable) io).writeBytesFullyAt(pos, buf, off, len));
		}

		@Override
		public void writeBytesFullyAt(long pos, byte[] buf) throws IOException {
			nonReactive(((ReactiveBytesIO.Writable.Seekable) io).writeBytesFullyAt(pos, buf));
		}

		@Override
		public void writeBytesFullyAt(long pos, List<ByteBuffer> buffers) throws IOException {
			nonReactive(((ReactiveBytesIO.Writable.Seekable) io).writeBytesFullyAt(pos, buffers));
		}

		@Override
		public void writeByte(byte value) throws IOException {
			nonReactive(((ReactiveBytesIO.Writable) io).writeByte(value));
		}

		@Override
		public int writeBytes(ByteBuffer buffer) throws IOException {
			return nonReactive(((ReactiveBytesIO.Writable) io).writeBytes(buffer));
		}

		@Override
		public int writeBytes(byte[] buf, int off, int len) throws IOException {
			return nonReactive(((ReactiveBytesIO.Writable) io).writeBytes(buf, off, len));
		}

		@Override
		public int writeBytes(byte[] buf) throws IOException {
			return nonReactive(((ReactiveBytesIO.Writable) io).writeBytes(buf));
		}

		@Override
		public void writeBytesFully(ByteBuffer buffer) throws IOException {
			nonReactive(((ReactiveBytesIO.Writable) io).writeBytesFully(buffer));
		}

		@Override
		public void writeBytesFully(List<ByteBuffer> buffers) throws IOException {
			nonReactive(((ReactiveBytesIO.Writable) io).writeBytesFully(buffers));
		}

		@Override
		public void writeBytesFully(byte[] buf, int off, int len) throws IOException {
			nonReactive(((ReactiveBytesIO.Writable) io).writeBytesFully(buf, off, len));
		}

		@Override
		public void writeBytesFully(byte[] buf) throws IOException {
			nonReactive(((ReactiveBytesIO.Writable) io).writeBytesFully(buf));
		}

		@Override
		public void flush() throws IOException {
			nonReactive(((ReactiveBytesIO.Writable) io).flush());
		}

	}
	
}

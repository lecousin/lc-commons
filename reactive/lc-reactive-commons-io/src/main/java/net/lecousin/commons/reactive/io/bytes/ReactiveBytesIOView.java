package net.lecousin.commons.reactive.io.bytes;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.reactive.io.ReactiveIOChecks;
import net.lecousin.commons.reactive.io.ReactiveIOView;
import reactor.core.publisher.Mono;

/**
 * Sub-view of a ReactiveBytesIO, wrapping the original IO.
 * @param <T> type of ReactiveBytesIO
 */
public class ReactiveBytesIOView<T extends ReactiveBytesIO> extends ReactiveIOView<T> {

	protected ReactiveBytesIOView(T io) {
		super(io);
	}
	
	/** Readable view of a ReactiveBytesIO. */
	public static class Readable extends ReactiveBytesIOView<ReactiveBytesIO.Readable> implements ReactiveBytesIO.Readable {
		
		/** Constructor
		 * @param io I/O to wrap
		 */
		public Readable(ReactiveBytesIO.Readable io) {
			super(io);
		}

		@Override
		public Mono<Byte> readByte() {
			return io.readByte();
		}

		@Override
		public Mono<Integer> readBytes(ByteBuffer buffer) {
			return io.readBytes(buffer);
		}

		@Override
		public Mono<Integer> readBytes(byte[] buf, int off, int len) {
			return io.readBytes(buf, off, len);
		}

		@Override
		public Mono<Integer> readBytes(byte[] buf) {
			return io.readBytes(buf);
		}

		@Override
		public Mono<ByteBuffer> readBuffer() {
			return io.readBuffer();
		}

		@Override
		public Mono<ByteBuffer> readBytesFully(ByteBuffer buffer) {
			return io.readBytesFully(buffer);
		}

		@Override
		public Mono<byte[]> readBytesFully(byte[] buf, int off, int len) {
			return io.readBytesFully(buf, off, len);
		}

		@Override
		public Mono<byte[]> readBytesFully(byte[] buf) {
			return io.readBytesFully(buf);
		}

		@Override
		public Mono<Long> skipUpTo(long toSkip) {
			return io.skipUpTo(toSkip);
		}

		@Override
		public Mono<Void> skipFully(long toSkip) {
			return io.skipFully(toSkip);
		}
		
		/** Readable and Seekable view of a ReactiveBytesIO. */
		public static class Seekable extends ReactiveBytesIOView<ReactiveBytesIO.Readable.Seekable> implements ReactiveBytesIO.Readable.Seekable {
			
			/**
			 * Constructor.
			 * @param io I/O to wrap.
			 */
			public Seekable(ReactiveBytesIO.Readable.Seekable io) {
				super(io);
			}

			@Override
			public Mono<Byte> readByte() {
				return io.readByte();
			}

			@Override
			public Mono<Integer> readBytes(ByteBuffer buffer) {
				return io.readBytes(buffer);
			}

			@Override
			public Mono<Integer> readBytes(byte[] buf, int off, int len) {
				return io.readBytes(buf, off, len);
			}

			@Override
			public Mono<Integer> readBytes(byte[] buf) {
				return io.readBytes(buf);
			}

			@Override
			public Mono<ByteBuffer> readBuffer() {
				return io.readBuffer();
			}

			@Override
			public Mono<ByteBuffer> readBytesFully(ByteBuffer buffer) {
				return io.readBytesFully(buffer);
			}

			@Override
			public Mono<byte[]> readBytesFully(byte[] buf, int off, int len) {
				return io.readBytesFully(buf, off, len);
			}

			@Override
			public Mono<byte[]> readBytesFully(byte[] buf) {
				return io.readBytesFully(buf);
			}

			@Override
			public Mono<Long> skipUpTo(long toSkip) {
				return io.skipUpTo(toSkip);
			}
			
			@Override
			public Mono<Long> size() {
				return io.size();
			}

			@Override
			public Mono<Long> position() {
				return io.position();
			}

			@Override
			public Mono<Long> seek(SeekFrom from, long offset) {
				if (!(io instanceof ReactiveIO.Writable.Appendable))
					return io.seek(from, offset);
				// we need to secure the seek
				return ReactiveIOChecks.deferNotClosed(io, () -> io.size())
				.flatMap(s -> {
					Mono<Long> mp;
					switch (Objects.requireNonNull(from, "from")) {
					case CURRENT: mp = io.position().map(p -> p + offset); break;
					case END: mp = Mono.just(s - offset); break;
					case START: default: mp = Mono.just(offset); break;
					}
					return mp.flatMap(p -> {
						if (p < 0) return Mono.error(new IllegalArgumentException("Cannot move beyond the start: " + p));
						if (p > s) return Mono.error(new EOFException());
						return io.seek(SeekFrom.START, p);
					});
				});
			}

			@Override
			public Mono<Byte> readByteAt(long pos) {
				return io.readByteAt(pos);
			}

			@Override
			public Mono<Integer> readBytesAt(long pos, ByteBuffer buffer) {
				return io.readBytesAt(pos, buffer);
			}

			@Override
			public Mono<Integer> readBytesAt(long pos, byte[] buf, int off, int len) {
				return io.readBytesAt(pos, buf, off, len);
			}

			@Override
			public Mono<Integer> readBytesAt(long pos, byte[] buf) {
				return io.readBytesAt(pos, buf);
			}

			@Override
			public Mono<ByteBuffer> readBytesFullyAt(long pos, ByteBuffer buffer) {
				return io.readBytesFullyAt(pos, buffer);
			}

			@Override
			public Mono<byte[]> readBytesFullyAt(long pos, byte[] buf, int off, int len) {
				return io.readBytesFullyAt(pos, buf, off, len);
			}

			@Override
			public Mono<byte[]> readBytesFullyAt(long pos, byte[] buf) {
				return io.readBytesFullyAt(pos, buf);
			}
			
		}
	}
	
	
	
	/** Writable view of a ReactiveBytesIO. */
	public static class Writable extends ReactiveBytesIOView<ReactiveBytesIO.Writable> implements ReactiveBytesIO.Writable {
		
		/**
		 * Create a Writable view of the given IO.<br/>
		 * If the IO is Appendable, the returned view will be also Appendable.
		 * 
		 * @param <T> type of given IO
		 * @param io the IO to wrap
		 * @return the Writable view
		 */
		public static <T extends ReactiveBytesIO.Writable> ReactiveBytesIOView.Writable of(T io) {
			if (io instanceof ReactiveIO.Writable.Appendable)
				return new Appendable(io);
			return new ReactiveBytesIOView.Writable(io);
		}
		
		private static final class Appendable extends ReactiveBytesIOView.Writable implements ReactiveIO.Writable.Appendable {
			private Appendable(ReactiveBytesIO.Writable io) {
				super(io);
			}
		}
		
		private Writable(ReactiveBytesIO.Writable io) {
			super(io);
		}

		@Override
		public Mono<Void> flush() {
			return io.flush();
		}

		@Override
		public Mono<Void> writeByte(byte value) {
			return io.writeByte(value);
		}

		@Override
		public Mono<Integer> writeBytes(ByteBuffer buffer) {
			return io.writeBytes(buffer);
		}

		@Override
		public Mono<Integer> writeBytes(byte[] buf, int off, int len) {
			return io.writeBytes(buf, off, len);
		}

		@Override
		public Mono<Integer> writeBytes(byte[] buf) {
			return io.writeBytes(buf);
		}

		@Override
		public Mono<Void> writeBytesFully(ByteBuffer buffer) {
			return io.writeBytesFully(buffer);
		}

		@Override
		public Mono<Void> writeBytesFully(List<ByteBuffer> buffers) {
			return io.writeBytesFully(buffers);
		}

		@Override
		public Mono<Void> writeBytesFully(byte[] buf, int off, int len) {
			return io.writeBytesFully(buf, off, len);
		}

		@Override
		public Mono<Void> writeBytesFully(byte[] buf) {
			return io.writeBytesFully(buf);
		}
		
		
		/** Writable and Seekable view of a ReactiveBytesIO. */
		public static class Seekable extends ReactiveBytesIOView<ReactiveBytesIO.Writable.Seekable> implements ReactiveBytesIO.Writable.Seekable {

			/**
			 * Create a Writable and Seekable view of the given IO.<br/>
			 * If the IO is Appendable, the returned view will be also Appendable.
			 * 
			 * @param <T> type of given IO
			 * @param io the IO to wrap
			 * @return the Writable view
			 */
			public static <T extends ReactiveBytesIO.Writable.Seekable> ReactiveBytesIOView.Writable.Seekable of(T io) {
				if (io instanceof ReactiveIO.Writable.Appendable)
					return new Appendable(io);
				return new ReactiveBytesIOView.Writable.Seekable(io);
			}
			
			private static final class Appendable extends ReactiveBytesIOView.Writable.Seekable implements ReactiveIO.Writable.Appendable {
				private Appendable(ReactiveBytesIO.Writable.Seekable io) {
					super(io);
				}
			}

			private Seekable(ReactiveBytesIO.Writable.Seekable io) {
				super(io);
			}

			@Override
			public Mono<Void> flush() {
				return io.flush();
			}

			@Override
			public Mono<Long> size() {
				return io.size();
			}

			@Override
			public Mono<Long> position() {
				return io.position();
			}

			@Override
			public Mono<Long> seek(SeekFrom from, long offset) {
				return io.seek(from, offset);
			}

			@Override
			public Mono<Void> writeByte(byte value) {
				return io.writeByte(value);
			}

			@Override
			public Mono<Integer> writeBytes(ByteBuffer buffer) {
				return io.writeBytes(buffer);
			}

			@Override
			public Mono<Integer> writeBytes(byte[] buf, int off, int len) {
				return io.writeBytes(buf, off, len);
			}

			@Override
			public Mono<Integer> writeBytes(byte[] buf) {
				return io.writeBytes(buf);
			}

			@Override
			public Mono<Void> writeBytesFully(ByteBuffer buffer) {
				return io.writeBytesFully(buffer);
			}

			@Override
			public Mono<Void> writeBytesFully(List<ByteBuffer> buffers) {
				return io.writeBytesFully(buffers);
			}

			@Override
			public Mono<Void> writeBytesFully(byte[] buf, int off, int len) {
				return io.writeBytesFully(buf, off, len);
			}

			@Override
			public Mono<Void> writeBytesFully(byte[] buf) {
				return io.writeBytesFully(buf);
			}

			@Override
			public Mono<Void> writeByteAt(long pos, byte value) {
				return io.writeByteAt(pos, value);
			}

			@Override
			public Mono<Integer> writeBytesAt(long pos, ByteBuffer buffer) {
				return io.writeBytesAt(pos, buffer);
			}

			@Override
			public Mono<Integer> writeBytesAt(long pos, byte[] buf, int off, int len) {
				return io.writeBytesAt(pos, buf, off, len);
			}

			@Override
			public Mono<Integer> writeBytesAt(long pos, byte[] buf) {
				return io.writeBytesAt(pos, buf);
			}

			@Override
			public Mono<Void> writeBytesFullyAt(long pos, ByteBuffer buffer) {
				return io.writeBytesFullyAt(pos, buffer);
			}

			@Override
			public Mono<Void> writeBytesFullyAt(long pos, byte[] buf, int off, int len) {
				return io.writeBytesFullyAt(pos, buf, off, len);
			}

			@Override
			public Mono<Void> writeBytesFullyAt(long pos, byte[] buf) {
				return io.writeBytesFullyAt(pos, buf);
			}

			@Override
			public Mono<Void> writeBytesFullyAt(long pos, List<ByteBuffer> buffers) {
				return io.writeBytesFullyAt(pos, buffers);
			}

			
			/** Writable and Seekable and Resizable view of a ReactiveBytesIO. */
			public static class Resizable extends ReactiveBytesIOView<ReactiveBytesIO.Writable.Seekable> implements ReactiveBytesIO.Writable.Seekable.Resizable {

				/**
				 * Create a Writable and Seekable view of the given IO.<br/>
				 * If the IO is Appendable, the returned view will be also Appendable.
				 * 
				 * @param <T> type of given IO
				 * @param io the IO to wrap
				 * @return the Writable view
				 */
				public static <T extends ReactiveBytesIO.Writable.Seekable & ReactiveIO.Writable.Resizable> ReactiveBytesIOView.Writable.Seekable.Resizable of(T io) {
					if (io instanceof ReactiveIO.Writable.Appendable)
						return new Appendable(io);
					return new ReactiveBytesIOView.Writable.Seekable.Resizable(io);
				}
				
				private static final class Appendable extends ReactiveBytesIOView.Writable.Seekable.Resizable implements ReactiveIO.Writable.Appendable {
					private Appendable(ReactiveBytesIO.Writable.Seekable io) {
						super(io);
					}
				}

				private Resizable(ReactiveBytesIO.Writable.Seekable io) {
					super(io);
				}
				
				@Override
				public Mono<Void> setSize(long newSize) {
					return ((ReactiveIO.Writable.Resizable) io).setSize(newSize);
				}

				@Override
				public Mono<Void> flush() {
					return io.flush();
				}

				@Override
				public Mono<Long> size() {
					return io.size();
				}

				@Override
				public Mono<Long> position() {
					return io.position();
				}

				@Override
				public Mono<Long> seek(SeekFrom from, long offset) {
					return io.seek(from, offset);
				}

				@Override
				public Mono<Void> writeByte(byte value) {
					return io.writeByte(value);
				}

				@Override
				public Mono<Integer> writeBytes(ByteBuffer buffer) {
					return io.writeBytes(buffer);
				}

				@Override
				public Mono<Integer> writeBytes(byte[] buf, int off, int len) {
					return io.writeBytes(buf, off, len);
				}

				@Override
				public Mono<Integer> writeBytes(byte[] buf) {
					return io.writeBytes(buf);
				}

				@Override
				public Mono<Void> writeBytesFully(ByteBuffer buffer) {
					return io.writeBytesFully(buffer);
				}

				@Override
				public Mono<Void> writeBytesFully(List<ByteBuffer> buffers) {
					return io.writeBytesFully(buffers);
				}

				@Override
				public Mono<Void> writeBytesFully(byte[] buf, int off, int len) {
					return io.writeBytesFully(buf, off, len);
				}

				@Override
				public Mono<Void> writeBytesFully(byte[] buf) {
					return io.writeBytesFully(buf);
				}

				@Override
				public Mono<Void> writeByteAt(long pos, byte value) {
					return io.writeByteAt(pos, value);
				}

				@Override
				public Mono<Integer> writeBytesAt(long pos, ByteBuffer buffer) {
					return io.writeBytesAt(pos, buffer);
				}

				@Override
				public Mono<Integer> writeBytesAt(long pos, byte[] buf, int off, int len) {
					return io.writeBytesAt(pos, buf, off, len);
				}

				@Override
				public Mono<Integer> writeBytesAt(long pos, byte[] buf) {
					return io.writeBytesAt(pos, buf);
				}

				@Override
				public Mono<Void> writeBytesFullyAt(long pos, ByteBuffer buffer) {
					return io.writeBytesFullyAt(pos, buffer);
				}

				@Override
				public Mono<Void> writeBytesFullyAt(long pos, byte[] buf, int off, int len) {
					return io.writeBytesFullyAt(pos, buf, off, len);
				}

				@Override
				public Mono<Void> writeBytesFullyAt(long pos, byte[] buf) {
					return io.writeBytesFullyAt(pos, buf);
				}

				@Override
				public Mono<Void> writeBytesFullyAt(long pos, List<ByteBuffer> buffers) {
					return io.writeBytesFullyAt(pos, buffers);
				}
				
			}
			
		}
	}
	
	
	
	/** Readable and Writable view of a ReactiveBytesIO.
	 * @param <T> type of BytesIO
	 */
	public static class ReadWrite<T extends ReactiveBytesIO.Readable.Seekable & ReactiveBytesIO.Writable.Seekable> extends ReactiveBytesIOView<T> implements ReactiveBytesIO.ReadWrite {
		
		/**
		 * Create a Readable and Writable view of the given IO.<br/>
		 * If the IO is Appendable, the returned view will be also Appendable.
		 * 
		 * @param <T> type of given IO
		 * @param io the IO to wrap
		 * @return the Writable view
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static <T extends ReactiveBytesIO.Readable.Seekable & ReactiveBytesIO.Writable.Seekable> ReactiveBytesIOView.ReadWrite<T> of(T io) {
			if (io instanceof ReactiveIO.Writable.Appendable)
				return new Appendable(io);
			return new ReactiveBytesIOView.ReadWrite<>(io);
		}
		
		private static final class Appendable<T extends ReactiveBytesIO.Readable.Seekable & ReactiveBytesIO.Writable.Seekable & ReactiveIO.Writable.Appendable>
		extends ReactiveBytesIOView.ReadWrite<T> implements ReactiveIO.Writable.Appendable {
			private Appendable(T io) {
				super(io);
			}
		}
		
		private ReadWrite(T io) {
			super(io);
		}

		@Override
		public Mono<Void> flush() {
			return io.flush();
		}

		@Override
		public Mono<Byte> readByte() {
			return io.readByte();
		}

		@Override
		public Mono<Integer> readBytes(ByteBuffer buffer) {
			return io.readBytes(buffer);
		}

		@Override
		public Mono<Long> size() {
			return io.size();
		}

		@Override
		public Mono<Long> position() {
			return io.position();
		}

		@Override
		public Mono<Integer> readBytes(byte[] buf, int off, int len) {
			return io.readBytes(buf, off, len);
		}

		@Override
		public Mono<Long> seek(SeekFrom from, long offset) {
			return io.seek(from, offset);
		}

		@Override
		public Mono<Integer> readBytes(byte[] buf) {
			return io.readBytes(buf);
		}

		@Override
		public Mono<ByteBuffer> readBuffer() {
			return io.readBuffer();
		}

		@Override
		public Mono<ByteBuffer> readBytesFully(ByteBuffer buffer) {
			return io.readBytesFully(buffer);
		}

		@Override
		public Mono<byte[]> readBytesFully(byte[] buf, int off, int len) {
			return io.readBytesFully(buf, off, len);
		}

		@Override
		public Mono<byte[]> readBytesFully(byte[] buf) {
			return io.readBytesFully(buf);
		}

		@Override
		public Mono<Long> skipUpTo(long toSkip) {
			return io.skipUpTo(toSkip);
		}

		@Override
		public Mono<Void> skipFully(long toSkip) {
			return io.skipFully(toSkip);
		}

		@Override
		public Mono<Byte> readByteAt(long pos) {
			return io.readByteAt(pos);
		}

		@Override
		public Mono<Integer> readBytesAt(long pos, ByteBuffer buffer) {
			return io.readBytesAt(pos, buffer);
		}

		@Override
		public Mono<Integer> readBytesAt(long pos, byte[] buf, int off, int len) {
			return io.readBytesAt(pos, buf, off, len);
		}

		@Override
		public Mono<Integer> readBytesAt(long pos, byte[] buf) {
			return io.readBytesAt(pos, buf);
		}

		@Override
		public Mono<ByteBuffer> readBytesFullyAt(long pos, ByteBuffer buffer) {
			return io.readBytesFullyAt(pos, buffer);
		}

		@Override
		public Mono<byte[]> readBytesFullyAt(long pos, byte[] buf, int off, int len) {
			return io.readBytesFullyAt(pos, buf, off, len);
		}

		@Override
		public Mono<byte[]> readBytesFullyAt(long pos, byte[] buf) {
			return io.readBytesFullyAt(pos, buf);
		}

		@Override
		public Mono<Void> writeByte(byte value) {
			return io.writeByte(value);
		}

		@Override
		public Mono<Integer> writeBytes(ByteBuffer buffer) {
			return io.writeBytes(buffer);
		}

		@Override
		public Mono<Integer> writeBytes(byte[] buf, int off, int len) {
			return io.writeBytes(buf, off, len);
		}

		@Override
		public Mono<Integer> writeBytes(byte[] buf) {
			return io.writeBytes(buf);
		}

		@Override
		public Mono<Void> writeBytesFully(ByteBuffer buffer) {
			return io.writeBytesFully(buffer);
		}

		@Override
		public Mono<Void> writeBytesFully(List<ByteBuffer> buffers) {
			return io.writeBytesFully(buffers);
		}

		@Override
		public Mono<Void> writeBytesFully(byte[] buf, int off, int len) {
			return io.writeBytesFully(buf, off, len);
		}

		@Override
		public Mono<Void> writeBytesFully(byte[] buf) {
			return io.writeBytesFully(buf);
		}

		@Override
		public Mono<Void> writeByteAt(long pos, byte value) {
			return io.writeByteAt(pos, value);
		}

		@Override
		public Mono<Integer> writeBytesAt(long pos, ByteBuffer buffer) {
			return io.writeBytesAt(pos, buffer);
		}

		@Override
		public Mono<Integer> writeBytesAt(long pos, byte[] buf, int off, int len) {
			return io.writeBytesAt(pos, buf, off, len);
		}

		@Override
		public Mono<Integer> writeBytesAt(long pos, byte[] buf) {
			return io.writeBytesAt(pos, buf);
		}

		@Override
		public Mono<Void> writeBytesFullyAt(long pos, ByteBuffer buffer) {
			return io.writeBytesFullyAt(pos, buffer);
		}

		@Override
		public Mono<Void> writeBytesFullyAt(long pos, byte[] buf, int off, int len) {
			return io.writeBytesFullyAt(pos, buf, off, len);
		}

		@Override
		public Mono<Void> writeBytesFullyAt(long pos, byte[] buf) {
			return io.writeBytesFullyAt(pos, buf);
		}

		@Override
		public Mono<Void> writeBytesFullyAt(long pos, List<ByteBuffer> buffers) {
			return io.writeBytesFullyAt(pos, buffers);
		}
		
	}
}

package net.lecousin.commons.io.bytes;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IOView;

/**
 * Sub-view of a BytesIO, wrapping the original IO.
 * @param <T> type of BytesIO
 */
public abstract class BytesIOView<T extends BytesIO> extends IOView<T> {

	protected BytesIOView(T io) {
		super(io);
	}
	
	/** Readable view of a BytesIO. */
	public static class Readable extends BytesIOView<BytesIO.Readable> implements BytesIO.Readable {
		
		/** Constructor
		 * @param io I/O to wrap
		 */
		public Readable(BytesIO.Readable io) {
			super(io);
		}

		@Override
		public byte readByte() throws IOException {
			return io.readByte();
		}

		@Override
		public int readBytes(ByteBuffer buffer) throws IOException {
			return io.readBytes(buffer);
		}

		@Override
		public int readBytes(byte[] buf, int off, int len) throws IOException {
			return io.readBytes(buf, off, len);
		}

		@Override
		public int readBytes(byte[] buf) throws IOException {
			return io.readBytes(buf);
		}

		@Override
		public Optional<ByteBuffer> readBuffer() throws IOException {
			return io.readBuffer();
		}

		@Override
		public void readBytesFully(ByteBuffer buffer) throws IOException {
			io.readBytesFully(buffer);
		}

		@Override
		public void readBytesFully(byte[] buf, int off, int len) throws IOException {
			io.readBytesFully(buf, off, len);
		}

		@Override
		public void readBytesFully(byte[] buf) throws IOException {
			io.readBytesFully(buf);
		}

		@Override
		public long skipUpTo(long toSkip) throws IOException {
			return io.skipUpTo(toSkip);
		}

		@Override
		public void skipFully(long toSkip) throws IOException {
			io.skipFully(toSkip);
		}
		
		
		/** Readable and Seekable view of a BytesIO. */
		public static class Seekable extends BytesIOView<BytesIO.Readable.Seekable> implements BytesIO.Readable.Seekable {
			
			/**
			 * Constructor.
			 * @param io I/O to wrap.
			 */
			public Seekable(BytesIO.Readable.Seekable io) {
				super(io);
			}
			
			@Override
			public byte readByte() throws IOException {
				return io.readByte();
			}

			@Override
			public int readBytes(ByteBuffer buffer) throws IOException {
				return io.readBytes(buffer);
			}

			@Override
			public int readBytes(byte[] buf, int off, int len) throws IOException {
				return io.readBytes(buf, off, len);
			}

			@Override
			public int readBytes(byte[] buf) throws IOException {
				return io.readBytes(buf);
			}

			@Override
			public Optional<ByteBuffer> readBuffer() throws IOException {
				return io.readBuffer();
			}

			@Override
			public void readBytesFully(ByteBuffer buffer) throws IOException {
				io.readBytesFully(buffer);
			}

			@Override
			public void readBytesFully(byte[] buf, int off, int len) throws IOException {
				io.readBytesFully(buf, off, len);
			}

			@Override
			public void readBytesFully(byte[] buf) throws IOException {
				io.readBytesFully(buf);
			}

			@Override
			public long skipUpTo(long toSkip) throws IOException {
				return io.skipUpTo(toSkip);
			}

			@Override
			public void skipFully(long toSkip) throws IOException {
				io.skipFully(toSkip);
			}
			
			@Override
			public long size() throws IOException {
				return io.size();
			}

			@Override
			public long position() throws IOException {
				return io.position();
			}

			@Override
			public long seek(SeekFrom from, long offset) throws IOException {
				if (!(io instanceof IO.Writable.Appendable))
					return io.seek(from, offset);
				// we need to secure the seek
				if (io.isClosed()) throw new ClosedChannelException();
				long s = io.size();
				long p;
				switch (Objects.requireNonNull(from, "from")) {
				case CURRENT: p = io.position() + offset; break;
				case END: p = s - offset; break;
				case START: default: p = offset; break;
				}
				if (p < 0) throw new IllegalArgumentException("Cannot move beyond the start: " + p);
				if (p > s) throw new EOFException();
				return io.seek(SeekFrom.START, p);
			}

			@Override
			public byte readByteAt(long pos) throws IOException {
				return io.readByteAt(pos);
			}

			@Override
			public int readBytesAt(long pos, ByteBuffer buffer) throws IOException {
				return io.readBytesAt(pos, buffer);
			}

			@Override
			public int readBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
				return io.readBytesAt(pos, buf, off, len);
			}

			@Override
			public int readBytesAt(long pos, byte[] buf) throws IOException {
				return io.readBytesAt(pos, buf);
			}

			@Override
			public void readBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
				io.readBytesFullyAt(pos, buffer);
			}

			@Override
			public void readBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
				io.readBytesFullyAt(pos, buf, off, len);
			}

			@Override
			public void readBytesFullyAt(long pos, byte[] buf) throws IOException {
				io.readBytesFullyAt(pos, buf);
			}
			
		}
		
	}
	
	/** Writable view of a BytesIO. */
	public static class Writable extends BytesIOView<BytesIO.Writable> implements BytesIO.Writable {
		
		/**
		 * Create a Writable view of the given IO.<br/>
		 * If the IO is Appendable, the returned view will be also Appendable.
		 * 
		 * @param <T> type of given IO
		 * @param io the IO to wrap
		 * @return the Writable view
		 */
		public static <T extends BytesIO.Writable> BytesIOView.Writable of(T io) {
			if (io instanceof IO.Writable.Appendable)
				return new Appendable(io);
			return new BytesIOView.Writable(io);
		}
		
		private static final class Appendable extends BytesIOView.Writable implements IO.Writable.Appendable {
			private Appendable(BytesIO.Writable io) {
				super(io);
			}
		}
		
		private Writable(BytesIO.Writable io) {
			super(io);
		}

		@Override
		public void flush() throws IOException {
			io.flush();
		}

		@Override
		public void writeByte(byte value) throws IOException {
			io.writeByte(value);
		}

		@Override
		public int writeBytes(ByteBuffer buffer) throws IOException {
			return io.writeBytes(buffer);
		}

		@Override
		public int writeBytes(byte[] buf, int off, int len) throws IOException {
			return io.writeBytes(buf, off, len);
		}

		@Override
		public int writeBytes(byte[] buf) throws IOException {
			return io.writeBytes(buf);
		}

		@Override
		public void writeBytesFully(ByteBuffer buffer) throws IOException {
			io.writeBytesFully(buffer);
		}

		@Override
		public void writeBytesFully(List<ByteBuffer> buffers) throws IOException {
			io.writeBytesFully(buffers);
		}

		@Override
		public void writeBytesFully(byte[] buf, int off, int len) throws IOException {
			io.writeBytesFully(buf, off, len);
		}

		@Override
		public void writeBytesFully(byte[] buf) throws IOException {
			io.writeBytesFully(buf);
		}

		/** Writable and Seekable view of a BytesIO. */
		public static class Seekable extends BytesIOView<BytesIO.Writable.Seekable> implements BytesIO.Writable.Seekable {

			/**
			 * Create a Writable and Seekable view of the given IO.<br/>
			 * If the IO is Appendable, the returned view will be also Appendable.
			 * 
			 * @param <T> type of given IO
			 * @param io the IO to wrap
			 * @return the Writable view
			 */
			public static <T extends BytesIO.Writable.Seekable> BytesIOView.Writable.Seekable of(T io) {
				if (io instanceof IO.Writable.Appendable)
					return new Appendable(io);
				return new BytesIOView.Writable.Seekable(io);
			}
			
			private static final class Appendable extends BytesIOView.Writable.Seekable implements IO.Writable.Appendable {
				private Appendable(BytesIO.Writable.Seekable io) {
					super(io);
				}
			}

			private Seekable(BytesIO.Writable.Seekable io) {
				super(io);
			}

			@Override
			public void flush() throws IOException {
				io.flush();
			}

			@Override
			public void writeByte(byte value) throws IOException {
				io.writeByte(value);
			}

			@Override
			public int writeBytes(ByteBuffer buffer) throws IOException {
				return io.writeBytes(buffer);
			}

			@Override
			public int writeBytes(byte[] buf, int off, int len) throws IOException {
				return io.writeBytes(buf, off, len);
			}

			@Override
			public int writeBytes(byte[] buf) throws IOException {
				return io.writeBytes(buf);
			}

			@Override
			public void writeBytesFully(ByteBuffer buffer) throws IOException {
				io.writeBytesFully(buffer);
			}

			@Override
			public void writeBytesFully(List<ByteBuffer> buffers) throws IOException {
				io.writeBytesFully(buffers);
			}

			@Override
			public void writeBytesFully(byte[] buf, int off, int len) throws IOException {
				io.writeBytesFully(buf, off, len);
			}

			@Override
			public void writeBytesFully(byte[] buf) throws IOException {
				io.writeBytesFully(buf);
			}
			
			@Override
			public long size() throws IOException {
				return io.size();
			}

			@Override
			public long position() throws IOException {
				return io.position();
			}

			@Override
			public long seek(SeekFrom from, long offset) throws IOException {
				return io.seek(from, offset);
			}

			@Override
			public void writeByteAt(long pos, byte value) throws IOException {
				io.writeByteAt(pos, value);
			}

			@Override
			public int writeBytesAt(long pos, ByteBuffer buffer) throws IOException {
				return io.writeBytesAt(pos, buffer);
			}

			@Override
			public int writeBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
				return io.writeBytesAt(pos, buf, off, len);
			}

			@Override
			public int writeBytesAt(long pos, byte[] buf) throws IOException {
				return io.writeBytesAt(pos, buf);
			}

			@Override
			public void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
				io.writeBytesFullyAt(pos, buffer);
			}

			@Override
			public void writeBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
				io.writeBytesFullyAt(pos, buf, off, len);
			}

			@Override
			public void writeBytesFullyAt(long pos, byte[] buf) throws IOException {
				io.writeBytesFullyAt(pos, buf);
			}

			@Override
			public void writeBytesFullyAt(long pos, List<ByteBuffer> buffers) throws IOException {
				io.writeBytesFullyAt(pos, buffers);
			}
			
		}

	}
	
	
	/** Readable and Writable view of a BytesIO.
	 * @param <T> type of BytesIO
	 */
	public static class ReadWrite<T extends BytesIO.Readable.Seekable & BytesIO.Writable.Seekable> extends BytesIOView<T> implements BytesIO.ReadWrite {
		
		/**
		 * Create a Readable and Writable view of the given IO.<br/>
		 * If the IO is Appendable, the returned view will be also Appendable.
		 * 
		 * @param <T> type of given IO
		 * @param io the IO to wrap
		 * @return the Writable view
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static <T extends BytesIO.Readable.Seekable & BytesIO.Writable.Seekable> BytesIOView.ReadWrite<T> of(T io) {
			if (io instanceof IO.Writable.Appendable)
				return new Appendable(io);
			return new BytesIOView.ReadWrite<>(io);
		}
		
		private static final class Appendable<T extends BytesIO.Readable.Seekable & BytesIO.Writable.Seekable & IO.Writable.Appendable>
		extends BytesIOView.ReadWrite<T> implements IO.Writable.Appendable {
			private Appendable(T io) {
				super(io);
			}
		}
		
		private ReadWrite(T io) {
			super(io);
		}

		@Override
		public byte readByte() throws IOException {
			return io.readByte();
		}

		@Override
		public int readBytes(ByteBuffer buffer) throws IOException {
			return io.readBytes(buffer);
		}

		@Override
		public int readBytes(byte[] buf, int off, int len) throws IOException {
			return io.readBytes(buf, off, len);
		}

		@Override
		public int readBytes(byte[] buf) throws IOException {
			return io.readBytes(buf);
		}

		@Override
		public Optional<ByteBuffer> readBuffer() throws IOException {
			return io.readBuffer();
		}

		@Override
		public void readBytesFully(ByteBuffer buffer) throws IOException {
			io.readBytesFully(buffer);
		}

		@Override
		public void readBytesFully(byte[] buf, int off, int len) throws IOException {
			io.readBytesFully(buf, off, len);
		}

		@Override
		public void readBytesFully(byte[] buf) throws IOException {
			io.readBytesFully(buf);
		}

		@Override
		public long skipUpTo(long toSkip) throws IOException {
			return io.skipUpTo(toSkip);
		}

		@Override
		public void skipFully(long toSkip) throws IOException {
			io.skipFully(toSkip);
		}
		
		@Override
		public long size() throws IOException {
			return io.size();
		}

		@Override
		public long position() throws IOException {
			return io.position();
		}

		@Override
		public long seek(SeekFrom from, long offset) throws IOException {
			return io.seek(from, offset);
		}

		@Override
		public byte readByteAt(long pos) throws IOException {
			return io.readByteAt(pos);
		}

		@Override
		public int readBytesAt(long pos, ByteBuffer buffer) throws IOException {
			return io.readBytesAt(pos, buffer);
		}

		@Override
		public int readBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			return io.readBytesAt(pos, buf, off, len);
		}

		@Override
		public int readBytesAt(long pos, byte[] buf) throws IOException {
			return io.readBytesAt(pos, buf);
		}

		@Override
		public void readBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
			io.readBytesFullyAt(pos, buffer);
		}

		@Override
		public void readBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
			io.readBytesFullyAt(pos, buf, off, len);
		}

		@Override
		public void readBytesFullyAt(long pos, byte[] buf) throws IOException {
			io.readBytesFullyAt(pos, buf);
		}
	

		@Override
		public void flush() throws IOException {
			io.flush();
		}

		@Override
		public void writeByte(byte value) throws IOException {
			io.writeByte(value);
		}

		@Override
		public int writeBytes(ByteBuffer buffer) throws IOException {
			return io.writeBytes(buffer);
		}

		@Override
		public int writeBytes(byte[] buf, int off, int len) throws IOException {
			return io.writeBytes(buf, off, len);
		}

		@Override
		public int writeBytes(byte[] buf) throws IOException {
			return io.writeBytes(buf);
		}

		@Override
		public void writeBytesFully(ByteBuffer buffer) throws IOException {
			io.writeBytesFully(buffer);
		}

		@Override
		public void writeBytesFully(List<ByteBuffer> buffers) throws IOException {
			io.writeBytesFully(buffers);
		}

		@Override
		public void writeBytesFully(byte[] buf, int off, int len) throws IOException {
			io.writeBytesFully(buf, off, len);
		}

		@Override
		public void writeBytesFully(byte[] buf) throws IOException {
			io.writeBytesFully(buf);
		}
		
		@Override
		public void writeByteAt(long pos, byte value) throws IOException {
			io.writeByteAt(pos, value);
		}

		@Override
		public int writeBytesAt(long pos, ByteBuffer buffer) throws IOException {
			return io.writeBytesAt(pos, buffer);
		}

		@Override
		public int writeBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			return io.writeBytesAt(pos, buf, off, len);
		}

		@Override
		public int writeBytesAt(long pos, byte[] buf) throws IOException {
			return io.writeBytesAt(pos, buf);
		}

		@Override
		public void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
			io.writeBytesFullyAt(pos, buffer);
		}

		@Override
		public void writeBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
			io.writeBytesFullyAt(pos, buf, off, len);
		}

		@Override
		public void writeBytesFullyAt(long pos, byte[] buf) throws IOException {
			io.writeBytesFullyAt(pos, buf);
		}

		@Override
		public void writeBytesFullyAt(long pos, List<ByteBuffer> buffers) throws IOException {
			io.writeBytesFullyAt(pos, buffers);
		}
	}

}

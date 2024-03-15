package net.lecousin.commons.io.chars;

import java.io.EOFException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IOView;

/**
 * Sub-view of a CharsIO, wrapping the original IO.
 * @param <T> type of CharsIO
 */
public abstract class CharsIOView<T extends CharsIO> extends IOView<T> {

	protected CharsIOView(T io) {
		super(io);
	}
	
	/** Readable view of a CharsIO. */
	public static class Readable extends CharsIOView<CharsIO.Readable> implements CharsIO.Readable {
		
		/** Constructor
		 * @param io I/O to wrap
		 */
		public Readable(CharsIO.Readable io) {
			super(io);
		}

		@Override
		public char readChar() throws IOException {
			return io.readChar();
		}

		@Override
		public int readChars(CharBuffer buffer) throws IOException {
			return io.readChars(buffer);
		}

		@Override
		public int readChars(char[] buf, int off, int len) throws IOException {
			return io.readChars(buf, off, len);
		}

		@Override
		public int readChars(char[] buf) throws IOException {
			return io.readChars(buf);
		}

		@Override
		public Optional<CharBuffer> readBuffer() throws IOException {
			return io.readBuffer();
		}

		@Override
		public void readCharsFully(CharBuffer buffer) throws IOException {
			io.readCharsFully(buffer);
		}

		@Override
		public void readCharsFully(char[] buf, int off, int len) throws IOException {
			io.readCharsFully(buf, off, len);
		}

		@Override
		public void readCharsFully(char[] buf) throws IOException {
			io.readCharsFully(buf);
		}

		@Override
		public long skipUpTo(long toSkip) throws IOException {
			return io.skipUpTo(toSkip);
		}

		@Override
		public void skipFully(long toSkip) throws IOException {
			io.skipFully(toSkip);
		}
		
		
		/** Readable and Seekable view of a CharsIO. */
		public static class Seekable extends CharsIOView<CharsIO.Readable.Seekable> implements CharsIO.Readable.Seekable {
			
			/**
			 * Constructor.
			 * @param io I/O to wrap.
			 */
			public Seekable(CharsIO.Readable.Seekable io) {
				super(io);
			}
			
			@Override
			public char readChar() throws IOException {
				return io.readChar();
			}

			@Override
			public int readChars(CharBuffer buffer) throws IOException {
				return io.readChars(buffer);
			}

			@Override
			public int readChars(char[] buf, int off, int len) throws IOException {
				return io.readChars(buf, off, len);
			}

			@Override
			public int readChars(char[] buf) throws IOException {
				return io.readChars(buf);
			}

			@Override
			public Optional<CharBuffer> readBuffer() throws IOException {
				return io.readBuffer();
			}

			@Override
			public void readCharsFully(CharBuffer buffer) throws IOException {
				io.readCharsFully(buffer);
			}

			@Override
			public void readCharsFully(char[] buf, int off, int len) throws IOException {
				io.readCharsFully(buf, off, len);
			}

			@Override
			public void readCharsFully(char[] buf) throws IOException {
				io.readCharsFully(buf);
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
			public char readCharAt(long pos) throws IOException {
				return io.readCharAt(pos);
			}

			@Override
			public int readCharsAt(long pos, CharBuffer buffer) throws IOException {
				return io.readCharsAt(pos, buffer);
			}

			@Override
			public int readCharsAt(long pos, char[] buf, int off, int len) throws IOException {
				return io.readCharsAt(pos, buf, off, len);
			}

			@Override
			public int readCharsAt(long pos, char[] buf) throws IOException {
				return io.readCharsAt(pos, buf);
			}

			@Override
			public void readCharsFullyAt(long pos, CharBuffer buffer) throws IOException {
				io.readCharsFullyAt(pos, buffer);
			}

			@Override
			public void readCharsFullyAt(long pos, char[] buf, int off, int len) throws IOException {
				io.readCharsFullyAt(pos, buf, off, len);
			}

			@Override
			public void readCharsFullyAt(long pos, char[] buf) throws IOException {
				io.readCharsFullyAt(pos, buf);
			}
			
		}
		
	}
	
	/** Writable view of a CharsIO. */
	public static class Writable extends CharsIOView<CharsIO.Writable> implements CharsIO.Writable {
		
		/**
		 * Create a Writable view of the given IO.<br/>
		 * If the IO is Appendable, the returned view will be also Appendable.
		 * 
		 * @param <T> type of given IO
		 * @param io the IO to wrap
		 * @return the Writable view
		 */
		public static <T extends CharsIO.Writable> CharsIOView.Writable of(T io) {
			if (io instanceof IO.Writable.Appendable)
				return new Appendable(io);
			return new CharsIOView.Writable(io);
		}
		
		private static final class Appendable extends CharsIOView.Writable implements IO.Writable.Appendable {
			private Appendable(CharsIO.Writable io) {
				super(io);
			}
		}
		
		private Writable(CharsIO.Writable io) {
			super(io);
		}

		@Override
		public void flush() throws IOException {
			io.flush();
		}

		@Override
		public void writeChar(char value) throws IOException {
			io.writeChar(value);
		}

		@Override
		public int writeChars(CharBuffer buffer) throws IOException {
			return io.writeChars(buffer);
		}

		@Override
		public int writeChars(char[] buf, int off, int len) throws IOException {
			return io.writeChars(buf, off, len);
		}

		@Override
		public int writeChars(char[] buf) throws IOException {
			return io.writeChars(buf);
		}

		@Override
		public void writeCharsFully(CharBuffer buffer) throws IOException {
			io.writeCharsFully(buffer);
		}

		@Override
		public void writeCharsFully(List<CharBuffer> buffers) throws IOException {
			io.writeCharsFully(buffers);
		}

		@Override
		public void writeCharsFully(char[] buf, int off, int len) throws IOException {
			io.writeCharsFully(buf, off, len);
		}

		@Override
		public void writeCharsFully(char[] buf) throws IOException {
			io.writeCharsFully(buf);
		}

		/** Writable and Seekable view of a CharsIO. */
		public static class Seekable extends CharsIOView<CharsIO.Writable.Seekable> implements CharsIO.Writable.Seekable {

			/**
			 * Create a Writable and Seekable view of the given IO.<br/>
			 * If the IO is Appendable, the returned view will be also Appendable.
			 * 
			 * @param <T> type of given IO
			 * @param io the IO to wrap
			 * @return the Writable view
			 */
			public static <T extends CharsIO.Writable.Seekable> CharsIOView.Writable.Seekable of(T io) {
				if (io instanceof IO.Writable.Appendable)
					return new Appendable(io);
				return new CharsIOView.Writable.Seekable(io);
			}
			
			private static final class Appendable extends CharsIOView.Writable.Seekable implements IO.Writable.Appendable {
				private Appendable(CharsIO.Writable.Seekable io) {
					super(io);
				}
			}

			private Seekable(CharsIO.Writable.Seekable io) {
				super(io);
			}

			@Override
			public void flush() throws IOException {
				io.flush();
			}

			@Override
			public void writeChar(char value) throws IOException {
				io.writeChar(value);
			}

			@Override
			public int writeChars(CharBuffer buffer) throws IOException {
				return io.writeChars(buffer);
			}

			@Override
			public int writeChars(char[] buf, int off, int len) throws IOException {
				return io.writeChars(buf, off, len);
			}

			@Override
			public int writeChars(char[] buf) throws IOException {
				return io.writeChars(buf);
			}

			@Override
			public void writeCharsFully(CharBuffer buffer) throws IOException {
				io.writeCharsFully(buffer);
			}

			@Override
			public void writeCharsFully(List<CharBuffer> buffers) throws IOException {
				io.writeCharsFully(buffers);
			}

			@Override
			public void writeCharsFully(char[] buf, int off, int len) throws IOException {
				io.writeCharsFully(buf, off, len);
			}

			@Override
			public void writeCharsFully(char[] buf) throws IOException {
				io.writeCharsFully(buf);
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
			public void writeCharAt(long pos, char value) throws IOException {
				io.writeCharAt(pos, value);
			}

			@Override
			public int writeCharsAt(long pos, CharBuffer buffer) throws IOException {
				return io.writeCharsAt(pos, buffer);
			}

			@Override
			public int writeCharsAt(long pos, char[] buf, int off, int len) throws IOException {
				return io.writeCharsAt(pos, buf, off, len);
			}

			@Override
			public int writeCharsAt(long pos, char[] buf) throws IOException {
				return io.writeCharsAt(pos, buf);
			}

			@Override
			public void writeCharsFullyAt(long pos, CharBuffer buffer) throws IOException {
				io.writeCharsFullyAt(pos, buffer);
			}

			@Override
			public void writeCharsFullyAt(long pos, char[] buf, int off, int len) throws IOException {
				io.writeCharsFullyAt(pos, buf, off, len);
			}

			@Override
			public void writeCharsFullyAt(long pos, char[] buf) throws IOException {
				io.writeCharsFullyAt(pos, buf);
			}

			@Override
			public void writeCharsFullyAt(long pos, List<CharBuffer> buffers) throws IOException {
				io.writeCharsFullyAt(pos, buffers);
			}
			
			
			/** Writable and Seekable and Resizable view of a CharsIO. */
			public static class Resizable extends CharsIOView<CharsIO.Writable.Seekable> implements CharsIO.Writable.Seekable.Resizable {

				/**
				 * Create a Writable and Seekable view of the given IO.<br/>
				 * If the IO is Appendable, the returned view will be also Appendable.
				 * 
				 * @param <T> type of given IO
				 * @param io the IO to wrap
				 * @return the Writable view
				 */
				public static <T extends CharsIO.Writable.Seekable & IO.Writable.Resizable> CharsIOView.Writable.Seekable.Resizable of(T io) {
					if (io instanceof IO.Writable.Appendable)
						return new Appendable(io);
					return new CharsIOView.Writable.Seekable.Resizable(io);
				}
				
				private static final class Appendable extends CharsIOView.Writable.Seekable.Resizable implements IO.Writable.Appendable {
					private Appendable(CharsIO.Writable.Seekable io) {
						super(io);
					}
				}

				private Resizable(CharsIO.Writable.Seekable io) {
					super(io);
				}
				
				@Override
				public void setSize(long newSize) throws IOException {
					((IO.Writable.Resizable) io).setSize(newSize);
				}

				@Override
				public void flush() throws IOException {
					io.flush();
				}

				@Override
				public void writeChar(char value) throws IOException {
					io.writeChar(value);
				}

				@Override
				public int writeChars(CharBuffer buffer) throws IOException {
					return io.writeChars(buffer);
				}

				@Override
				public int writeChars(char[] buf, int off, int len) throws IOException {
					return io.writeChars(buf, off, len);
				}

				@Override
				public int writeChars(char[] buf) throws IOException {
					return io.writeChars(buf);
				}

				@Override
				public void writeCharsFully(CharBuffer buffer) throws IOException {
					io.writeCharsFully(buffer);
				}

				@Override
				public void writeCharsFully(List<CharBuffer> buffers) throws IOException {
					io.writeCharsFully(buffers);
				}

				@Override
				public void writeCharsFully(char[] buf, int off, int len) throws IOException {
					io.writeCharsFully(buf, off, len);
				}

				@Override
				public void writeCharsFully(char[] buf) throws IOException {
					io.writeCharsFully(buf);
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
				public void writeCharAt(long pos, char value) throws IOException {
					io.writeCharAt(pos, value);
				}

				@Override
				public int writeCharsAt(long pos, CharBuffer buffer) throws IOException {
					return io.writeCharsAt(pos, buffer);
				}

				@Override
				public int writeCharsAt(long pos, char[] buf, int off, int len) throws IOException {
					return io.writeCharsAt(pos, buf, off, len);
				}

				@Override
				public int writeCharsAt(long pos, char[] buf) throws IOException {
					return io.writeCharsAt(pos, buf);
				}

				@Override
				public void writeCharsFullyAt(long pos, CharBuffer buffer) throws IOException {
					io.writeCharsFullyAt(pos, buffer);
				}

				@Override
				public void writeCharsFullyAt(long pos, char[] buf, int off, int len) throws IOException {
					io.writeCharsFullyAt(pos, buf, off, len);
				}

				@Override
				public void writeCharsFullyAt(long pos, char[] buf) throws IOException {
					io.writeCharsFullyAt(pos, buf);
				}

				@Override
				public void writeCharsFullyAt(long pos, List<CharBuffer> buffers) throws IOException {
					io.writeCharsFullyAt(pos, buffers);
				}
			} 
		}

	}
	
	
	/** Readable and Writable view of a CharsIO.
	 * @param <T> type of CharsIO
	 */
	public static class ReadWrite<T extends CharsIO.Readable.Seekable & CharsIO.Writable.Seekable> extends CharsIOView<T> implements CharsIO.ReadWrite {
		
		/**
		 * Create a Readable and Writable view of the given IO.<br/>
		 * If the IO is Appendable, the returned view will be also Appendable.
		 * 
		 * @param <T> type of given IO
		 * @param io the IO to wrap
		 * @return the Writable view
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static <T extends CharsIO.Readable.Seekable & CharsIO.Writable.Seekable> CharsIOView.ReadWrite<T> of(T io) {
			if (io instanceof IO.Writable.Appendable)
				return new Appendable(io);
			return new CharsIOView.ReadWrite<>(io);
		}
		
		private static final class Appendable<T extends CharsIO.Readable.Seekable & CharsIO.Writable.Seekable & IO.Writable.Appendable>
		extends CharsIOView.ReadWrite<T> implements IO.Writable.Appendable {
			private Appendable(T io) {
				super(io);
			}
		}
		
		private ReadWrite(T io) {
			super(io);
		}

		@Override
		public char readChar() throws IOException {
			return io.readChar();
		}

		@Override
		public int readChars(CharBuffer buffer) throws IOException {
			return io.readChars(buffer);
		}

		@Override
		public int readChars(char[] buf, int off, int len) throws IOException {
			return io.readChars(buf, off, len);
		}

		@Override
		public int readChars(char[] buf) throws IOException {
			return io.readChars(buf);
		}

		@Override
		public Optional<CharBuffer> readBuffer() throws IOException {
			return io.readBuffer();
		}

		@Override
		public void readCharsFully(CharBuffer buffer) throws IOException {
			io.readCharsFully(buffer);
		}

		@Override
		public void readCharsFully(char[] buf, int off, int len) throws IOException {
			io.readCharsFully(buf, off, len);
		}

		@Override
		public void readCharsFully(char[] buf) throws IOException {
			io.readCharsFully(buf);
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
		public char readCharAt(long pos) throws IOException {
			return io.readCharAt(pos);
		}

		@Override
		public int readCharsAt(long pos, CharBuffer buffer) throws IOException {
			return io.readCharsAt(pos, buffer);
		}

		@Override
		public int readCharsAt(long pos, char[] buf, int off, int len) throws IOException {
			return io.readCharsAt(pos, buf, off, len);
		}

		@Override
		public int readCharsAt(long pos, char[] buf) throws IOException {
			return io.readCharsAt(pos, buf);
		}

		@Override
		public void readCharsFullyAt(long pos, CharBuffer buffer) throws IOException {
			io.readCharsFullyAt(pos, buffer);
		}

		@Override
		public void readCharsFullyAt(long pos, char[] buf, int off, int len) throws IOException {
			io.readCharsFullyAt(pos, buf, off, len);
		}

		@Override
		public void readCharsFullyAt(long pos, char[] buf) throws IOException {
			io.readCharsFullyAt(pos, buf);
		}
	

		@Override
		public void flush() throws IOException {
			io.flush();
		}

		@Override
		public void writeChar(char value) throws IOException {
			io.writeChar(value);
		}

		@Override
		public int writeChars(CharBuffer buffer) throws IOException {
			return io.writeChars(buffer);
		}

		@Override
		public int writeChars(char[] buf, int off, int len) throws IOException {
			return io.writeChars(buf, off, len);
		}

		@Override
		public int writeChars(char[] buf) throws IOException {
			return io.writeChars(buf);
		}

		@Override
		public void writeCharsFully(CharBuffer buffer) throws IOException {
			io.writeCharsFully(buffer);
		}

		@Override
		public void writeCharsFully(List<CharBuffer> buffers) throws IOException {
			io.writeCharsFully(buffers);
		}

		@Override
		public void writeCharsFully(char[] buf, int off, int len) throws IOException {
			io.writeCharsFully(buf, off, len);
		}

		@Override
		public void writeCharsFully(char[] buf) throws IOException {
			io.writeCharsFully(buf);
		}
		
		@Override
		public void writeCharAt(long pos, char value) throws IOException {
			io.writeCharAt(pos, value);
		}

		@Override
		public int writeCharsAt(long pos, CharBuffer buffer) throws IOException {
			return io.writeCharsAt(pos, buffer);
		}

		@Override
		public int writeCharsAt(long pos, char[] buf, int off, int len) throws IOException {
			return io.writeCharsAt(pos, buf, off, len);
		}

		@Override
		public int writeCharsAt(long pos, char[] buf) throws IOException {
			return io.writeCharsAt(pos, buf);
		}

		@Override
		public void writeCharsFullyAt(long pos, CharBuffer buffer) throws IOException {
			io.writeCharsFullyAt(pos, buffer);
		}

		@Override
		public void writeCharsFullyAt(long pos, char[] buf, int off, int len) throws IOException {
			io.writeCharsFullyAt(pos, buf, off, len);
		}

		@Override
		public void writeCharsFullyAt(long pos, char[] buf) throws IOException {
			io.writeCharsFullyAt(pos, buf);
		}

		@Override
		public void writeCharsFullyAt(long pos, List<CharBuffer> buffers) throws IOException {
			io.writeCharsFullyAt(pos, buffers);
		}
	}

}

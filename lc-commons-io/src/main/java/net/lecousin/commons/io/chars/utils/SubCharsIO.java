package net.lecousin.commons.io.chars.utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Optional;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.chars.CharsIO;
import net.lecousin.commons.io.utils.AbstractSubIO;

/**
 * Sub-part of a seekable IO.
 */
public interface SubCharsIO {
	
	/** Create a SubCharsIO Read and Write.
	 * 
	 * @param <T> type of IO
	 * @param io IO
	 * @param start start offset in the IO
	 * @param size size of the sub-io
	 * @param closeIoOnClose if true, when this SubIO is closed, the underlying IO will also be closed 
	 * @return a CharsIO.ReadWrite corresponding to the requested slice 
	 */
	static <T extends CharsIO.Readable.Seekable & CharsIO.Writable.Seekable> SubCharsIO.ReadWrite fromReadWrite(T io, long start, long size, boolean closeIoOnClose) {
		return new ReadWrite(io, start, size, closeIoOnClose);
	}
	
	/** Create a SubCharsIO Read-only.
	 * 
	 * @param <T> type of IO
	 * @param io IO
	 * @param start start offset in the IO
	 * @param size size of the sub-io
	 * @param closeIoOnClose if true, when this SubIO is closed, the underlying IO will also be closed 
	 * @return a CharsIO.Readable.Seekable corresponding to the requested slice 
	 */
	@SuppressWarnings("resource")
	static <T extends CharsIO.Readable.Seekable> CharsIO.Readable.Seekable fromReadable(T io, long start, long size, boolean closeIoOnClose) {
		return new ReadWrite(io, start, size, closeIoOnClose).asReadableSeekableCharsIO();
	}
	
	/** Create a SubCharsIO Write-only.
	 * 
	 * @param <T> type of IO
	 * @param io IO
	 * @param start start offset in the IO
	 * @param size size of the sub-io
	 * @param closeIoOnClose if true, when this SubIO is closed, the underlying IO will also be closed 
	 * @return a CharsIO.Writable.Seekable corresponding to the requested slice 
	 */
	@SuppressWarnings("resource")
	static <T extends CharsIO.Writable.Seekable> CharsIO.Writable.Seekable fromWritable(T io, long start, long size, boolean closeIoOnClose) {
		return new ReadWrite(io, start, size, closeIoOnClose).asWritableSeekableCharsIO();
	}

	/** Read-Write implementation. */
	class ReadWrite extends AbstractSubIO<CharsIO> implements CharsIO.ReadWrite {

		protected ReadWrite(CharsIO io, long start, long size, boolean closeIoOnClose) {
			super(io, start, size, closeIoOnClose);
		}
		
		private static final int DEFAULT_BUFFER_SIZE = 8192;
		
		// --- Readable ---
		
		@Override
		public char readChar() throws IOException {
			if (io == null) throw new ClosedChannelException();
			if (position == size) throw new EOFException();
			return ((CharsIO.Readable.Seekable) io).readCharAt(start + position++);
		}
		
		@Override
		public char readCharAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos >= size) throw new EOFException();
			return ((CharsIO.Readable.Seekable) io).readCharAt(start + pos);
		}
		
		@Override
		public int readChars(CharBuffer buffer) throws IOException {
			if (io == null) throw new ClosedChannelException();
			int r = buffer.remaining();
			if (r == 0) return 0;
			if (position == size) return -1;
			if (position + r <= size) {
				int n = ((CharsIO.Readable.Seekable) io).readCharsAt(start + position, buffer);
				position += n;
				return n;
			}
			int l = buffer.limit();
			buffer.limit(buffer.position() + (int) (size - position));
			int n = ((CharsIO.Readable.Seekable) io).readCharsAt(start + position, buffer);
			position += n;
			buffer.limit(l);
			return n;
		}
		
		@Override
		public int readCharsAt(long pos, CharBuffer buffer) throws IOException {
			IOChecks.checkBufferOperation(this, pos, buffer);
			int r = buffer.remaining();
			if (r == 0) return 0;
			if (pos >= size) return -1;
			if (pos + r <= size)
				return ((CharsIO.Readable.Seekable) io).readCharsAt(start + pos, buffer);
			int l = buffer.limit();
			buffer.limit(buffer.position() + (int) (size - pos));
			int n = ((CharsIO.Readable.Seekable) io).readCharsAt(start + pos, buffer);
			buffer.limit(l);
			return n;
		}
		
		@Override
		public int readChars(char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			if (len == 0) return 0;
			if (position == size) return -1;
			len = (int) Math.min(len, size - position);
			int n = ((CharsIO.Readable.Seekable) io).readCharsAt(start + position, buf, off, len);
			position += n;
			return n;
		}
		
		@Override
		public int readCharsAt(long pos, char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, pos, buf, off, len);
			if (len == 0) return 0;
			if (pos >= size) return -1;
			len = (int) Math.min(len, size - pos);
			return ((CharsIO.Readable.Seekable) io).readCharsAt(start + pos, buf, off, len);
		}
		
		@Override
		public Optional<CharBuffer> readBuffer() throws IOException {
			if (io == null) throw new ClosedChannelException();
			if (position == size) return Optional.empty();
			int len = (int) Math.min(DEFAULT_BUFFER_SIZE, size - position);
			CharBuffer b = CharBuffer.allocate(len);
			position += ((CharsIO.Readable.Seekable) io).readCharsAt(start + position, b);
			return Optional.of(b.flip());
		}
		
		@Override
		public void readCharsFully(CharBuffer buffer) throws IOException {
			if (io == null) throw new ClosedChannelException();
			int r = buffer.remaining();
			if (r == 0) return;
			if (position + r > size) throw new EOFException();
			((CharsIO.Readable.Seekable) io).readCharsFullyAt(start + position, buffer);
			position += r;
		}
		
		@Override
		public void readCharsFullyAt(long pos, CharBuffer buffer) throws IOException {
			IOChecks.checkBufferOperation(this, pos, buffer);
			int r = buffer.remaining();
			if (r == 0) return;
			if (pos + r > size) throw new EOFException();
			((CharsIO.Readable.Seekable) io).readCharsFullyAt(start + pos, buffer);
		}
		
		@Override
		public void readCharsFully(char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			if (len == 0) return;
			if (position + len > size) throw new EOFException();
			((CharsIO.Readable.Seekable) io).readCharsFullyAt(start + position, buf, off, len);
			position += len;
		}
		
		@Override
		public void readCharsFullyAt(long pos, char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, pos, buf, off, len);
			if (len == 0) return;
			if (pos + len > size) throw new EOFException();
			((CharsIO.Readable.Seekable) io).readCharsFullyAt(start + pos, buf, off, len);
		}
		
		
		// --- Writable ---
		
		@Override
		public void writeChar(char value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			if (position == size) throw new EOFException();
			((CharsIO.Writable.Seekable) io).writeCharAt(start + position++, value);
		}

		@Override
		public void writeCharAt(long pos, char value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos >= size) throw new EOFException();
			((CharsIO.Writable.Seekable) io).writeCharAt(start + pos, value);
		}

		@Override
		public int writeChars(CharBuffer buffer) throws IOException {
			if (io == null) throw new ClosedChannelException();
			int r = buffer.remaining();
			if (r == 0) return 0;
			if (position == size) return -1;
			if (position + r <= size) {
				int n = ((CharsIO.Writable.Seekable) io).writeCharsAt(start + position, buffer);
				position += n;
				return n;
			}
			int l = buffer.limit();
			buffer.limit(buffer.position() + (int) (size - position));
			int n = ((CharsIO.Writable.Seekable) io).writeCharsAt(start + position, buffer);
			position += n;
			buffer.limit(l);
			return n;
		}
		
		@Override
		public int writeCharsAt(long pos, CharBuffer buffer) throws IOException {
			IOChecks.checkBufferOperation(this, pos, buffer);
			int r = buffer.remaining();
			if (r == 0) return 0;
			if (pos >= size) return -1;
			if (pos + r <= size)
				return ((CharsIO.Writable.Seekable) io).writeCharsAt(start + pos, buffer);
			int l = buffer.limit();
			buffer.limit(buffer.position() + (int) (size - pos));
			int n = ((CharsIO.Writable.Seekable) io).writeCharsAt(start + pos, buffer);
			buffer.limit(l);
			return n;
		}
		
		@Override
		public int writeChars(char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			if (len == 0) return 0;
			if (position == size) return -1;
			len = (int) Math.min(len, size - position);
			int n = ((CharsIO.Writable.Seekable) io).writeCharsAt(start + position, buf, off, len);
			position += n;
			return n;
		}
		
		@Override
		public int writeCharsAt(long pos, char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, pos, buf, off, len);
			if (len == 0) return 0;
			if (pos >= size) return -1;
			len = (int) Math.min(len, size - pos);
			return ((CharsIO.Writable.Seekable) io).writeCharsAt(start + pos, buf, off, len);
		}
		
		@Override
		public void writeCharsFully(CharBuffer buffer) throws IOException {
			if (io == null) throw new ClosedChannelException();
			int r = buffer.remaining();
			if (r == 0) return;
			if (position + r > size) throw new EOFException();
			((CharsIO.Writable.Seekable) io).writeCharsFullyAt(start + position, buffer);
			position += r;
		}
		
		@Override
		public void writeCharsFullyAt(long pos, CharBuffer buffer) throws IOException {
			IOChecks.checkBufferOperation(this, pos, buffer);
			int r = buffer.remaining();
			if (r == 0) return;
			if (pos + r > size) throw new EOFException();
			((CharsIO.Writable.Seekable) io).writeCharsFullyAt(start + pos, buffer);
		}
		
		@Override
		public void writeCharsFully(char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			if (len == 0) return;
			if (position + len > size) throw new EOFException();
			((CharsIO.Writable.Seekable) io).writeCharsFullyAt(start + position, buf, off, len);
			position += len;
		}
		
		@Override
		public void writeCharsFullyAt(long pos, char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, pos, buf, off, len);
			if (len == 0) return;
			if (pos + len > size) throw new EOFException();
			((CharsIO.Writable.Seekable) io).writeCharsFullyAt(start + pos, buf, off, len);
		}
		
	}

}

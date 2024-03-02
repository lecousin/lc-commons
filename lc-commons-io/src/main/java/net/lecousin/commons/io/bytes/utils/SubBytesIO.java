package net.lecousin.commons.io.bytes.utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Optional;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.BytesIO;

/**
 * Sub-part of a seekable IO.
 */
public interface SubBytesIO {
	
	/** Create a SubBytesIO Read and Write.
	 * 
	 * @param <T> type of IO
	 * @param io IO
	 * @param start start offset in the IO
	 * @param size size of the sub-io
	 * @param closeIoOnClose if true, when this SubIO is closed, the underlying IO will also be closed 
	 * @return a BytesIO.ReadWrite corresponding to the requested slice 
	 */
	static <T extends BytesIO.Readable.Seekable & BytesIO.Writable.Seekable> SubBytesIO.ReadWrite fromReadWrite(T io, long start, long size, boolean closeIoOnClose) {
		return new ReadWrite(io, start, size, closeIoOnClose);
	}
	
	/** Create a SubBytesIO Read-only.
	 * 
	 * @param <T> type of IO
	 * @param io IO
	 * @param start start offset in the IO
	 * @param size size of the sub-io
	 * @param closeIoOnClose if true, when this SubIO is closed, the underlying IO will also be closed 
	 * @return a BytesIO.Readable.Seekable corresponding to the requested slice 
	 */
	@SuppressWarnings("resource")
	static <T extends BytesIO.Readable.Seekable> BytesIO.Readable.Seekable fromReadable(T io, long start, long size, boolean closeIoOnClose) {
		return new ReadWrite(io, start, size, closeIoOnClose).asReadableSeekableBytesIO();
	}
	
	/** Create a SubBytesIO Write-only.
	 * 
	 * @param <T> type of IO
	 * @param io IO
	 * @param start start offset in the IO
	 * @param size size of the sub-io
	 * @param closeIoOnClose if true, when this SubIO is closed, the underlying IO will also be closed 
	 * @return a BytesIO.Writable.Seekable corresponding to the requested slice 
	 */
	@SuppressWarnings("resource")
	static <T extends BytesIO.Writable.Seekable> BytesIO.Writable.Seekable fromWritable(T io, long start, long size, boolean closeIoOnClose) {
		return new ReadWrite(io, start, size, closeIoOnClose).asWritableSeekableBytesIO();
	}

	/** Read-Write implementation. */
	class ReadWrite extends AbstractIO implements BytesIO.ReadWrite {

		protected BytesIO io;
		protected long start;
		protected long size;
		protected long position = 0;
		private boolean closeIoOnClose;
		
		protected ReadWrite(BytesIO io, long start, long size, boolean closeIoOnClose) {
			NegativeValueException.check(start, "start");
			NegativeValueException.check(size, "size");
			this.io = io;
			this.start = start;
			this.size = size;
			this.closeIoOnClose = closeIoOnClose;
		}
		
		private static final int DEFAULT_BUFFER_SIZE = 8192;
		
		@Override
		protected void closeInternal() throws IOException {
			if (closeIoOnClose) io.close();
			io = null;
		}
		
		@Override
		public long position() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return position;
		}
		
		@Override
		public long size() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return size;
		}
		
		@Override
		public long seek(SeekFrom from, long offset) throws IOException {
			if (io == null) throw new ClosedChannelException();
			long p;
			switch (Objects.requireNonNull(from, "from")) {
			case CURRENT: p = position + offset; break;
			case END: p = size - offset; break;
			case START: default: p = offset; break;
			}
			if (p < 0) throw new IllegalArgumentException("Cannot move beyond the start: " + p);
			if (p > size) throw new EOFException();
			this.position = p;
			return p;
		}
		
		// --- Readable ---
		
		@Override
		public byte readByte() throws IOException {
			if (io == null) throw new ClosedChannelException();
			if (position == size) throw new EOFException();
			return ((BytesIO.Readable.Seekable) io).readByteAt(start + position++);
		}
		
		@Override
		public byte readByteAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos >= size) throw new EOFException();
			return ((BytesIO.Readable.Seekable) io).readByteAt(start + pos);
		}
		
		@Override
		public int readBytes(ByteBuffer buffer) throws IOException {
			if (io == null) throw new ClosedChannelException();
			int r = buffer.remaining();
			if (r == 0) return 0;
			if (position == size) return -1;
			if (position + r <= size) {
				int n = ((BytesIO.Readable.Seekable) io).readBytesAt(start + position, buffer);
				position += n;
				return n;
			}
			int l = buffer.limit();
			buffer.limit(buffer.position() + (int) (size - position));
			int n = ((BytesIO.Readable.Seekable) io).readBytesAt(start + position, buffer);
			position += n;
			buffer.limit(l);
			return n;
		}
		
		@Override
		public int readBytesAt(long pos, ByteBuffer buffer) throws IOException {
			IOChecks.checkByteBufferOperation(this, pos, buffer);
			int r = buffer.remaining();
			if (r == 0) return 0;
			if (pos >= size) return -1;
			if (pos + r <= size)
				return ((BytesIO.Readable.Seekable) io).readBytesAt(start + pos, buffer);
			int l = buffer.limit();
			buffer.limit(buffer.position() + (int) (size - pos));
			int n = ((BytesIO.Readable.Seekable) io).readBytesAt(start + pos, buffer);
			buffer.limit(l);
			return n;
		}
		
		@Override
		public int readBytes(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf, off, len);
			if (len == 0) return 0;
			if (position == size) return -1;
			len = (int) Math.min(len, size - position);
			int n = ((BytesIO.Readable.Seekable) io).readBytesAt(start + position, buf, off, len);
			position += n;
			return n;
		}
		
		@Override
		public int readBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
			if (len == 0) return 0;
			if (pos >= size) return -1;
			len = (int) Math.min(len, size - pos);
			return ((BytesIO.Readable.Seekable) io).readBytesAt(start + pos, buf, off, len);
		}
		
		@Override
		public Optional<ByteBuffer> readBuffer() throws IOException {
			if (io == null) throw new ClosedChannelException();
			if (position == size) return Optional.empty();
			int len = (int) Math.min(DEFAULT_BUFFER_SIZE, size - position);
			ByteBuffer b = ByteBuffer.allocate(len);
			position += ((BytesIO.Readable.Seekable) io).readBytesAt(start + position, b);
			return Optional.of(b.flip());
		}
		
		@Override
		public void readBytesFully(ByteBuffer buffer) throws IOException {
			if (io == null) throw new ClosedChannelException();
			int r = buffer.remaining();
			if (r == 0) return;
			if (position + r > size) throw new EOFException();
			((BytesIO.Readable.Seekable) io).readBytesFullyAt(start + position, buffer);
			position += r;
		}
		
		@Override
		public void readBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
			IOChecks.checkByteBufferOperation(this, pos, buffer);
			int r = buffer.remaining();
			if (r == 0) return;
			if (pos + r > size) throw new EOFException();
			((BytesIO.Readable.Seekable) io).readBytesFullyAt(start + pos, buffer);
		}
		
		@Override
		public void readBytesFully(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf, off, len);
			if (len == 0) return;
			if (position + len > size) throw new EOFException();
			((BytesIO.Readable.Seekable) io).readBytesFullyAt(start + position, buf, off, len);
			position += len;
		}
		
		@Override
		public void readBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
			if (len == 0) return;
			if (pos + len > size) throw new EOFException();
			((BytesIO.Readable.Seekable) io).readBytesFullyAt(start + pos, buf, off, len);
		}
		
		@Override
		public long skipUpTo(long toSkip) throws IOException {
			if (io == null) throw new ClosedChannelException();
			if (toSkip == 0) return 0;
			NegativeValueException.check(toSkip, "toSkip");
			if (position == size) return -1;
			long skip = Math.min(toSkip, size - position);
			this.position += skip;
			return skip;
		}
		
		@Override
		public void skipFully(long toSkip) throws IOException {
			if (io == null) throw new ClosedChannelException();
			NegativeValueException.check(toSkip, "toSkip");
			if (toSkip > size - position) throw new EOFException();
			this.position += toSkip;
		}
		
		
		// --- Writable ---
		
		@Override
		public void flush() throws IOException {
			if (io == null) throw new ClosedChannelException();
			((BytesIO.Writable) io).flush();
		}

		@Override
		public void writeByte(byte value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			if (position == size) throw new EOFException();
			((BytesIO.Writable.Seekable) io).writeByteAt(start + position++, value);
		}

		@Override
		public void writeByteAt(long pos, byte value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos >= size) throw new EOFException();
			((BytesIO.Writable.Seekable) io).writeByteAt(start + pos, value);
		}

		@Override
		public int writeBytes(ByteBuffer buffer) throws IOException {
			if (io == null) throw new ClosedChannelException();
			int r = buffer.remaining();
			if (r == 0) return 0;
			if (position == size) return -1;
			if (position + r <= size) {
				int n = ((BytesIO.Writable.Seekable) io).writeBytesAt(start + position, buffer);
				position += n;
				return n;
			}
			int l = buffer.limit();
			buffer.limit(buffer.position() + (int) (size - position));
			int n = ((BytesIO.Writable.Seekable) io).writeBytesAt(start + position, buffer);
			position += n;
			buffer.limit(l);
			return n;
		}
		
		@Override
		public int writeBytesAt(long pos, ByteBuffer buffer) throws IOException {
			IOChecks.checkByteBufferOperation(this, pos, buffer);
			int r = buffer.remaining();
			if (r == 0) return 0;
			if (pos >= size) return -1;
			if (pos + r <= size)
				return ((BytesIO.Writable.Seekable) io).writeBytesAt(start + pos, buffer);
			int l = buffer.limit();
			buffer.limit(buffer.position() + (int) (size - pos));
			int n = ((BytesIO.Writable.Seekable) io).writeBytesAt(start + pos, buffer);
			buffer.limit(l);
			return n;
		}
		
		@Override
		public int writeBytes(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf, off, len);
			if (len == 0) return 0;
			if (position == size) return -1;
			len = (int) Math.min(len, size - position);
			int n = ((BytesIO.Writable.Seekable) io).writeBytesAt(start + position, buf, off, len);
			position += n;
			return n;
		}
		
		@Override
		public int writeBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
			if (len == 0) return 0;
			if (pos >= size) return -1;
			len = (int) Math.min(len, size - pos);
			return ((BytesIO.Writable.Seekable) io).writeBytesAt(start + pos, buf, off, len);
		}
		
		@Override
		public void writeBytesFully(ByteBuffer buffer) throws IOException {
			if (io == null) throw new ClosedChannelException();
			int r = buffer.remaining();
			if (r == 0) return;
			if (position + r > size) throw new EOFException();
			((BytesIO.Writable.Seekable) io).writeBytesFullyAt(start + position, buffer);
			position += r;
		}
		
		@Override
		public void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
			IOChecks.checkByteBufferOperation(this, pos, buffer);
			int r = buffer.remaining();
			if (r == 0) return;
			if (pos + r > size) throw new EOFException();
			((BytesIO.Writable.Seekable) io).writeBytesFullyAt(start + pos, buffer);
		}
		
		@Override
		public void writeBytesFully(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf, off, len);
			if (len == 0) return;
			if (position + len > size) throw new EOFException();
			((BytesIO.Writable.Seekable) io).writeBytesFullyAt(start + position, buf, off, len);
			position += len;
		}
		
		@Override
		public void writeBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
			if (len == 0) return;
			if (pos + len > size) throw new EOFException();
			((BytesIO.Writable.Seekable) io).writeBytesFullyAt(start + pos, buf, off, len);
		}
		
	}

}

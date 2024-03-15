package net.lecousin.commons.io.bytes.utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Optional;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.utils.AbstractSubIO;

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
	class ReadWrite extends AbstractSubIO<BytesIO> implements BytesIO.ReadWrite {

		protected ReadWrite(BytesIO io, long start, long size, boolean closeIoOnClose) {
			super(io, start, size, closeIoOnClose);
		}
		
		private static final int DEFAULT_BUFFER_SIZE = 8192;
		
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
			IOChecks.checkBufferOperation(this, pos, buffer);
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
			IOChecks.checkArrayOperation(this, buf, off, len);
			if (len == 0) return 0;
			if (position == size) return -1;
			len = (int) Math.min(len, size - position);
			int n = ((BytesIO.Readable.Seekable) io).readBytesAt(start + position, buf, off, len);
			position += n;
			return n;
		}
		
		@Override
		public int readBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, pos, buf, off, len);
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
			IOChecks.checkBufferOperation(this, pos, buffer);
			int r = buffer.remaining();
			if (r == 0) return;
			if (pos + r > size) throw new EOFException();
			((BytesIO.Readable.Seekable) io).readBytesFullyAt(start + pos, buffer);
		}
		
		@Override
		public void readBytesFully(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			if (len == 0) return;
			if (position + len > size) throw new EOFException();
			((BytesIO.Readable.Seekable) io).readBytesFullyAt(start + position, buf, off, len);
			position += len;
		}
		
		@Override
		public void readBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, pos, buf, off, len);
			if (len == 0) return;
			if (pos + len > size) throw new EOFException();
			((BytesIO.Readable.Seekable) io).readBytesFullyAt(start + pos, buf, off, len);
		}
		
		
		// --- Writable ---
		
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
			IOChecks.checkBufferOperation(this, pos, buffer);
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
			IOChecks.checkArrayOperation(this, buf, off, len);
			if (len == 0) return 0;
			if (position == size) return -1;
			len = (int) Math.min(len, size - position);
			int n = ((BytesIO.Writable.Seekable) io).writeBytesAt(start + position, buf, off, len);
			position += n;
			return n;
		}
		
		@Override
		public int writeBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, pos, buf, off, len);
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
			IOChecks.checkBufferOperation(this, pos, buffer);
			int r = buffer.remaining();
			if (r == 0) return;
			if (pos + r > size) throw new EOFException();
			((BytesIO.Writable.Seekable) io).writeBytesFullyAt(start + pos, buffer);
		}
		
		@Override
		public void writeBytesFully(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			if (len == 0) return;
			if (position + len > size) throw new EOFException();
			((BytesIO.Writable.Seekable) io).writeBytesFullyAt(start + position, buf, off, len);
			position += len;
		}
		
		@Override
		public void writeBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, pos, buf, off, len);
			if (len == 0) return;
			if (pos + len > size) throw new EOFException();
			((BytesIO.Writable.Seekable) io).writeBytesFullyAt(start + pos, buf, off, len);
		}
		
	}

}

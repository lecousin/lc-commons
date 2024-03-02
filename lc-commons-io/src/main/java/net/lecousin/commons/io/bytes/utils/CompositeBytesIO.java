package net.lecousin.commons.io.bytes.utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.function.FailableSupplier;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.BytesIO;

/** BytesIO aggregating of multiple IOs. */
public interface CompositeBytesIO {

	/** Create a CompositeBytesIO Read and Write.
	 * 
	 * @param <T> type of IO
	 * @param ios list of IOs, possibly empty
	 * @param closeIosOnClose if true, when this CompositeBytesIO is closed, the underlying IOs will also be closed 
	 * @return a BytesIO.ReadWrite
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	static <T extends BytesIO.Readable.Seekable & BytesIO.Writable.Seekable> CompositeBytesIO.ReadWrite fromReadWrite(List<? extends T> ios, boolean closeIosOnClose) throws IOException {
		return new ReadWrite(ios, closeIosOnClose, false);
	}
	
	/** Create a CompositeBytesIO Read-only and Seekable.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param closeIosOnClose if true, when this CompositeBytesIO is closed, the underlying IOs will also be closed
	 * @return a BytesIO.Readable.Seekable 
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends BytesIO.Readable.Seekable> BytesIO.Readable.Seekable fromReadableSeekable(List<? extends T> ios, boolean closeIosOnClose) throws IOException {
		return new ReadWrite(ios, closeIosOnClose, false).asReadableSeekableBytesIO();
	}
	
	/** Create a CompositeBytesIO Read-only.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param closeIosOnClose if true, when this CompositeBytesIO is closed, the underlying IOs will also be closed
	 * @param garbageIoOnConsumed if true, once an IO is completely read, the reference to the IO will be removed so the garbage collector can
	 *   free memory linked to it. If closeIosOnClose is also true, the IO is closed before to remove the reference.
	 * @return a BytesIO.Readable
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends BytesIO.Readable> BytesIO.Readable fromReadable(List<? extends T> ios, boolean closeIosOnClose, boolean garbageIoOnConsumed) throws IOException {
		return new ReadWrite(ios, closeIosOnClose, garbageIoOnConsumed).asReadableBytesIO();
	}
	
	/** Create a CompositeBytesIO Write-only Seekable.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param closeIosOnClose if true, when this CompositeBytesIO is closed, the underlying IOs will also be closed
	 * @return a BytesIO.Writable.Seekable 
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends BytesIO.Writable.Seekable> BytesIO.Writable.Seekable fromWritableSeekable(List<? extends T> ios, boolean closeIosOnClose) throws IOException {
		return new ReadWrite(ios, closeIosOnClose, false).asWritableSeekableBytesIO();
	}
	
	/** Create a CompositeBytesIO Write-only.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param closeIosOnClose if true, when this CompositeBytesIO is closed, the underlying IOs will also be closed
	 * @param garbageIoOnConsumed if true, once an IO is completely filled, the reference to the IO will be removed so the garbage collector can
	 *   free memory linked to it. If closeIosOnClose is also true, the IO is closed before to remove the reference.
	 * @return a BytesIO.Writable
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends BytesIO.Writable> BytesIO.Writable fromWritable(List<? extends T> ios, boolean closeIosOnClose, boolean garbageIoOnConsumed) throws IOException {
		return new ReadWrite(ios, closeIosOnClose, garbageIoOnConsumed).asWritableBytesIO();
	}


	/** Read-Write implementation. */
	class ReadWrite extends AbstractIO implements BytesIO.ReadWrite {
		
		@SuppressWarnings("java:S2156") // protected members
		protected static final class Element {
			protected BytesIO io;
			protected long size;
			protected long startPosition;
			protected long ioPosition;
			protected Element next;
		}
		
		protected Element head;
		protected Element cursor;
		private boolean closeIosOnClose;
		private boolean garbageIoOnConsumed;
		protected long position = 0;
		protected long size = 0;
		
		protected ReadWrite(List<? extends BytesIO> ios, boolean closeIosOnClose, boolean garbageIoOnConsumed) throws IOException {
			this.closeIosOnClose = closeIosOnClose;
			this.garbageIoOnConsumed = garbageIoOnConsumed;
			if (ios.isEmpty()) {
				head = cursor = null;
			} else {
				Iterator<? extends BytesIO> it = ios.iterator();
				head = cursor = createElement(it.next(), null);
				Element last = head;
				while (it.hasNext()) last = createElement(it.next(), last);
			}
		}
		
		private Element createElement(BytesIO io, Element previous) throws IOException {
			Element e = new Element();
			e.io = io;
			if (io instanceof IO.KnownSize ks) {
				e.size = ks.size();
				size += e.size;
			} else {
				e.size = -1;
			}
			if (previous == null) {
				e.startPosition = 0;
			} else {
				e.startPosition = previous.size != -1 && previous.startPosition != -1 ? previous.startPosition + previous.size : -1;
				previous.next = e;
			}
			e.ioPosition = 0;
			return e;
		}
		
		protected void moveNext() throws IOException {
			if (cursor.io instanceof IO.Writable w) w.flush();
			cursor = cursor.next;
			if (garbageIoOnConsumed) {
				if (closeIosOnClose) head.io.close();
				head = cursor;
			}
			if (cursor != null && cursor.io instanceof IO.Seekable s) s.seek(SeekFrom.START, 0L);
		}
		
		protected Element getElementForPosition(long pos) {
			Element e = head;
			while (pos >= e.startPosition + e.size) e = e.next;
			return e;
		}
		
		private <T> T doOperationOnPosition(FailableSupplier<T, IOException> op, T resultOnEOF) throws IOException {
			do {
				if (cursor == null) {
					if (resultOnEOF != null) return resultOnEOF;
					throw new EOFException();
				}
				try {
					T result = op.get();
					if (cursor.size != -1 && cursor.ioPosition == cursor.size) {
						moveNext();
					}
					return result;
				} catch (EOFException e) {
					moveNext();
				}
			} while (true);
		}
		
		@Override
		protected void closeInternal() throws IOException {
			while (head != null) {
				if (closeIosOnClose) head.io.close();
				head = head.next;
			}
		}
		
		@Override
		public long position() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return position;
		}
		
		@Override
		public long size() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return size;
		}
		
		@Override
		public long seek(SeekFrom from, long offset) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			long p;
			switch (Objects.requireNonNull(from, "from")) {
			case CURRENT: p = position + offset; break;
			case END: p = size - offset; break;
			case START: default: p = offset; break;
			}
			if (p < 0) throw new IllegalArgumentException("Cannot move beyond the start: " + p);
			if (p > size) throw new EOFException();
			if (p == size || head == null) {
				cursor = null;
			} else {
				cursor = getElementForPosition(p);
				cursor.ioPosition = p - cursor.startPosition;
				((IO.Seekable) cursor.io).seek(SeekFrom.START, cursor.ioPosition);
			}
			position = p;
			return p;
		}
		
		@Override
		public void flush() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			for (Element e = head; e != null; e = e.next)
				((IO.Writable) e.io).flush();
		}
		
		@Override
		public byte readByte() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return doOperationOnPosition(() -> {
				byte value = ((BytesIO.Readable) cursor.io).readByte();
				position++;
				cursor.ioPosition++;
				return value;
			}, null);
		}
		
		@Override
		public void writeByte(byte value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			doOperationOnPosition(() -> {
				((BytesIO.Writable) cursor.io).writeByte(value);
				position++;
				cursor.ioPosition++;
				return null;
			}, null);
		}
		
		@Override
		public byte readByteAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos >= size || head == null) throw new EOFException();
			Element e = getElementForPosition(pos);
			return ((BytesIO.Readable.Seekable) e.io).readByteAt(pos - e.startPosition);
		}
		
		@Override
		public void writeByteAt(long pos, byte value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos >= size || head == null) throw new EOFException();
			Element e = getElementForPosition(pos);
			((BytesIO.Writable.Seekable) e.io).writeByteAt(pos - e.startPosition, value);
		}
		
		@Override
		public int readBytes(ByteBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			if (buffer.remaining() == 0) return 0;
			return doOperationOnPosition(() -> {
				int nb = ((BytesIO.Readable) cursor.io).readBytes(buffer);
				if (nb < 0) throw new EOFException();
				position += nb;
				cursor.ioPosition += nb;
				return nb;
			}, -1);
		}
		
		@Override
		public int readBytes(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf, off, len);
			if (len == 0) return 0;
			return doOperationOnPosition(() -> {
				int nb = ((BytesIO.Readable) cursor.io).readBytes(buf, off, len);
				if (nb < 0) throw new EOFException();
				position += nb;
				cursor.ioPosition += nb;
				return nb;
			}, -1);
		}
		
		@Override
		public int writeBytes(ByteBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			if (buffer.remaining() == 0) return 0;
			return doOperationOnPosition(() -> {
				int nb = ((BytesIO.Writable) cursor.io).writeBytes(buffer);
				if (nb < 0) throw new EOFException();
				position += nb;
				cursor.ioPosition += nb;
				return nb;
			}, -1);
		}
		
		@Override
		public int writeBytes(byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, buf, off, len);
			if (len == 0) return 0;
			return doOperationOnPosition(() -> {
				int nb = ((BytesIO.Writable) cursor.io).writeBytes(buf, off, len);
				if (nb < 0) throw new EOFException();
				position += nb;
				cursor.ioPosition += nb;
				return nb;
			}, -1);
		}
		
		@Override
		public int readBytesAt(long pos, ByteBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (buffer.remaining() == 0) return 0;
			if (pos >= size || head == null) return -1;
			Element e = getElementForPosition(pos);
			return ((BytesIO.Readable.Seekable) e.io).readBytesAt(pos - e.startPosition, buffer);
		}
		
		@Override
		public int readBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
			if (len == 0) return 0;
			if (pos >= size || head == null) return -1;
			Element e = getElementForPosition(pos);
			return ((BytesIO.Readable.Seekable) e.io).readBytesAt(pos - e.startPosition, buf, off, len);
		}
		
		@Override
		public int writeBytesAt(long pos, ByteBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (buffer.remaining() == 0) return 0;
			if (pos >= size || head == null) return -1;
			Element e = getElementForPosition(pos);
			return ((BytesIO.Writable.Seekable) e.io).writeBytesAt(pos - e.startPosition, buffer);
		}
		
		@Override
		public int writeBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
			if (len == 0) return 0;
			if (pos >= size || head == null) return -1;
			Element e = getElementForPosition(pos);
			return ((BytesIO.Writable.Seekable) e.io).writeBytesAt(pos - e.startPosition, buf, off, len);
		}
		
		@Override
		public Optional<ByteBuffer> readBuffer() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return doOperationOnPosition(() -> {
				Optional<ByteBuffer> result = ((BytesIO.Readable) cursor.io).readBuffer();
				if (result.isEmpty()) throw new EOFException();
				ByteBuffer b = result.get();
				position += b.remaining();
				cursor.ioPosition += b.remaining();
				return result;
			}, Optional.empty());
		}
		
		@Override
		public long skipUpTo(long toSkip) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			if (toSkip == 0) return 0;
			NegativeValueException.check(toSkip, "toSkip");
			return doOperationOnPosition(() -> {
				long result = ((BytesIO.Readable) cursor.io).skipUpTo(toSkip);
				if (result <= 0) throw new EOFException();
				position += result;
				cursor.ioPosition += result;
				return result;
			}, -1L);
		}
	}
	
}

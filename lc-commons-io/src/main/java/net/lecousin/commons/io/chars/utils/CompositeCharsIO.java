package net.lecousin.commons.io.chars.utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.Optional;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.chars.CharsIO;
import net.lecousin.commons.io.utils.AbstractCompositeIO;

/** CharsIO aggregating of multiple IOs. */
public interface CompositeCharsIO {

	/** Create a CompositeCharsIO Read and Write.
	 * 
	 * @param <T> type of IO
	 * @param ios list of IOs, possibly empty
	 * @param closeIosOnClose if true, when this CompositeCharsIO is closed, the underlying IOs will also be closed 
	 * @return a CharsIO.ReadWrite
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	static <T extends CharsIO.Readable.Seekable & CharsIO.Writable.Seekable> CompositeCharsIO.ReadWrite fromReadWrite(List<? extends T> ios, boolean closeIosOnClose) throws IOException {
		return new ReadWrite(ios, closeIosOnClose, false);
	}
	
	/** Create a CompositeCharsIO Read-only and Seekable.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param closeIosOnClose if true, when this CompositeCharsIO is closed, the underlying IOs will also be closed
	 * @return a CharsIO.Readable.Seekable 
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends CharsIO.Readable.Seekable> CharsIO.Readable.Seekable fromReadableSeekable(List<? extends T> ios, boolean closeIosOnClose) throws IOException {
		return new ReadWrite(ios, closeIosOnClose, false).asReadableSeekableCharsIO();
	}
	
	/** Create a CompositeCharsIO Read-only.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param closeIosOnClose if true, when this CompositeCharsIO is closed, the underlying IOs will also be closed
	 * @param garbageIoOnConsumed if true, once an IO is completely read, the reference to the IO will be removed so the garbage collector can
	 *   free memory linked to it. If closeIosOnClose is also true, the IO is closed before to remove the reference.
	 * @return a CharsIO.Readable
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends CharsIO.Readable> CharsIO.Readable fromReadable(List<? extends T> ios, boolean closeIosOnClose, boolean garbageIoOnConsumed) throws IOException {
		return new ReadWrite(ios, closeIosOnClose, garbageIoOnConsumed).asReadableCharsIO();
	}
	
	/** Create a CompositeCharsIO Write-only Seekable.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param closeIosOnClose if true, when this CompositeCharsIO is closed, the underlying IOs will also be closed
	 * @return a CharsIO.Writable.Seekable 
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends CharsIO.Writable.Seekable> CharsIO.Writable.Seekable fromWritableSeekable(List<? extends T> ios, boolean closeIosOnClose) throws IOException {
		return new ReadWrite(ios, closeIosOnClose, false).asWritableSeekableCharsIO();
	}
	
	/** Create a CompositeCharsIO Write-only.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param closeIosOnClose if true, when this CompositeCharsIO is closed, the underlying IOs will also be closed
	 * @param garbageIoOnConsumed if true, once an IO is completely filled, the reference to the IO will be removed so the garbage collector can
	 *   free memory linked to it. If closeIosOnClose is also true, the IO is closed before to remove the reference.
	 * @return a CharsIO.Writable
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends CharsIO.Writable> CharsIO.Writable fromWritable(List<? extends T> ios, boolean closeIosOnClose, boolean garbageIoOnConsumed) throws IOException {
		return new ReadWrite(ios, closeIosOnClose, garbageIoOnConsumed).asWritableCharsIO();
	}


	/** Read-Write implementation. */
	class ReadWrite extends AbstractCompositeIO<CharsIO> implements CharsIO.ReadWrite {
		
		protected ReadWrite(List<? extends CharsIO> ios, boolean closeIosOnClose, boolean garbageIoOnConsumed) throws IOException {
			super(ios, closeIosOnClose, garbageIoOnConsumed);
		}
		
		@Override
		public char readChar() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return doOperationOnPosition(() -> {
				char value = ((CharsIO.Readable) cursor.io).readChar();
				position++;
				cursor.ioPosition++;
				return value;
			}, null);
		}
		
		@Override
		public void writeChar(char value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			doOperationOnPosition(() -> {
				((CharsIO.Writable) cursor.io).writeChar(value);
				position++;
				cursor.ioPosition++;
				return null;
			}, null);
		}
		
		@Override
		public char readCharAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos >= size) throw new EOFException();
			Element e = getElementForPosition(pos);
			return ((CharsIO.Readable.Seekable) e.io).readCharAt(pos - e.startPosition);
		}
		
		@Override
		public void writeCharAt(long pos, char value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos >= size) throw new EOFException();
			Element e = getElementForPosition(pos);
			((CharsIO.Writable.Seekable) e.io).writeCharAt(pos - e.startPosition, value);
		}
		
		@Override
		public int readChars(CharBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			if (buffer.remaining() == 0) return 0;
			return doOperationOnPosition(() -> {
				int nb = ((CharsIO.Readable) cursor.io).readChars(buffer);
				if (nb < 0) throw new EOFException();
				position += nb;
				cursor.ioPosition += nb;
				return nb;
			}, -1);
		}
		
		@Override
		public int readChars(char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			if (len == 0) return 0;
			return doOperationOnPosition(() -> {
				int nb = ((CharsIO.Readable) cursor.io).readChars(buf, off, len);
				if (nb < 0) throw new EOFException();
				position += nb;
				cursor.ioPosition += nb;
				return nb;
			}, -1);
		}
		
		@Override
		public int writeChars(CharBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			if (buffer.remaining() == 0) return 0;
			return doOperationOnPosition(() -> {
				int nb = ((CharsIO.Writable) cursor.io).writeChars(buffer);
				if (nb < 0) throw new EOFException();
				position += nb;
				cursor.ioPosition += nb;
				return nb;
			}, -1);
		}
		
		@Override
		public int writeChars(char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, buf, off, len);
			if (len == 0) return 0;
			return doOperationOnPosition(() -> {
				int nb = ((CharsIO.Writable) cursor.io).writeChars(buf, off, len);
				if (nb < 0) throw new EOFException();
				position += nb;
				cursor.ioPosition += nb;
				return nb;
			}, -1);
		}
		
		@Override
		public int readCharsAt(long pos, CharBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (buffer.remaining() == 0) return 0;
			if (pos >= size) return -1;
			Element e = getElementForPosition(pos);
			return ((CharsIO.Readable.Seekable) e.io).readCharsAt(pos - e.startPosition, buffer);
		}
		
		@Override
		public int readCharsAt(long pos, char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, pos, buf, off, len);
			if (len == 0) return 0;
			if (pos >= size) return -1;
			Element e = getElementForPosition(pos);
			return ((CharsIO.Readable.Seekable) e.io).readCharsAt(pos - e.startPosition, buf, off, len);
		}
		
		@Override
		public int writeCharsAt(long pos, CharBuffer buffer) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (buffer.remaining() == 0) return 0;
			if (pos >= size) return -1;
			Element e = getElementForPosition(pos);
			return ((CharsIO.Writable.Seekable) e.io).writeCharsAt(pos - e.startPosition, buffer);
		}
		
		@Override
		public int writeCharsAt(long pos, char[] buf, int off, int len) throws IOException {
			IOChecks.checkArrayOperation(this, pos, buf, off, len);
			if (len == 0) return 0;
			if (pos >= size) return -1;
			Element e = getElementForPosition(pos);
			return ((CharsIO.Writable.Seekable) e.io).writeCharsAt(pos - e.startPosition, buf, off, len);
		}
		
		@Override
		public Optional<CharBuffer> readBuffer() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return doOperationOnPosition(() -> {
				Optional<CharBuffer> result = ((CharsIO.Readable) cursor.io).readBuffer();
				if (result.isEmpty()) throw new EOFException();
				CharBuffer b = result.get();
				position += b.remaining();
				cursor.ioPosition += b.remaining();
				return result;
			}, Optional.empty());
		}
		
	}
	
}

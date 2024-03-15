package net.lecousin.commons.io.chars.memory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.Objects;

import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.chars.CharsIO;

// CHECKSTYLE DISABLE: MagicNumber

/** Buffered writable chars data IO.
 * @param <I> type of wrapped IO
 */
public class BufferedWritableCharsIO<I extends CharsIO.Writable & IO.Writable.Appendable> extends AbstractIO implements CharsIO.Writable, IO.Writable.Appendable {

	/** Minimum buffer size. */
	public static final int MINIMUM_BUFFER_SIZE = 64;
	/** Default buffer size. */
	public static final int DEFAULT_BUFFER_SIZE = 8192;
	
	private I io;
	private boolean closeIoOnClose;
	private int bufferSize;
	private LinkedList<CharArray> toWrite = new LinkedList<>();
	private CharArray currentBuffer = null;
	
	/**
	 * Constructor.
	 * @param io I/O
	 * @param bufferSize buffer size
	 * @param closeIoOnClose if true, the underlying I/O will be closed together with this I/O
	 */
	public BufferedWritableCharsIO(I io, int bufferSize, boolean closeIoOnClose) {
		this.io = Objects.requireNonNull(io, "io");
		this.bufferSize = Math.max(MINIMUM_BUFFER_SIZE, bufferSize);
		this.closeIoOnClose = closeIoOnClose;
	}
	
	/**
	 * Constructor with default buffer size.
	 * @param io I/O
	 * @param closeIoOnClose if true, the underlying I/O will be closed together with this I/O
	 */
	public BufferedWritableCharsIO(I io, boolean closeIoOnClose) {
		this(io, DEFAULT_BUFFER_SIZE, closeIoOnClose);
	}
	
	@Override
	protected void closeInternal() throws IOException {
		if (closeIoOnClose) io.close();
		io = null;
	}
	
	@Override
	public void flush() throws IOException {
		if (io == null) throw new ClosedChannelException();
		if (currentBuffer != null) {
			toWrite.add(currentBuffer.flip());
			currentBuffer = null;
		}
		while (!toWrite.isEmpty()) {
			CharArray buffer = toWrite.removeFirst();
			io.writeCharsFully(buffer.getArray(), buffer.getArrayStartOffset() + buffer.getPosition(), buffer.remaining());
		}
	}
	
	private void start() throws IOException {
		if (io == null) throw new ClosedChannelException();
		if (currentBuffer == null) currentBuffer = new CharArray(new char[bufferSize]);
		if (!toWrite.isEmpty()) flushPartial();
	}
	
	private void checkCurrentBuffer() throws IOException {
		if (currentBuffer.remaining() == 0) {
			toWrite.add(currentBuffer.flip());
			currentBuffer = null;
			flushPartial();
		} else if (!toWrite.isEmpty())
			flushPartial();
	}
	
	private void flushPartial() throws IOException {
		CharArray buffer = toWrite.removeFirst();
		int nb = io.writeChars(buffer.getArray(), buffer.getArrayStartOffset() + buffer.getPosition(), buffer.remaining());
		if (nb <= 0) throw new EOFException();
		buffer.moveForward(nb);
		if (buffer.remaining() > 0) {
			toWrite.addFirst(buffer);
		}
	}
	
	@Override
	public void writeChar(char value) throws IOException {
		start();
		currentBuffer.writeChar(value);
		checkCurrentBuffer();
	}
	
	@Override
	public int writeChars(char[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, buf, off, len);
		if (len == 0) return 0;
		if (len >= bufferSize) {
			// want to write more than buffer size
			if (currentBuffer != null) {
				// but we have a current buffer => first fill the buffer
				int r = currentBuffer.remaining();
				currentBuffer.write(buf, off, r);
				checkCurrentBuffer();
				return r;
			}
			// no current buffer
			if (!toWrite.isEmpty()) flushPartial();
			if (!toWrite.isEmpty()) {
				// add the buffer
				toWrite.add(new CharArray(buf, off, len));
				return len;
			}
			// write directly
			return io.writeChars(buf, off, len);
		}
		// less than buffer size, bufferization must happen
		if (currentBuffer == null) currentBuffer = new CharArray(new char[bufferSize]);
		int r = Math.min(currentBuffer.remaining(), len);
		currentBuffer.write(buf, off, r);
		checkCurrentBuffer();
		return r;
	}
	
	@Override
	public int writeChars(CharBuffer buffer) throws IOException {
		if (io == null) throw new ClosedChannelException();
		int len = buffer.remaining();
		if (len == 0) return 0;
		if (len >= bufferSize) {
			// want to write more than buffer size
			if (currentBuffer != null) {
				// but we have a current buffer => first fill the buffer
				int r = currentBuffer.remaining();
				buffer.get(currentBuffer.getArray(), currentBuffer.getArrayStartOffset() + currentBuffer.getPosition(), r);
				currentBuffer.moveForward(r);
				checkCurrentBuffer();
				return r;
			}
			// no current buffer
			if (!toWrite.isEmpty()) flushPartial();
			if (!toWrite.isEmpty()) {
				// add the buffer
				toWrite.add(new CharArray(buffer));
				return len;
			}
			// write directly
			return io.writeChars(buffer);
		}
		// less than buffer size, bufferization must happen
		if (currentBuffer == null) currentBuffer = new CharArray(new char[bufferSize]);
		int r = Math.min(currentBuffer.remaining(), len);
		buffer.get(currentBuffer.getArray(), currentBuffer.getArrayStartOffset() + currentBuffer.getPosition(), r);
		currentBuffer.moveForward(r);
		checkCurrentBuffer();
		return r;
	}
	
}

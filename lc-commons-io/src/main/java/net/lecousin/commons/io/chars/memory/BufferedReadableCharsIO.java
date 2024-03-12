package net.lecousin.commons.io.chars.memory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Optional;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.chars.CharsIO;

// CHECKSTYLE DISABLE: MagicNumber
/**
 * Buffered Readable IO.
 */
public class BufferedReadableCharsIO extends AbstractIO implements CharsIO.Readable {

	private CharsIO.Readable io;
	private boolean closeIoOnClose;
	private CharBuffer currentBuffer = null;
	
	/**
	 * Constructor.
	 * @param io I/O
	 * @param closeIoOnClose if true, the underlying IO will be closed when this IO is closed
	 */
	public BufferedReadableCharsIO(CharsIO.Readable io, boolean closeIoOnClose) {
		this.io = io;
		this.closeIoOnClose = closeIoOnClose;
	}
	
	@Override
	protected void closeInternal() throws IOException {
		if (closeIoOnClose) io.close();
		io = null;
	}
	
	private boolean start(boolean throwEof) throws IOException {
		if (io == null) throw new ClosedChannelException();
		if (currentBuffer == null) {
			var next = io.readBuffer();
			if (next.isEmpty()) {
				if (throwEof) throw new EOFException();
				return false;
			}
			currentBuffer = next.get();
		}
		return true;
	}
	
	@Override
	public Optional<CharBuffer> readBuffer() throws IOException {
		if (!start(false)) return Optional.empty();
		var result = Optional.of(currentBuffer);
		currentBuffer = null;
		return result;
	}
	
	@Override
	public char readChar() throws IOException {
		start(true);
		char result = currentBuffer.get();
		if (!currentBuffer.hasRemaining()) currentBuffer = null;
		return result;
	}
	
	@Override
	public int readChars(CharBuffer buffer) throws IOException {
		boolean hasData = start(false);
		if (buffer.remaining() == 0) return 0;
		if (!hasData) return -1;
		int cr = currentBuffer.remaining();
		int br = buffer.remaining();
		if (cr <= br) {
			buffer.put(currentBuffer);
			currentBuffer = null;
			return cr;
		}
		int l = currentBuffer.limit();
		currentBuffer.limit(currentBuffer.position() + br);
		buffer.put(currentBuffer);
		currentBuffer.limit(l);
		return br;
	}
	
	@Override
	public long skipUpTo(long toSkip) throws IOException {
		boolean hasData = start(false);
		if (toSkip == 0) return 0;
		NegativeValueException.check(toSkip, "toSkip");
		if (!hasData) return -1;
		int r = currentBuffer.remaining();
		if (toSkip >= r) {
			currentBuffer = null;
			return r;
		}
		currentBuffer.position(currentBuffer.position() + (int) toSkip);
		return toSkip;
	}
	
}

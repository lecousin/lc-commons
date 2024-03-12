package net.lecousin.commons.io.chars.utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.Optional;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.chars.CharsIO;

/**
 * Decode bytes into characters.
 */
// CHECKSTYLE DISABLE: MagicNumber
public class ReadableCharsIOFromBytesIO extends AbstractIO implements CharsIO.Readable {

	private BytesIO.Readable bytes;
	private CharsetDecoder decoder;
	private ByteBuffer currentInput = ByteBuffer.allocate(0);
	private CharBuffer currentBuffer = CharBuffer.allocate(0);
	private int outputRatio;
	private boolean end = false;
	private boolean closeIoOnClose;
	
	/**
	 * Constructor.
	 * @param bytes input
	 * @param charset charset to use to decode bytes
	 * @param closeIoOnClose if true the BytesIO will be closed together with this IO
	 */
	public ReadableCharsIOFromBytesIO(BytesIO.Readable bytes, Charset charset, boolean closeIoOnClose) {
		this.bytes = bytes;
		this.decoder = charset.newDecoder();
		outputRatio = (int) Math.floor(decoder.averageCharsPerByte()) + 1;
		this.closeIoOnClose = closeIoOnClose;
	}
	
	@Override
	protected void closeInternal() throws IOException {
		if (closeIoOnClose) bytes.close();
		bytes = null;
	}
	
	private boolean needData() throws IOException {
		if (end) return false;
		if (currentInput.hasRemaining()) {
			currentBuffer = CharBuffer.allocate(outputRatio * currentInput.remaining() + 1);
			if (decoder.decode(currentInput, currentBuffer, false) == CoderResult.OVERFLOW) {
				outputRatio++;
				return needData();
			}
			if (currentBuffer.position() > 0) {
				currentBuffer.flip();
				return true;
			}
		}
		var optBuffer = bytes.readBuffer();
		if (optBuffer.isEmpty()) {
			currentBuffer = CharBuffer.allocate(256);
			decoder.decode(ByteBuffer.allocate(0), currentBuffer, true);
			decoder.flush(currentBuffer);
			currentBuffer.flip();
			end = true;
			return currentBuffer.hasRemaining();
		}
		currentInput = optBuffer.get();
		return needData();
	}
	
	@Override
	public char readChar() throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		if (!currentBuffer.hasRemaining() && !needData()) throw new EOFException();
		return currentBuffer.get();
	}
	
	@Override
	public Optional<CharBuffer> readBuffer() throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		if (!currentBuffer.hasRemaining() && !needData()) return Optional.empty();
		Optional<CharBuffer> result = Optional.of(currentBuffer);
		currentBuffer = CharBuffer.allocate(0);
		return result;
	}
	
	@Override
	public int readChars(CharBuffer buffer) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		int br = buffer.remaining();
		if (br == 0) return 0;
		if (!currentBuffer.hasRemaining() && !needData()) return -1;
		int r = currentBuffer.remaining();
		if (r <= br) {
			buffer.put(currentBuffer);
			return r;
		}
		int l = currentBuffer.limit();
		currentBuffer.limit(currentBuffer.position() + br);
		buffer.put(currentBuffer);
		currentBuffer.limit(l);
		return br;
	}
	
	@Override
	public long skipUpTo(long toSkip) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(toSkip, "toSkip");
		if (toSkip == 0) return 0;
		if (!currentBuffer.hasRemaining() && !needData()) return -1;
		int r = currentBuffer.remaining();
		if (toSkip >= r) {
			currentBuffer.position(currentBuffer.position() + r);
			return r;
		}
		currentBuffer.position(currentBuffer.position() + (int) toSkip);
		return toSkip;
	}
	
}

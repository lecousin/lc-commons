package net.lecousin.commons.io.bytes.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Optional;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.BytesIO;

/** Readable BytesIO from an InputStream. */
public class BytesIOFromInputStream extends AbstractIO implements BytesIO.Readable {

	private InputStream stream;
	private boolean closeStreamOnClose;
	
	/** Constructor.
	 * 
	 * @param stream input stream
	 * @param closeStreamOnClose if true, the stream will be closed together with this BytesIO
	 */
	public BytesIOFromInputStream(InputStream stream, boolean closeStreamOnClose) {
		this.stream = stream;
		this.closeStreamOnClose = closeStreamOnClose;
	}
	
	private static final int SKIP_MAX_BUFFER_SIZE = 8192;
	private static final int READ_BUFFER_SIZE = 4096;
	
	/** @return the input stream wrapped by this IO. */
	public InputStream getUnderlyingStream() {
		return stream;
	}
	
	@Override
	protected void closeInternal() throws IOException {
		if (closeStreamOnClose) stream.close();
		stream = null;
	}
	
	@Override
	public byte readByte() throws IOException {
		if (stream == null) throw new ClosedChannelException();
		int v = stream.read();
		if (v < 0) throw new EOFException();
		return (byte) v;
	}
	
	@Override
	public int readBytes(byte[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, buf, off, len);
		if (len == 0) return 0;
		int nb = stream.read(buf, off, len);
		if (nb > 0) return nb;
		return -1;
	}
	
	@Override
	public int readBytes(ByteBuffer buffer) throws IOException {
		if (stream == null) throw new ClosedChannelException();
		int r = buffer.remaining();
		if (r == 0) return 0;
		if (buffer.hasArray()) {
			int p = buffer.position();
			int nb = stream.read(buffer.array(), buffer.arrayOffset() + p, r);
			if (nb > 0)
				buffer.position(p + nb);
			return nb;
		}
		byte[] buf = new byte[r];
		int nb = stream.read(buf);
		if (nb > 0) {
			buffer.put(buf, 0, nb);
			return nb;
		}
		return -1;
	}
	
	@Override
	public long skipUpTo(long toSkip) throws IOException {
		if (stream == null) throw new ClosedChannelException();
		NegativeValueException.check(toSkip, "toSkip");
		if (toSkip == 0) return 0;
		// because some implementations (ie. FileInputStream) may return a value greater than the size, we cannot rely on it
		int nb = (int) Math.min(SKIP_MAX_BUFFER_SIZE, toSkip);
		int real = stream.read(new byte[nb]);
		return real > 0 ? real : -1;
	}
	
	@Override
	public Optional<ByteBuffer> readBuffer() throws IOException {
		if (stream == null) throw new ClosedChannelException();
		byte[] b = new byte[READ_BUFFER_SIZE];
		int nb = stream.read(b);
		if (nb > 0) return Optional.of(ByteBuffer.wrap(b, 0, nb));
		return Optional.empty();
	}
	
}

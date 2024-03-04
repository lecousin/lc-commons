package net.lecousin.commons.io.bytes.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.BytesIO;

/** BytesIO from an OutputStream. */
public class BytesIOFromOutputStream extends AbstractIO implements BytesIO.Writable, IO.Writable.Appendable {

	private OutputStream stream;
	private boolean closeStreamOnClose;
	
	/** Constructor.
	 * 
	 * @param stream output stream
	 * @param closeStreamOnClose if true the stream will be closed together with this IO
	 */
	public BytesIOFromOutputStream(OutputStream stream, boolean closeStreamOnClose) {
		this.stream = stream;
		this.closeStreamOnClose = closeStreamOnClose;
	}
	
	/** @return the output stream wrapped by this IO. */
	public OutputStream getUnderlyingStream() {
		return stream;
	}
	
	@Override
	protected void closeInternal() throws IOException {
		if (closeStreamOnClose) stream.close();
		stream = null;
	}
	
	@Override
	public void flush() throws IOException {
		if (stream == null) throw new ClosedChannelException();
		stream.flush();
	}
	
	// CHECKSTYLE DISABLE: MagicNumber
	@Override
	public void writeByte(byte value) throws IOException {
		if (stream == null) throw new ClosedChannelException();
		stream.write(value & 0xFF);
	}
	// CHECKSTYLE ENABLE: MagicNumber
	
	@Override
	public int writeBytes(byte[] buf, int off, int len) throws IOException {
		IOChecks.checkByteArrayOperation(this, buf, off, len);
		if (len == 0) return 0;
		stream.write(buf, off, len);
		return len;
	}
	
	@Override
	public int writeBytes(ByteBuffer buffer) throws IOException {
		if (stream == null) throw new ClosedChannelException();
		int r = buffer.remaining();
		if (r == 0) return 0;
		if (buffer.hasArray()) {
			int p = buffer.position();
			stream.write(buffer.array(), buffer.arrayOffset() + p, r);
			buffer.position(p + r);
		} else {
			byte[] buf = new byte[r];
			buffer.get(buf);
			stream.write(buf);
		}
		return r;
	}
	
}

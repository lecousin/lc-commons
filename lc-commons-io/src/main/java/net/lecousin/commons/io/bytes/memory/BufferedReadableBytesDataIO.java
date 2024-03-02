package net.lecousin.commons.io.bytes.memory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.Optional;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.data.BytesData;
import net.lecousin.commons.io.bytes.data.BytesDataIO;

// CHECKSTYLE DISABLE: MagicNumber
/**
 * Buffered Readable IO.
 */
public class BufferedReadableBytesDataIO extends AbstractIO implements BytesDataIO.Readable {

	private BytesIO.Readable io;
	private boolean closeIoOnClose;
	private ByteOrder order;
	private ByteBuffer currentBuffer = null;
	
	/**
	 * Constructor.
	 * @param io I/O
	 * @param order byte order
	 * @param closeIoOnClose if true, the underlying IO will be closed when this IO is closed
	 */
	public BufferedReadableBytesDataIO(BytesIO.Readable io, ByteOrder order, boolean closeIoOnClose) {
		this.io = io;
		this.order = order;
		this.closeIoOnClose = closeIoOnClose;
	}
	
	/**
	 * Constructor with default byte order Little-Endian.
	 * @param io I/O
	 * @param closeIoOnClose if true, the underlying IO will be closed when this IO is closed
	 */
	public BufferedReadableBytesDataIO(BytesIO.Readable io, boolean closeIoOnClose) {
		this(io, ByteOrder.LITTLE_ENDIAN, closeIoOnClose);
	}
	
	@Override
	protected void closeInternal() throws IOException {
		if (closeIoOnClose) io.close();
		io = null;
	}
	
	@Override
	public ByteOrder getByteOrder() {
		return order;
	}
	
	@Override
	public void setByteOrder(ByteOrder byteOrder) {
		order = byteOrder;
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
	public Optional<ByteBuffer> readBuffer() throws IOException {
		if (!start(false)) return Optional.empty();
		var result = Optional.of(currentBuffer);
		currentBuffer = null;
		return result;
	}
	
	@Override
	public byte readByte() throws IOException {
		start(true);
		byte result = currentBuffer.get();
		if (!currentBuffer.hasRemaining()) currentBuffer = null;
		return result;
	}
	
	@Override
	public int readBytes(ByteBuffer buffer) throws IOException {
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
	
	@Override
	public int readUnsigned2Bytes() throws IOException {
		start(true);
		if (currentBuffer.remaining() >= 2) {
			short s = currentBuffer.order(order).getShort();
			if (!currentBuffer.hasRemaining()) currentBuffer = null;
			return s & 0xFFFF;
		}
		byte b1 = currentBuffer.get();
		currentBuffer = null;
		byte b2 = readByte();
		return BytesData.of(order).readUnsigned2Bytes(b1, b2);
	}
	
	@Override
	public int readUnsigned3Bytes() throws IOException {
		byte[] b = new byte[3];
		readBytesFully(b);
		return BytesData.of(order).readUnsigned3Bytes(b);
	}
	
	@Override
	public long readUnsigned4Bytes() throws IOException {
		start(true);
		if (currentBuffer.remaining() >= 4) {
			int i = currentBuffer.order(order).getInt();
			if (!currentBuffer.hasRemaining()) currentBuffer = null;
			return i & 0xFFFFFFFFL;
		}
		byte[] b = new byte[4];
		readBytesFully(b);
		return BytesData.of(order).readUnsigned4Bytes(b);
	}
	
	@Override
	public long readUnsigned5Bytes() throws IOException {
		byte[] b = new byte[5];
		readBytesFully(b);
		return BytesData.of(order).readUnsigned5Bytes(b);
	}
	
	@Override
	public long readUnsigned6Bytes() throws IOException {
		byte[] b = new byte[6];
		readBytesFully(b);
		return BytesData.of(order).readUnsigned6Bytes(b);
	}
	
	@Override
	public long readUnsigned7Bytes() throws IOException {
		byte[] b = new byte[7];
		readBytesFully(b);
		return BytesData.of(order).readUnsigned7Bytes(b);
	}
	
	@Override
	public long readSigned8Bytes() throws IOException {
		start(true);
		if (currentBuffer.remaining() >= 8) {
			long l = currentBuffer.order(order).getLong();
			if (!currentBuffer.hasRemaining()) currentBuffer = null;
			return l;
		}
		byte[] b = new byte[8];
		readBytesFully(b);
		return BytesData.of(order).readSigned8Bytes(b);
	}
	
}

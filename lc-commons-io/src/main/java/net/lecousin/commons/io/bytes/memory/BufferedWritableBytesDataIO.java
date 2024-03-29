package net.lecousin.commons.io.bytes.memory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.Objects;

import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.data.BytesData;
import net.lecousin.commons.io.bytes.data.BytesDataIO;

// CHECKSTYLE DISABLE: MagicNumber

/** Buffered writable bytes data IO.
 * @param <I> type of wrapped IO
 */
public class BufferedWritableBytesDataIO<I extends BytesIO.Writable & IO.Writable.Appendable> extends AbstractIO implements BytesDataIO.Writable, IO.Writable.Appendable {

	/** Minimum buffer size. */
	public static final int MINIMUM_BUFFER_SIZE = 64;
	/** Default buffer size. */
	public static final int DEFAULT_BUFFER_SIZE = 8192;
	
	private I io;
	private boolean closeIoOnClose;
	private ByteOrder order;
	private int bufferSize;
	private LinkedList<ByteArray> toWrite = new LinkedList<>();
	private ByteArray currentBuffer = null;
	
	/**
	 * Constructor.
	 * @param io I/O
	 * @param bufferSize buffer size
	 * @param order byte order
	 * @param closeIoOnClose if true, the underlying I/O will be closed together with this I/O
	 */
	public BufferedWritableBytesDataIO(I io, int bufferSize, ByteOrder order, boolean closeIoOnClose) {
		this.io = Objects.requireNonNull(io, "io");
		this.bufferSize = Math.max(MINIMUM_BUFFER_SIZE, bufferSize);
		this.order = Objects.requireNonNull(order, "order");
		this.closeIoOnClose = closeIoOnClose;
	}
	
	/**
	 * Constructor with default byte order Little-Endian.
	 * @param io I/O
	 * @param bufferSize buffer size
	 * @param closeIoOnClose if true, the underlying I/O will be closed together with this I/O
	 */
	public BufferedWritableBytesDataIO(I io, int bufferSize, boolean closeIoOnClose) {
		this(io, bufferSize, ByteOrder.LITTLE_ENDIAN, closeIoOnClose);
	}
	
	/**
	 * Constructor with default buffer size.
	 * @param io I/O
	 * @param order byte order
	 * @param closeIoOnClose if true, the underlying I/O will be closed together with this I/O
	 */
	public BufferedWritableBytesDataIO(I io, ByteOrder order, boolean closeIoOnClose) {
		this(io, DEFAULT_BUFFER_SIZE, order, closeIoOnClose);
	}
	
	/**
	 * Constructor with default buffer size and byte order Little-Endian.
	 * @param io I/O
	 * @param closeIoOnClose if true, the underlying I/O will be closed together with this I/O
	 */
	public BufferedWritableBytesDataIO(I io, boolean closeIoOnClose) {
		this(io, DEFAULT_BUFFER_SIZE, ByteOrder.LITTLE_ENDIAN, closeIoOnClose);
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
			ByteArray buffer = toWrite.removeFirst();
			io.writeBytesFully(buffer.getArray(), buffer.getArrayStartOffset() + buffer.getPosition(), buffer.remaining());
		}
	}
	
	@Override
	public void setByteOrder(ByteOrder byteOrder) {
		order = byteOrder;
	}
	
	@Override
	public ByteOrder getByteOrder() {
		return order;
	}
	
	private void start() throws IOException {
		if (io == null) throw new ClosedChannelException();
		if (currentBuffer == null) currentBuffer = new ByteArray(new byte[bufferSize]);
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
		ByteArray buffer = toWrite.removeFirst();
		int nb = io.writeBytes(buffer.getArray(), buffer.getArrayStartOffset() + buffer.getPosition(), buffer.remaining());
		if (nb <= 0) throw new EOFException();
		buffer.moveForward(nb);
		if (buffer.remaining() > 0) {
			toWrite.addFirst(buffer);
		}
	}
	
	@Override
	public void writeByte(byte value) throws IOException {
		start();
		currentBuffer.writeByte(value);
		checkCurrentBuffer();
	}
	
	@Override
	public int writeBytes(byte[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, buf, off, len);
		if (len == 0) return 0;
		if (len >= bufferSize) {
			// want to write more than buffer size
			if (currentBuffer != null) {
				// but we have a current buffer => first fill the buffer
				int r = currentBuffer.remaining();
				System.arraycopy(buf, off, currentBuffer.getArray(), currentBuffer.getArrayStartOffset() + currentBuffer.getPosition(), r);
				currentBuffer.moveForward(r);
				checkCurrentBuffer();
				return r;
			}
			// no current buffer
			if (!toWrite.isEmpty()) flushPartial();
			if (!toWrite.isEmpty()) {
				// add the buffer
				toWrite.add(new ByteArray(buf, off, len));
				return len;
			}
			// write directly
			return io.writeBytes(buf, off, len);
		}
		// less than buffer size, bufferization must happen
		if (currentBuffer == null) currentBuffer = new ByteArray(new byte[bufferSize]);
		int r = Math.min(currentBuffer.remaining(), len);
		System.arraycopy(buf, off, currentBuffer.getArray(), currentBuffer.getArrayStartOffset() + currentBuffer.getPosition(), r);
		currentBuffer.moveForward(r);
		checkCurrentBuffer();
		return r;
	}
	
	@Override
	public int writeBytes(ByteBuffer buffer) throws IOException {
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
				toWrite.add(new ByteArray(buffer));
				return len;
			}
			// write directly
			return io.writeBytes(buffer);
		}
		// less than buffer size, bufferization must happen
		if (currentBuffer == null) currentBuffer = new ByteArray(new byte[bufferSize]);
		int r = Math.min(currentBuffer.remaining(), len);
		buffer.get(currentBuffer.getArray(), currentBuffer.getArrayStartOffset() + currentBuffer.getPosition(), r);
		currentBuffer.moveForward(r);
		checkCurrentBuffer();
		return r;
	}
	
	private interface DataWriter<T extends Number> {
		void accept(BytesData data, byte[] buf, int off, T value);
	}
	
	private <T extends Number> void writeData(int nbBytes, T value, DataWriter<T> writer) throws IOException {
		start();
		if (currentBuffer.remaining() < nbBytes) {
			toWrite.add(currentBuffer.flip());
			currentBuffer = new ByteArray(new byte[bufferSize]);
			flushPartial();
		}
		writer.accept(BytesData.of(order), currentBuffer.getArray(), currentBuffer.getArrayStartOffset() + currentBuffer.getPosition(), value);
		currentBuffer.moveForward(nbBytes);
		checkCurrentBuffer();
	}
	
	@Override
	public void writeUnsigned2Bytes(int value) throws IOException {
		writeData(2, value, BytesData::writeUnsigned2Bytes);
	}
	
	@Override
	public void writeUnsigned3Bytes(int value) throws IOException {
		writeData(3, value, BytesData::writeUnsigned3Bytes);
	}
	
	@Override
	public void writeUnsigned4Bytes(long value) throws IOException {
		writeData(4, value, BytesData::writeUnsigned4Bytes);
	}
	
	@Override
	public void writeUnsigned5Bytes(long value) throws IOException {
		writeData(5, value, BytesData::writeUnsigned5Bytes);
	}
	
	@Override
	public void writeUnsigned6Bytes(long value) throws IOException {
		writeData(6, value, BytesData::writeUnsigned6Bytes);
	}
	
	@Override
	public void writeUnsigned7Bytes(long value) throws IOException {
		writeData(7, value, BytesData::writeUnsigned7Bytes);
	}
	
	@Override
	public void writeSigned8Bytes(long value) throws IOException {
		writeData(8, value, BytesData::writeSigned8Bytes);
	}
	
}

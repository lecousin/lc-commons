package net.lecousin.commons.io.bytes.memory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntBinaryOperator;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.BytesIO;

/**
 * BytesIO based on a ByteArray.
 */
public class ByteArrayIO extends AbstractIO implements BytesIO.ReadWrite.Resizable {

	protected ByteArray bytes;
	private Optional<IntBinaryOperator> extensionStrategy;
	
	protected ByteArrayIO(ByteArray bytes, Optional<IntBinaryOperator> extensionStrategy) {
		this.bytes = bytes;
		this.extensionStrategy = extensionStrategy;
	}

	/**
	 * Constructor.
	 * @param bytes byte array
	 */
	public ByteArrayIO(ByteArray bytes) {
		this(bytes, Optional.empty());
	}
	
	@Override
	protected void closeInternal() throws IOException {
		bytes = null;
	}
	
	@Override
	public long position() throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		return bytes.position;
	}
	
	@Override
	public long size() throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		return bytes.getSize();
	}
	
	@Override
	public void setSize(long newSize) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		LimitExceededException.check(newSize, Integer.MAX_VALUE, "newSize", "Integer.MAX_VALUE");
		bytes.setSize((int) newSize);
	}
	
	protected boolean extendCapacity(long newSize) {
		if (extensionStrategy.isEmpty()) return false;
		LimitExceededException.check(newSize, Integer.MAX_VALUE, "newSize", "Integer.MAX_VALUE");
		if (bytes.start + newSize <= bytes.bytes.length) {
			bytes.end = bytes.start + (int) newSize;
			return true;
		}
		IntBinaryOperator strategy = extensionStrategy.get();
		int current = bytes.getSize();
		int newValue = strategy.applyAsInt(current, (int) (newSize - current));
		bytes.setSize(newValue);
		bytes.end = (int) (bytes.start + newSize);
		return true;
	}
	
	@Override
	public long seek(SeekFrom from, long offset) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		long p;
		switch (Objects.requireNonNull(from, "from")) {
		case CURRENT: p = bytes.position + offset; break;
		case END: p = bytes.end - bytes.start - offset; break;
		case START: default: p = offset; break;
		}
		if (p < 0) throw new IllegalArgumentException("Cannot seek beyond the start: " + p);
		if (p > bytes.end - bytes.start && !extendCapacity(p)) throw new EOFException(); 
		bytes.position = (int) p;
		return p;
	}
	
	// --- Readable ---

	@Override
	public Optional<ByteBuffer> readBuffer() throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		int len = bytes.remaining();
		if (len == 0) return Optional.empty();
		ByteBuffer buffer = ByteBuffer.wrap(bytes.bytes, bytes.start + bytes.position, len);
		bytes.position += len;
		return Optional.of(buffer);
	}
	
	@Override
	public int readBytes(ByteBuffer buffer) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		Objects.requireNonNull(buffer, IOChecks.FIELD_BUFFER);
		int r1 = buffer.remaining();
		if (r1 == 0) return 0;
		int r2 = bytes.remaining();
		if (r2 == 0) return -1;
		int len = Math.min(r1, r2);
		buffer.put(bytes.bytes, bytes.start + bytes.position, len);
		bytes.position += len;
		return len;
	}
	
	@Override
	public int readBytes(byte[] buf, int off, int len) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		IOChecks.checkByteArray(buf, off, len);
		if (len == 0) return 0;
		int r = bytes.remaining();
		if (r == 0) return -1;
		len = Math.min(len, r);
		System.arraycopy(bytes.bytes, bytes.start + bytes.position, buf, off, len);
		bytes.position += len;
		return len;
	}
	
	@Override
	public int readBytesAt(long pos, ByteBuffer buffer) throws IOException {
		IOChecks.checkByteBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (r == 0) return 0;
		if (pos >= bytes.end - bytes.start) return -1;
		int len = Math.min(r, bytes.end - bytes.start - (int) pos);
		buffer.put(bytes.bytes, bytes.start + (int) pos, len);
		return len;
	}
	
	@Override
	public int readBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
		IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
		if (len == 0) return 0;
		if (pos >= bytes.end - bytes.start) return -1;
		len = Math.min(len, bytes.end - bytes.start - (int) pos);
		System.arraycopy(bytes.bytes, bytes.start + (int) pos, buf, off, len);
		return len;
	}
	
	@Override
	public void readBytesFully(ByteBuffer buffer) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		Objects.requireNonNull(buffer, IOChecks.FIELD_BUFFER);
		int r = buffer.remaining();
		if (r == 0) return;
		if (r > bytes.remaining()) throw new EOFException();
		buffer.put(bytes.bytes, bytes.start + bytes.position, r);
		bytes.position += r;
	}
	
	@Override
	public void readBytesFully(byte[] buf, int off, int len) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		IOChecks.checkByteArray(buf, off, len);
		if (len == 0) return;
		if (len > bytes.remaining()) throw new EOFException();
		System.arraycopy(bytes.bytes, bytes.start + bytes.position, buf, off, len);
		bytes.position += len;
	}
	
	@Override
	public void readBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
		IOChecks.checkByteBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (r == 0) return;
		if (bytes.start + pos + r > bytes.end) throw new EOFException();
		buffer.put(bytes.bytes, bytes.start + (int) pos, r);
	}
	
	@Override
	public void readBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
		IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
		if (len == 0) return;
		if (bytes.start + pos + len > bytes.end) throw new EOFException();
		System.arraycopy(bytes.bytes, bytes.start + (int) pos, buf, off, len);
	}
	
	@Override
	public byte readByte() throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		if (bytes.start + bytes.position == bytes.end) throw new EOFException();
		return bytes.bytes[bytes.start + bytes.position++];
	}
	
	@Override
	public byte readByteAt(long pos) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		if (bytes.start + pos >= bytes.end) throw new EOFException();
		return bytes.bytes[bytes.start + (int) pos];
	}
	
	@Override
	public long skipUpTo(long toSkip) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(toSkip, "toSkip");
		long nb = Math.min(toSkip, bytes.remaining());
		bytes.position += (int) nb;
		return nb;
	}
	
	@Override
	public void skipFully(long toSkip) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(toSkip, "toSkip");
		if (bytes.start + bytes.position + toSkip > bytes.end) throw new EOFException();
		bytes.position += (int) toSkip;
	}
	
	
	// --- Writable ---
	
	@Override
	public int writeBytes(ByteBuffer buffer) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		Objects.requireNonNull(buffer, IOChecks.FIELD_BUFFER);
		int r = buffer.remaining();
		if (r == 0) return 0;
		int len = bytes.remaining();
		if (len == 0) {
			if (!extendCapacity((long) bytes.position + r)) return -1;
			len = r;
		}
		len = Math.min(r, len);
		buffer.get(bytes.bytes, bytes.start + bytes.position, len);
		bytes.position += len;
		return len;
	}
	
	@Override
	public int writeBytes(byte[] buf, int off, int len) throws IOException {
		IOChecks.checkByteArrayOperation(this, buf, off, len);
		if (len == 0) return 0;
		int r = bytes.remaining();
		if (r == 0) {
			if (!extendCapacity((long) bytes.position + len)) return -1;
			r = len;
		}
		len = Math.min(r, len);
		System.arraycopy(buf, off, bytes.bytes, bytes.start + bytes.position, len);
		bytes.position += len;
		return len;
	}
	
	@Override
	public int writeBytesAt(long pos, ByteBuffer buffer) throws IOException {
		IOChecks.checkByteBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (r == 0) return 0;
		if (bytes.start + pos >= bytes.end && !extendCapacity(pos + r)) return -1;
		r = Math.min(r, bytes.end - bytes.start - (int) pos);
		buffer.get(bytes.bytes, bytes.start + (int) pos, r);
		return r;
	}
	
	@Override
	public int writeBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
		IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
		if (len == 0) return 0;
		if (bytes.start + pos >= bytes.end && !extendCapacity(pos + len)) return -1;
		len = Math.min(len, bytes.end - bytes.start - (int) pos);
		System.arraycopy(buf, off, bytes.bytes, bytes.start + (int) pos, len);
		return len;
	}
	
	@Override
	public void writeBytesFully(ByteBuffer buffer) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		Objects.requireNonNull(buffer, IOChecks.FIELD_BUFFER);
		int r = buffer.remaining();
		if (r == 0) return;
		if (r > bytes.remaining() && !extendCapacity((long) bytes.position + r)) throw new EOFException();
		buffer.get(bytes.bytes, bytes.start + bytes.position, r);
		bytes.position += r;
	}
	
	@Override
	public void writeBytesFully(byte[] buf, int off, int len) throws IOException {
		IOChecks.checkByteArrayOperation(this, buf, off, len);
		if (len == 0) return;
		if (len > bytes.remaining() && !extendCapacity((long) bytes.position + len)) throw new EOFException();
		System.arraycopy(buf, off, bytes.bytes, bytes.start + bytes.position, len);
		bytes.position += len;
	}
	
	@Override
	public void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
		IOChecks.checkByteBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (bytes.start + pos + r > bytes.end && !extendCapacity(pos + r)) throw new EOFException();
		buffer.get(bytes.bytes, bytes.start + (int) pos, r);
	}
	
	@Override
	public void writeBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
		IOChecks.checkByteArrayOperation(this, pos, buf, off, len);
		if (bytes.start + pos + len > bytes.end && !extendCapacity(pos + len)) throw new EOFException();
		System.arraycopy(buf, off, bytes.bytes, bytes.start + (int) pos, len);
	}
	
	@Override
	public void writeByte(byte value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		if (bytes.position == bytes.end - bytes.start && !extendCapacity(bytes.getSize() + 1L)) throw new EOFException();
		bytes.bytes[bytes.start + bytes.position++] = value;
	}
	
	@Override
	public void writeByteAt(long pos, byte value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		if (pos >= bytes.end - bytes.start && !extendCapacity(pos + 1)) throw new EOFException();
		bytes.bytes[bytes.start + (int) pos] = value;
	}
	
	@Override
	public void flush() throws IOException {
		if (bytes == null) throw new ClosedChannelException();
	}

	/** Appendable ByteArrayIO. */
	public static class Appendable extends ByteArrayIO implements BytesIO.ReadWrite.AppendableResizable {
		
		/**
		 * Constructor.
		 * @param bytes byte array
		 * @param extensionStrategy take the current size and the additional requested size in parameter
		 *   and returns the new size to be allocated.
		 */
		public Appendable(ByteArray bytes, IntBinaryOperator extensionStrategy) {
			super(bytes, Optional.of(extensionStrategy));
		}
		
		/**
		 * Constructor.
		 * @param bytes byte array
		 * @param minimumAppendSize when appending bytes beyond the byte array size, the byte array is extended
		 *   of at least <code>appendMinimum</code> bytes, or the requested additional bytes if greater than
		 *   <code>appendMinimum</code>.
		 */
		public Appendable(ByteArray bytes, int minimumAppendSize) {
			this(bytes, extensionStrategyWithMinimumAppendSize(minimumAppendSize));
		}
		
		/**
		 * Constructor with default extension strategy.
		 * @param bytes byte array
		 */
		public Appendable(ByteArray bytes) {
			this(bytes, DEFAULT_EXTENSION_STRATEGY);
		}
		
		/**
		 * Extension strategy for Appendable, that double the size of the array by default, or if not enough double the additional
		 * requested size or 1024 if then additional size is less than 1024.
		 */
		// CHECKSTYLE DISABLE: MagicNumber
		public static final IntBinaryOperator DEFAULT_EXTENSION_STRATEGY =
			(currentSize, additionalSizeRequested) -> {
				if (((long) currentSize) + additionalSizeRequested > Integer.MAX_VALUE)
					throw new IllegalStateException("Cannot extend");
				int minimum = currentSize + additionalSizeRequested;
				if (minimum < currentSize * 2) return currentSize * 2;
				return minimum + Math.max(additionalSizeRequested, 1024);
			};
		// CHECKSTYLE ENABLE: MagicNumber
		
		/** Creates an extension strategy that extend of at least the given minimum each time an extension is required.
		 * @param minimum minimum number of bytes to add to the byte array
		 * @return the extension strategy
		 */
		public static IntBinaryOperator extensionStrategyWithMinimumAppendSize(int minimum) {
			return (currentSize, additionalSizeRequested) -> currentSize + Math.min(additionalSizeRequested, minimum);
		}
		
	}
}

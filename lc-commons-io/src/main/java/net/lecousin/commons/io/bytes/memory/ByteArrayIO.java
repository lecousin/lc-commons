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
		return bytes.getPosition();
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
		if (bytes.getArrayStartOffset() + newSize <= bytes.getArray().length) {
			bytes.setSize((int) newSize);
			return true;
		}
		IntBinaryOperator strategy = extensionStrategy.get();
		int current = bytes.getSize();
		int newValue = strategy.applyAsInt(current, (int) (newSize - current));
		bytes.setSize(newValue);
		bytes.setSize((int) newSize);
		return true;
	}
	
	@Override
	public long seek(SeekFrom from, long offset) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		long p;
		switch (Objects.requireNonNull(from, "from")) {
		case CURRENT: p = bytes.getPosition() + offset; break;
		case END: p = bytes.getSize() - offset; break;
		case START: default: p = offset; break;
		}
		if (p < 0) throw new IllegalArgumentException("Cannot seek beyond the start: " + p);
		if (p > bytes.getSize() && !extendCapacity(p)) throw new EOFException(); 
		bytes.setPosition((int) p);
		return p;
	}
	
	// --- Readable ---

	@Override
	public Optional<ByteBuffer> readBuffer() throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		int len = bytes.remaining();
		if (len == 0) return Optional.empty();
		ByteBuffer buffer = bytes.toByteBuffer();
		bytes.moveForward(len);
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
		buffer.put(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), len);
		bytes.moveForward(len);
		return len;
	}
	
	@Override
	public int readBytes(byte[] buf, int off, int len) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		IOChecks.checkArray(buf, off, len);
		if (len == 0) return 0;
		int r = bytes.remaining();
		if (r == 0) return -1;
		len = Math.min(len, r);
		System.arraycopy(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), buf, off, len);
		bytes.moveForward(len);
		return len;
	}
	
	@Override
	public int readBytesAt(long pos, ByteBuffer buffer) throws IOException {
		IOChecks.checkBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (r == 0) return 0;
		if (pos >= bytes.getSize()) return -1;
		int len = Math.min(r, bytes.getSize() - (int) pos);
		buffer.put(bytes.getArray(), bytes.getArrayStartOffset() + (int) pos, len);
		return len;
	}
	
	@Override
	public int readBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, pos, buf, off, len);
		if (len == 0) return 0;
		if (pos >= bytes.getSize()) return -1;
		len = Math.min(len, bytes.getSize() - (int) pos);
		System.arraycopy(bytes.getArray(), bytes.getArrayStartOffset() + (int) pos, buf, off, len);
		return len;
	}
	
	@Override
	public void readBytesFully(ByteBuffer buffer) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		Objects.requireNonNull(buffer, IOChecks.FIELD_BUFFER);
		int r = buffer.remaining();
		if (r == 0) return;
		if (r > bytes.remaining()) throw new EOFException();
		buffer.put(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), r);
		bytes.moveForward(r);
	}
	
	@Override
	public void readBytesFully(byte[] buf, int off, int len) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		IOChecks.checkArray(buf, off, len);
		if (len == 0) return;
		if (len > bytes.remaining()) throw new EOFException();
		System.arraycopy(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), buf, off, len);
		bytes.moveForward(len);
	}
	
	@Override
	public void readBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
		IOChecks.checkBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (r == 0) return;
		if (pos + r > bytes.getSize()) throw new EOFException();
		buffer.put(bytes.getArray(), bytes.getArrayStartOffset() + (int) pos, r);
	}
	
	@Override
	public void readBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, pos, buf, off, len);
		if (len == 0) return;
		if (pos + len > bytes.getSize()) throw new EOFException();
		System.arraycopy(bytes.getArray(), bytes.getArrayStartOffset() + (int) pos, buf, off, len);
	}
	
	@Override
	public byte readByte() throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		if (bytes.remaining() == 0) throw new EOFException();
		return bytes.readByte();
	}
	
	@Override
	public byte readByteAt(long pos) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		if (pos >= bytes.getSize()) throw new EOFException();
		return bytes.getArray()[bytes.getArrayStartOffset() + (int) pos];
	}
	
	@Override
	public long skipUpTo(long toSkip) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(toSkip, "toSkip");
		if (toSkip == 0) return 0;
		int r = bytes.remaining();
		if (r == 0) return -1;
		long nb = Math.min(toSkip, r);
		bytes.moveForward((int) nb);
		return nb;
	}
	
	@Override
	public void skipFully(long toSkip) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(toSkip, "toSkip");
		if (bytes.getPosition() + toSkip > bytes.getSize()) throw new EOFException();
		bytes.moveForward((int) toSkip);
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
			if (!extendCapacity((long) bytes.getPosition() + r)) return -1;
			len = r;
		}
		len = Math.min(r, len);
		buffer.get(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), len);
		bytes.moveForward(len);
		return len;
	}
	
	@Override
	public int writeBytes(byte[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, buf, off, len);
		if (len == 0) return 0;
		int r = bytes.remaining();
		if (r == 0) {
			if (!extendCapacity((long) bytes.getPosition() + len)) return -1;
			r = len;
		}
		len = Math.min(r, len);
		System.arraycopy(buf, off, bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), len);
		bytes.moveForward(len);
		return len;
	}
	
	@Override
	public int writeBytesAt(long pos, ByteBuffer buffer) throws IOException {
		IOChecks.checkBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (r == 0) return 0;
		if (pos >= bytes.getSize() && !extendCapacity(pos + r)) return -1;
		r = Math.min(r, bytes.getSize() - (int) pos);
		buffer.get(bytes.getArray(), bytes.getArrayStartOffset() + (int) pos, r);
		return r;
	}
	
	@Override
	public int writeBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, pos, buf, off, len);
		if (len == 0) return 0;
		if (pos >= bytes.getSize() && !extendCapacity(pos + len)) return -1;
		len = Math.min(len, bytes.getSize() - (int) pos);
		System.arraycopy(buf, off, bytes.getArray(), bytes.getArrayStartOffset() + (int) pos, len);
		return len;
	}
	
	@Override
	public void writeBytesFully(ByteBuffer buffer) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		Objects.requireNonNull(buffer, IOChecks.FIELD_BUFFER);
		int r = buffer.remaining();
		if (r == 0) return;
		if (r > bytes.remaining() && !extendCapacity((long) bytes.getPosition() + r)) throw new EOFException();
		buffer.get(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), r);
		bytes.moveForward(r);
	}
	
	@Override
	public void writeBytesFully(byte[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, buf, off, len);
		if (len == 0) return;
		if (len > bytes.remaining() && !extendCapacity((long) bytes.getPosition() + len)) throw new EOFException();
		bytes.write(buf, off, len);
	}
	
	@Override
	public void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
		IOChecks.checkBufferOperation(this, pos, buffer);
		int r = buffer.remaining();
		if (pos + r > bytes.getSize() && !extendCapacity(pos + r)) throw new EOFException();
		buffer.get(bytes.getArray(), bytes.getArrayStartOffset() + (int) pos, r);
	}
	
	@Override
	public void writeBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
		IOChecks.checkArrayOperation(this, pos, buf, off, len);
		if (pos + len > bytes.getSize() && !extendCapacity(pos + len)) throw new EOFException();
		System.arraycopy(buf, off, bytes.getArray(), bytes.getArrayStartOffset() + (int) pos, len);
	}
	
	@Override
	public void writeByte(byte value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		if (bytes.getPosition() == bytes.getSize() && !extendCapacity(bytes.getSize() + 1L)) throw new EOFException();
		bytes.writeByte(value);
	}
	
	@Override
	public void writeByteAt(long pos, byte value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		if (pos >= bytes.getSize() && !extendCapacity(pos + 1)) throw new EOFException();
		bytes.getArray()[bytes.getArrayStartOffset() + (int) pos] = value;
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
			return (currentSize, additionalSizeRequested) -> currentSize + Math.max(additionalSizeRequested, minimum);
		}
		
	}
}

package net.lecousin.commons.io.bytes.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.IntBinaryOperator;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.memory.DataArray;

/** Wrapper for a byte[] as a DataArray. */
public class ByteArray implements DataArray<byte[]> {

	protected byte[] bytes;
	protected int start;
	protected int position;
	protected int end;
	
	/**
	 * Constructor.
	 * @param buf byte array to use
	 * @param off start position in the array, that will be considered as position 0
	 * @param len number of bytes to used, that will be considered as the size of this IO
	 */
	public ByteArray(byte[] buf, int off, int len) {
		IOChecks.checkByteArray(buf, off, len);
		this.bytes = buf;
		this.start = off;
		this.position = 0;
		this.end = off + len;
	}

	/**
	 * Constructor.
	 * @param buf byte array to use
	 */
	public ByteArray(byte[] buf) {
		this(Objects.requireNonNull(buf), 0, buf.length);
	}
	
	/**
	 * Constructor.
	 * @param buffer byte buffer
	 */
	public ByteArray(ByteBuffer buffer) {
		if (buffer.hasArray()) {
			this.bytes = buffer.array();
			this.start = buffer.arrayOffset();
			this.position = buffer.position();
			this.end = this.start + buffer.limit();
		} else {
			this.bytes = new byte[buffer.remaining()];
			buffer.get(this.bytes);
			this.start = 0;
			this.position = 0;
			this.end = this.bytes.length;
		}
	}
	
	@Override
	public byte[] getArray() {
		return bytes;
	}
	
	/** @return the start offset in the array (corresponding to position 0). */
	@Override
	public int getArrayStartOffset() {
		return start;
	}
	
	@Override
	public int getPosition() {
		return position;
	}
	
	@Override
	public void setPosition(int newPosition) {
		LimitExceededException.checkWithNonNegative(newPosition, (long) end - start, "newPosition", "end - start");
		this.position = newPosition;
	}
	
	@Override
	public int getSize() {
		return end - start;
	}
	
	@Override
	public void setSize(int newSize) {
		NegativeValueException.check(newSize, "newSize");
		if (newSize < end - start || start + newSize <= bytes.length) {
			end = start + newSize;
		} else {
			byte[] b = new byte[newSize];
			System.arraycopy(bytes, start, b, 0, end - start);
			bytes = b;
			start = 0;
			end = newSize;
		}
	}
	
	@Override
	public void trim() {
		if (start > 0 || end < bytes.length) {
			byte[] b = new byte[end - start];
			System.arraycopy(bytes, start, b, 0, end - start);
			bytes = b;
			start = 0;
			end = b.length;
		}
	}
	
	/**
	 * Flips this buffer. The end is set to the current position and then
     * the position is set to zero.
	 * @return this
	 */
	public ByteArray flip() {
		end = start + position;
        position = 0;
        return this;
	}
	
	/** Read a byte from the current position, and increment the position.
	 * @return the byte
	 */
	public byte readByte() {
		return bytes[position++];
	}
	
	/**
	 * Write a byte at the current position, and increment the position.
	 * @param b the byte
	 */
	public void writeByte(byte b) {
		bytes[position++] = b;
	}
	
	/**
	 * Write bytes at the current position
	 * @param src bytes to write
	 * @param off offset in src
	 * @param len number of bytes
	 */
	public void write(byte[] src, int off, int len) {
		System.arraycopy(src, off, bytes, position, len);
		position += len;
	}
	
	/** @return a ByteBuffer from this ByteArray. */
	public ByteBuffer toByteBuffer() {
		return ByteBuffer.wrap(bytes, start + position, end - start - position);
	}

	/** @return a ByteArrayIO based on this byte array. */
	public ByteArrayIO asBytesIO() {
		return new ByteArrayIO(this);
	}
	
	/** @return a ByteArrayIO.Appendable based on this byte array. */
	public ByteArrayIO.Appendable asAppendableBytesIO() {
		return new ByteArrayIO.Appendable(this);
	}
	
	/** @return a ByteArrayIO Appendable based on this byte array.
	 * @param minimumAppendSize when appending bytes beyond the byte array size, the byte array is extended
	 *   of at least <code>appendMinimum</code> bytes, or the requested additional bytes if greater than
	 *   <code>appendMinimum</code>.
	 */
	public ByteArrayIO.Appendable asAppendableBytesIO(int minimumAppendSize) {
		return new ByteArrayIO.Appendable(this, minimumAppendSize);
	}

	/** @return a ByteArrayIO Appendable based on this byte array, with the given byte order.
	 * @param extensionStrategy take the current size and the additional requested size in parameter
	 *   and returns the new size to be allocated.
	 */
	public ByteArrayIO.Appendable asAppendableBytesIO(IntBinaryOperator extensionStrategy) {
		return new ByteArrayIO.Appendable(this, extensionStrategy);
	}

	
	/** @return a ByteArrayDataIO based on this byte array, default to Little-Endian. */
	public ByteArrayDataIO asBytesDataIO() {
		return new ByteArrayDataIO(this);
	}
	
	/** @return a ByteArrayDataIO based on this byte array, with the given byte order.
	 * @param order byte order
	 */
	public ByteArrayDataIO asBytesDataIO(ByteOrder order) {
		ByteArrayDataIO io = new ByteArrayDataIO(this);
		io.setByteOrder(order);
		return io;
	}
	
	/** @return a ByteArrayDataIO Appendable based on this byte array, default to Little-Endian. */
	public ByteArrayDataIO.Appendable asAppendableBytesDataIO() {
		return new ByteArrayDataIO.Appendable(this);
	}
	
	/** @return a ByteArrayDataIO Appendable based on this byte array, with the given byte order.
	 * @param order byte order
	 */
	public ByteArrayDataIO.Appendable asAppendableBytesDataIO(ByteOrder order) {
		ByteArrayDataIO.Appendable io = new ByteArrayDataIO.Appendable(this);
		io.setByteOrder(order);
		return io;
	}

	/** @return a ByteArrayDataIO Appendable based on this byte array, default to Little-Endian.
	 * @param minimumAppendSize when appending bytes beyond the byte array size, the byte array is extended
	 *   of at least <code>appendMinimum</code> bytes, or the requested additional bytes if greater than
	 *   <code>appendMinimum</code>.
	 */
	public ByteArrayDataIO.Appendable asAppendableBytesDataIO(int minimumAppendSize) {
		return new ByteArrayDataIO.Appendable(this, minimumAppendSize);
	}

	/** @return a ByteArrayDataIO Appendable based on this byte array, default to Little-Endian.
	 * @param extensionStrategy take the current size and the additional requested size in parameter
	 *   and returns the new size to be allocated.
	 */
	public ByteArrayDataIO.Appendable asAppendableBytesDataIO(IntBinaryOperator extensionStrategy) {
		return new ByteArrayDataIO.Appendable(this, extensionStrategy);
	}
	
	/** @return a ByteArrayDataIO Appendable based on this byte array, with the given byte order.
	 * @param order byte order
	 * @param minimumAppendSize when appending bytes beyond the byte array size, the byte array is extended
	 *   of at least <code>appendMinimum</code> bytes, or the requested additional bytes if greater than
	 *   <code>appendMinimum</code>.
	 */
	public ByteArrayDataIO.Appendable asAppendableBytesDataIO(ByteOrder order, int minimumAppendSize) {
		ByteArrayDataIO.Appendable io = new ByteArrayDataIO.Appendable(this, minimumAppendSize);
		io.setByteOrder(order);
		return io;
	}
	
	/** @return a ByteArrayDataIO Appendable based on this byte array, with the given byte order.
	 * @param order byte order
	 * @param extensionStrategy take the current size and the additional requested size in parameter
	 *   and returns the new size to be allocated.
	 */
	public ByteArrayDataIO.Appendable asAppendableBytesDataIO(ByteOrder order, IntBinaryOperator extensionStrategy) {
		ByteArrayDataIO.Appendable io = new ByteArrayDataIO.Appendable(this, extensionStrategy);
		io.setByteOrder(order);
		return io;
	}
	
	
	/** @return a ByteArrayDataBuffer based on this byte array, default to Little-Endian. */
	public ByteArrayDataBuffer asBytesDataBuffer() {
		return new ByteArrayDataBuffer(this);
	}
	
	/** @return a ByteArrayDataBuffer based on this byte array, with the given byte order.
	 * @param order byte order
	  */
	public ByteArrayDataBuffer asBytesDataBuffer(ByteOrder order) {
		ByteArrayDataBuffer b = new ByteArrayDataBuffer(this);
		b.setByteOrder(order);
		return b;
	}
	
}

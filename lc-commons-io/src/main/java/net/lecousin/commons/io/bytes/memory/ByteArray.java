package net.lecousin.commons.io.bytes.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.IntBinaryOperator;

import net.lecousin.commons.io.memory.DataArray;

/** Wrapper for a byte[] as a DataArray. */
public class ByteArray extends DataArray<byte[]> {

	/**
	 * Constructor.
	 * @param buf byte array to use
	 * @param off start position in the array, that will be considered as position 0
	 * @param len number of bytes to used, that will be considered as the size of this IO
	 */
	public ByteArray(byte[] buf, int off, int len) {
		super(buf, off, len);
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
			this.array = buffer.array();
			this.start = buffer.arrayOffset();
			this.position = buffer.position();
			this.end = this.start + buffer.limit();
		} else {
			this.array = new byte[buffer.remaining()];
			buffer.get(this.array);
			this.start = 0;
			this.position = 0;
			this.end = this.array.length;
		}
	}
	
	@Override
	protected byte[] createArray(int size) {
		return new byte[size];
	}
	
	@Override
	public ByteArray flip() {
		return (ByteArray) super.flip();
	}
	
	/** Read a byte from the current position, and increment the position.
	 * @return the byte
	 */
	public byte readByte() {
		return array[start + position++];
	}
	
	/**
	 * Write a byte at the current position, and increment the position.
	 * @param b the byte
	 */
	public void writeByte(byte b) {
		array[start + position++] = b;
	}
	
	/** @return a ByteBuffer from this ByteArray. */
	public ByteBuffer toByteBuffer() {
		return ByteBuffer.wrap(array, start + position, end - start - position);
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

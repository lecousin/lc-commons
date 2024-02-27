package net.lecousin.commons.io.stream;

import java.io.ByteArrayOutputStream;

/**
 * A ByteArrayOutputStream on which the internal array can be accessed.
 * This can be useful to avoid the copy of the array on the default {@link ByteArrayOutputStream#toByteArray()} method.
 */
public class ByteArrayOutputStreamAccessible extends ByteArrayOutputStream {

	/** Create a new stream with an initial capacity of 32 bytes. */
	public ByteArrayOutputStreamAccessible() {
		// default
	}

	/** Create a new stream with the specified size as initial capacity.
	 * 
	 * @param size initial capacity
	 */
	public ByteArrayOutputStreamAccessible(int size) {
		super(size);
	}
	
	/** Create a new stream with the specified array.
	 * 
	 * @param array the array
	 * @param count the number of bytes already written on it.
	 */
	public ByteArrayOutputStreamAccessible(byte[] array, int count) {
		super(0);
		this.buf = array;
		this.count = count;
	}
	
	/** @return the internal array. */
	public byte[] getArray() {
		return buf;
	}
	
	/** @return the number of bytes already written to the internal array. */
	public int getArrayCount() {
		return count;
	}
	
}

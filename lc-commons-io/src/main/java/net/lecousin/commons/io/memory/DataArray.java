package net.lecousin.commons.io.memory;

import java.lang.reflect.Array;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOChecks;

/** DataBuffer using an array.
 * @param <T> type of array
 */
public abstract class DataArray<T> implements DataBuffer {

	protected T array;
	protected int start;
	protected int position;
	protected int end;
	
	/**
	 * Constructor.
	 * @param buf array to use
	 * @param off start position in the array, that will be considered as position 0
	 * @param len number of elements to use in the array, that will be considered as the size
	 */
	protected DataArray(T buf, int off, int len) {
		IOChecks.checkArray(buf, off, len);
		this.array = buf;
		this.start = off;
		this.position = 0;
		this.end = off + len;
	}

	/** Empty constructor, implementation must initialize all fields. */
	protected DataArray() {
		// nothing
	}

	/** @return the array. */
	public T getArray() {
		return array;
	}
	
	/** @return the start offset in the array (corresponding to position 0). */
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
	
	/**
	 * Move the position.
	 * @param nb number of elements to skip
	 */
	public void moveForward(int nb) {
		this.position += nb;
	}
	
	@Override
	public int getSize() {
		return end - start;
	}
	
	protected abstract T createArray(int size);
	
	@Override
	public void setSize(int newSize) {
		NegativeValueException.check(newSize, "newSize");
		if (newSize < end - start || start + newSize <= Array.getLength(array)) {
			end = start + newSize;
		} else {
			T b = createArray(newSize);
			System.arraycopy(array, start, b, 0, end - start);
			array = b;
			start = 0;
			end = newSize;
		}
	}
	
	/** Trim the array so the start offset becomes 0 and it contains exactly size number of data. */
	public void trim() {
		if (start > 0 || end < Array.getLength(array)) {
			T b = createArray(end - start);
			System.arraycopy(array, start, b, 0, end - start);
			array = b;
			end -= start;
			start = 0;
		}
	}
	
	/**
	 * Flips this buffer. The end is set to the current position and then
     * the position is set to zero.
	 * @return this
	 */
	public DataArray<T> flip() {
		end = start + position;
        position = 0;
        return this;
	}

	/**
	 * Write elements at the current position
	 * @param src elements to write
	 * @param off offset in src
	 * @param len number of elements
	 */
	public void write(T src, int off, int len) {
		System.arraycopy(src, off, array, start + position, len);
		position += len;
	}

}

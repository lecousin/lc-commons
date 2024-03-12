package net.lecousin.commons.io.chars.memory;

import java.nio.CharBuffer;
import java.util.Objects;
import java.util.function.IntBinaryOperator;

import net.lecousin.commons.exceptions.LimitExceededException;
import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.memory.DataArray;

/** Wrapper for a char[] as a DataArray. */
public class CharArray implements DataArray<char[]>, CharSequence {

	protected char[] chars;
	protected int start;
	protected int position;
	protected int end;
	
	/**
	 * Constructor.
	 * @param buf char array to use
	 * @param off start position in the array, that will be considered as position 0
	 * @param len number of chars to used, that will be considered as the size of this IO
	 */
	public CharArray(char[] buf, int off, int len) {
		IOChecks.checkCharArray(buf, off, len);
		this.chars = buf;
		this.start = off;
		this.position = 0;
		this.end = off + len;
	}

	/**
	 * Constructor.
	 * @param buf char array to use
	 */
	public CharArray(char[] buf) {
		this(Objects.requireNonNull(buf), 0, buf.length);
	}
	
	/**
	 * Constructor.
	 * @param buffer char buffer
	 */
	public CharArray(CharBuffer buffer) {
		if (buffer.hasArray()) {
			this.chars = buffer.array();
			this.start = buffer.arrayOffset();
			this.position = buffer.position();
			this.end = this.start + buffer.limit();
		} else {
			this.chars = new char[buffer.remaining()];
			buffer.get(this.chars);
			this.start = 0;
			this.position = 0;
			this.end = this.chars.length;
		}
	}
	
	@Override
	public char[] getArray() {
		return chars;
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
		if (newSize < end - start || start + newSize <= chars.length) {
			end = start + newSize;
		} else {
			char[] b = new char[newSize];
			System.arraycopy(chars, start, b, 0, end - start);
			chars = b;
			start = 0;
			end = newSize;
		}
	}
	
	@Override
	public void trim() {
		if (start > 0 || end < chars.length) {
			char[] b = new char[end - start];
			System.arraycopy(chars, start, b, 0, end - start);
			chars = b;
			start = 0;
			end = b.length;
		}
	}
	
	/**
	 * Flips this buffer. The end is set to the current position and then
     * the position is set to zero.
	 * @return this
	 */
	public CharArray flip() {
		end = start + position;
        position = 0;
        return this;
	}
	
	/** Read a char from the current position, and increment the position.
	 * @return the char
	 */
	public char readChar() {
		return chars[start + position++];
	}
	
	/**
	 * Write a char at the current position, and increment the position.
	 * @param b the char
	 */
	public void writeChar(char b) {
		chars[start + position++] = b;
	}
	
	/**
	 * Write chars at the current position
	 * @param src chars to write
	 * @param off offset in src
	 * @param len number of chars
	 */
	public void write(char[] src, int off, int len) {
		System.arraycopy(src, off, chars, start + position, len);
		position += len;
	}
	
	/** @return a CharBuffer from this CharArray. */
	public CharBuffer toCharBuffer() {
		return CharBuffer.wrap(chars, start + position, end - start - position);
	}

	/** @return a CharArrayIO based on this char array. */
	public CharArrayIO asCharsIO() {
		return new CharArrayIO(this);
	}
	
	/** @return a CharArrayIO.Appendable based on this char array. */
	public CharArrayIO.Appendable asAppendableCharsIO() {
		return new CharArrayIO.Appendable(this);
	}
	
	/** @return a CharArrayIO Appendable based on this char array.
	 * @param minimumAppendSize when appending chars beyond the char array size, the char array is extended
	 *   of at least <code>appendMinimum</code> chars, or the requested additional chars if greater than
	 *   <code>appendMinimum</code>.
	 */
	public CharArrayIO.Appendable asAppendableCharsIO(int minimumAppendSize) {
		return new CharArrayIO.Appendable(this, minimumAppendSize);
	}

	/** @return a CharArrayIO Appendable based on this char array, with the given char order.
	 * @param extensionStrategy take the current size and the additional requested size in parameter
	 *   and returns the new size to be allocated.
	 */
	public CharArrayIO.Appendable asAppendableCharsIO(IntBinaryOperator extensionStrategy) {
		return new CharArrayIO.Appendable(this, extensionStrategy);
	}

	@Override
	public int length() {
		return getSize();
	}

	@Override
	public char charAt(int index) {
		return chars[start + index];
	}

	@Override
	public CharSequence subSequence(int startOffset, int endOffset) {
		return new CharArray(chars, this.start + startOffset, endOffset - startOffset);
	}
	
	@Override
	public String toString() {
		return new String(chars, start, end - start);
	}

}

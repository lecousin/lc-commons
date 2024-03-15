package net.lecousin.commons.io.chars.memory;

import java.nio.CharBuffer;
import java.util.Objects;
import java.util.function.IntBinaryOperator;

import net.lecousin.commons.io.memory.DataArray;

/** Wrapper for a char[] as a DataArray. */
public class CharArray extends DataArray<char[]> implements CharSequence {

	/**
	 * Constructor.
	 * @param buf char array to use
	 * @param off start position in the array, that will be considered as position 0
	 * @param len number of chars to used, that will be considered as the size of this IO
	 */
	public CharArray(char[] buf, int off, int len) {
		super(buf, off, len);
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
			this.array = buffer.array();
			this.start = buffer.arrayOffset();
			this.position = buffer.position();
			this.end = this.start + buffer.limit();
		} else {
			this.array = new char[buffer.remaining()];
			buffer.get(this.array);
			this.start = 0;
			this.position = 0;
			this.end = this.array.length;
		}
	}
	
	@Override
	protected char[] createArray(int size) {
		return new char[size];
	}

	@Override
	public CharArray flip() {
		return (CharArray) super.flip();
	}
	
	/** Read a char from the current position, and increment the position.
	 * @return the char
	 */
	public char readChar() {
		return array[start + position++];
	}
	
	/**
	 * Write a char at the current position, and increment the position.
	 * @param b the char
	 */
	public void writeChar(char b) {
		array[start + position++] = b;
	}
	
	/** @return a CharBuffer from this CharArray. */
	public CharBuffer toCharBuffer() {
		return CharBuffer.wrap(array, start + position, end - start - position);
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
		return array[start + index];
	}

	@Override
	public CharSequence subSequence(int startOffset, int endOffset) {
		return new CharArray(array, this.start + startOffset, endOffset - startOffset);
	}
	
	@Override
	public String toString() {
		return new String(array, start, end - start);
	}

}

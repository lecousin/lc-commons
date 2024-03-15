package net.lecousin.commons.io.chars;

import java.nio.CharBuffer;

/** Utilities for CharBuffer. */
public final class LcCharBufferUtils {

	private LcCharBufferUtils() {
		// no instance
	}
	
	/** Calculate the total remaining characters from the given buffers.
	 * @param buffers buffers
	 * @return total number of remaining characters
	 */
	public static int remaining(CharBuffer... buffers) {
		int r = 0;
		for (CharBuffer b : buffers) r += b.remaining();
		return r;
	}
	
	/** Concatenate the remaining characters from all the given buffers.
	 * @param buffers buffers to concatenate
	 * @return a char array containing all remaining characters from all the buffers in order
	 */
	public static char[] concat(CharBuffer... buffers) {
		char[] result = new char[remaining(buffers)];
		int pos = 0;
		for (var b : buffers) {
			int r = b.remaining();
			b.get(result, pos, r);
			pos += r;
		}
		return result;
	}
	
}

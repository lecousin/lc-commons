package net.lecousin.commons.io;

import java.nio.ByteBuffer;

/** Utilities for ByteBuffer. */
public final class LcByteBufferUtils {

	private LcByteBufferUtils() {
		// no instance
	}
	
	/** Calculate the total remaining bytes from the given buffers.
	 * @param buffers buffers
	 * @return total number of remaining bytes
	 */
	public static int remaining(ByteBuffer... buffers) {
		int r = 0;
		for (ByteBuffer b : buffers) r += b.remaining();
		return r;
	}
	
	/** Concatenate the remaining bytes from all the given buffers.
	 * @param buffers buffers to concatenate
	 * @return a byte array containing all remaining bytes from all the buffers in order
	 */
	public static byte[] concat(ByteBuffer... buffers) {
		byte[] result = new byte[remaining(buffers)];
		int pos = 0;
		for (var b : buffers) {
			int r = b.remaining();
			b.get(result, pos, r);
			pos += r;
		}
		return result;
	}
	
}

package net.lecousin.commons.collections;

/** Additional utilities for arrays. */
public final class LcArrayUtils {

	private LcArrayUtils() {
		// no instance
	}
	
	/** Count the number of bytes from the given byte arrays.
	 * @param arrays byte arrays
	 * @return total number of bytes
	 */
	public static int count(byte[]... arrays) {
		int count = 0;
		for (var a : arrays) count += a.length;
		return count;
	}
	
	/** Concatenate the given arrays.
	 * @param arrays arrays to concatenate
	 * @return new array containing all bytes from all arrays in order
	 */
	public static byte[] concat(byte[]... arrays) {
		byte[] result = new byte[count(arrays)];
		int pos = 0;
		for (var a : arrays) {
			System.arraycopy(a, 0, result, pos, a.length);
			pos += a.length;
		}
		return result;
	}
	
}

package net.lecousin.commons.io.memory;

/** DataBuffer using an array.
 * @param <T> type of array
 */
public interface DataArray<T> extends DataBuffer {

	/** @return the array. */
	T getArray();
	
	/** @return the start offset of the array (corresponding to position 0). */
	int getArrayStartOffset();
	
	/** Trim the array so the start offset becomes 0 and it contains exactly size number of data. */
	void trim();
	
}

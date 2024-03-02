package net.lecousin.commons.io.memory;

/** Buffer of data. */
public interface DataBuffer {

	/** @return the position in this buffer. */
	int getPosition();
	
	/** Change the position in this buffer.
	 * @param newPosition new position
	 */
	void setPosition(int newPosition);
	
	/** @return the size of this buffer. */
	int getSize();
	
	/** Change the size of this buffer.
	 * @param newSize new size
	 */
	void setSize(int newSize);
	
	/** @return the remaining data in this buffer (size - position). */
	default int remaining() {
		return getSize() - getPosition();
	}
	
}

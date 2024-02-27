package net.lecousin.commons.io.bytes.data;

import java.nio.ByteOrder;

/** BytesData from a buffer.<br/>
 * The difference from a BytesDataIO are:<ul>
 * <li>It does not check is the IO is closed, as it is not an IO</li>
 * <li>It does not check position or end of data, but let the buffer throw its exception such as IndexOutOfBoundException</li>
 * <li>It is restricted to in memory buffer, so no IOException to throw</li> 
 * </ul>
 * The advantage is it is a bit faster (less checks to perform) and it does not throw any checked exception.
 */
// CHECKSTYLE DISABLE: MagicNumber
public interface BytesDataBuffer {

	/** @return current byte order used by this buffer. */
	ByteOrder getByteOrder();
	
	/** Change byte order.
	 * @param order new byte order
	 */
	void setByteOrder(ByteOrder order);
	
	/** @return the position in the buffer. */
	int getPosition();
	
	/** @return the size of the buffer. */
	int getSize();
	
	/** Set the position in the buffer.
	 * @param newPosition new position
	 */
	void setPosition(int newPosition);
	
	/** @return remaining bytes in the buffer (size - position). */
	default int remaining() {
		return getSize() - getPosition();
	}
	
	/** Readable BytesDataBuffer. */
	interface Readable extends BytesDataBuffer {
		
		/** @return byte. */
		byte readSignedByte();
		
		/** @return unsigned byte. */
		default int readUnsignedByte() {
			return readSignedByte() & 0xFF;
		}
		
		/** @return signed 2-bytes. */
		short readSigned2Bytes();
		
		/** @return signed 2-bytes. */
		default short readShort() {
			return readSigned2Bytes();
		}
		
		/** @return unsigned 2-bytes. */
		int readUnsigned2Bytes();
		
		/** @return signed 3-bytes. */
		int readSigned3Bytes();
		
		/** @return unsigned 3-bytes. */
		int readUnsigned3Bytes();
		
		/** @return signed 4-bytes. */
		int readSigned4Bytes();
		
		/** @return signed 4-bytes. */
		default int readInteger() {
			return readSigned4Bytes();
		}
		
		/** @return unsigned 4-bytes. */
		long readUnsigned4Bytes();

		/** @return signed 5-bytes. */
		long readSigned5Bytes();
		
		/** @return unsigned 5-bytes. */
		long readUnsigned5Bytes();
		
		/** @return signed 6-bytes. */
		long readSigned6Bytes();
		
		/** @return unsigned 6-bytes. */
		long readUnsigned6Bytes();
		
		/** @return signed 7-bytes. */
		long readSigned7Bytes();
		
		/** @return unsigned 7-bytes. */
		long readUnsigned7Bytes();
		
		/** @return signed 8-bytes. */
		long readSigned8Bytes();
		
		/** @return signed 8-bytes. */
		default long readLong() {
			return readSigned8Bytes();
		}
		
	}
	
	/** Writable BytesDataBuffer. */
	interface Writable extends BytesDataBuffer {
		
		/** Write a signed byte.
		 * @param value byte to write
		 */
		void writeSignedByte(byte value);
		
		/** Write an unsigned byte.
		 * @param value unsigned byte
		 */
		default void writeUnsignedByte(int value) {
			writeSignedByte((byte) (value & 0xFF));
		}
		
		/** Write a signed 2-bytes value.
		 * @param value value to write
		 */
		void writeSigned2Bytes(short value);

		/** Write a signed 2-bytes value.
		 * @param value value to write
		 */
		default void writeShort(short value) {
			writeSigned2Bytes(value);
		}
		
		/** Write an unsigned 2-bytes value.
		 * @param value value to write
		 */
		void writeUnsigned2Bytes(int value);
		
		/** Write a signed 3-bytes value.
		 * @param value value to write
		 */
		void writeSigned3Bytes(int value);
		
		/** Write an unsigned 3-bytes value.
		 * @param value value to write
		 */
		void writeUnsigned3Bytes(int value);
		
		/** Write a signed 4-bytes value.
		 * @param value value to write
		 */
		void writeSigned4Bytes(int value);

		/** Write a signed 4-bytes value.
		 * @param value value to write
		 */
		default void writeInteger(int value) {
			writeSigned4Bytes(value);
		}
		
		/** Write an unsigned 4-bytes value.
		 * @param value value to write
		 */
		void writeUnsigned4Bytes(long value);
		
		/** Write a signed 5-bytes value.
		 * @param value value to write
		 */
		void writeSigned5Bytes(long value);
		
		/** Write an unsigned 5-bytes value.
		 * @param value value to write
		 */
		void writeUnsigned5Bytes(long value);
		
		/** Write a signed 6-bytes value.
		 * @param value value to write
		 */
		void writeSigned6Bytes(long value);
		
		/** Write an unsigned 6-bytes value.
		 * @param value value to write
		 */
		void writeUnsigned6Bytes(long value);
		
		/** Write a signed 7-bytes value.
		 * @param value value to write
		 */
		void writeSigned7Bytes(long value);
		
		/** Write an unsigned 7-bytes value.
		 * @param value value to write
		 */
		void writeUnsigned7Bytes(long value);
		
		/** Write a signed 8-bytes value.
		 * @param value value to write
		 */
		void writeSigned8Bytes(long value);
		
		/** Write a signed 8-bytes value.
		 * @param value value to write
		 */
		default void writeLong(long value) {
			writeSigned8Bytes(value);
		}
		
	}

	
	/** Readable and Writable BytesDataBuffer. */
	interface ReadWrite extends BytesDataBuffer.Readable, BytesDataBuffer.Writable {
		
	}
	
}

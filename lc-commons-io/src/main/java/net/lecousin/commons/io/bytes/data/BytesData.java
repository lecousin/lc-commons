package net.lecousin.commons.io.bytes.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Reading and writing data (numbers) using bytes.
 */
// CHECKSTYLE DISABLE: LeftCurly
// CHECKSTYLE DISABLE: RightCurly
// CHECKSTYLE DISABLE: MagicNumber
public interface BytesData {

	/** LittleEndian implementation. */
	LittleEndian LE = new LittleEndian();
	/** BigEndian implementation. */
	BigEndian BE = new BigEndian();
	
	/**
	 * @param order byte order
	 * @return implementation
	 */
	static BytesData of(ByteOrder order) {
		if (ByteOrder.LITTLE_ENDIAN.equals(order))
			return LE;
		return BE;
	}
	
	/** @return the byte order of this implementation. */
	ByteOrder getByteOrder();
	
	// 2 Bytes unsigned
	
	/**
	 * Read 2 bytes as an unsigned integer.
	 * @param data bytes
	 * @return integer
	 */
	int readUnsigned2Bytes(byte[] data);

	/**
	 * Read 2 bytes as an unsigned integer.
	 * @param data bytes
	 * @param off offset in bytes where the 2 bytes are located
	 * @return integer
	 */
	int readUnsigned2Bytes(byte[] data, int off);
	
	/**
	 * Read 2 bytes as an unsigned integer.
	 * @param buffer bytes
	 * @return integer
	 */
	int readUnsigned2Bytes(ByteBuffer buffer);
	
	/**
	 * Generate 2 bytes containing the value as an unsigned integer.
	 * @param value value
	 * @return 2 bytes
	 */
	default byte[] getUnsigned2Bytes(int value) {
		byte[] b = new byte[2];
		writeUnsigned2Bytes(b, value);
		return b;
	}
	
	/**
	 * Read an unsigned 2-bytes integer from 2 bytes.
	 * @param b1 byte1
	 * @param b2 byte2
	 * @return unsigned integer
	 */
	int readUnsigned2Bytes(byte b1, byte b2);
	
	/**
	 * Write a value as an unsigned integer on 2 bytes.
	 * @param data buffer where to write the 2 bytes
	 * @param value value to write
	 */
	void writeUnsigned2Bytes(byte[] data, int value);
	
	/**
	 * Write a value as an unsigned integer on 2 bytes.
	 * @param data buffer where to write the 2 bytes
	 * @param off offset in the buffer where the 2 bytes will be written
	 * @param value value to write
	 */
	void writeUnsigned2Bytes(byte[] data, int off, int value);
	
	/**
	 * Write a value as an unsigned integer on 2 bytes.
	 * @param buffer buffer where to write the 2 bytes
	 * @param value value to write
	 */
	void writeUnsigned2Bytes(ByteBuffer buffer, int value);
	
	// 2 bytes signed
	
	/**
	 * Read 2 bytes as a signed integer.
	 * @param data bytes
	 * @return signed integer
	 */
	default short readShort(byte[] data) { return (short) readUnsigned2Bytes(data); }
	
	/**
	 * Read 2 bytes as a signed integer.
	 * @param data bytes
	 * @param off offset in the data where to read the bytes
	 * @return signed integer
	 */
	default short readShort(byte[] data, int off) { return (short) readUnsigned2Bytes(data, off); }
	
	/**
	 * Read 2 bytes as a signed integer.
	 * @param buffer bytes
	 * @return signed integer
	 */
	default short readShort(ByteBuffer buffer) { return (short) readUnsigned2Bytes(buffer); }
	
	/**
	 * Read 2 bytes as a signed integer.
	 * @param data bytes
	 * @return signed integer
	 */
	default short readSigned2Bytes(byte[] data) { return readShort(data); }
	
	/**
	 * Read 2 bytes as a signed integer.
	 * @param data bytes
	 * @param off offset in the data where to read the bytes
	 * @return signed integer
	 */
	default short readSigned2Bytes(byte[] data, int off) { return readShort(data, off); }
	
	/**
	 * Read 2 bytes as a signed integer.
	 * @param buffer bytes
	 * @return signed integer
	 */
	default short readSigned2Bytes(ByteBuffer buffer) { return readShort(buffer); }
	
	/**
	 * Generate 2 bytes containing the value as a signed integer.
	 * @param value value
	 * @return 2 bytes
	 */
	default byte[] getShort(short value) {
		byte[] b = new byte[2];
		writeShort(b, value);
		return b;
	}
	
	/**
	 * Generate 2 bytes containing the value as a signed integer.
	 * @param value value
	 * @return 2 bytes
	 */
	default byte[] getSigned2Bytes(short value) { return getShort(value); }
	
	/**
	 * Write a value as a signed integer on 2 bytes.
	 * @param data buffer where to write the 2 bytes
	 * @param value value to write
	 */
	default void writeShort(byte[] data, short value) { writeUnsigned2Bytes(data, value & 0xFFFF); }
	
	/**
	 * Write a value as a signed integer on 2 bytes.
	 * @param data buffer where to write the 2 bytes
	 * @param off offset in the buffer where the 2 bytes will be written
	 * @param value value to write
	 */
	default void writeShort(byte[] data, int off, short value) { writeUnsigned2Bytes(data, off, value & 0xFFFF); }
	
	/**
	 * Write a value as a signed integer on 2 bytes.
	 * @param buffer where to write the 2 bytes
	 * @param value value to write
	 */
	default void writeShort(ByteBuffer buffer, short value) { writeUnsigned2Bytes(buffer, value & 0xFFFF); }
	
	/**
	 * Write a value as a signed integer on 2 bytes.
	 * @param data buffer where to write the 2 bytes
	 * @param value value to write
	 */
	default void writeSigned2Bytes(byte[] data, short value) { writeShort(data, value); }
	
	/**
	 * Write a value as a signed integer on 2 bytes.
	 * @param data buffer where to write the 2 bytes
	 * @param off offset in the buffer where the 2 bytes will be written
	 * @param value value to write
	 */
	default void writeSigned2Bytes(byte[] data, int off, short value) { writeShort(data, off, value); }
	
	/**
	 * Write a value as a signed integer on 2 bytes.
	 * @param buffer where to write the 2 bytes
	 * @param value value to write
	 */
	default void writeSigned2Bytes(ByteBuffer buffer, short value) { writeShort(buffer, value); }

	
	// 3 Bytes unsigned
	
	/**
	 * Read 3 bytes as an unsigned integer.
	 * @param data bytes
	 * @return integer
	 */
	int readUnsigned3Bytes(byte[] data);
	
	/**
	 * Read 3 bytes as an unsigned integer.
	 * @param data bytes
	 * @param off offset in bytes where the 2 bytes are located
	 * @return integer
	 */
	int readUnsigned3Bytes(byte[] data, int off);
	
	/**
	 * Read 3 bytes as an unsigned integer.
	 * @param buffer bytes
	 * @return integer
	 */
	int readUnsigned3Bytes(ByteBuffer buffer);
	
	/**
	 * Generate 3 bytes containing the value as an unsigned integer.
	 * @param value value
	 * @return 3 bytes
	 */
	default byte[] getUnsigned3Bytes(int value) {
		byte[] b = new byte[3];
		writeUnsigned3Bytes(b, value);
		return b;
	}
	
	/**
	 * Write a value as an unsigned integer on 3 bytes.
	 * @param data buffer where to write the 3 bytes
	 * @param value value to write
	 */
	void writeUnsigned3Bytes(byte[] data, int value);
	
	/**
	 * Write a value as an unsigned integer on 3 bytes.
	 * @param data buffer where to write the 3 bytes
	 * @param off offset in the buffer where the 3 bytes will be written
	 * @param value value to write
	 */
	void writeUnsigned3Bytes(byte[] data, int off, int value);
	
	/**
	 * Write a value as an unsigned integer on 3 bytes.
	 * @param buffer buffer where to write the 3 bytes
	 * @param value value to write
	 */
	void writeUnsigned3Bytes(ByteBuffer buffer, int value);
	
	// 3 bytes signed
	
	/**
	 * Converts an unsigned value into a signed value.
	 * @param unsigned unsigned value
	 * @param mask mask
	 * @return signed value
	 */
	static int unsignedToSignedInt(int unsigned, int mask) {
		if ((unsigned & (mask + 1)) == 0)
			return unsigned;
		return -((~(unsigned - 1)) & ((mask << 1) | 1));
	}
	
	/**
	 * Converts a signed value into an unsigned value.
	 * @param signed signed value
	 * @param mask mask
	 * @return unsigned value
	 */
	static int signedToUnsignedInt(int signed, int mask) {
		if (signed >= 0) return signed;
		return (~(-signed) & mask) + 1;
	}
	
	/**
	 * Read 3 bytes as a signed integer.
	 * @param data bytes
	 * @return signed integer
	 */
	default int readSigned3Bytes(byte[] data) { return unsignedToSignedInt(readUnsigned3Bytes(data), 0x7FFFFF); }
	
	/**
	 * Read 3 bytes as a signed integer.
	 * @param data bytes
	 * @param off offset in the data where to read the bytes
	 * @return signed integer
	 */
	default int readSigned3Bytes(byte[] data, int off) { return unsignedToSignedInt(readUnsigned3Bytes(data, off), 0x7FFFFF); }
	
	/**
	 * Read 3 bytes as a signed integer.
	 * @param buffer bytes
	 * @return signed integer
	 */
	default int readSigned3Bytes(ByteBuffer buffer) { return unsignedToSignedInt(readUnsigned3Bytes(buffer), 0x7FFFFF); }
	
	/**
	 * Generate 3 bytes containing the value as a signed integer.
	 * @param value value
	 * @return 3 bytes
	 */
	default byte[] getSigned3Bytes(int value) {
		byte[] b = new byte[3];
		writeSigned3Bytes(b, value);
		return b;
	}
	
	/**
	 * Write a value as a signed integer on 3 bytes.
	 * @param data buffer where to write the 3 bytes
	 * @param value value to write
	 */
	default void writeSigned3Bytes(byte[] data, int value) { writeUnsigned3Bytes(data, signedToUnsignedInt(value, 0xFFFFFF)); }
	
	/**
	 * Write a value as a signed integer on 3 bytes.
	 * @param data buffer where to write the 3 bytes
	 * @param off offset in the buffer where the 3 bytes will be written
	 * @param value value to write
	 */
	default void writeSigned3Bytes(byte[] data, int off, int value) { writeUnsigned3Bytes(data, off, signedToUnsignedInt(value, 0xFFFFFF)); }
	
	/**
	 * Write a value as a signed integer on 3 bytes.
	 * @param buffer where to write the 3 bytes
	 * @param value value to write
	 */
	default void writeSigned3Bytes(ByteBuffer buffer, int value) { writeUnsigned3Bytes(buffer, signedToUnsignedInt(value, 0xFFFFFF)); }

	
	// 4 Bytes unsigned
	
	/**
	 * Read 4 bytes as an unsigned integer.
	 * @param data bytes
	 * @return integer
	 */
	long readUnsigned4Bytes(byte[] data);
	
	/**
	 * Read 4 bytes as an unsigned integer.
	 * @param data bytes
	 * @param off offset in bytes where the 2 bytes are located
	 * @return integer
	 */
	long readUnsigned4Bytes(byte[] data, int off);
	
	/**
	 * Read 4 bytes as an unsigned integer.
	 * @param buffer bytes
	 * @return integer
	 */
	long readUnsigned4Bytes(ByteBuffer buffer);
	
	/**
	 * Generate 4 bytes containing the value as an unsigned integer.
	 * @param value value
	 * @return 4 bytes
	 */
	default byte[] getUnsigned4Bytes(long value) {
		byte[] b = new byte[4];
		writeUnsigned4Bytes(b, value);
		return b;
	}
	
	/**
	 * Write a value as an unsigned integer on 4 bytes.
	 * @param data buffer where to write the 4 bytes
	 * @param value value to write
	 */
	void writeUnsigned4Bytes(byte[] data, long value);
	
	/**
	 * Write a value as an unsigned integer on 4 bytes.
	 * @param data buffer where to write the 4 bytes
	 * @param off offset in the buffer where the 4 bytes will be written
	 * @param value value to write
	 */
	void writeUnsigned4Bytes(byte[] data, int off, long value);
	
	/**
	 * Write a value as an unsigned integer on 4 bytes.
	 * @param buffer buffer where to write the 4 bytes
	 * @param value value to write
	 */
	void writeUnsigned4Bytes(ByteBuffer buffer, long value);
	
	// 4 bytes signed
	
	/**
	 * Read 4 bytes as a signed integer.
	 * @param data bytes
	 * @return signed integer
	 */
	default int readSigned4Bytes(byte[] data) { return (int) unsignedToSignedLong(readUnsigned4Bytes(data), 0x7FFFFFFF); }

	/**
	 * Read 4 bytes as a signed integer.
	 * @param data bytes
	 * @return signed integer
	 */
	default int readInteger(byte[] data) { return readSigned4Bytes(data); }
	
	/**
	 * Read 4 bytes as a signed integer.
	 * @param data bytes
	 * @param off offset in the data where to read the bytes
	 * @return signed integer
	 */
	default int readSigned4Bytes(byte[] data, int off) { return (int) unsignedToSignedLong(readUnsigned4Bytes(data, off), 0x7FFFFFFF); }
	
	/**
	 * Read 4 bytes as a signed integer.
	 * @param data bytes
	 * @param off offset in the data where to read the bytes
	 * @return signed integer
	 */
	default int readInteger(byte[] data, int off) { return readSigned4Bytes(data, off); }
	
	/**
	 * Read 4 bytes as a signed integer.
	 * @param buffer bytes
	 * @return signed integer
	 */
	default int readSigned4Bytes(ByteBuffer buffer) { return (int) unsignedToSignedLong(readUnsigned4Bytes(buffer), 0x7FFFFFFF); }
	
	/**
	 * Read 4 bytes as a signed integer.
	 * @param buffer bytes
	 * @return signed integer
	 */
	default int readInteger(ByteBuffer buffer) { return readSigned4Bytes(buffer); }
	
	/**
	 * Generate 4 bytes containing the value as a signed integer.
	 * @param value value
	 * @return 4 bytes
	 */
	default byte[] getSigned4Bytes(int value) {
		byte[] b = new byte[4];
		writeSigned4Bytes(b, value);
		return b;
	}
	
	/**
	 * Generate 4 bytes containing the value as a signed integer.
	 * @param value value
	 * @return 4 bytes
	 */
	default byte[] getInteger(int value) { return getSigned4Bytes(value); }
	
	/**
	 * Write a value as a signed integer on 4 bytes.
	 * @param data buffer where to write the 4 bytes
	 * @param value value to write
	 */
	default void writeSigned4Bytes(byte[] data, int value) { writeUnsigned4Bytes(data, signedToUnsignedLong(value, 0xFFFFFFFFL)); }
	
	/**
	 * Write a value as a signed integer on 4 bytes.
	 * @param data buffer where to write the 4 bytes
	 * @param value value to write
	 */
	default void writeInteger(byte[] data, int value) { writeSigned4Bytes(data, value); }
	
	/**
	 * Write a value as a signed integer on 4 bytes.
	 * @param data buffer where to write the 4 bytes
	 * @param off offset in the buffer where the 4 bytes will be written
	 * @param value value to write
	 */
	default void writeSigned4Bytes(byte[] data, int off, int value) { writeUnsigned4Bytes(data, off, signedToUnsignedLong(value, 0xFFFFFFFFL)); }
	
	/**
	 * Write a value as a signed integer on 4 bytes.
	 * @param data buffer where to write the 4 bytes
	 * @param off offset in the buffer where the 4 bytes will be written
	 * @param value value to write
	 */
	default void writeInteger(byte[] data, int off, int value) { writeSigned4Bytes(data, off, value); }
	
	/**
	 * Write a value as a signed integer on 4 bytes.
	 * @param buffer where to write the 4 bytes
	 * @param value value to write
	 */
	default void writeSigned4Bytes(ByteBuffer buffer, int value) { writeUnsigned4Bytes(buffer, signedToUnsignedLong(value, 0xFFFFFFFFL)); }
	
	/**
	 * Write a value as a signed integer on 4 bytes.
	 * @param buffer where to write the 4 bytes
	 * @param value value to write
	 */
	default void writeInteger(ByteBuffer buffer, int value) { writeSigned4Bytes(buffer, value); }
	
	
	// 5 Bytes unsigned
	
	/**
	 * Read 5 bytes as an unsigned integer.
	 * @param data bytes
	 * @return integer
	 */
	long readUnsigned5Bytes(byte[] data);
	
	/**
	 * Read 5 bytes as an unsigned integer.
	 * @param data bytes
	 * @param off offset in bytes where the 2 bytes are located
	 * @return integer
	 */
	long readUnsigned5Bytes(byte[] data, int off);
	
	/**
	 * Read 5 bytes as an unsigned integer.
	 * @param buffer bytes
	 * @return integer
	 */
	long readUnsigned5Bytes(ByteBuffer buffer);
	
	/**
	 * Generate 5 bytes containing the value as an unsigned integer.
	 * @param value value
	 * @return 5 bytes
	 */
	default byte[] getUnsigned5Bytes(long value) {
		byte[] b = new byte[5];
		writeUnsigned5Bytes(b, value);
		return b;
	}
	
	/**
	 * Write a value as an unsigned integer on 5 bytes.
	 * @param data buffer where to write the 5 bytes
	 * @param value value to write
	 */
	void writeUnsigned5Bytes(byte[] data, long value);
	
	/**
	 * Write a value as an unsigned integer on 5 bytes.
	 * @param data buffer where to write the 5 bytes
	 * @param off offset in the buffer where the 5 bytes will be written
	 * @param value value to write
	 */
	void writeUnsigned5Bytes(byte[] data, int off, long value);
	
	/**
	 * Write a value as an unsigned integer on 5 bytes.
	 * @param buffer buffer where to write the 5 bytes
	 * @param value value to write
	 */
	void writeUnsigned5Bytes(ByteBuffer buffer, long value);
	
	// 5 bytes signed
	
	/**
	 * Converts an unsigned value into a signed value.
	 * @param unsigned unsigned value
	 * @param mask mask
	 * @return signed value
	 */
	static long unsignedToSignedLong(long unsigned, long mask) {
		if ((unsigned & (mask + 1)) == 0)
			return unsigned;
		return -((~(unsigned - 1)) & ((mask << 1) | 1));
	}
	
	/**
	 * Converts a signed value into an unsigned value.
	 * @param signed signed value
	 * @param mask mask
	 * @return unsigned value
	 */
	static long signedToUnsignedLong(long signed, long mask) {
		if (signed >= 0) return signed;
		return (~(-signed) & mask) + 1;
	}

	
	/**
	 * Read 5 bytes as a signed integer.
	 * @param data bytes
	 * @return signed integer
	 */
	default long readSigned5Bytes(byte[] data) { return unsignedToSignedLong(readUnsigned5Bytes(data), 0x7FFFFFFFFFL); }
	
	/**
	 * Read 5 bytes as a signed integer.
	 * @param data bytes
	 * @param off offset in the data where to read the bytes
	 * @return signed integer
	 */
	default long readSigned5Bytes(byte[] data, int off) { return unsignedToSignedLong(readUnsigned5Bytes(data, off), 0x7FFFFFFFFFL); }
	
	/**
	 * Read 5 bytes as a signed integer.
	 * @param buffer bytes
	 * @return signed integer
	 */
	default long readSigned5Bytes(ByteBuffer buffer) { return unsignedToSignedLong(readUnsigned5Bytes(buffer), 0x7FFFFFFFFFL); }
	
	/**
	 * Generate 5 bytes containing the value as a signed integer.
	 * @param value value
	 * @return 5 bytes
	 */
	default byte[] getSigned5Bytes(long value) {
		byte[] b = new byte[5];
		writeSigned5Bytes(b, value);
		return b;
	}
	
	/**
	 * Write a value as a signed integer on 5 bytes.
	 * @param data buffer where to write the 5 bytes
	 * @param value value to write
	 */
	default void writeSigned5Bytes(byte[] data, long value) { writeUnsigned5Bytes(data, signedToUnsignedLong(value, 0xFFFFFFFFFFL)); }
	
	/**
	 * Write a value as a signed integer on 5 bytes.
	 * @param data buffer where to write the 5 bytes
	 * @param off offset in the buffer where the 5 bytes will be written
	 * @param value value to write
	 */
	default void writeSigned5Bytes(byte[] data, int off, long value) { writeUnsigned5Bytes(data, off, signedToUnsignedLong(value, 0xFFFFFFFFFFL)); }
	
	/**
	 * Write a value as a signed integer on 5 bytes.
	 * @param buffer where to write the 5 bytes
	 * @param value value to write
	 */
	default void writeSigned5Bytes(ByteBuffer buffer, long value) { writeUnsigned5Bytes(buffer, signedToUnsignedLong(value, 0xFFFFFFFFFFL)); }
	
	
	// 6 Bytes unsigned
	
	/**
	 * Read 6 bytes as an unsigned integer.
	 * @param data bytes
	 * @return integer
	 */
	long readUnsigned6Bytes(byte[] data);
	
	/**
	 * Read 6 bytes as an unsigned integer.
	 * @param data bytes
	 * @param off offset in bytes where the 2 bytes are located
	 * @return integer
	 */
	long readUnsigned6Bytes(byte[] data, int off);
	
	/**
	 * Read 6 bytes as an unsigned integer.
	 * @param buffer bytes
	 * @return integer
	 */
	long readUnsigned6Bytes(ByteBuffer buffer);
	
	/**
	 * Generate 6 bytes containing the value as an unsigned integer.
	 * @param value value
	 * @return 6 bytes
	 */
	default byte[] getUnsigned6Bytes(long value) {
		byte[] b = new byte[6];
		writeUnsigned6Bytes(b, value);
		return b;
	}
	
	/**
	 * Write a value as an unsigned integer on 6 bytes.
	 * @param data buffer where to write the 6 bytes
	 * @param value value to write
	 */
	void writeUnsigned6Bytes(byte[] data, long value);
	
	/**
	 * Write a value as an unsigned integer on 6 bytes.
	 * @param data buffer where to write the 6 bytes
	 * @param off offset in the buffer where the 6 bytes will be written
	 * @param value value to write
	 */
	void writeUnsigned6Bytes(byte[] data, int off, long value);
	
	/**
	 * Write a value as an unsigned integer on 6 bytes.
	 * @param buffer buffer where to write the 6 bytes
	 * @param value value to write
	 */
	void writeUnsigned6Bytes(ByteBuffer buffer, long value);
	
	// 6 bytes signed
	
	/**
	 * Read 6 bytes as a signed integer.
	 * @param data bytes
	 * @return signed integer
	 */
	default long readSigned6Bytes(byte[] data) { return unsignedToSignedLong(readUnsigned6Bytes(data), 0x7FFFFFFFFFFFL); }
	
	/**
	 * Read 6 bytes as a signed integer.
	 * @param data bytes
	 * @param off offset in the data where to read the bytes
	 * @return signed integer
	 */
	default long readSigned6Bytes(byte[] data, int off) { return unsignedToSignedLong(readUnsigned6Bytes(data, off), 0x7FFFFFFFFFFFL); }
	
	/**
	 * Read 6 bytes as a signed integer.
	 * @param buffer bytes
	 * @return signed integer
	 */
	default long readSigned6Bytes(ByteBuffer buffer) { return unsignedToSignedLong(readUnsigned6Bytes(buffer), 0x7FFFFFFFFFFFL); }
	
	/**
	 * Generate 6 bytes containing the value as a signed integer.
	 * @param value value
	 * @return 6 bytes
	 */
	default byte[] getSigned6Bytes(long value) {
		byte[] b = new byte[6];
		writeSigned6Bytes(b, value);
		return b;
	}
	
	/**
	 * Write a value as a signed integer on 6 bytes.
	 * @param data buffer where to write the 6 bytes
	 * @param value value to write
	 */
	default void writeSigned6Bytes(byte[] data, long value) { writeUnsigned6Bytes(data, signedToUnsignedLong(value, 0xFFFFFFFFFFFFL)); }
	
	/**
	 * Write a value as a signed integer on 6 bytes.
	 * @param data buffer where to write the 6 bytes
	 * @param off offset in the buffer where the 6 bytes will be written
	 * @param value value to write
	 */
	default void writeSigned6Bytes(byte[] data, int off, long value) { writeUnsigned6Bytes(data, off, signedToUnsignedLong(value, 0xFFFFFFFFFFFFL)); }
	
	/**
	 * Write a value as a signed integer on 6 bytes.
	 * @param buffer where to write the 6 bytes
	 * @param value value to write
	 */
	default void writeSigned6Bytes(ByteBuffer buffer, long value) { writeUnsigned6Bytes(buffer, signedToUnsignedLong(value, 0xFFFFFFFFFFFFL)); }
	
	
	// 7 Bytes unsigned
	
	/**
	 * Read 7 bytes as an unsigned integer.
	 * @param data bytes
	 * @return integer
	 */
	long readUnsigned7Bytes(byte[] data);
	
	/**
	 * Read 7 bytes as an unsigned integer.
	 * @param data bytes
	 * @param off offset in bytes where the 2 bytes are located
	 * @return integer
	 */
	long readUnsigned7Bytes(byte[] data, int off);
	
	/**
	 * Read 7 bytes as an unsigned integer.
	 * @param buffer bytes
	 * @return integer
	 */
	long readUnsigned7Bytes(ByteBuffer buffer);
	
	/**
	 * Generate 7 bytes containing the value as an unsigned integer.
	 * @param value value
	 * @return 7 bytes
	 */
	default byte[] getUnsigned7Bytes(long value) {
		byte[] b = new byte[7];
		writeUnsigned7Bytes(b, value);
		return b;
	}
	
	/**
	 * Write a value as an unsigned integer on 7 bytes.
	 * @param data buffer where to write the 7 bytes
	 * @param value value to write
	 */
	void writeUnsigned7Bytes(byte[] data, long value);
	
	/**
	 * Write a value as an unsigned integer on 7 bytes.
	 * @param data buffer where to write the 7 bytes
	 * @param off offset in the buffer where the 7 bytes will be written
	 * @param value value to write
	 */
	void writeUnsigned7Bytes(byte[] data, int off, long value);
	
	/**
	 * Write a value as an unsigned integer on 7 bytes.
	 * @param buffer buffer where to write the 7 bytes
	 * @param value value to write
	 */
	void writeUnsigned7Bytes(ByteBuffer buffer, long value);
	
	// 7 bytes signed
	
	/**
	 * Read 7 bytes as a signed integer.
	 * @param data bytes
	 * @return signed integer
	 */
	default long readSigned7Bytes(byte[] data) { return unsignedToSignedLong(readUnsigned7Bytes(data), 0x7FFFFFFFFFFFFFL); }
	
	/**
	 * Read 7 bytes as a signed integer.
	 * @param data bytes
	 * @param off offset in the data where to read the bytes
	 * @return signed integer
	 */
	default long readSigned7Bytes(byte[] data, int off) { return unsignedToSignedLong(readUnsigned7Bytes(data, off), 0x7FFFFFFFFFFFFFL); }
	
	/**
	 * Read 7 bytes as a signed integer.
	 * @param buffer bytes
	 * @return signed integer
	 */
	default long readSigned7Bytes(ByteBuffer buffer) { return unsignedToSignedLong(readUnsigned7Bytes(buffer), 0x7FFFFFFFFFFFFFL); }
	
	/**
	 * Generate 7 bytes containing the value as a signed integer.
	 * @param value value
	 * @return 7 bytes
	 */
	default byte[] getSigned7Bytes(long value) {
		byte[] b = new byte[7];
		writeSigned7Bytes(b, value);
		return b;
	}
	
	/**
	 * Write a value as a signed integer on 7 bytes.
	 * @param data buffer where to write the 7 bytes
	 * @param value value to write
	 */
	default void writeSigned7Bytes(byte[] data, long value) { writeUnsigned7Bytes(data, signedToUnsignedLong(value, 0xFFFFFFFFFFFFFFL)); }
	
	/**
	 * Write a value as a signed integer on 7 bytes.
	 * @param data buffer where to write the 7 bytes
	 * @param off offset in the buffer where the 7 bytes will be written
	 * @param value value to write
	 */
	default void writeSigned7Bytes(byte[] data, int off, long value) { writeUnsigned7Bytes(data, off, signedToUnsignedLong(value, 0xFFFFFFFFFFFFFFL)); }
	
	/**
	 * Write a value as a signed integer on 7 bytes.
	 * @param buffer where to write the 7 bytes
	 * @param value value to write
	 */
	default void writeSigned7Bytes(ByteBuffer buffer, long value) { writeUnsigned7Bytes(buffer, signedToUnsignedLong(value, 0xFFFFFFFFFFFFFFL)); }
	
	// 8 bytes

	/**
	 * Read 8 bytes as a signed integer.
	 * @param data bytes
	 * @return signed integer
	 */
	long readSigned8Bytes(byte[] data);

	/**
	 * Read 8 bytes as a signed integer.
	 * @param data bytes
	 * @return signed integer
	 */
	default long readLong(byte[] data) { return readSigned8Bytes(data); }
	
	/**
	 * Read 8 bytes as a signed integer.
	 * @param data bytes
	 * @param off offset in the data where to read the bytes
	 * @return signed integer
	 */
	long readSigned8Bytes(byte[] data, int off);
	
	/**
	 * Read 8 bytes as a signed integer.
	 * @param data bytes
	 * @param off offset in the data where to read the bytes
	 * @return signed integer
	 */
	default long readLong(byte[] data, int off) { return readSigned8Bytes(data, off); }
	
	/**
	 * Read 8 bytes as a signed integer.
	 * @param buffer bytes
	 * @return signed integer
	 */
	long readSigned8Bytes(ByteBuffer buffer);
	
	/**
	 * Read 8 bytes as a signed integer.
	 * @param buffer bytes
	 * @return signed integer
	 */
	default long readLong(ByteBuffer buffer) { return readSigned8Bytes(buffer); }
	
	/**
	 * Generate 8 bytes containing the value as a signed integer.
	 * @param value value
	 * @return 8 bytes
	 */
	default byte[] getSigned8Bytes(long value) {
		byte[] b = new byte[8];
		writeSigned8Bytes(b, value);
		return b;
	}
	
	/**
	 * Generate 8 bytes containing the value as a signed integer.
	 * @param value value
	 * @return 8 bytes
	 */
	default byte[] getLong(long value) { return getSigned8Bytes(value); }
	
	/**
	 * Write a value as a signed integer on 8 bytes.
	 * @param data buffer where to write the 8 bytes
	 * @param value value to write
	 */
	void writeSigned8Bytes(byte[] data, long value);
	
	/**
	 * Write a value as a signed integer on 8 bytes.
	 * @param data buffer where to write the 8 bytes
	 * @param value value to write
	 */
	default void writeLong(byte[] data, long value) { writeSigned8Bytes(data, value); }
	
	/**
	 * Write a value as a signed integer on 8 bytes.
	 * @param data buffer where to write the 8 bytes
	 * @param off offset in the buffer where the 8 bytes will be written
	 * @param value value to write
	 */
	void writeSigned8Bytes(byte[] data, int off, long value);
	
	/**
	 * Write a value as a signed integer on 8 bytes.
	 * @param data buffer where to write the 8 bytes
	 * @param off offset in the buffer where the 8 bytes will be written
	 * @param value value to write
	 */
	default void writeLong(byte[] data, int off, long value) { writeSigned8Bytes(data, off, value); }
	
	/**
	 * Write a value as a signed integer on 8 bytes.
	 * @param buffer where to write the 8 bytes
	 * @param value value to write
	 */
	void writeSigned8Bytes(ByteBuffer buffer, long value);
	
	/**
	 * Write a value as a signed integer on 8 bytes.
	 * @param buffer where to write the 8 bytes
	 * @param value value to write
	 */
	default void writeLong(ByteBuffer buffer, long value) { writeSigned8Bytes(buffer, value); }
	
	
	// generic nbBytes
	
	/**
	 * Read an unsigned integer using the given number of bytes.
	 * @param nbBytes number of bytes &gt; 0 and &lt;= 7
	 * @param data bytes to read
	 * @return unsigned integer
	 */
	default long readUnsignedBytes(int nbBytes, byte[] data) {
		switch (nbBytes) {
		case 1: return data[0] & 0xFF;
		case 2: return readUnsigned2Bytes(data);
		case 3: return readUnsigned3Bytes(data);
		case 4: return readUnsigned4Bytes(data);
		case 5: return readUnsigned5Bytes(data);
		case 6: return readUnsigned6Bytes(data);
		case 7: return readUnsigned7Bytes(data);
		default: throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Read an unsigned integer using the given number of bytes.
	 * @param nbBytes number of bytes &gt; 0 and &lt;= 7
	 * @param data bytes to read
	 * @param off offset in bytes
	 * @return unsigned integer
	 */
	default long readUnsignedBytes(int nbBytes, byte[] data, int off) {
		switch (nbBytes) {
		case 1: return data[off] & 0xFF;
		case 2: return readUnsigned2Bytes(data, off);
		case 3: return readUnsigned3Bytes(data, off);
		case 4: return readUnsigned4Bytes(data, off);
		case 5: return readUnsigned5Bytes(data, off);
		case 6: return readUnsigned6Bytes(data, off);
		case 7: return readUnsigned7Bytes(data, off);
		default: throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Read an unsigned integer using the given number of bytes.
	 * @param nbBytes number of bytes &gt; 0 and &lt;= 7
	 * @param buffer bytes to read
	 * @return unsigned integer
	 */
	default long readUnsignedBytes(int nbBytes, ByteBuffer buffer) {
		switch (nbBytes) {
		case 1: return buffer.get() & 0xFF;
		case 2: return readUnsigned2Bytes(buffer);
		case 3: return readUnsigned3Bytes(buffer);
		case 4: return readUnsigned4Bytes(buffer);
		case 5: return readUnsigned5Bytes(buffer);
		case 6: return readUnsigned6Bytes(buffer);
		case 7: return readUnsigned7Bytes(buffer);
		default: throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Read a signed integer using the given number of bytes.
	 * @param nbBytes number of bytes &gt; 0 and &lt;= 7
	 * @param data bytes to read
	 * @return unsigned integer
	 */
	default long readSignedBytes(int nbBytes, byte[] data) {
		switch (nbBytes) {
		case 1: return data[0];
		case 2: return readSigned2Bytes(data);
		case 3: return readSigned3Bytes(data);
		case 4: return readSigned4Bytes(data);
		case 5: return readSigned5Bytes(data);
		case 6: return readSigned6Bytes(data);
		case 7: return readSigned7Bytes(data);
		case 8: return readSigned8Bytes(data);
		default: throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Read a signed integer using the given number of bytes.
	 * @param nbBytes number of bytes &gt; 0 and &lt;= 7
	 * @param data bytes to read
	 * @param off offset in bytes
	 * @return unsigned integer
	 */
	default long readSignedBytes(int nbBytes, byte[] data, int off) {
		switch (nbBytes) {
		case 1: return data[off];
		case 2: return readSigned2Bytes(data, off);
		case 3: return readSigned3Bytes(data, off);
		case 4: return readSigned4Bytes(data, off);
		case 5: return readSigned5Bytes(data, off);
		case 6: return readSigned6Bytes(data, off);
		case 7: return readSigned7Bytes(data, off);
		case 8: return readSigned8Bytes(data, off);
		default: throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Read a signed integer using the given number of bytes.
	 * @param nbBytes number of bytes &gt; 0 and &lt;= 7
	 * @param buffer bytes to read
	 * @return unsigned integer
	 */
	default long readSignedBytes(int nbBytes, ByteBuffer buffer) {
		switch (nbBytes) {
		case 1: return buffer.get();
		case 2: return readSigned2Bytes(buffer);
		case 3: return readSigned3Bytes(buffer);
		case 4: return readSigned4Bytes(buffer);
		case 5: return readSigned5Bytes(buffer);
		case 6: return readSigned6Bytes(buffer);
		case 7: return readSigned7Bytes(buffer);
		case 8: return readSigned8Bytes(buffer);
		default: throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Generate bytes to encode the given value as an unsigned integer, on the given number of bytes.
	 * @param nbBytes number of bytes
	 * @param value value
	 * @return bytes
	 */
	default byte[] getUnsignedBytes(int nbBytes, long value) {
		switch (nbBytes) {
		case 1: return new byte[] { (byte) (value & 0xFF) };
		case 2: return getUnsigned2Bytes((int) value);
		case 3: return getUnsigned3Bytes((int) value);
		case 4: return getUnsigned4Bytes(value);
		case 5: return getUnsigned5Bytes(value);
		case 6: return getUnsigned6Bytes(value);
		case 7: return getUnsigned7Bytes(value);
		default: throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Generate bytes to encode the given value as a signed integer, on the given number of bytes.
	 * @param nbBytes number of bytes
	 * @param value value
	 * @return bytes
	 */
	default byte[] getSignedBytes(int nbBytes, long value) {
		switch (nbBytes) {
		case 1: return new byte[] { (byte) value };
		case 2: return getSigned2Bytes((short) value);
		case 3: return getSigned3Bytes((int) value);
		case 4: return getSigned4Bytes((int) value);
		case 5: return getSigned5Bytes(value);
		case 6: return getSigned6Bytes(value);
		case 7: return getSigned7Bytes(value);
		case 8: return getSigned8Bytes(value);
		default: throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Write the given value as an unsigned integer encoded on the given number of bytes.
	 * @param nbBytes number of bytes
	 * @param value value
	 * @param data where to write bytes
	 */
	default void writeUnsignedBytes(int nbBytes, long value, byte[] data) {
		switch (nbBytes) {
		case 1: data[0] = (byte) (value & 0xFF); break;
		case 2: writeUnsigned2Bytes(data, (int) (value & 0xFFFF)); break;
		case 3: writeUnsigned3Bytes(data, (int) (value & 0xFFFFFF)); break;
		case 4: writeUnsigned4Bytes(data, value); break;
		case 5: writeUnsigned5Bytes(data, value); break;
		case 6: writeUnsigned6Bytes(data, value); break;
		case 7: writeUnsigned7Bytes(data, value); break;
		default: throw new IllegalArgumentException();
		}
	}

	/**
	 * Write the given value as a signed integer encoded on the given number of bytes.
	 * @param nbBytes number of bytes
	 * @param value value
	 * @param data where to write bytes
	 */
	default void writeSignedBytes(int nbBytes, long value, byte[] data) {
		switch (nbBytes) {
		case 1: data[0] = (byte) value; break;
		case 2: writeSigned2Bytes(data, (short) value); break;
		case 3: writeSigned3Bytes(data, (int) value); break;
		case 4: writeSigned4Bytes(data, (int) value); break;
		case 5: writeSigned5Bytes(data, value); break;
		case 6: writeSigned6Bytes(data, value); break;
		case 7: writeSigned7Bytes(data, value); break;
		case 8: writeSigned8Bytes(data, value); break;
		default: throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Write the given value as an unsigned integer encoded on the given number of bytes.
	 * @param nbBytes number of bytes
	 * @param value value
	 * @param data where to write bytes
	 * @param off offset in data where to write bytes
	 */
	default void writeUnsignedBytes(int nbBytes, long value, byte[] data, int off) {
		switch (nbBytes) {
		case 1: data[off] = (byte) (value & 0xFF); break;
		case 2: writeUnsigned2Bytes(data, off, (int) (value & 0xFFFF)); break;
		case 3: writeUnsigned3Bytes(data, off, (int) (value & 0xFFFFFF)); break;
		case 4: writeUnsigned4Bytes(data, off, value); break;
		case 5: writeUnsigned5Bytes(data, off, value); break;
		case 6: writeUnsigned6Bytes(data, off, value); break;
		case 7: writeUnsigned7Bytes(data, off, value); break;
		default: throw new IllegalArgumentException();
		}
	}

	/**
	 * Write the given value as a signed integer encoded on the given number of bytes.
	 * @param nbBytes number of bytes
	 * @param value value
	 * @param data where to write bytes
	 * @param off offset in data where to write bytes
	 */
	default void writeSignedBytes(int nbBytes, long value, byte[] data, int off) {
		switch (nbBytes) {
		case 1: data[off] = (byte) value; break;
		case 2: writeSigned2Bytes(data, off, (short) value); break;
		case 3: writeSigned3Bytes(data, off, (int) value); break;
		case 4: writeSigned4Bytes(data, off, (int) value); break;
		case 5: writeSigned5Bytes(data, off, value); break;
		case 6: writeSigned6Bytes(data, off, value); break;
		case 7: writeSigned7Bytes(data, off, value); break;
		case 8: writeSigned8Bytes(data, off, value); break;
		default: throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Write the given value as an unsigned integer encoded on the given number of bytes.
	 * @param nbBytes number of bytes
	 * @param value value
	 * @param buffer where to write bytes
	 */
	default void writeUnsignedBytes(int nbBytes, long value, ByteBuffer buffer) {
		switch (nbBytes) {
		case 1: buffer.put((byte) (value & 0xFF)); break;
		case 2: writeUnsigned2Bytes(buffer, (int) (value & 0xFFFF)); break;
		case 3: writeUnsigned3Bytes(buffer, (int) (value & 0xFFFFFF)); break;
		case 4: writeUnsigned4Bytes(buffer, value); break;
		case 5: writeUnsigned5Bytes(buffer, value); break;
		case 6: writeUnsigned6Bytes(buffer, value); break;
		case 7: writeUnsigned7Bytes(buffer, value); break;
		default: throw new IllegalArgumentException();
		}
	}

	/**
	 * Write the given value as a signed integer encoded on the given number of bytes.
	 * @param nbBytes number of bytes
	 * @param value value
	 * @param buffer where to write bytes
	 */
	default void writeSignedBytes(int nbBytes, long value, ByteBuffer buffer) {
		switch (nbBytes) {
		case 1: buffer.put((byte) value); break;
		case 2: writeSigned2Bytes(buffer, (short) value); break;
		case 3: writeSigned3Bytes(buffer, (int) value); break;
		case 4: writeSigned4Bytes(buffer, (int) value); break;
		case 5: writeSigned5Bytes(buffer, value); break;
		case 6: writeSigned6Bytes(buffer, value); break;
		case 7: writeSigned7Bytes(buffer, value); break;
		case 8: writeSigned8Bytes(buffer, value); break;
		default: throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Little Endian implementation.
	 */
	class LittleEndian implements BytesData {
		@Override
		public ByteOrder getByteOrder() {
			return ByteOrder.LITTLE_ENDIAN;
		}
		
		@Override
		public int readUnsigned2Bytes(byte[] data) {
			return (data[0] & 0xFF) |
				((data[1] & 0xFF) << 8);
		}
		
		@Override
		public int readUnsigned2Bytes(byte[] data, int off) {
			return (data[off++] & 0xFF) |
				((data[off] & 0xFF) << 8);
		}
		
		@Override
		public int readUnsigned2Bytes(ByteBuffer buffer) {
			return (buffer.get() & 0xFF) |
				((buffer.get() & 0xFF) << 8);
		}
		
		@Override
		public int readUnsigned2Bytes(byte b1, byte b2) {
			return b1 & 0xFF | ((b2 & 0xFF) << 8);
		}
		
		@Override
		public void writeUnsigned2Bytes(byte[] data, int value) {
			data[0] = (byte) (value & 0xFF);
			data[1] = (byte) ((value & 0xFF00) >> 8);
		}
		
		@Override
		public void writeUnsigned2Bytes(byte[] data, int off, int value) {
			data[off++] = (byte) (value & 0xFF);
			data[off] = (byte) ((value & 0xFF00) >> 8);
		}
		
		@Override
		public void writeUnsigned2Bytes(ByteBuffer buffer, int value) {
			buffer.put((byte) (value & 0xFF));
			buffer.put((byte) ((value & 0xFF00) >> 8));
		}
		
		@Override
		public int readUnsigned3Bytes(byte[] data) {
			return (data[0] & 0xFF) |
				((data[1] & 0xFF) << 8) |
				((data[2] & 0xFF) << 16);
		}
		
		@Override
		public int readUnsigned3Bytes(byte[] data, int off) {
			return (data[off++] & 0xFF) |
				((data[off++] & 0xFF) << 8) |
				((data[off] & 0xFF) << 16);
		}
		
		@Override
		public int readUnsigned3Bytes(ByteBuffer buffer) {
			return (buffer.get() & 0xFF) |
				((buffer.get() & 0xFF) << 8) |
				((buffer.get() & 0xFF) << 16);
		}
		
		@Override
		public void writeUnsigned3Bytes(byte[] data, int value) {
			data[0] = (byte) (value & 0xFF);
			data[1] = (byte) ((value & 0xFF00) >> 8);
			data[2] = (byte) ((value & 0xFF0000) >> 16);
		}
		
		@Override
		public void writeUnsigned3Bytes(byte[] data, int off, int value) {
			data[off++] = (byte) (value & 0xFF);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off] = (byte) ((value & 0xFF0000) >> 16);
		}
		
		@Override
		public void writeUnsigned3Bytes(ByteBuffer buffer, int value) {
			buffer.put((byte) (value & 0xFF));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) ((value & 0xFF0000) >> 16));
		}
		
		@Override
		public long readUnsigned4Bytes(byte[] data) {
			return (data[0] & 0xFF) |
				((data[1] & 0xFF) << 8) |
				((data[2] & 0xFF) << 16) |
				(((long) (data[3] & 0xFF)) << 24);
		}
		
		@Override
		public long readUnsigned4Bytes(byte[] data, int off) {
			return (data[off++] & 0xFF) |
				((data[off++] & 0xFF) << 8) |
				((data[off++] & 0xFF) << 16) |
				(((long) (data[off] & 0xFF)) << 24);
		}
		
		@Override
		public long readUnsigned4Bytes(ByteBuffer buffer) {
			return (buffer.get() & 0xFF) |
				((buffer.get() & 0xFF) << 8) |
				((buffer.get() & 0xFF) << 16) |
				(((long) (buffer.get() & 0xFF)) << 24);
		}
		
		@Override
		public void writeUnsigned4Bytes(byte[] data, long value) {
			data[0] = (byte) (value & 0xFF);
			data[1] = (byte) ((value & 0xFF00) >> 8);
			data[2] = (byte) ((value & 0xFF0000) >> 16);
			data[3] = (byte) ((value & 0xFF000000L) >> 24);
		}
		
		@Override
		public void writeUnsigned4Bytes(byte[] data, int off, long value) {
			data[off++] = (byte) (value & 0xFF);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off++] = (byte) ((value & 0xFF0000) >> 16);
			data[off] = (byte) ((value & 0xFF000000L) >> 24);
		}
		
		@Override
		public void writeUnsigned4Bytes(ByteBuffer buffer, long value) {
			buffer.put((byte) (value & 0xFF));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) ((value & 0xFF0000) >> 16));
			buffer.put((byte) ((value & 0xFF000000L) >> 24));
		}
		
		@Override
		public long readUnsigned5Bytes(byte[] data) {
			return (data[0] & 0xFF) |
				((data[1] & 0xFF) << 8) |
				((data[2] & 0xFF) << 16) |
				(((long) (data[3] & 0xFF)) << 24) |
				(((long) (data[4] & 0xFF)) << 32);
		}
		
		@Override
		public long readUnsigned5Bytes(byte[] data, int off) {
			return (data[off++] & 0xFF) |
				((data[off++] & 0xFF) << 8) |
				((data[off++] & 0xFF) << 16) |
				(((long) (data[off++] & 0xFF)) << 24) |
				(((long) (data[off] & 0xFF)) << 32);
		}
		
		@Override
		public long readUnsigned5Bytes(ByteBuffer buffer) {
			return (buffer.get() & 0xFF) |
				((buffer.get() & 0xFF) << 8) |
				((buffer.get() & 0xFF) << 16) |
				(((long) (buffer.get() & 0xFF)) << 24) |
				(((long) (buffer.get() & 0xFF)) << 32);
		}
		
		@Override
		public void writeUnsigned5Bytes(byte[] data, long value) {
			data[0] = (byte) (value & 0xFF);
			data[1] = (byte) ((value & 0xFF00) >> 8);
			data[2] = (byte) ((value & 0xFF0000) >> 16);
			data[3] = (byte) ((value & 0xFF000000L) >> 24);
			data[4] = (byte) ((value & 0xFF00000000L) >> 32);
		}
		
		@Override
		public void writeUnsigned5Bytes(byte[] data, int off, long value) {
			data[off++] = (byte) (value & 0xFF);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off++] = (byte) ((value & 0xFF0000) >> 16);
			data[off++] = (byte) ((value & 0xFF000000L) >> 24);
			data[off] = (byte) ((value & 0xFF00000000L) >> 32);
		}
		
		@Override
		public void writeUnsigned5Bytes(ByteBuffer buffer, long value) {
			buffer.put((byte) (value & 0xFF));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) ((value & 0xFF0000) >> 16));
			buffer.put((byte) ((value & 0xFF000000L) >> 24));
			buffer.put((byte) ((value & 0xFF00000000L) >> 32));
		}
		
		@Override
		public long readUnsigned6Bytes(byte[] data) {
			return (data[0] & 0xFF) |
				((data[1] & 0xFF) << 8) |
				((data[2] & 0xFF) << 16) |
				(((long) (data[3] & 0xFF)) << 24) |
				(((long) (data[4] & 0xFF)) << 32) |
				(((long) (data[5] & 0xFF)) << 40);
		}
		
		@Override
		public long readUnsigned6Bytes(byte[] data, int off) {
			return (data[off++] & 0xFF) |
				((data[off++] & 0xFF) << 8) |
				((data[off++] & 0xFF) << 16) |
				(((long) (data[off++] & 0xFF)) << 24) |
				(((long) (data[off++] & 0xFF)) << 32) |
				(((long) (data[off] & 0xFF)) << 40);
		}
		
		@Override
		public long readUnsigned6Bytes(ByteBuffer buffer) {
			return (buffer.get() & 0xFF) |
				((buffer.get() & 0xFF) << 8) |
				((buffer.get() & 0xFF) << 16) |
				(((long) (buffer.get() & 0xFF)) << 24) |
				(((long) (buffer.get() & 0xFF)) << 32) |
				(((long) (buffer.get() & 0xFF)) << 40);
		}
		
		@Override
		public void writeUnsigned6Bytes(byte[] data, long value) {
			data[0] = (byte) (value & 0xFF);
			data[1] = (byte) ((value & 0xFF00) >> 8);
			data[2] = (byte) ((value & 0xFF0000) >> 16);
			data[3] = (byte) ((value & 0xFF000000L) >> 24);
			data[4] = (byte) ((value & 0xFF00000000L) >> 32);
			data[5] = (byte) ((value & 0xFF0000000000L) >> 40);
		}
		
		@Override
		public void writeUnsigned6Bytes(byte[] data, int off, long value) {
			data[off++] = (byte) (value & 0xFF);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off++] = (byte) ((value & 0xFF0000) >> 16);
			data[off++] = (byte) ((value & 0xFF000000L) >> 24);
			data[off++] = (byte) ((value & 0xFF00000000L) >> 32);
			data[off] = (byte) ((value & 0xFF0000000000L) >> 40);
		}
		
		@Override
		public void writeUnsigned6Bytes(ByteBuffer buffer, long value) {
			buffer.put((byte) (value & 0xFF));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) ((value & 0xFF0000) >> 16));
			buffer.put((byte) ((value & 0xFF000000L) >> 24));
			buffer.put((byte) ((value & 0xFF00000000L) >> 32));
			buffer.put((byte) ((value & 0xFF0000000000L) >> 40));
		}
		
		@Override
		public long readUnsigned7Bytes(byte[] data) {
			return (data[0] & 0xFF) |
				((data[1] & 0xFF) << 8) |
				((data[2] & 0xFF) << 16) |
				(((long) (data[3] & 0xFF)) << 24) |
				(((long) (data[4] & 0xFF)) << 32) |
				(((long) (data[5] & 0xFF)) << 40) |
				(((long) (data[6] & 0xFF)) << 48);
		}
		
		@Override
		public long readUnsigned7Bytes(byte[] data, int off) {
			return (data[off++] & 0xFF) |
				((data[off++] & 0xFF) << 8) |
				((data[off++] & 0xFF) << 16) |
				(((long) (data[off++] & 0xFF)) << 24) |
				(((long) (data[off++] & 0xFF)) << 32) |
				(((long) (data[off++] & 0xFF)) << 40) |
				(((long) (data[off] & 0xFF)) << 48);
		}
		
		@Override
		public long readUnsigned7Bytes(ByteBuffer buffer) {
			return (buffer.get() & 0xFF) |
				((buffer.get() & 0xFF) << 8) |
				((buffer.get() & 0xFF) << 16) |
				(((long) (buffer.get() & 0xFF)) << 24) |
				(((long) (buffer.get() & 0xFF)) << 32) |
				(((long) (buffer.get() & 0xFF)) << 40) |
				(((long) (buffer.get() & 0xFF)) << 48);
		}
		
		@Override
		public void writeUnsigned7Bytes(byte[] data, long value) {
			data[0] = (byte) (value & 0xFF);
			data[1] = (byte) ((value & 0xFF00) >> 8);
			data[2] = (byte) ((value & 0xFF0000) >> 16);
			data[3] = (byte) ((value & 0xFF000000L) >> 24);
			data[4] = (byte) ((value & 0xFF00000000L) >> 32);
			data[5] = (byte) ((value & 0xFF0000000000L) >> 40);
			data[6] = (byte) ((value & 0xFF000000000000L) >> 48);
		}
		
		@Override
		public void writeUnsigned7Bytes(byte[] data, int off, long value) {
			data[off++] = (byte) (value & 0xFF);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off++] = (byte) ((value & 0xFF0000) >> 16);
			data[off++] = (byte) ((value & 0xFF000000L) >> 24);
			data[off++] = (byte) ((value & 0xFF00000000L) >> 32);
			data[off++] = (byte) ((value & 0xFF0000000000L) >> 40);
			data[off] = (byte) ((value & 0xFF000000000000L) >> 48);
		}
		
		@Override
		public void writeUnsigned7Bytes(ByteBuffer buffer, long value) {
			buffer.put((byte) (value & 0xFF));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) ((value & 0xFF0000) >> 16));
			buffer.put((byte) ((value & 0xFF000000L) >> 24));
			buffer.put((byte) ((value & 0xFF00000000L) >> 32));
			buffer.put((byte) ((value & 0xFF0000000000L) >> 40));
			buffer.put((byte) ((value & 0xFF000000000000L) >> 48));
		}
		
		@Override
		public long readSigned8Bytes(byte[] data) {
			return (data[0] & 0xFF) |
				((data[1] & 0xFF) << 8) |
				((data[2] & 0xFF) << 16) |
				(((long) (data[3] & 0xFF)) << 24) |
				(((long) (data[4] & 0xFF)) << 32) |
				(((long) (data[5] & 0xFF)) << 40) |
				(((long) (data[6] & 0xFF)) << 48) |
				(((long) (data[7] & 0xFF)) << 56);
		}
		
		@Override
		public long readSigned8Bytes(byte[] data, int off) {
			return (data[off++] & 0xFF) |
				((data[off++] & 0xFF) << 8) |
				((data[off++] & 0xFF) << 16) |
				(((long) (data[off++] & 0xFF)) << 24) |
				(((long) (data[off++] & 0xFF)) << 32) |
				(((long) (data[off++] & 0xFF)) << 40) |
				(((long) (data[off++] & 0xFF)) << 48) |
				(((long) (data[off] & 0xFF)) << 56);
		}
		
		@Override
		public long readSigned8Bytes(ByteBuffer buffer) {
			return (buffer.get() & 0xFF) |
				((buffer.get() & 0xFF) << 8) |
				((buffer.get() & 0xFF) << 16) |
				(((long) (buffer.get() & 0xFF)) << 24) |
				(((long) (buffer.get() & 0xFF)) << 32) |
				(((long) (buffer.get() & 0xFF)) << 40) |
				(((long) (buffer.get() & 0xFF)) << 48) |
				(((long) (buffer.get() & 0xFF)) << 56);
		}
		
		@Override
		public void writeSigned8Bytes(byte[] data, long value) {
			data[0] = (byte) (value & 0xFF);
			data[1] = (byte) ((value & 0xFF00) >> 8);
			data[2] = (byte) ((value & 0xFF0000) >> 16);
			data[3] = (byte) ((value & 0xFF000000) >> 24);
			data[4] = (byte) ((value & 0xFF00000000L) >> 32);
			data[5] = (byte) ((value & 0xFF0000000000L) >> 40);
			data[6] = (byte) ((value & 0xFF000000000000L) >> 48);
			data[7] = (byte) (value >> 56);
		}
		
		@Override
		public void writeSigned8Bytes(byte[] data, int off, long value) {
			data[off++] = (byte) (value & 0xFF);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off++] = (byte) ((value & 0xFF0000) >> 16);
			data[off++] = (byte) ((value & 0xFF000000) >> 24);
			data[off++] = (byte) ((value & 0xFF00000000L) >> 32);
			data[off++] = (byte) ((value & 0xFF0000000000L) >> 40);
			data[off++] = (byte) ((value & 0xFF000000000000L) >> 48);
			data[off] = (byte) (value >> 56);
		}
		
		@Override
		public void writeSigned8Bytes(ByteBuffer buffer, long value) {
			buffer.put((byte) (value & 0xFF));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) ((value & 0xFF0000) >> 16));
			buffer.put((byte) ((value & 0xFF000000) >> 24));
			buffer.put((byte) ((value & 0xFF00000000L) >> 32));
			buffer.put((byte) ((value & 0xFF0000000000L) >> 40));
			buffer.put((byte) ((value & 0xFF000000000000L) >> 48));
			buffer.put((byte) (value >> 56));
		}
		
	}
	
	/**
	 * Big Endian implementation.
	 */
	class BigEndian implements BytesData {
		@Override
		public ByteOrder getByteOrder() {
			return ByteOrder.BIG_ENDIAN;
		}
		
		@Override
		public int readUnsigned2Bytes(byte[] data) {
			return ((data[0] & 0xFF) << 8)
				| (data[1] & 0xFF);
		}
		
		@Override
		public int readUnsigned2Bytes(byte[] data, int off) {
			return ((data[off++] & 0xFF) << 8)
				| (data[off] & 0xFF);
		}
		
		@Override
		public int readUnsigned2Bytes(ByteBuffer buffer) {
			return ((buffer.get() & 0xFF) << 8) |
				(buffer.get() & 0xFF);
		}
		
		@Override
		public int readUnsigned2Bytes(byte b1, byte b2) {
			return ((b1 & 0xFF) << 8) | (b2 & 0xFF);
		}
		
		@Override
		public void writeUnsigned2Bytes(byte[] data, int value) {
			data[0] = (byte) ((value & 0xFF00) >> 8);
			data[1] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned2Bytes(byte[] data, int off, int value) {
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned2Bytes(ByteBuffer buffer, int value) {
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) (value & 0xFF));
		}
		
		@Override
		public int readUnsigned3Bytes(byte[] data) {
			return ((data[0] & 0xFF) << 16)
				| ((data[1] & 0xFF) << 8)
				| (data[2] & 0xFF);
		}
		
		@Override
		public int readUnsigned3Bytes(byte[] data, int off) {
			return ((data[off++] & 0xFF) << 16)
				| ((data[off++] & 0xFF) << 8)
				| (data[off] & 0xFF);
		}
		
		@Override
		public int readUnsigned3Bytes(ByteBuffer buffer) {
			return ((buffer.get() & 0xFF) << 16) |
				((buffer.get() & 0xFF) << 8) |
				(buffer.get() & 0xFF);
		}
		
		@Override
		public void writeUnsigned3Bytes(byte[] data, int value) {
			data[0] = (byte) ((value & 0xFF0000) >> 16);
			data[1] = (byte) ((value & 0xFF00) >> 8);
			data[2] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned3Bytes(byte[] data, int off, int value) {
			data[off++] = (byte) ((value & 0xFF0000) >> 16);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned3Bytes(ByteBuffer buffer, int value) {
			buffer.put((byte) ((value & 0xFF0000) >> 16));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) (value & 0xFF));
		}
		
		@Override
		public long readUnsigned4Bytes(byte[] data) {
			return (((long) (data[0] & 0xFF)) << 24)
				| ((data[1] & 0xFF) << 16)
				| ((data[2] & 0xFF) << 8)
				| (data[3] & 0xFF);
		}
		
		@Override
		public long readUnsigned4Bytes(byte[] data, int off) {
			return (((long) (data[off++] & 0xFF)) << 24)
				| ((data[off++] & 0xFF) << 16)
				| ((data[off++] & 0xFF) << 8)
				| (data[off] & 0xFF);
		}
		
		@Override
		public long readUnsigned4Bytes(ByteBuffer buffer) {
			return (((long) (buffer.get() & 0xFF)) << 24) |
				((buffer.get() & 0xFF) << 16) |
				((buffer.get() & 0xFF) << 8) |
				(buffer.get() & 0xFF);
		}
		
		@Override
		public void writeUnsigned4Bytes(byte[] data, long value) {
			data[0] = (byte) ((value & 0xFF000000L) >> 24);
			data[1] = (byte) ((value & 0xFF0000) >> 16);
			data[2] = (byte) ((value & 0xFF00) >> 8);
			data[3] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned4Bytes(byte[] data, int off, long value) {
			data[off++] = (byte) ((value & 0xFF000000L) >> 24);
			data[off++] = (byte) ((value & 0xFF0000) >> 16);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned4Bytes(ByteBuffer buffer, long value) {
			buffer.put((byte) ((value & 0xFF000000L) >> 24));
			buffer.put((byte) ((value & 0xFF0000) >> 16));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) (value & 0xFF));
		}
		
		@Override
		public long readUnsigned5Bytes(byte[] data) {
			return (((long) (data[0] & 0xFF)) << 32)
				| (((long) (data[1] & 0xFF)) << 24)
				| ((data[2] & 0xFF) << 16)
				| ((data[3] & 0xFF) << 8)
				| (data[4] & 0xFF);
		}
		
		@Override
		public long readUnsigned5Bytes(byte[] data, int off) {
			return (((long) (data[off++] & 0xFF)) << 32)
				| (((long) (data[off++] & 0xFF)) << 24)
				| ((data[off++] & 0xFF) << 16)
				| ((data[off++] & 0xFF) << 8)
				| (data[off] & 0xFF);
		}
		
		@Override
		public long readUnsigned5Bytes(ByteBuffer buffer) {
			return (((long) (buffer.get() & 0xFF)) << 32) |
				(((long) (buffer.get() & 0xFF)) << 24) |
				((buffer.get() & 0xFF) << 16) |
				((buffer.get() & 0xFF) << 8) |
				(buffer.get() & 0xFF);
		}

		@Override
		public void writeUnsigned5Bytes(byte[] data, long value) {
			data[0] = (byte) ((value & 0xFF00000000L) >> 32);
			data[1] = (byte) ((value & 0xFF000000L) >> 24);
			data[2] = (byte) ((value & 0xFF0000) >> 16);
			data[3] = (byte) ((value & 0xFF00) >> 8);
			data[4] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned5Bytes(byte[] data, int off, long value) {
			data[off++] = (byte) ((value & 0xFF00000000L) >> 32);
			data[off++] = (byte) ((value & 0xFF000000L) >> 24);
			data[off++] = (byte) ((value & 0xFF0000) >> 16);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned5Bytes(ByteBuffer buffer, long value) {
			buffer.put((byte) ((value & 0xFF00000000L) >> 32));
			buffer.put((byte) ((value & 0xFF000000L) >> 24));
			buffer.put((byte) ((value & 0xFF0000) >> 16));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) (value & 0xFF));
		}
		
		@Override
		public long readUnsigned6Bytes(byte[] data) {
			return (((long) (data[0] & 0xFF)) << 40)
				| (((long) (data[1] & 0xFF)) << 32)
				| (((long) (data[2] & 0xFF)) << 24)
				| ((data[3] & 0xFF) << 16)
				| ((data[4] & 0xFF) << 8)
				| (data[5] & 0xFF);
		}
		
		@Override
		public long readUnsigned6Bytes(byte[] data, int off) {
			return (((long) (data[off++] & 0xFF)) << 40)
				| (((long) (data[off++] & 0xFF)) << 32)
				| (((long) (data[off++] & 0xFF)) << 24)
				| ((data[off++] & 0xFF) << 16)
				| ((data[off++] & 0xFF) << 8)
				| (data[off] & 0xFF);
		}
		
		@Override
		public long readUnsigned6Bytes(ByteBuffer buffer) {
			return (((long) (buffer.get() & 0xFF)) << 40) |
				(((long) (buffer.get() & 0xFF)) << 32) |
				(((long) (buffer.get() & 0xFF)) << 24) |
				((buffer.get() & 0xFF) << 16) |
				((buffer.get() & 0xFF) << 8) |
				(buffer.get() & 0xFF);
		}
		
		@Override
		public void writeUnsigned6Bytes(byte[] data, long value) {
			data[0] = (byte) ((value & 0xFF0000000000L) >> 40);
			data[1] = (byte) ((value & 0xFF00000000L) >> 32);
			data[2] = (byte) ((value & 0xFF000000L) >> 24);
			data[3] = (byte) ((value & 0xFF0000) >> 16);
			data[4] = (byte) ((value & 0xFF00) >> 8);
			data[5] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned6Bytes(byte[] data, int off, long value) {
			data[off++] = (byte) ((value & 0xFF0000000000L) >> 40);
			data[off++] = (byte) ((value & 0xFF00000000L) >> 32);
			data[off++] = (byte) ((value & 0xFF000000L) >> 24);
			data[off++] = (byte) ((value & 0xFF0000) >> 16);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned6Bytes(ByteBuffer buffer, long value) {
			buffer.put((byte) ((value & 0xFF0000000000L) >> 40));
			buffer.put((byte) ((value & 0xFF00000000L) >> 32));
			buffer.put((byte) ((value & 0xFF000000L) >> 24));
			buffer.put((byte) ((value & 0xFF0000) >> 16));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) (value & 0xFF));
		}
		
		@Override
		public long readUnsigned7Bytes(byte[] data) {
			return (((long) (data[0] & 0xFF)) << 48)
				| (((long) (data[1] & 0xFF)) << 40)
				| (((long) (data[2] & 0xFF)) << 32)
				| (((long) (data[3] & 0xFF)) << 24)
				| ((data[4] & 0xFF) << 16)
				| ((data[5] & 0xFF) << 8)
				| (data[6] & 0xFF);
		}
		
		@Override
		public long readUnsigned7Bytes(byte[] data, int off) {
			return (((long) (data[off++] & 0xFF)) << 48)
				| (((long) (data[off++] & 0xFF)) << 40)
				| (((long) (data[off++] & 0xFF)) << 32)
				| (((long) (data[off++] & 0xFF)) << 24)
				| ((data[off++] & 0xFF) << 16)
				| ((data[off++] & 0xFF) << 8)
				| (data[off] & 0xFF);
		}
		
		@Override
		public long readUnsigned7Bytes(ByteBuffer buffer) {
			return (((long) (buffer.get() & 0xFF)) << 48) |
				(((long) (buffer.get() & 0xFF)) << 40) |
				(((long) (buffer.get() & 0xFF)) << 32) |
				(((long) (buffer.get() & 0xFF)) << 24) |
				((buffer.get() & 0xFF) << 16) |
				((buffer.get() & 0xFF) << 8) |
				(buffer.get() & 0xFF);
		}
		
		@Override
		public void writeUnsigned7Bytes(byte[] data, long value) {
			data[0] = (byte) ((value & 0xFF000000000000L) >> 48);
			data[1] = (byte) ((value & 0xFF0000000000L) >> 40);
			data[2] = (byte) ((value & 0xFF00000000L) >> 32);
			data[3] = (byte) ((value & 0xFF000000L) >> 24);
			data[4] = (byte) ((value & 0xFF0000) >> 16);
			data[5] = (byte) ((value & 0xFF00) >> 8);
			data[6] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned7Bytes(byte[] data, int off, long value) {
			data[off++] = (byte) ((value & 0xFF000000000000L) >> 48);
			data[off++] = (byte) ((value & 0xFF0000000000L) >> 40);
			data[off++] = (byte) ((value & 0xFF00000000L) >> 32);
			data[off++] = (byte) ((value & 0xFF000000L) >> 24);
			data[off++] = (byte) ((value & 0xFF0000) >> 16);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeUnsigned7Bytes(ByteBuffer buffer, long value) {
			buffer.put((byte) ((value & 0xFF000000000000L) >> 48));
			buffer.put((byte) ((value & 0xFF0000000000L) >> 40));
			buffer.put((byte) ((value & 0xFF00000000L) >> 32));
			buffer.put((byte) ((value & 0xFF000000L) >> 24));
			buffer.put((byte) ((value & 0xFF0000) >> 16));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) (value & 0xFF));
		}
		
		@Override
		public long readSigned8Bytes(byte[] data) {
			return (((long) (data[0] & 0xFF)) << 56)
				| (((long) (data[1] & 0xFF)) << 48)
				| (((long) (data[2] & 0xFF)) << 40)
				| (((long) (data[3] & 0xFF)) << 32)
				| (((long) (data[4] & 0xFF)) << 24)
				| ((data[5] & 0xFF) << 16)
				| ((data[6] & 0xFF) << 8)
				| (data[7] & 0xFF);
		}
		
		@Override
		public long readSigned8Bytes(byte[] data, int off) {
			return (((long) (data[off++] & 0xFF)) << 56)
				| (((long) (data[off++] & 0xFF)) << 48)
				| (((long) (data[off++] & 0xFF)) << 40)
				| (((long) (data[off++] & 0xFF)) << 32)
				| (((long) (data[off++] & 0xFF)) << 24)
				| ((data[off++] & 0xFF) << 16)
				| ((data[off++] & 0xFF) << 8)
				| (data[off] & 0xFF);
		}
		
		@Override
		public long readSigned8Bytes(ByteBuffer buffer) {
			return (((long) (buffer.get() & 0xFF)) << 56) |
				(((long) (buffer.get() & 0xFF)) << 48) |
				(((long) (buffer.get() & 0xFF)) << 40) |
				(((long) (buffer.get() & 0xFF)) << 32) |
				(((long) (buffer.get() & 0xFF)) << 24) |
				((buffer.get() & 0xFF) << 16) |
				((buffer.get() & 0xFF) << 8) |
				(buffer.get() & 0xFF);
		}
		
		@Override
		public void writeSigned8Bytes(byte[] data, long value) {
			data[0] = (byte) (value >> 56);
			data[1] = (byte) ((value & 0xFF000000000000L) >> 48);
			data[2] = (byte) ((value & 0xFF0000000000L) >> 40);
			data[3] = (byte) ((value & 0xFF00000000L) >> 32);
			data[4] = (byte) ((value & 0xFF000000L) >> 24);
			data[5] = (byte) ((value & 0xFF0000) >> 16);
			data[6] = (byte) ((value & 0xFF00) >> 8);
			data[7] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeSigned8Bytes(byte[] data, int off, long value) {
			data[off++] = (byte) (value >> 56);
			data[off++] = (byte) ((value & 0xFF000000000000L) >> 48);
			data[off++] = (byte) ((value & 0xFF0000000000L) >> 40);
			data[off++] = (byte) ((value & 0xFF00000000L) >> 32);
			data[off++] = (byte) ((value & 0xFF000000L) >> 24);
			data[off++] = (byte) ((value & 0xFF0000) >> 16);
			data[off++] = (byte) ((value & 0xFF00) >> 8);
			data[off] = (byte) (value & 0xFF);
		}
		
		@Override
		public void writeSigned8Bytes(ByteBuffer buffer, long value) {
			buffer.put((byte) (value >> 56));
			buffer.put((byte) ((value & 0xFF000000000000L) >> 48));
			buffer.put((byte) ((value & 0xFF0000000000L) >> 40));
			buffer.put((byte) ((value & 0xFF00000000L) >> 32));
			buffer.put((byte) ((value & 0xFF000000L) >> 24));
			buffer.put((byte) ((value & 0xFF0000) >> 16));
			buffer.put((byte) ((value & 0xFF00) >> 8));
			buffer.put((byte) (value & 0xFF));
		}
	}
	
}

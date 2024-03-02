package net.lecousin.commons.io.bytes.memory;

import java.nio.ByteOrder;

import net.lecousin.commons.io.bytes.data.BytesData;
import net.lecousin.commons.io.bytes.data.BytesDataBuffer;

/** BytesDataBuffer using a ByteArray. */
// CHECKSTYLE DISABLE: MagicNumber
public class ByteArrayDataBuffer implements BytesDataBuffer.ReadWrite {

	private ByteArray bytes;
	private BytesData data;
	
	/**
	 * Constructor.
	 * @param bytes byte array
	 * @param order byte order
	 */
	public ByteArrayDataBuffer(ByteArray bytes, ByteOrder order) {
		this.bytes = bytes;
		this.data = BytesData.of(order);
	}

	/**
	 * Constructor with default byte order Little-Endian.
	 * @param bytes byte array
	 */
	public ByteArrayDataBuffer(ByteArray bytes) {
		this(bytes, ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public ByteOrder getByteOrder() {
		return data.getByteOrder();
	}

	@Override
	public void setByteOrder(ByteOrder order) {
		data = BytesData.of(order);
	}

	@Override
	public int getPosition() {
		return bytes.position;
	}

	@Override
	public int getSize() {
		return bytes.getSize();
	}

	@Override
	public void setPosition(int newPosition) {
		bytes.setPosition(newPosition);
	}


	
	@Override
	public byte readSignedByte() {
		return bytes.bytes[bytes.start + bytes.position++];
	}
	
	@Override
	public void writeSignedByte(byte value) {
		bytes.bytes[bytes.start + bytes.position++] = value;
	}

	@Override
	public short readSigned2Bytes() {
		short value = data.readSigned2Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 2;
		return value;
	}

	@Override
	public int readUnsigned2Bytes() {
		int value = data.readUnsigned2Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 2;
		return value;
	}

	@Override
	public void writeSigned2Bytes(short value) {
		data.writeSigned2Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 2;
	}
	
	@Override
	public void writeUnsigned2Bytes(int value) {
		data.writeUnsigned2Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 2;
	}

	@Override
	public int readSigned3Bytes() {
		int value = data.readSigned3Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 3;
		return value;
	}

	@Override
	public int readUnsigned3Bytes() {
		int value = data.readUnsigned3Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 3;
		return value;
	}

	@Override
	public void writeSigned3Bytes(int value) {
		data.writeSigned3Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 3;
	}
	
	@Override
	public void writeUnsigned3Bytes(int value) {
		data.writeUnsigned3Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 3;
	}

	@Override
	public int readSigned4Bytes() {
		int value = data.readSigned4Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 4;
		return value;
	}

	@Override
	public long readUnsigned4Bytes() {
		long value = data.readUnsigned4Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 4;
		return value;
	}

	@Override
	public void writeSigned4Bytes(int value) {
		data.writeSigned4Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 4;
	}
	
	@Override
	public void writeUnsigned4Bytes(long value) {
		data.writeUnsigned4Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 4;
	}

	@Override
	public long readSigned5Bytes() {
		long value = data.readSigned5Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 5;
		return value;
	}

	@Override
	public long readUnsigned5Bytes() {
		long value = data.readUnsigned5Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 5;
		return value;
	}

	@Override
	public void writeSigned5Bytes(long value) {
		data.writeSigned5Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 5;
	}
	
	@Override
	public void writeUnsigned5Bytes(long value) {
		data.writeUnsigned5Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 5;
	}

	@Override
	public long readSigned6Bytes() {
		long value = data.readSigned6Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 6;
		return value;
	}

	@Override
	public long readUnsigned6Bytes() {
		long value = data.readUnsigned6Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 6;
		return value;
	}

	@Override
	public void writeSigned6Bytes(long value) {
		data.writeSigned6Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 6;
	}
	
	@Override
	public void writeUnsigned6Bytes(long value) {
		data.writeUnsigned6Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 6;
	}

	@Override
	public long readSigned7Bytes() {
		long value = data.readSigned7Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 7;
		return value;
	}

	@Override
	public long readUnsigned7Bytes() {
		long value = data.readUnsigned7Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 7;
		return value;
	}

	@Override
	public void writeSigned7Bytes(long value) {
		data.writeSigned7Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 7;
	}
	
	@Override
	public void writeUnsigned7Bytes(long value) {
		data.writeUnsigned7Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 7;
	}

	@Override
	public long readSigned8Bytes() {
		long value = data.readSigned8Bytes(bytes.bytes, bytes.start + bytes.position);
		bytes.position += 8;
		return value;
	}

	@Override
	public void writeSigned8Bytes(long value) {
		data.writeSigned8Bytes(bytes.bytes, bytes.start + bytes.position, value);
		bytes.position += 8;
	}
	
}

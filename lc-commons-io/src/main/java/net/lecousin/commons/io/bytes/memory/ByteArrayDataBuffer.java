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
		return bytes.getPosition();
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
		return bytes.readByte();
	}
	
	@Override
	public void writeSignedByte(byte value) {
		bytes.writeByte(value);
	}

	@Override
	public short readSigned2Bytes() {
		short value = data.readSigned2Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(2);
		return value;
	}

	@Override
	public int readUnsigned2Bytes() {
		int value = data.readUnsigned2Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(2);
		return value;
	}

	@Override
	public void writeSigned2Bytes(short value) {
		data.writeSigned2Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(2);
	}
	
	@Override
	public void writeUnsigned2Bytes(int value) {
		data.writeUnsigned2Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(2);
	}

	@Override
	public int readSigned3Bytes() {
		int value = data.readSigned3Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(3);
		return value;
	}

	@Override
	public int readUnsigned3Bytes() {
		int value = data.readUnsigned3Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(3);
		return value;
	}

	@Override
	public void writeSigned3Bytes(int value) {
		data.writeSigned3Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(3);
	}
	
	@Override
	public void writeUnsigned3Bytes(int value) {
		data.writeUnsigned3Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(3);
	}

	@Override
	public int readSigned4Bytes() {
		int value = data.readSigned4Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(4);
		return value;
	}

	@Override
	public long readUnsigned4Bytes() {
		long value = data.readUnsigned4Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(4);
		return value;
	}

	@Override
	public void writeSigned4Bytes(int value) {
		data.writeSigned4Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(4);
	}
	
	@Override
	public void writeUnsigned4Bytes(long value) {
		data.writeUnsigned4Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(4);
	}

	@Override
	public long readSigned5Bytes() {
		long value = data.readSigned5Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(5);
		return value;
	}

	@Override
	public long readUnsigned5Bytes() {
		long value = data.readUnsigned5Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(5);
		return value;
	}

	@Override
	public void writeSigned5Bytes(long value) {
		data.writeSigned5Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(5);
	}
	
	@Override
	public void writeUnsigned5Bytes(long value) {
		data.writeUnsigned5Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(5);
	}

	@Override
	public long readSigned6Bytes() {
		long value = data.readSigned6Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(6);
		return value;
	}

	@Override
	public long readUnsigned6Bytes() {
		long value = data.readUnsigned6Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(6);
		return value;
	}

	@Override
	public void writeSigned6Bytes(long value) {
		data.writeSigned6Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(6);
	}
	
	@Override
	public void writeUnsigned6Bytes(long value) {
		data.writeUnsigned6Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(6);
	}

	@Override
	public long readSigned7Bytes() {
		long value = data.readSigned7Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(7);
		return value;
	}

	@Override
	public long readUnsigned7Bytes() {
		long value = data.readUnsigned7Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(7);
		return value;
	}

	@Override
	public void writeSigned7Bytes(long value) {
		data.writeSigned7Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(7);
	}
	
	@Override
	public void writeUnsigned7Bytes(long value) {
		data.writeUnsigned7Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(7);
	}

	@Override
	public long readSigned8Bytes() {
		long value = data.readSigned8Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(8);
		return value;
	}

	@Override
	public void writeSigned8Bytes(long value) {
		data.writeSigned8Bytes(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(8);
	}
	
}

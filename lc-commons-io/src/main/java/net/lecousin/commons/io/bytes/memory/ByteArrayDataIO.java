package net.lecousin.commons.io.bytes.memory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.IntBinaryOperator;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.data.BytesData;
import net.lecousin.commons.io.bytes.data.BytesDataIO;

/**
 * BytesDataIO from a byte array.
 */
// CHECKSTYLE DISABLE: MagicNumber
public class ByteArrayDataIO extends ByteArrayIO implements BytesDataIO.ReadWrite.Resizable {

	protected BytesData data;
	
	protected ByteArrayDataIO(ByteArray bytes, BytesData data, Optional<IntBinaryOperator> extensionStrategy) {
		super(bytes, extensionStrategy);
		this.data = data;
	}

	/**
	 * Constructor with a specified byte order.
	 * @param bytes byte array
	 * @param order byte order
	 */
	public ByteArrayDataIO(ByteArray bytes, ByteOrder order) {
		this(bytes, BytesData.of(order), Optional.empty());
	}

	/**
	 * Constructor with default Little-Endian order.
	 * @param bytes byte array
	 */
	public ByteArrayDataIO(ByteArray bytes) {
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

	// --- Readable ---
	
	protected <T extends Number> T readData(int nbBytes, BiFunction<byte[], Integer, T> reader) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		if (nbBytes > bytes.remaining()) throw new EOFException();
		T result = reader.apply(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition());
		bytes.moveForward(nbBytes);
		return result;
	}

	protected <T extends Number> T readDataAt(long pos, int nbBytes, BiFunction<byte[], Integer, T> reader) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		if (pos + nbBytes > bytes.getSize()) throw new EOFException();
		return reader.apply(bytes.getArray(), bytes.getArrayStartOffset() + (int) pos);
	}
	
	@Override
	public short readSigned2Bytes() throws IOException {
		return readData(2, data::readSigned2Bytes);
	}
	
	@Override
	public short readSigned2BytesAt(long pos) throws IOException {
		return readDataAt(pos, 2, data::readSigned2Bytes);
	}
	
	@Override
	public int readUnsigned2Bytes() throws IOException {
		return readData(2, data::readUnsigned2Bytes);
	}
	
	@Override
	public int readUnsigned2BytesAt(long pos) throws IOException {
		return readDataAt(pos, 2, data::readUnsigned2Bytes);
	}
	
	@Override
	public int readSigned3Bytes() throws IOException {
		return readData(3, data::readSigned3Bytes);
	}
	
	@Override
	public int readSigned3BytesAt(long pos) throws IOException {
		return readDataAt(pos, 3, data::readSigned3Bytes);
	}
	
	@Override
	public int readUnsigned3Bytes() throws IOException {
		return readData(3, data::readUnsigned3Bytes);
	}
	
	@Override
	public int readUnsigned3BytesAt(long pos) throws IOException {
		return readDataAt(pos, 3, data::readUnsigned3Bytes);
	}
	
	@Override
	public int readSigned4Bytes() throws IOException {
		return readData(4, data::readSigned4Bytes);
	}
	
	@Override
	public int readSigned4BytesAt(long pos) throws IOException {
		return readDataAt(pos, 4, data::readSigned4Bytes);
	}
	
	@Override
	public long readUnsigned4Bytes() throws IOException {
		return readData(4, data::readUnsigned4Bytes);
	}
	
	@Override
	public long readUnsigned4BytesAt(long pos) throws IOException {
		return readDataAt(pos, 4, data::readUnsigned4Bytes);
	}
	
	@Override
	public long readSigned5Bytes() throws IOException {
		return readData(5, data::readSigned5Bytes);
	}
	
	@Override
	public long readSigned5BytesAt(long pos) throws IOException {
		return readDataAt(pos, 5, data::readSigned5Bytes);
	}
	
	@Override
	public long readUnsigned5Bytes() throws IOException {
		return readData(5, data::readUnsigned5Bytes);
	}
	
	@Override
	public long readUnsigned5BytesAt(long pos) throws IOException {
		return readDataAt(pos, 5, data::readUnsigned5Bytes);
	}
	
	@Override
	public long readSigned6Bytes() throws IOException {
		return readData(6, data::readSigned6Bytes);
	}
	
	@Override
	public long readSigned6BytesAt(long pos) throws IOException {
		return readDataAt(pos, 6, data::readSigned6Bytes);
	}
	
	@Override
	public long readUnsigned6Bytes() throws IOException {
		return readData(6, data::readUnsigned6Bytes);
	}
	
	@Override
	public long readUnsigned6BytesAt(long pos) throws IOException {
		return readDataAt(pos, 6, data::readUnsigned6Bytes);
	}
	
	@Override
	public long readSigned7Bytes() throws IOException {
		return readData(7, data::readSigned7Bytes);
	}
	
	@Override
	public long readSigned7BytesAt(long pos) throws IOException {
		return readDataAt(pos, 7, data::readSigned7Bytes);
	}
	
	@Override
	public long readUnsigned7Bytes() throws IOException {
		return readData(7, data::readUnsigned7Bytes);
	}
	
	@Override
	public long readUnsigned7BytesAt(long pos) throws IOException {
		return readDataAt(pos, 7, data::readUnsigned7Bytes);
	}
	
	@Override
	public long readSigned8Bytes() throws IOException {
		return readData(8, data::readSigned8Bytes);
	}
	
	@Override
	public long readSigned8BytesAt(long pos) throws IOException {
		return readDataAt(pos, 8, data::readSigned8Bytes);
	}

	

	// --- Writable ---
	
	private interface DataWriter<T extends Number> {
		void write(byte[] buf, int off, T value);
	}
	
	protected <T extends Number> void writeData(int nbBytes, DataWriter<T> writer, T value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		if (nbBytes > bytes.remaining() && !extendCapacity((long) bytes.getPosition() + nbBytes)) throw new EOFException();
		writer.write(bytes.getArray(), bytes.getArrayStartOffset() + bytes.getPosition(), value);
		bytes.moveForward(nbBytes);
	}

	protected <T extends Number> void writeDataAt(long pos, int nbBytes, DataWriter<T> writer, T value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, IOChecks.FIELD_POS);
		if (pos + nbBytes > bytes.getSize() && !extendCapacity(pos + nbBytes)) throw new EOFException();
		writer.write(bytes.getArray(), bytes.getArrayStartOffset() + (int) pos, value);
	}
	
	@Override
	public void writeSigned2Bytes(short value) throws IOException {
		writeData(2, data::writeSigned2Bytes, value);
	}
	
	@Override
	public void writeSigned2BytesAt(long pos, short value) throws IOException {
		writeDataAt(pos, 2, data::writeSigned2Bytes, value);
	}
	
	@Override
	public void writeUnsigned2Bytes(int value) throws IOException {
		writeData(2, data::writeUnsigned2Bytes, value);
	}
	
	@Override
	public void writeUnsigned2BytesAt(long pos, int value) throws IOException {
		writeDataAt(pos, 2, data::writeUnsigned2Bytes, value);
	}
	
	@Override
	public void writeSigned3Bytes(int value) throws IOException {
		writeData(3, data::writeSigned3Bytes, value);
	}
	
	@Override
	public void writeSigned3BytesAt(long pos, int value) throws IOException {
		writeDataAt(pos, 3, data::writeSigned3Bytes, value);
	}
	
	@Override
	public void writeUnsigned3Bytes(int value) throws IOException {
		writeData(3, data::writeUnsigned3Bytes, value);
	}
	
	@Override
	public void writeUnsigned3BytesAt(long pos, int value) throws IOException {
		writeDataAt(pos, 3, data::writeUnsigned3Bytes, value);
	}
	
	@Override
	public void writeSigned4Bytes(int value) throws IOException {
		writeData(4, data::writeSigned4Bytes, value);
	}
	
	@Override
	public void writeSigned4BytesAt(long pos, int value) throws IOException {
		writeDataAt(pos, 4, data::writeSigned4Bytes, value);
	}
	
	@Override
	public void writeUnsigned4Bytes(long value) throws IOException {
		writeData(4, data::writeUnsigned4Bytes, value);
	}
	
	@Override
	public void writeUnsigned4BytesAt(long pos, long value) throws IOException {
		writeDataAt(pos, 4, data::writeUnsigned4Bytes, value);
	}

	@Override
	public void writeSigned5Bytes(long value) throws IOException {
		writeData(5, data::writeSigned5Bytes, value);
	}
	
	@Override
	public void writeSigned5BytesAt(long pos, long value) throws IOException {
		writeDataAt(pos, 5, data::writeSigned5Bytes, value);
	}
	
	@Override
	public void writeUnsigned5Bytes(long value) throws IOException {
		writeData(5, data::writeUnsigned5Bytes, value);
	}
	
	@Override
	public void writeUnsigned5BytesAt(long pos, long value) throws IOException {
		writeDataAt(pos, 5, data::writeUnsigned5Bytes, value);
	}

	@Override
	public void writeSigned6Bytes(long value) throws IOException {
		writeData(6, data::writeSigned6Bytes, value);
	}
	
	@Override
	public void writeSigned6BytesAt(long pos, long value) throws IOException {
		writeDataAt(pos, 6, data::writeSigned6Bytes, value);
	}
	
	@Override
	public void writeUnsigned6Bytes(long value) throws IOException {
		writeData(6, data::writeUnsigned6Bytes, value);
	}
	
	@Override
	public void writeUnsigned6BytesAt(long pos, long value) throws IOException {
		writeDataAt(pos, 6, data::writeUnsigned6Bytes, value);
	}

	@Override
	public void writeSigned7Bytes(long value) throws IOException {
		writeData(7, data::writeSigned7Bytes, value);
	}
	
	@Override
	public void writeSigned7BytesAt(long pos, long value) throws IOException {
		writeDataAt(pos, 7, data::writeSigned7Bytes, value);
	}
	
	@Override
	public void writeUnsigned7Bytes(long value) throws IOException {
		writeData(7, data::writeUnsigned7Bytes, value);
	}
	
	@Override
	public void writeUnsigned7BytesAt(long pos, long value) throws IOException {
		writeDataAt(pos, 7, data::writeUnsigned7Bytes, value);
	}

	@Override
	public void writeSigned8Bytes(long value) throws IOException {
		writeData(8, data::writeSigned8Bytes, value);
	}
	
	@Override
	public void writeSigned8BytesAt(long pos, long value) throws IOException {
		writeDataAt(pos, 8, data::writeSigned8Bytes, value);
	}

	/** Appendable ByteArrayDataIO. */
	public static class Appendable extends ByteArrayDataIO implements BytesIO.ReadWrite.AppendableResizable {
		
		/**
		 * Constructor.
		 * @param bytes byte array
		 * @param order byte order
		 * @param extensionStrategy take the current size and the additional requested size in parameter
		 *   and returns the new size to be allocated.
		 */
		public Appendable(ByteArray bytes, ByteOrder order, IntBinaryOperator extensionStrategy) {
			super(bytes, BytesData.of(order), Optional.of(extensionStrategy));
		}

		/**
		 * Constructor with default byte order Little-Endian.
		 * @param bytes byte array
		 * @param extensionStrategy take the current size and the additional requested size in parameter
		 *   and returns the new size to be allocated.
		 */
		public Appendable(ByteArray bytes, IntBinaryOperator extensionStrategy) {
			this(bytes, ByteOrder.LITTLE_ENDIAN, extensionStrategy);
		}
		
		/**
		 * Constructor.
		 * @param bytes byte array
		 * @param order byte order
		 * @param minimumAppendSize when appending bytes beyond the byte array size, the byte array is extended
		 *   of at least <code>appendMinimum</code> bytes, or the requested additional bytes if greater than
		 *   <code>appendMinimum</code>.
		 */
		public Appendable(ByteArray bytes, ByteOrder order, int minimumAppendSize) {
			this(bytes, order, ByteArrayIO.Appendable.extensionStrategyWithMinimumAppendSize(minimumAppendSize));
		}

		/**
		 * Constructor with default byte order Little-Endian.
		 * @param bytes byte array
		 * @param minimumAppendSize when appending bytes beyond the byte array size, the byte array is extended
		 *   of at least <code>appendMinimum</code> bytes, or the requested additional bytes if greater than
		 *   <code>appendMinimum</code>.
		 */
		public Appendable(ByteArray bytes, int minimumAppendSize) {
			this(bytes, ByteOrder.LITTLE_ENDIAN, minimumAppendSize);
		}
		
		/**
		 * Constructor with default extension strategy.
		 * @param bytes byte array
		 * @param order byte order
		 */
		public Appendable(ByteArray bytes, ByteOrder order) {
			this(bytes, order, ByteArrayIO.Appendable.DEFAULT_EXTENSION_STRATEGY);
		}

		/**
		 * Constructor with default extension strategy and byte order Little-Endian.
		 * @param bytes byte array
		 */
		public Appendable(ByteArray bytes) {
			this(bytes, ByteOrder.LITTLE_ENDIAN);
		}
		
	}
	
}

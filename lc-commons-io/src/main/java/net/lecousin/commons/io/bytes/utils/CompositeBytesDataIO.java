package net.lecousin.commons.io.bytes.utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableBiFunction;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.TriConsumer;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.function.FailableTriConsumer;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.data.BytesData;
import net.lecousin.commons.io.bytes.data.BytesDataIO;

/** BytesDataIO aggregating of multiple IOs. */
public interface CompositeBytesDataIO {

	/** Create a CompositeBytesDataIO Read and Write.
	 * 
	 * @param <T> type of IO
	 * @param ios list of IOs, possibly empty
	 * @param order byte order
	 * @param closeIosOnClose if true, when this CompositeBytesIO is closed, the underlying IOs will also be closed 
	 * @return a BytesDataIO.ReadWrite
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	static <T extends BytesDataIO.Readable.Seekable & BytesDataIO.Writable.Seekable>
	CompositeBytesDataIO.ReadWrite fromReadWrite(List<? extends T> ios, ByteOrder order, boolean closeIosOnClose) throws IOException {
		return new ReadWrite(ios, order, closeIosOnClose, false);
	}
	
	/** Create a CompositeBytesDataIO Read-only and Seekable.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param order byte order
	 * @param closeIosOnClose if true, when this CompositeBytesIO is closed, the underlying IOs will also be closed
	 * @return a BytesDataIO.Readable.Seekable 
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends BytesDataIO.Readable.Seekable> BytesDataIO.Readable.Seekable fromReadableSeekable(List<? extends T> ios, ByteOrder order, boolean closeIosOnClose) throws IOException {
		return new ReadWrite(ios, order, closeIosOnClose, false).asReadableSeekableBytesDataIO();
	}
	
	/** Create a CompositeBytesDataIO Read-only.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param order byte order
	 * @param closeIosOnClose if true, when this CompositeBytesIO is closed, the underlying IOs will also be closed
	 * @param garbageIoOnConsumed if true, once an IO is completely read, the reference to the IO will be removed so the garbage collector can
	 *   free memory linked to it. If closeIosOnClose is also true, the IO is closed before to remove the reference.
	 * @return a BytesDataIO.Readable
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends BytesDataIO.Readable> BytesDataIO.Readable fromReadable(List<? extends T> ios, ByteOrder order, boolean closeIosOnClose, boolean garbageIoOnConsumed) throws IOException {
		return new ReadWrite(ios, order, closeIosOnClose, garbageIoOnConsumed).asReadableBytesDataIO();
	}
	
	/** Create a CompositeBytesDataIO Write-only Seekable.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param order byte order
	 * @param closeIosOnClose if true, when this CompositeBytesIO is closed, the underlying IOs will also be closed
	 * @return a BytesDataIO.Writable.Seekable 
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends BytesDataIO.Writable.Seekable> BytesDataIO.Writable.Seekable fromWritableSeekable(List<? extends T> ios, ByteOrder order, boolean closeIosOnClose) throws IOException {
		return new ReadWrite(ios, order, closeIosOnClose, false).asWritableSeekableBytesDataIO();
	}
	
	/** Create a CompositeBytesDataIO Write-only.
	 * 
	 * @param <T> type of IO
	 * @param ios IOs
	 * @param order byte order
	 * @param closeIosOnClose if true, when this CompositeBytesIO is closed, the underlying IOs will also be closed
	 * @param garbageIoOnConsumed if true, once an IO is completely filled, the reference to the IO will be removed so the garbage collector can
	 *   free memory linked to it. If closeIosOnClose is also true, the IO is closed before to remove the reference.
	 * @return a BytesDataIO.Writable 
	 * @throws IOException if at least one IO implements IO.KnownSize and an error occurred when trying to get its size
	 */
	@SuppressWarnings("resource")
	static <T extends BytesDataIO.Writable> BytesDataIO.Writable fromWritable(List<? extends T> ios, ByteOrder order, boolean closeIosOnClose, boolean garbageIoOnConsumed) throws IOException {
		return new ReadWrite(ios, order, closeIosOnClose, garbageIoOnConsumed).asWritableBytesDataIO();
	}



	// CHECKSTYLE DISABLE: MagicNumber
	
	/** Read-Write implementation. */
	class ReadWrite extends CompositeBytesIO.ReadWrite implements BytesDataIO.ReadWrite {
		
		private ByteOrder order;
		
		protected ReadWrite(List<? extends BytesDataIO> ios, ByteOrder order, boolean closeIosOnClose, boolean garbageIoOnConsumed) throws IOException {
			super(ios, closeIosOnClose, garbageIoOnConsumed);
			setByteOrder(order);
		}
		
		@Override
		public void setByteOrder(ByteOrder byteOrder) {
			this.order = byteOrder;
			for (Element e = head; e != null; e = e.next)
				((BytesDataIO) e.io).setByteOrder(order);
		}
		
		@Override
		public ByteOrder getByteOrder() {
			return order;
		}
		
		private <T extends Number> T readData(int nbBytes, FailableFunction<BytesDataIO.Readable, T, IOException> ioReader, BiFunction<BytesData, byte[], T> dataReader) throws IOException {
			if (cursor == null) throw new EOFException();
			if (cursor.size != -1 && cursor.ioPosition + nbBytes <= cursor.size) {
				// we can perform the operation on the io
				T value = ioReader.apply((BytesDataIO.Readable) cursor.io);
				position += nbBytes;
				cursor.ioPosition += nbBytes;
				if (cursor.ioPosition == cursor.size) {
					moveNext();
				}
				return value;
			}
			// we cannot perform the operation directly
			byte[] b = new byte[nbBytes];
			readBytesFully(b);
			return dataReader.apply(BytesData.of(order), b);
		}
		
		private <T extends Number> T readDataAt(
			int nbBytes, long pos, FailableBiFunction<BytesDataIO.Readable.Seekable, Long, T, IOException> ioReader, BiFunction<BytesData, byte[], T> dataReader
		) throws IOException {
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos >= size) throw new EOFException();
			Element e = getElementForPosition(pos);
			if (pos - e.startPosition + nbBytes <= e.size) {
				// we can perform the operation on the io
				return ioReader.apply((BytesDataIO.Readable.Seekable) e.io, pos - e.startPosition);
			}
			// we cannot perform the operation directly
			byte[] b = new byte[nbBytes];
			readBytesFullyAt(pos, b);
			return dataReader.apply(BytesData.of(order), b);
		}
		
		@Override
		public short readSigned2Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(2, BytesDataIO.Readable::readSigned2Bytes, BytesData::readSigned2Bytes);
		}
		
		@Override
		public short readSigned2BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(2, pos, BytesDataIO.Readable.Seekable::readSigned2BytesAt, BytesData::readSigned2Bytes);
		}
		
		@Override
		public int readUnsigned2Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(2, BytesDataIO.Readable::readUnsigned2Bytes, BytesData::readUnsigned2Bytes);
		}
		
		@Override
		public int readUnsigned2BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(2, pos, BytesDataIO.Readable.Seekable::readUnsigned2BytesAt, BytesData::readUnsigned2Bytes);
		}
		
		@Override
		public int readSigned3Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(3, BytesDataIO.Readable::readSigned3Bytes, BytesData::readSigned3Bytes);
		}
		
		@Override
		public int readSigned3BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(3, pos, BytesDataIO.Readable.Seekable::readSigned3BytesAt, BytesData::readSigned3Bytes);
		}
		
		@Override
		public int readUnsigned3Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(3, BytesDataIO.Readable::readUnsigned3Bytes, BytesData::readUnsigned3Bytes);
		}
		
		@Override
		public int readUnsigned3BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(3, pos, BytesDataIO.Readable.Seekable::readUnsigned3BytesAt, BytesData::readUnsigned3Bytes);
		}
		
		@Override
		public int readSigned4Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(4, BytesDataIO.Readable::readSigned4Bytes, BytesData::readSigned4Bytes);
		}
		
		@Override
		public int readSigned4BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(4, pos, BytesDataIO.Readable.Seekable::readSigned4BytesAt, BytesData::readSigned4Bytes);
		}
		
		@Override
		public long readUnsigned4Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(4, BytesDataIO.Readable::readUnsigned4Bytes, BytesData::readUnsigned4Bytes);
		}
		
		@Override
		public long readUnsigned4BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(4, pos, BytesDataIO.Readable.Seekable::readUnsigned4BytesAt, BytesData::readUnsigned4Bytes);
		}
		
		@Override
		public long readSigned5Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(5, BytesDataIO.Readable::readSigned5Bytes, BytesData::readSigned5Bytes);
		}
		
		@Override
		public long readSigned5BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(5, pos, BytesDataIO.Readable.Seekable::readSigned5BytesAt, BytesData::readSigned5Bytes);
		}
		
		@Override
		public long readUnsigned5Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(5, BytesDataIO.Readable::readUnsigned5Bytes, BytesData::readUnsigned5Bytes);
		}
		
		@Override
		public long readUnsigned5BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(5, pos, BytesDataIO.Readable.Seekable::readUnsigned5BytesAt, BytesData::readUnsigned5Bytes);
		}
		
		@Override
		public long readSigned6Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(6, BytesDataIO.Readable::readSigned6Bytes, BytesData::readSigned6Bytes);
		}
		
		@Override
		public long readSigned6BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(6, pos, BytesDataIO.Readable.Seekable::readSigned6BytesAt, BytesData::readSigned6Bytes);
		}
		
		@Override
		public long readUnsigned6Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(6, BytesDataIO.Readable::readUnsigned6Bytes, BytesData::readUnsigned6Bytes);
		}
		
		@Override
		public long readUnsigned6BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(6, pos, BytesDataIO.Readable.Seekable::readUnsigned6BytesAt, BytesData::readUnsigned6Bytes);
		}
		
		@Override
		public long readSigned7Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(7, BytesDataIO.Readable::readSigned7Bytes, BytesData::readSigned7Bytes);
		}
		
		@Override
		public long readSigned7BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(7, pos, BytesDataIO.Readable.Seekable::readSigned7BytesAt, BytesData::readSigned7Bytes);
		}
		
		@Override
		public long readUnsigned7Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(7, BytesDataIO.Readable::readUnsigned7Bytes, BytesData::readUnsigned7Bytes);
		}
		
		@Override
		public long readUnsigned7BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(7, pos, BytesDataIO.Readable.Seekable::readUnsigned7BytesAt, BytesData::readUnsigned7Bytes);
		}
		
		@Override
		public long readSigned8Bytes() throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readData(8, BytesDataIO.Readable::readSigned8Bytes, BytesData::readSigned8Bytes);
		}
		
		@Override
		public long readSigned8BytesAt(long pos) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			return readDataAt(8, pos, BytesDataIO.Readable.Seekable::readSigned8BytesAt, BytesData::readSigned8Bytes);
		}
		

		
		
		private <T extends Number> void writeData(
			int nbBytes, T value, FailableBiConsumer<BytesDataIO.Writable, T, IOException> ioWriter, TriConsumer<BytesData, byte[], T> dataWriter
		) throws IOException {
			if (cursor == null) throw new EOFException();
			if (cursor.size != -1 && cursor.ioPosition + nbBytes <= cursor.size) {
				// we can perform the operation on the io
				ioWriter.accept((BytesDataIO.Writable) cursor.io, value);
				position += nbBytes;
				cursor.ioPosition += nbBytes;
				if (cursor.ioPosition == cursor.size) {
					moveNext();
				}
				return;
			}
			// we cannot perform the operation directly
			byte[] b = new byte[nbBytes];
			dataWriter.accept(BytesData.of(order), b, value);
			writeBytesFully(b);
		}
		
		private <T extends Number> void writeDataAt(
			int nbBytes, long pos, T value, FailableTriConsumer<BytesDataIO.Writable.Seekable, Long, T, IOException> ioWriter, TriConsumer<BytesData, byte[], T> dataWriter
		) throws IOException {
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos >= size) throw new EOFException();
			Element e = getElementForPosition(pos);
			if (pos - e.startPosition + nbBytes <= e.size) {
				// we can perform the operation on the io
				ioWriter.accept((BytesDataIO.Writable.Seekable) e.io, pos - e.startPosition, value);
				return;
			}
			// we cannot perform the operation directly
			byte[] b = new byte[nbBytes];
			dataWriter.accept(BytesData.of(order), b, value);
			writeBytesFullyAt(pos, b);
		}
		
		@Override
		public void writeSigned2Bytes(short value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(2, value, BytesDataIO.Writable::writeSigned2Bytes, BytesData::writeSigned2Bytes);
		}
		
		@Override
		public void writeSigned2BytesAt(long pos, short value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(2, pos, value, BytesDataIO.Writable.Seekable::writeSigned2BytesAt, BytesData::writeSigned2Bytes);
		}
		
		@Override
		public void writeUnsigned2Bytes(int value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(2, value, BytesDataIO.Writable::writeUnsigned2Bytes, BytesData::writeUnsigned2Bytes);
		}
		
		@Override
		public void writeUnsigned2BytesAt(long pos, int value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(2, pos, value, BytesDataIO.Writable.Seekable::writeUnsigned2BytesAt, BytesData::writeUnsigned2Bytes);
		}
		
		@Override
		public void writeSigned3Bytes(int value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(3, value, BytesDataIO.Writable::writeSigned3Bytes, BytesData::writeSigned3Bytes);
		}
		
		@Override
		public void writeSigned3BytesAt(long pos, int value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(3, pos, value, BytesDataIO.Writable.Seekable::writeSigned3BytesAt, BytesData::writeSigned3Bytes);
		}
		
		@Override
		public void writeUnsigned3Bytes(int value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(3, value, BytesDataIO.Writable::writeUnsigned3Bytes, BytesData::writeUnsigned3Bytes);
		}
		
		@Override
		public void writeUnsigned3BytesAt(long pos, int value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(3, pos, value, BytesDataIO.Writable.Seekable::writeUnsigned3BytesAt, BytesData::writeUnsigned3Bytes);
		}
		
		@Override
		public void writeSigned4Bytes(int value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(4, value, BytesDataIO.Writable::writeSigned4Bytes, BytesData::writeSigned4Bytes);
		}
		
		@Override
		public void writeSigned4BytesAt(long pos, int value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(4, pos, value, BytesDataIO.Writable.Seekable::writeSigned4BytesAt, BytesData::writeSigned4Bytes);
		}
		
		@Override
		public void writeUnsigned4Bytes(long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(4, value, BytesDataIO.Writable::writeUnsigned4Bytes, BytesData::writeUnsigned4Bytes);
		}
		
		@Override
		public void writeUnsigned4BytesAt(long pos, long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(4, pos, value, BytesDataIO.Writable.Seekable::writeUnsigned4BytesAt, BytesData::writeUnsigned4Bytes);
		}
		
		@Override
		public void writeSigned5Bytes(long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(5, value, BytesDataIO.Writable::writeSigned5Bytes, BytesData::writeSigned5Bytes);
		}
		
		@Override
		public void writeSigned5BytesAt(long pos, long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(5, pos, value, BytesDataIO.Writable.Seekable::writeSigned5BytesAt, BytesData::writeSigned5Bytes);
		}
		
		@Override
		public void writeUnsigned5Bytes(long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(5, value, BytesDataIO.Writable::writeUnsigned5Bytes, BytesData::writeUnsigned5Bytes);
		}
		
		@Override
		public void writeUnsigned5BytesAt(long pos, long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(5, pos, value, BytesDataIO.Writable.Seekable::writeUnsigned5BytesAt, BytesData::writeUnsigned5Bytes);
		}
		
		@Override
		public void writeSigned6Bytes(long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(6, value, BytesDataIO.Writable::writeSigned6Bytes, BytesData::writeSigned6Bytes);
		}
		
		@Override
		public void writeSigned6BytesAt(long pos, long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(6, pos, value, BytesDataIO.Writable.Seekable::writeSigned6BytesAt, BytesData::writeSigned6Bytes);
		}
		
		@Override
		public void writeUnsigned6Bytes(long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(6, value, BytesDataIO.Writable::writeUnsigned6Bytes, BytesData::writeUnsigned6Bytes);
		}
		
		@Override
		public void writeUnsigned6BytesAt(long pos, long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(6, pos, value, BytesDataIO.Writable.Seekable::writeUnsigned6BytesAt, BytesData::writeUnsigned6Bytes);
		}
		
		@Override
		public void writeSigned7Bytes(long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(7, value, BytesDataIO.Writable::writeSigned7Bytes, BytesData::writeSigned7Bytes);
		}
		
		@Override
		public void writeSigned7BytesAt(long pos, long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(7, pos, value, BytesDataIO.Writable.Seekable::writeSigned7BytesAt, BytesData::writeSigned7Bytes);
		}
		
		@Override
		public void writeUnsigned7Bytes(long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(7, value, BytesDataIO.Writable::writeUnsigned7Bytes, BytesData::writeUnsigned7Bytes);
		}
		
		@Override
		public void writeUnsigned7BytesAt(long pos, long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(7, pos, value, BytesDataIO.Writable.Seekable::writeUnsigned7BytesAt, BytesData::writeUnsigned7Bytes);
		}
		
		@Override
		public void writeSigned8Bytes(long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeData(8, value, BytesDataIO.Writable::writeSigned8Bytes, BytesData::writeSigned8Bytes);
		}
		
		@Override
		public void writeSigned8BytesAt(long pos, long value) throws IOException {
			if (isClosed()) throw new ClosedChannelException();
			writeDataAt(8, pos, value, BytesDataIO.Writable.Seekable::writeSigned8BytesAt, BytesData::writeSigned8Bytes);
		}
		
	}
	
}

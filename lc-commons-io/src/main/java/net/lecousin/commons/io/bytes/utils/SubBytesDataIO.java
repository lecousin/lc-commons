package net.lecousin.commons.io.bytes.utils;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableFunction;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOChecks;
import net.lecousin.commons.io.bytes.data.BytesDataIO;

/**
 * Sub-part of a seekable BytesDataIO.
 */
public interface SubBytesDataIO {
	
	/** Create a SubBytesDataIO Read and Write.
	 * 
	 * @param <T> type of IO
	 * @param io IO
	 * @param start start offset in the IO
	 * @param size size of the sub-io
	 * @param closeIoOnClose if true, when this SubIO is closed, the underlying IO will also be closed 
	 * @return a BytesDataIO.ReadWrite corresponding to the requested slice 
	 */
	static <T extends BytesDataIO.Readable.Seekable & BytesDataIO.Writable.Seekable> SubBytesDataIO.ReadWrite fromReadWrite(T io, long start, long size, boolean closeIoOnClose) {
		return new ReadWrite(io, start, size, closeIoOnClose);
	}
	
	/** Create a SubBytesDataIO Read-only.
	 * 
	 * @param <T> type of IO
	 * @param io IO
	 * @param start start offset in the IO
	 * @param size size of the sub-io
	 * @param closeIoOnClose if true, when this SubIO is closed, the underlying IO will also be closed 
	 * @return a BytesDataIO.Readable.Seekable corresponding to the requested slice 
	 */
	@SuppressWarnings("resource")
	static <T extends BytesDataIO.Readable.Seekable> BytesDataIO.Readable.Seekable fromReadable(T io, long start, long size, boolean closeIoOnClose) {
		return new ReadWrite(io, start, size, closeIoOnClose).asReadableSeekableBytesDataIO();
	}
	
	/** Create a SubBytesDataIO Write-only.
	 * 
	 * @param <T> type of IO
	 * @param io IO
	 * @param start start offset in the IO
	 * @param size size of the sub-io
	 * @param closeIoOnClose if true, when this SubIO is closed, the underlying IO will also be closed 
	 * @return a BytesDataIO.Writable.Seekable corresponding to the requested slice 
	 */
	@SuppressWarnings("resource")
	static <T extends BytesDataIO.Writable.Seekable> BytesDataIO.Writable.Seekable fromWritable(T io, long start, long size, boolean closeIoOnClose) {
		return new ReadWrite(io, start, size, closeIoOnClose).asWritableSeekableBytesDataIO();
	}


	// CHECKSTYLE DISABLE: MagicNumber
	
	/** Read-Write implementation. */
	class ReadWrite extends SubBytesIO.ReadWrite implements BytesDataIO.ReadWrite {
		
		ReadWrite(BytesDataIO io, long start, long size, boolean closeIoOnClose) {
			super(io, start, size, closeIoOnClose);
		}
		
		@Override
		public ByteOrder getByteOrder() {
			return ((BytesDataIO) io).getByteOrder();
		}
		
		@Override
		public void setByteOrder(ByteOrder order) {
			((BytesDataIO) io).setByteOrder(order);
		}
		
		private <T extends Number> T readData(int nbBytes, FailableFunction<Long, T, IOException> reader) throws IOException {
			if (position > size - nbBytes) throw new EOFException();
			T value = reader.apply(start + position);
			position += nbBytes;
			return value;
		}
		
		private <T extends Number> T readDataAt(int nbBytes, long pos, FailableFunction<Long, T, IOException> reader) throws IOException {
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos > size - nbBytes) throw new EOFException();
			return reader.apply(start + pos);
		}
		
		@Override
		public short readSigned2Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(2, ((BytesDataIO.Readable.Seekable) io)::readSigned2BytesAt);
		}
		
		@Override
		public short readSigned2BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(2, pos, ((BytesDataIO.Readable.Seekable) io)::readSigned2BytesAt);
		}
		
		@Override
		public int readUnsigned2Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(2, ((BytesDataIO.Readable.Seekable) io)::readUnsigned2BytesAt);
		}
		
		@Override
		public int readUnsigned2BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(2, pos, ((BytesDataIO.Readable.Seekable) io)::readUnsigned2BytesAt);
		}
		
		@Override
		public int readSigned3Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(3, ((BytesDataIO.Readable.Seekable) io)::readSigned3BytesAt);
		}
		
		@Override
		public int readSigned3BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(3, pos, ((BytesDataIO.Readable.Seekable) io)::readSigned3BytesAt);
		}
		
		@Override
		public int readUnsigned3Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(3, ((BytesDataIO.Readable.Seekable) io)::readUnsigned3BytesAt);
		}
		
		@Override
		public int readUnsigned3BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(3, pos, ((BytesDataIO.Readable.Seekable) io)::readUnsigned3BytesAt);
		}
		
		@Override
		public int readSigned4Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(4, ((BytesDataIO.Readable.Seekable) io)::readSigned4BytesAt);
		}
		
		@Override
		public int readSigned4BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(4, pos, ((BytesDataIO.Readable.Seekable) io)::readSigned4BytesAt);
		}
		
		@Override
		public long readUnsigned4Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(4, ((BytesDataIO.Readable.Seekable) io)::readUnsigned4BytesAt);
		}
		
		@Override
		public long readUnsigned4BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(4, pos, ((BytesDataIO.Readable.Seekable) io)::readUnsigned4BytesAt);
		}
		
		@Override
		public long readSigned5Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(5, ((BytesDataIO.Readable.Seekable) io)::readSigned5BytesAt);
		}
		
		@Override
		public long readSigned5BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(5, pos, ((BytesDataIO.Readable.Seekable) io)::readSigned5BytesAt);
		}
		
		@Override
		public long readUnsigned5Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(5, ((BytesDataIO.Readable.Seekable) io)::readUnsigned5BytesAt);
		}
		
		@Override
		public long readUnsigned5BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(5, pos, ((BytesDataIO.Readable.Seekable) io)::readUnsigned5BytesAt);
		}
		
		@Override
		public long readSigned6Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(6, ((BytesDataIO.Readable.Seekable) io)::readSigned6BytesAt);
		}
		
		@Override
		public long readSigned6BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(6, pos, ((BytesDataIO.Readable.Seekable) io)::readSigned6BytesAt);
		}
		
		@Override
		public long readUnsigned6Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(6, ((BytesDataIO.Readable.Seekable) io)::readUnsigned6BytesAt);
		}
		
		@Override
		public long readUnsigned6BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(6, pos, ((BytesDataIO.Readable.Seekable) io)::readUnsigned6BytesAt);
		}
		
		@Override
		public long readSigned7Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(7, ((BytesDataIO.Readable.Seekable) io)::readSigned7BytesAt);
		}
		
		@Override
		public long readSigned7BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(7, pos, ((BytesDataIO.Readable.Seekable) io)::readSigned7BytesAt);
		}
		
		@Override
		public long readUnsigned7Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(7, ((BytesDataIO.Readable.Seekable) io)::readUnsigned7BytesAt);
		}
		
		@Override
		public long readUnsigned7BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(7, pos, ((BytesDataIO.Readable.Seekable) io)::readUnsigned7BytesAt);
		}
		
		@Override
		public long readSigned8Bytes() throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readData(8, ((BytesDataIO.Readable.Seekable) io)::readSigned8BytesAt);
		}
		
		@Override
		public long readSigned8BytesAt(long pos) throws IOException {
			if (io == null) throw new ClosedChannelException();
			return readDataAt(8, pos, ((BytesDataIO.Readable.Seekable) io)::readSigned8BytesAt);
		}
		

		private <T extends Number> void writeData(int nbBytes, T value, FailableBiConsumer<Long, T, IOException> writer) throws IOException {
			if (position > size - nbBytes) throw new EOFException();
			writer.accept(start + position, value);
			position += nbBytes;
		}

		private <T extends Number> void writeDataAt(int nbBytes, T value, long pos, FailableBiConsumer<Long, T, IOException> writer) throws IOException {
			NegativeValueException.check(pos, IOChecks.FIELD_POS);
			if (pos > size - nbBytes) throw new EOFException();
			writer.accept(start + pos, value);
		}
		
		@Override
		public void writeSigned2Bytes(short value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(2, value, ((BytesDataIO.Writable.Seekable) io)::writeSigned2BytesAt);
		}
		
		@Override
		public void writeSigned2BytesAt(long pos, short value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(2, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeSigned2BytesAt);
		}
		
		@Override
		public void writeUnsigned2Bytes(int value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(2, value, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned2BytesAt);
		}
		
		@Override
		public void writeUnsigned2BytesAt(long pos, int value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(2, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned2BytesAt);
		}
		
		@Override
		public void writeSigned3Bytes(int value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(3, value, ((BytesDataIO.Writable.Seekable) io)::writeSigned3BytesAt);
		}
		
		@Override
		public void writeSigned3BytesAt(long pos, int value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(3, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeSigned3BytesAt);
		}
		
		@Override
		public void writeUnsigned3Bytes(int value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(3, value, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned3BytesAt);
		}
		
		@Override
		public void writeUnsigned3BytesAt(long pos, int value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(3, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned3BytesAt);
		}
		
		@Override
		public void writeSigned4Bytes(int value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(4, value, ((BytesDataIO.Writable.Seekable) io)::writeSigned4BytesAt);
		}
		
		@Override
		public void writeSigned4BytesAt(long pos, int value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(4, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeSigned4BytesAt);
		}
		
		@Override
		public void writeUnsigned4Bytes(long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(4, value, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned4BytesAt);
		}
		
		@Override
		public void writeUnsigned4BytesAt(long pos, long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(4, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned4BytesAt);
		}
		
		@Override
		public void writeSigned5Bytes(long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(5, value, ((BytesDataIO.Writable.Seekable) io)::writeSigned5BytesAt);
		}
		
		@Override
		public void writeSigned5BytesAt(long pos, long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(5, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeSigned5BytesAt);
		}
		
		@Override
		public void writeUnsigned5Bytes(long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(5, value, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned5BytesAt);
		}
		
		@Override
		public void writeUnsigned5BytesAt(long pos, long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(5, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned5BytesAt);
		}
		
		@Override
		public void writeSigned6Bytes(long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(6, value, ((BytesDataIO.Writable.Seekable) io)::writeSigned6BytesAt);
		}
		
		@Override
		public void writeSigned6BytesAt(long pos, long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(6, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeSigned6BytesAt);
		}
		
		@Override
		public void writeUnsigned6Bytes(long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(6, value, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned6BytesAt);
		}
		
		@Override
		public void writeUnsigned6BytesAt(long pos, long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(6, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned6BytesAt);
		}
		
		@Override
		public void writeSigned7Bytes(long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(7, value, ((BytesDataIO.Writable.Seekable) io)::writeSigned7BytesAt);
		}
		
		@Override
		public void writeSigned7BytesAt(long pos, long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(7, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeSigned7BytesAt);
		}
		
		@Override
		public void writeUnsigned7Bytes(long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(7, value, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned7BytesAt);
		}
		
		@Override
		public void writeUnsigned7BytesAt(long pos, long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(7, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeUnsigned7BytesAt);
		}
		
		@Override
		public void writeSigned8Bytes(long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeData(8, value, ((BytesDataIO.Writable.Seekable) io)::writeSigned8BytesAt);
		}
		
		@Override
		public void writeSigned8BytesAt(long pos, long value) throws IOException {
			if (io == null) throw new ClosedChannelException();
			writeDataAt(8, value, pos, ((BytesDataIO.Writable.Seekable) io)::writeSigned8BytesAt);
		}
		
	}
	
}

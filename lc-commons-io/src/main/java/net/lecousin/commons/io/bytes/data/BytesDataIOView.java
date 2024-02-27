package net.lecousin.commons.io.bytes.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Optional;

import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IOView;

/**
 * Sub-view of a BytesDataIO.
 * @param <T> type of BytesDataIO
 */
public abstract class BytesDataIOView<T extends BytesDataIO> extends IOView<T> {

	protected BytesDataIOView(T io) {
		super(io);
	}
	
	/** Readable view of a BytesDataIO. */
	public static class Readable extends BytesDataIOView<BytesDataIO.Readable> implements BytesDataIO.Readable {
		
		/**
		 * Constructor.
		 * @param io IO to wrap
		 */
		public Readable(BytesDataIO.Readable io) {
			super(io);
		}

		@Override
		public byte readByte() throws IOException {
			return io.readByte();
		}

		@Override
		public int readBytes(ByteBuffer buffer) throws IOException {
			return io.readBytes(buffer);
		}

		@Override
		public int readBytes(byte[] buf, int off, int len) throws IOException {
			return io.readBytes(buf, off, len);
		}

		@Override
		public int readBytes(byte[] buf) throws IOException {
			return io.readBytes(buf);
		}

		@Override
		public Optional<ByteBuffer> readBuffer() throws IOException {
			return io.readBuffer();
		}

		@Override
		public void readBytesFully(ByteBuffer buffer) throws IOException {
			io.readBytesFully(buffer);
		}

		@Override
		public void readBytesFully(byte[] buf, int off, int len) throws IOException {
			io.readBytesFully(buf, off, len);
		}

		@Override
		public void readBytesFully(byte[] buf) throws IOException {
			io.readBytesFully(buf);
		}

		@Override
		public long skipUpTo(long toSkip) throws IOException {
			return io.skipUpTo(toSkip);
		}

		@Override
		public void skipFully(long toSkip) throws IOException {
			io.skipFully(toSkip);
		}
		
		@Override
		public ByteOrder getByteOrder() {
			return io.getByteOrder();
		}

		@Override
		public void setByteOrder(ByteOrder order) {
			io.setByteOrder(order);
		}

		@Override
		public int readUnsignedByte() throws IOException {
			return io.readUnsignedByte();
		}

		@Override
		public int readUnsigned2Bytes() throws IOException {
			return io.readUnsigned2Bytes();
		}

		@Override
		public short readSigned2Bytes() throws IOException {
			return io.readSigned2Bytes();
		}

		@Override
		public short readShort() throws IOException {
			return io.readShort();
		}

		@Override
		public int readUnsigned3Bytes() throws IOException {
			return io.readUnsigned3Bytes();
		}

		@Override
		public int readSigned3Bytes() throws IOException {
			return io.readSigned3Bytes();
		}

		@Override
		public long readUnsigned4Bytes() throws IOException {
			return io.readUnsigned4Bytes();
		}

		@Override
		public int readSigned4Bytes() throws IOException {
			return io.readSigned4Bytes();
		}

		@Override
		public int readInteger() throws IOException {
			return io.readInteger();
		}

		@Override
		public long readUnsigned5Bytes() throws IOException {
			return io.readUnsigned5Bytes();
		}

		@Override
		public long readSigned5Bytes() throws IOException {
			return io.readSigned5Bytes();
		}

		@Override
		public long readUnsigned6Bytes() throws IOException {
			return io.readUnsigned6Bytes();
		}

		@Override
		public long readSigned6Bytes() throws IOException {
			return io.readSigned6Bytes();
		}

		@Override
		public long readUnsigned7Bytes() throws IOException {
			return io.readUnsigned7Bytes();
		}

		@Override
		public long readSigned7Bytes() throws IOException {
			return io.readSigned7Bytes();
		}

		@Override
		public long readSigned8Bytes() throws IOException {
			return io.readSigned8Bytes();
		}

		@Override
		public long readLong() throws IOException {
			return io.readLong();
		}

		@Override
		public long readUnsignedBytes(int nbBytes) throws IOException {
			return io.readUnsignedBytes(nbBytes);
		}

		@Override
		public long readSignedBytes(int nbBytes) throws IOException {
			return io.readSignedBytes(nbBytes);
		}


		/** Readable and Seekable view of a BytesDataIO. */
		public static class Seekable extends BytesDataIOView<BytesDataIO.Readable.Seekable> implements BytesDataIO.Readable.Seekable {
			
			/**
			 * Constructor.
			 * @param io IO to wrap
			 */
			public Seekable(BytesDataIO.Readable.Seekable io) {
				super(io);
			}
			
			@Override
			public byte readByte() throws IOException {
				return io.readByte();
			}

			@Override
			public int readBytes(ByteBuffer buffer) throws IOException {
				return io.readBytes(buffer);
			}

			@Override
			public int readBytes(byte[] buf, int off, int len) throws IOException {
				return io.readBytes(buf, off, len);
			}

			@Override
			public int readBytes(byte[] buf) throws IOException {
				return io.readBytes(buf);
			}

			@Override
			public Optional<ByteBuffer> readBuffer() throws IOException {
				return io.readBuffer();
			}

			@Override
			public void readBytesFully(ByteBuffer buffer) throws IOException {
				io.readBytesFully(buffer);
			}

			@Override
			public void readBytesFully(byte[] buf, int off, int len) throws IOException {
				io.readBytesFully(buf, off, len);
			}

			@Override
			public void readBytesFully(byte[] buf) throws IOException {
				io.readBytesFully(buf);
			}

			@Override
			public long skipUpTo(long toSkip) throws IOException {
				return io.skipUpTo(toSkip);
			}

			@Override
			public void skipFully(long toSkip) throws IOException {
				io.skipFully(toSkip);
			}
			
			@Override
			public long size() throws IOException {
				return io.size();
			}

			@Override
			public long position() throws IOException {
				return io.position();
			}

			@Override
			public long seek(SeekFrom from, long offset) throws IOException {
				return io.seek(from, offset);
			}

			@Override
			public byte readByteAt(long pos) throws IOException {
				return io.readByteAt(pos);
			}

			@Override
			public int readBytesAt(long pos, ByteBuffer buffer) throws IOException {
				return io.readBytesAt(pos, buffer);
			}

			@Override
			public int readBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
				return io.readBytesAt(pos, buf, off, len);
			}

			@Override
			public int readBytesAt(long pos, byte[] buf) throws IOException {
				return io.readBytesAt(pos, buf);
			}

			@Override
			public void readBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
				io.readBytesFullyAt(pos, buffer);
			}

			@Override
			public void readBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
				io.readBytesFullyAt(pos, buf, off, len);
			}

			@Override
			public void readBytesFullyAt(long pos, byte[] buf) throws IOException {
				io.readBytesFullyAt(pos, buf);
			}
			
			@Override
			public ByteOrder getByteOrder() {
				return io.getByteOrder();
			}

			@Override
			public void setByteOrder(ByteOrder order) {
				io.setByteOrder(order);
			}

			@Override
			public int readUnsignedByte() throws IOException {
				return io.readUnsignedByte();
			}

			@Override
			public int readUnsigned2Bytes() throws IOException {
				return io.readUnsigned2Bytes();
			}

			@Override
			public short readSigned2Bytes() throws IOException {
				return io.readSigned2Bytes();
			}

			@Override
			public short readShort() throws IOException {
				return io.readShort();
			}

			@Override
			public int readUnsigned3Bytes() throws IOException {
				return io.readUnsigned3Bytes();
			}

			@Override
			public int readSigned3Bytes() throws IOException {
				return io.readSigned3Bytes();
			}

			@Override
			public long readUnsigned4Bytes() throws IOException {
				return io.readUnsigned4Bytes();
			}

			@Override
			public int readSigned4Bytes() throws IOException {
				return io.readSigned4Bytes();
			}

			@Override
			public int readInteger() throws IOException {
				return io.readInteger();
			}

			@Override
			public long readUnsigned5Bytes() throws IOException {
				return io.readUnsigned5Bytes();
			}

			@Override
			public long readSigned5Bytes() throws IOException {
				return io.readSigned5Bytes();
			}

			@Override
			public long readUnsigned6Bytes() throws IOException {
				return io.readUnsigned6Bytes();
			}

			@Override
			public long readSigned6Bytes() throws IOException {
				return io.readSigned6Bytes();
			}

			@Override
			public long readUnsigned7Bytes() throws IOException {
				return io.readUnsigned7Bytes();
			}

			@Override
			public long readSigned7Bytes() throws IOException {
				return io.readSigned7Bytes();
			}

			@Override
			public long readSigned8Bytes() throws IOException {
				return io.readSigned8Bytes();
			}

			@Override
			public long readLong() throws IOException {
				return io.readLong();
			}

			@Override
			public long readUnsignedBytes(int nbBytes) throws IOException {
				return io.readUnsignedBytes(nbBytes);
			}

			@Override
			public long readSignedBytes(int nbBytes) throws IOException {
				return io.readSignedBytes(nbBytes);
			}

			@Override
			public int readUnsignedByteAt(long pos) throws IOException {
				return io.readUnsignedByteAt(pos);
			}

			@Override
			public int readUnsigned2BytesAt(long pos) throws IOException {
				return io.readUnsigned2BytesAt(pos);
			}

			@Override
			public short readSigned2BytesAt(long pos) throws IOException {
				return io.readSigned2BytesAt(pos);
			}

			@Override
			public short readShortAt(long pos) throws IOException {
				return io.readShortAt(pos);
			}

			@Override
			public int readUnsigned3BytesAt(long pos) throws IOException {
				return io.readUnsigned3BytesAt(pos);
			}

			@Override
			public int readSigned3BytesAt(long pos) throws IOException {
				return io.readSigned3BytesAt(pos);
			}

			@Override
			public long readUnsigned4BytesAt(long pos) throws IOException {
				return io.readUnsigned4BytesAt(pos);
			}

			@Override
			public int readSigned4BytesAt(long pos) throws IOException {
				return io.readSigned4BytesAt(pos);
			}

			@Override
			public int readIntegerAt(long pos) throws IOException {
				return io.readIntegerAt(pos);
			}

			@Override
			public long readUnsigned5BytesAt(long pos) throws IOException {
				return io.readUnsigned5BytesAt(pos);
			}

			@Override
			public long readSigned5BytesAt(long pos) throws IOException {
				return io.readSigned5BytesAt(pos);
			}

			@Override
			public long readUnsigned6BytesAt(long pos) throws IOException {
				return io.readUnsigned6BytesAt(pos);
			}

			@Override
			public long readSigned6BytesAt(long pos) throws IOException {
				return io.readSigned6BytesAt(pos);
			}

			@Override
			public long readUnsigned7BytesAt(long pos) throws IOException {
				return io.readUnsigned7BytesAt(pos);
			}

			@Override
			public long readSigned7BytesAt(long pos) throws IOException {
				return io.readSigned7BytesAt(pos);
			}

			@Override
			public long readSigned8BytesAt(long pos) throws IOException {
				return io.readSigned8BytesAt(pos);
			}

			@Override
			public long readLongAt(long pos) throws IOException {
				return io.readLongAt(pos);
			}

			@Override
			public long readUnsignedBytesAt(long pos, int nbBytes) throws IOException {
				return io.readUnsignedBytesAt(pos, nbBytes);
			}

			@Override
			public long readSignedBytesAt(long pos, int nbBytes) throws IOException {
				return io.readSignedBytesAt(pos, nbBytes);
			}
			
		}
		
	}
	
	/** Writable view of a BytesDataIO. */
	public static class Writable extends BytesDataIOView<BytesDataIO.Writable> implements BytesDataIO.Writable {
		
		/**
		 * Create a Writable view of the given IO.<br/>
		 * If the IO is Appendable, the returned view will be also Appendable.
		 * 
		 * @param <T> type of given IO
		 * @param io the IO to wrap
		 * @return the Writable view
		 */
		public static <T extends BytesDataIO.Writable> BytesDataIOView.Writable of(T io) {
			if (io instanceof IO.Writable.Appendable)
				return new Appendable(io);
			return new BytesDataIOView.Writable(io);
		}
		
		private static final class Appendable extends BytesDataIOView.Writable implements IO.Writable.Appendable {
			private Appendable(BytesDataIO.Writable io) {
				super(io);
			}
		}
		
		private Writable(BytesDataIO.Writable io) {
			super(io);
		}

		@Override
		public void flush() throws IOException {
			io.flush();
		}

		@Override
		public void writeByte(byte value) throws IOException {
			io.writeByte(value);
		}

		@Override
		public int writeBytes(ByteBuffer buffer) throws IOException {
			return io.writeBytes(buffer);
		}

		@Override
		public int writeBytes(byte[] buf, int off, int len) throws IOException {
			return io.writeBytes(buf, off, len);
		}

		@Override
		public int writeBytes(byte[] buf) throws IOException {
			return io.writeBytes(buf);
		}

		@Override
		public void writeBytesFully(ByteBuffer buffer) throws IOException {
			io.writeBytesFully(buffer);
		}

		@Override
		public void writeBytesFully(List<ByteBuffer> buffers) throws IOException {
			io.writeBytesFully(buffers);
		}

		@Override
		public void writeBytesFully(byte[] buf, int off, int len) throws IOException {
			io.writeBytesFully(buf, off, len);
		}

		@Override
		public void writeBytesFully(byte[] buf) throws IOException {
			io.writeBytesFully(buf);
		}

		@Override
		public ByteOrder getByteOrder() {
			return io.getByteOrder();
		}

		@Override
		public void setByteOrder(ByteOrder order) {
			io.setByteOrder(order);
		}

		@Override
		public void writeUnsignedByte(int value) throws IOException {
			io.writeUnsignedByte(value);
		}

		@Override
		public void writeUnsigned2Bytes(int value) throws IOException {
			io.writeUnsigned2Bytes(value);
		}

		@Override
		public void writeSigned2Bytes(short value) throws IOException {
			io.writeSigned2Bytes(value);
		}

		@Override
		public void writeShort(short value) throws IOException {
			io.writeShort(value);
		}

		@Override
		public void writeUnsigned3Bytes(int value) throws IOException {
			io.writeUnsigned3Bytes(value);
		}

		@Override
		public void writeSigned3Bytes(int value) throws IOException {
			io.writeSigned3Bytes(value);
		}

		@Override
		public void writeUnsigned4Bytes(long value) throws IOException {
			io.writeUnsigned4Bytes(value);
		}

		@Override
		public void writeSigned4Bytes(int value) throws IOException {
			io.writeSigned4Bytes(value);
		}

		@Override
		public void writeInteger(int value) throws IOException {
			io.writeInteger(value);
		}

		@Override
		public void writeUnsigned5Bytes(long value) throws IOException {
			io.writeUnsigned5Bytes(value);
		}

		@Override
		public void writeSigned5Bytes(long value) throws IOException {
			io.writeSigned5Bytes(value);
		}

		@Override
		public void writeUnsigned6Bytes(long value) throws IOException {
			io.writeUnsigned6Bytes(value);
		}

		@Override
		public void writeSigned6Bytes(long value) throws IOException {
			io.writeSigned6Bytes(value);
		}

		@Override
		public void writeUnsigned7Bytes(long value) throws IOException {
			io.writeUnsigned7Bytes(value);
		}

		@Override
		public void writeSigned7Bytes(long value) throws IOException {
			io.writeSigned7Bytes(value);
		}

		@Override
		public void writeSigned8Bytes(long value) throws IOException {
			io.writeSigned8Bytes(value);
		}

		@Override
		public void writeLong(long value) throws IOException {
			io.writeLong(value);
		}

		@Override
		public void writeUnsignedBytes(int nbBytes, long value) throws IOException {
			io.writeUnsignedBytes(nbBytes, value);
		}

		@Override
		public void writeSignedBytes(int nbBytes, long value) throws IOException {
			io.writeSignedBytes(nbBytes, value);
		}

		/** Writable and Seekable view of a BytesDataIO. */
		public static class Seekable extends BytesDataIOView<BytesDataIO.Writable.Seekable> implements BytesDataIO.Writable.Seekable {

			/**
			 * Create a Writable view of the given IO.<br/>
			 * If the IO is Appendable, the returned view will be also Appendable.
			 * 
			 * @param <T> type of given IO
			 * @param io the IO to wrap
			 * @return the Writable view
			 */
			public static <T extends BytesDataIO.Writable.Seekable> BytesDataIOView.Writable.Seekable of(T io) {
				if (io instanceof IO.Writable.Appendable)
					return new Appendable(io);
				return new BytesDataIOView.Writable.Seekable(io);
			}
			
			private static final class Appendable extends BytesDataIOView.Writable.Seekable implements IO.Writable.Appendable {
				private Appendable(BytesDataIO.Writable.Seekable io) {
					super(io);
				}
			}

			private Seekable(BytesDataIO.Writable.Seekable io) {
				super(io);
			}

			@Override
			public void flush() throws IOException {
				io.flush();
			}

			@Override
			public void writeByte(byte value) throws IOException {
				io.writeByte(value);
			}

			@Override
			public int writeBytes(ByteBuffer buffer) throws IOException {
				return io.writeBytes(buffer);
			}

			@Override
			public int writeBytes(byte[] buf, int off, int len) throws IOException {
				return io.writeBytes(buf, off, len);
			}

			@Override
			public int writeBytes(byte[] buf) throws IOException {
				return io.writeBytes(buf);
			}

			@Override
			public void writeBytesFully(ByteBuffer buffer) throws IOException {
				io.writeBytesFully(buffer);
			}

			@Override
			public void writeBytesFully(List<ByteBuffer> buffers) throws IOException {
				io.writeBytesFully(buffers);
			}

			@Override
			public void writeBytesFully(byte[] buf, int off, int len) throws IOException {
				io.writeBytesFully(buf, off, len);
			}

			@Override
			public void writeBytesFully(byte[] buf) throws IOException {
				io.writeBytesFully(buf);
			}
			
			@Override
			public long size() throws IOException {
				return io.size();
			}

			@Override
			public long position() throws IOException {
				return io.position();
			}

			@Override
			public long seek(SeekFrom from, long offset) throws IOException {
				return io.seek(from, offset);
			}

			@Override
			public void writeByteAt(long pos, byte value) throws IOException {
				io.writeByteAt(pos, value);
			}

			@Override
			public int writeBytesAt(long pos, ByteBuffer buffer) throws IOException {
				return io.writeBytesAt(pos, buffer);
			}

			@Override
			public int writeBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
				return io.writeBytesAt(pos, buf, off, len);
			}

			@Override
			public int writeBytesAt(long pos, byte[] buf) throws IOException {
				return io.writeBytesAt(pos, buf);
			}

			@Override
			public void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
				io.writeBytesFullyAt(pos, buffer);
			}

			@Override
			public void writeBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
				io.writeBytesFullyAt(pos, buf, off, len);
			}

			@Override
			public void writeBytesFullyAt(long pos, byte[] buf) throws IOException {
				io.writeBytesFullyAt(pos, buf);
			}

			@Override
			public void writeBytesFullyAt(long pos, List<ByteBuffer> buffers) throws IOException {
				io.writeBytesFullyAt(pos, buffers);
			}
			
			@Override
			public ByteOrder getByteOrder() {
				return io.getByteOrder();
			}

			@Override
			public void setByteOrder(ByteOrder order) {
				io.setByteOrder(order);
			}

			@Override
			public void writeUnsignedByte(int value) throws IOException {
				io.writeUnsignedByte(value);
			}

			@Override
			public void writeUnsigned2Bytes(int value) throws IOException {
				io.writeUnsigned2Bytes(value);
			}

			@Override
			public void writeSigned2Bytes(short value) throws IOException {
				io.writeSigned2Bytes(value);
			}

			@Override
			public void writeShort(short value) throws IOException {
				io.writeShort(value);
			}

			@Override
			public void writeUnsigned3Bytes(int value) throws IOException {
				io.writeUnsigned3Bytes(value);
			}

			@Override
			public void writeSigned3Bytes(int value) throws IOException {
				io.writeSigned3Bytes(value);
			}

			@Override
			public void writeUnsigned4Bytes(long value) throws IOException {
				io.writeUnsigned4Bytes(value);
			}

			@Override
			public void writeSigned4Bytes(int value) throws IOException {
				io.writeSigned4Bytes(value);
			}

			@Override
			public void writeInteger(int value) throws IOException {
				io.writeInteger(value);
			}

			@Override
			public void writeUnsigned5Bytes(long value) throws IOException {
				io.writeUnsigned5Bytes(value);
			}

			@Override
			public void writeSigned5Bytes(long value) throws IOException {
				io.writeSigned5Bytes(value);
			}

			@Override
			public void writeUnsigned6Bytes(long value) throws IOException {
				io.writeUnsigned6Bytes(value);
			}

			@Override
			public void writeSigned6Bytes(long value) throws IOException {
				io.writeSigned6Bytes(value);
			}

			@Override
			public void writeUnsigned7Bytes(long value) throws IOException {
				io.writeUnsigned7Bytes(value);
			}

			@Override
			public void writeSigned7Bytes(long value) throws IOException {
				io.writeSigned7Bytes(value);
			}

			@Override
			public void writeSigned8Bytes(long value) throws IOException {
				io.writeSigned8Bytes(value);
			}

			@Override
			public void writeLong(long value) throws IOException {
				io.writeLong(value);
			}

			@Override
			public void writeUnsignedBytes(int nbBytes, long value) throws IOException {
				io.writeUnsignedBytes(nbBytes, value);
			}

			@Override
			public void writeSignedBytes(int nbBytes, long value) throws IOException {
				io.writeSignedBytes(nbBytes, value);
			}

			@Override
			public void writeUnsignedByteAt(long pos, int value) throws IOException {
				io.writeUnsignedByteAt(pos, value);
			}

			@Override
			public void writeUnsigned2BytesAt(long pos, int value) throws IOException {
				io.writeUnsigned2BytesAt(pos, value);
			}

			@Override
			public void writeSigned2BytesAt(long pos, short value) throws IOException {
				io.writeSigned2BytesAt(pos, value);
			}

			@Override
			public void writeShortAt(long pos, short value) throws IOException {
				io.writeShortAt(pos, value);
			}

			@Override
			public void writeUnsigned3BytesAt(long pos, int value) throws IOException {
				io.writeUnsigned3BytesAt(pos, value);
			}

			@Override
			public void writeSigned3BytesAt(long pos, int value) throws IOException {
				io.writeSigned3BytesAt(pos, value);
			}

			@Override
			public void writeUnsigned4BytesAt(long pos, long value) throws IOException {
				io.writeUnsigned4BytesAt(pos, value);
			}

			@Override
			public void writeSigned4BytesAt(long pos, int value) throws IOException {
				io.writeSigned4BytesAt(pos, value);
			}

			@Override
			public void writeIntegerAt(long pos, int value) throws IOException {
				io.writeIntegerAt(pos, value);
			}

			@Override
			public void writeUnsigned5BytesAt(long pos, long value) throws IOException {
				io.writeUnsigned5BytesAt(pos, value);
			}

			@Override
			public void writeSigned5BytesAt(long pos, long value) throws IOException {
				io.writeSigned5BytesAt(pos, value);
			}

			@Override
			public void writeUnsigned6BytesAt(long pos, long value) throws IOException {
				io.writeUnsigned6BytesAt(pos, value);
			}

			@Override
			public void writeSigned6BytesAt(long pos, long value) throws IOException {
				io.writeSigned6BytesAt(pos, value);
			}

			@Override
			public void writeUnsigned7BytesAt(long pos, long value) throws IOException {
				io.writeUnsigned7BytesAt(pos, value);
			}

			@Override
			public void writeSigned7BytesAt(long pos, long value) throws IOException {
				io.writeSigned7BytesAt(pos, value);
			}

			@Override
			public void writeSigned8BytesAt(long pos, long value) throws IOException {
				io.writeSigned8BytesAt(pos, value);
			}

			@Override
			public void writeLongAt(long pos, long value) throws IOException {
				io.writeLongAt(pos, value);
			}

			@Override
			public void writeUnsignedBytesAt(long pos, int nbBytes, long value) throws IOException {
				io.writeUnsignedBytesAt(pos, nbBytes, value);
			}

			@Override
			public void writeSignedBytesAt(long pos, int nbBytes, long value) throws IOException {
				io.writeSignedBytesAt(pos, nbBytes, value);
			}
			
		}

	}

	/** Readable and Writable view of a BytesDataIO.
	 * @param <T> type
	 */
	public static class ReadWrite<T extends BytesDataIO.Readable.Seekable & BytesDataIO.Writable.Seekable> extends BytesDataIOView<T> implements BytesDataIO.ReadWrite {
		
		/**
		 * Create a Readable and Writable view of the given IO.<br/>
		 * If the IO is Appendable, the returned view will be also Appendable.
		 * 
		 * @param <T> type of given IO
		 * @param io the IO to wrap
		 * @return the Writable view
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static <T extends BytesDataIO.Readable.Seekable & BytesDataIO.Writable.Seekable> BytesDataIOView.ReadWrite<T> of(T io) {
			if (io instanceof IO.Writable.Appendable)
				return new Appendable(io);
			return new BytesDataIOView.ReadWrite<>(io);
		}
		
		private static final class Appendable<T extends BytesDataIO.Readable.Seekable & BytesDataIO.Writable.Seekable & IO.Writable.Appendable>
		extends BytesDataIOView.ReadWrite<T> implements IO.Writable.Appendable {
			private Appendable(T io) {
				super(io);
			}
		}
		
		private ReadWrite(T io) {
			super(io);
		}
		
		@Override
		public byte readByte() throws IOException {
			return io.readByte();
		}

		@Override
		public int readBytes(ByteBuffer buffer) throws IOException {
			return io.readBytes(buffer);
		}

		@Override
		public int readBytes(byte[] buf, int off, int len) throws IOException {
			return io.readBytes(buf, off, len);
		}

		@Override
		public int readBytes(byte[] buf) throws IOException {
			return io.readBytes(buf);
		}

		@Override
		public Optional<ByteBuffer> readBuffer() throws IOException {
			return io.readBuffer();
		}

		@Override
		public void readBytesFully(ByteBuffer buffer) throws IOException {
			io.readBytesFully(buffer);
		}

		@Override
		public void readBytesFully(byte[] buf, int off, int len) throws IOException {
			io.readBytesFully(buf, off, len);
		}

		@Override
		public void readBytesFully(byte[] buf) throws IOException {
			io.readBytesFully(buf);
		}

		@Override
		public long skipUpTo(long toSkip) throws IOException {
			return io.skipUpTo(toSkip);
		}

		@Override
		public void skipFully(long toSkip) throws IOException {
			io.skipFully(toSkip);
		}
		
		@Override
		public long size() throws IOException {
			return io.size();
		}

		@Override
		public long position() throws IOException {
			return io.position();
		}

		@Override
		public long seek(SeekFrom from, long offset) throws IOException {
			return io.seek(from, offset);
		}

		@Override
		public byte readByteAt(long pos) throws IOException {
			return io.readByteAt(pos);
		}

		@Override
		public int readBytesAt(long pos, ByteBuffer buffer) throws IOException {
			return io.readBytesAt(pos, buffer);
		}

		@Override
		public int readBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			return io.readBytesAt(pos, buf, off, len);
		}

		@Override
		public int readBytesAt(long pos, byte[] buf) throws IOException {
			return io.readBytesAt(pos, buf);
		}

		@Override
		public void readBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
			io.readBytesFullyAt(pos, buffer);
		}

		@Override
		public void readBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
			io.readBytesFullyAt(pos, buf, off, len);
		}

		@Override
		public void readBytesFullyAt(long pos, byte[] buf) throws IOException {
			io.readBytesFullyAt(pos, buf);
		}
		
		@Override
		public ByteOrder getByteOrder() {
			return io.getByteOrder();
		}

		@Override
		public void setByteOrder(ByteOrder order) {
			io.setByteOrder(order);
		}

		@Override
		public int readUnsignedByte() throws IOException {
			return io.readUnsignedByte();
		}

		@Override
		public int readUnsigned2Bytes() throws IOException {
			return io.readUnsigned2Bytes();
		}

		@Override
		public short readSigned2Bytes() throws IOException {
			return io.readSigned2Bytes();
		}

		@Override
		public short readShort() throws IOException {
			return io.readShort();
		}

		@Override
		public int readUnsigned3Bytes() throws IOException {
			return io.readUnsigned3Bytes();
		}

		@Override
		public int readSigned3Bytes() throws IOException {
			return io.readSigned3Bytes();
		}

		@Override
		public long readUnsigned4Bytes() throws IOException {
			return io.readUnsigned4Bytes();
		}

		@Override
		public int readSigned4Bytes() throws IOException {
			return io.readSigned4Bytes();
		}

		@Override
		public int readInteger() throws IOException {
			return io.readInteger();
		}

		@Override
		public long readUnsigned5Bytes() throws IOException {
			return io.readUnsigned5Bytes();
		}

		@Override
		public long readSigned5Bytes() throws IOException {
			return io.readSigned5Bytes();
		}

		@Override
		public long readUnsigned6Bytes() throws IOException {
			return io.readUnsigned6Bytes();
		}

		@Override
		public long readSigned6Bytes() throws IOException {
			return io.readSigned6Bytes();
		}

		@Override
		public long readUnsigned7Bytes() throws IOException {
			return io.readUnsigned7Bytes();
		}

		@Override
		public long readSigned7Bytes() throws IOException {
			return io.readSigned7Bytes();
		}

		@Override
		public long readSigned8Bytes() throws IOException {
			return io.readSigned8Bytes();
		}

		@Override
		public long readLong() throws IOException {
			return io.readLong();
		}

		@Override
		public long readUnsignedBytes(int nbBytes) throws IOException {
			return io.readUnsignedBytes(nbBytes);
		}

		@Override
		public long readSignedBytes(int nbBytes) throws IOException {
			return io.readSignedBytes(nbBytes);
		}

		@Override
		public int readUnsignedByteAt(long pos) throws IOException {
			return io.readUnsignedByteAt(pos);
		}

		@Override
		public int readUnsigned2BytesAt(long pos) throws IOException {
			return io.readUnsigned2BytesAt(pos);
		}

		@Override
		public short readSigned2BytesAt(long pos) throws IOException {
			return io.readSigned2BytesAt(pos);
		}

		@Override
		public short readShortAt(long pos) throws IOException {
			return io.readShortAt(pos);
		}

		@Override
		public int readUnsigned3BytesAt(long pos) throws IOException {
			return io.readUnsigned3BytesAt(pos);
		}

		@Override
		public int readSigned3BytesAt(long pos) throws IOException {
			return io.readSigned3BytesAt(pos);
		}

		@Override
		public long readUnsigned4BytesAt(long pos) throws IOException {
			return io.readUnsigned4BytesAt(pos);
		}

		@Override
		public int readSigned4BytesAt(long pos) throws IOException {
			return io.readSigned4BytesAt(pos);
		}

		@Override
		public int readIntegerAt(long pos) throws IOException {
			return io.readIntegerAt(pos);
		}

		@Override
		public long readUnsigned5BytesAt(long pos) throws IOException {
			return io.readUnsigned5BytesAt(pos);
		}

		@Override
		public long readSigned5BytesAt(long pos) throws IOException {
			return io.readSigned5BytesAt(pos);
		}

		@Override
		public long readUnsigned6BytesAt(long pos) throws IOException {
			return io.readUnsigned6BytesAt(pos);
		}

		@Override
		public long readSigned6BytesAt(long pos) throws IOException {
			return io.readSigned6BytesAt(pos);
		}

		@Override
		public long readUnsigned7BytesAt(long pos) throws IOException {
			return io.readUnsigned7BytesAt(pos);
		}

		@Override
		public long readSigned7BytesAt(long pos) throws IOException {
			return io.readSigned7BytesAt(pos);
		}

		@Override
		public long readSigned8BytesAt(long pos) throws IOException {
			return io.readSigned8BytesAt(pos);
		}

		@Override
		public long readLongAt(long pos) throws IOException {
			return io.readLongAt(pos);
		}

		@Override
		public long readUnsignedBytesAt(long pos, int nbBytes) throws IOException {
			return io.readUnsignedBytesAt(pos, nbBytes);
		}

		@Override
		public long readSignedBytesAt(long pos, int nbBytes) throws IOException {
			return io.readSignedBytesAt(pos, nbBytes);
		}

		@Override
		public void flush() throws IOException {
			io.flush();
		}

		@Override
		public void writeByte(byte value) throws IOException {
			io.writeByte(value);
		}

		@Override
		public int writeBytes(ByteBuffer buffer) throws IOException {
			return io.writeBytes(buffer);
		}

		@Override
		public int writeBytes(byte[] buf, int off, int len) throws IOException {
			return io.writeBytes(buf, off, len);
		}

		@Override
		public int writeBytes(byte[] buf) throws IOException {
			return io.writeBytes(buf);
		}

		@Override
		public void writeBytesFully(ByteBuffer buffer) throws IOException {
			io.writeBytesFully(buffer);
		}

		@Override
		public void writeBytesFully(List<ByteBuffer> buffers) throws IOException {
			io.writeBytesFully(buffers);
		}

		@Override
		public void writeBytesFully(byte[] buf, int off, int len) throws IOException {
			io.writeBytesFully(buf, off, len);
		}

		@Override
		public void writeBytesFully(byte[] buf) throws IOException {
			io.writeBytesFully(buf);
		}
		
		@Override
		public void writeByteAt(long pos, byte value) throws IOException {
			io.writeByteAt(pos, value);
		}

		@Override
		public int writeBytesAt(long pos, ByteBuffer buffer) throws IOException {
			return io.writeBytesAt(pos, buffer);
		}

		@Override
		public int writeBytesAt(long pos, byte[] buf, int off, int len) throws IOException {
			return io.writeBytesAt(pos, buf, off, len);
		}

		@Override
		public int writeBytesAt(long pos, byte[] buf) throws IOException {
			return io.writeBytesAt(pos, buf);
		}

		@Override
		public void writeBytesFullyAt(long pos, ByteBuffer buffer) throws IOException {
			io.writeBytesFullyAt(pos, buffer);
		}

		@Override
		public void writeBytesFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
			io.writeBytesFullyAt(pos, buf, off, len);
		}

		@Override
		public void writeBytesFullyAt(long pos, byte[] buf) throws IOException {
			io.writeBytesFullyAt(pos, buf);
		}

		@Override
		public void writeBytesFullyAt(long pos, List<ByteBuffer> buffers) throws IOException {
			io.writeBytesFullyAt(pos, buffers);
		}
		
		@Override
		public void writeUnsignedByte(int value) throws IOException {
			io.writeUnsignedByte(value);
		}

		@Override
		public void writeUnsigned2Bytes(int value) throws IOException {
			io.writeUnsigned2Bytes(value);
		}

		@Override
		public void writeSigned2Bytes(short value) throws IOException {
			io.writeSigned2Bytes(value);
		}

		@Override
		public void writeShort(short value) throws IOException {
			io.writeShort(value);
		}

		@Override
		public void writeUnsigned3Bytes(int value) throws IOException {
			io.writeUnsigned3Bytes(value);
		}

		@Override
		public void writeSigned3Bytes(int value) throws IOException {
			io.writeSigned3Bytes(value);
		}

		@Override
		public void writeUnsigned4Bytes(long value) throws IOException {
			io.writeUnsigned4Bytes(value);
		}

		@Override
		public void writeSigned4Bytes(int value) throws IOException {
			io.writeSigned4Bytes(value);
		}

		@Override
		public void writeInteger(int value) throws IOException {
			io.writeInteger(value);
		}

		@Override
		public void writeUnsigned5Bytes(long value) throws IOException {
			io.writeUnsigned5Bytes(value);
		}

		@Override
		public void writeSigned5Bytes(long value) throws IOException {
			io.writeSigned5Bytes(value);
		}

		@Override
		public void writeUnsigned6Bytes(long value) throws IOException {
			io.writeUnsigned6Bytes(value);
		}

		@Override
		public void writeSigned6Bytes(long value) throws IOException {
			io.writeSigned6Bytes(value);
		}

		@Override
		public void writeUnsigned7Bytes(long value) throws IOException {
			io.writeUnsigned7Bytes(value);
		}

		@Override
		public void writeSigned7Bytes(long value) throws IOException {
			io.writeSigned7Bytes(value);
		}

		@Override
		public void writeSigned8Bytes(long value) throws IOException {
			io.writeSigned8Bytes(value);
		}

		@Override
		public void writeLong(long value) throws IOException {
			io.writeLong(value);
		}

		@Override
		public void writeUnsignedBytes(int nbBytes, long value) throws IOException {
			io.writeUnsignedBytes(nbBytes, value);
		}

		@Override
		public void writeSignedBytes(int nbBytes, long value) throws IOException {
			io.writeSignedBytes(nbBytes, value);
		}

		@Override
		public void writeUnsignedByteAt(long pos, int value) throws IOException {
			io.writeUnsignedByteAt(pos, value);
		}

		@Override
		public void writeUnsigned2BytesAt(long pos, int value) throws IOException {
			io.writeUnsigned2BytesAt(pos, value);
		}

		@Override
		public void writeSigned2BytesAt(long pos, short value) throws IOException {
			io.writeSigned2BytesAt(pos, value);
		}

		@Override
		public void writeShortAt(long pos, short value) throws IOException {
			io.writeShortAt(pos, value);
		}

		@Override
		public void writeUnsigned3BytesAt(long pos, int value) throws IOException {
			io.writeUnsigned3BytesAt(pos, value);
		}

		@Override
		public void writeSigned3BytesAt(long pos, int value) throws IOException {
			io.writeSigned3BytesAt(pos, value);
		}

		@Override
		public void writeUnsigned4BytesAt(long pos, long value) throws IOException {
			io.writeUnsigned4BytesAt(pos, value);
		}

		@Override
		public void writeSigned4BytesAt(long pos, int value) throws IOException {
			io.writeSigned4BytesAt(pos, value);
		}

		@Override
		public void writeIntegerAt(long pos, int value) throws IOException {
			io.writeIntegerAt(pos, value);
		}

		@Override
		public void writeUnsigned5BytesAt(long pos, long value) throws IOException {
			io.writeUnsigned5BytesAt(pos, value);
		}

		@Override
		public void writeSigned5BytesAt(long pos, long value) throws IOException {
			io.writeSigned5BytesAt(pos, value);
		}

		@Override
		public void writeUnsigned6BytesAt(long pos, long value) throws IOException {
			io.writeUnsigned6BytesAt(pos, value);
		}

		@Override
		public void writeSigned6BytesAt(long pos, long value) throws IOException {
			io.writeSigned6BytesAt(pos, value);
		}

		@Override
		public void writeUnsigned7BytesAt(long pos, long value) throws IOException {
			io.writeUnsigned7BytesAt(pos, value);
		}

		@Override
		public void writeSigned7BytesAt(long pos, long value) throws IOException {
			io.writeSigned7BytesAt(pos, value);
		}

		@Override
		public void writeSigned8BytesAt(long pos, long value) throws IOException {
			io.writeSigned8BytesAt(pos, value);
		}

		@Override
		public void writeLongAt(long pos, long value) throws IOException {
			io.writeLongAt(pos, value);
		}

		@Override
		public void writeUnsignedBytesAt(long pos, int nbBytes, long value) throws IOException {
			io.writeUnsignedBytesAt(pos, nbBytes, value);
		}

		@Override
		public void writeSignedBytesAt(long pos, int nbBytes, long value) throws IOException {
			io.writeSignedBytesAt(pos, nbBytes, value);
		}
	}
}

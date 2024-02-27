package net.lecousin.commons.io.bytes.data;

import java.io.IOException;
import java.nio.ByteOrder;

import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.bytes.BytesIO;

/**
 * An IO implementing data manipulation on bytes.
 */
// CHECKSTYLE DISABLE: LeftCurly
// CHECKSTYLE DISABLE: RightCurly
// CHECKSTYLE DISABLE: MagicNumber
public interface BytesDataIO extends BytesIO {
	
	/** @return current byte order used by this IO. */
	ByteOrder getByteOrder();
	
	/** Change the byte order for next operations.
	 * @param order new byte order
	 */
	void setByteOrder(ByteOrder order);
	
	/** Readable bytes data. */
	interface Readable extends BytesDataIO, BytesIO.Readable {

		/** 
		 * @return unsigned byte read
		 * @throws IOException in case of error
		 */
		default int readUnsignedByte() throws IOException { return readByte() & 0xFF; }
		
		/** 
		 * @return unsigned 2-bytes integer read
		 * @throws IOException in case of error
		 */
		int readUnsigned2Bytes() throws IOException;
		
		/** 
		 * @return signed 2-bytes integer read
		 * @throws IOException in case of error
		 */
		default short readSigned2Bytes() throws IOException { return (short) readUnsigned2Bytes(); }
		
		/** 
		 * @return signed 2-bytes integer read
		 * @throws IOException in case of error
		 */
		default short readShort() throws IOException { return readSigned2Bytes(); }
		
		/** 
		 * @return unsigned 3-bytes integer read
		 * @throws IOException in case of error
		 */
		int readUnsigned3Bytes() throws IOException;
		
		/** 
		 * @return signed 3-bytes integer read
		 * @throws IOException in case of error
		 */
		default int readSigned3Bytes() throws IOException { return BytesData.unsignedToSignedInt(readUnsigned3Bytes(), 0x7FFFFF); }
		
		/** 
		 * @return unsigned 4-bytes integer read
		 * @throws IOException in case of error
		 */
		long readUnsigned4Bytes() throws IOException;
		
		/** 
		 * @return signed 4-bytes integer read
		 * @throws IOException in case of error
		 */
		default int readSigned4Bytes() throws IOException { return (int) BytesData.unsignedToSignedLong(readUnsigned4Bytes(), 0x7FFFFFFF); }
		
		/** 
		 * @return signed 4-bytes integer read
		 * @throws IOException in case of error
		 */
		default int readInteger() throws IOException { return readSigned4Bytes(); }
		
		/** 
		 * @return unsigned 5-bytes integer read
		 * @throws IOException in case of error
		 */
		long readUnsigned5Bytes() throws IOException;
		
		/** 
		 * @return signed 5-bytes integer read
		 * @throws IOException in case of error
		 */
		default long readSigned5Bytes() throws IOException { return BytesData.unsignedToSignedLong(readUnsigned5Bytes(), 0x7FFFFFFFFFL); }
		
		/** 
		 * @return unsigned 6-bytes integer read
		 * @throws IOException in case of error
		 */
		long readUnsigned6Bytes() throws IOException;
		
		/** 
		 * @return signed 6-bytes integer read
		 * @throws IOException in case of error
		 */
		default long readSigned6Bytes() throws IOException { return BytesData.unsignedToSignedLong(readUnsigned6Bytes(), 0x7FFFFFFFFFFFL); }
		
		/** 
		 * @return unsigned 7-bytes integer read
		 * @throws IOException in case of error
		 */
		long readUnsigned7Bytes() throws IOException;
		
		/** 
		 * @return signed 7-bytes integer read
		 * @throws IOException in case of error
		 */
		default long readSigned7Bytes() throws IOException { return BytesData.unsignedToSignedLong(readUnsigned7Bytes(), 0x7FFFFFFFFFFFFFL); }
		
		/** 
		 * @return signed 8-bytes integer read
		 * @throws IOException in case of error
		 */
		long readSigned8Bytes() throws IOException;
		
		/** 
		 * @return signed 8-bytes integer read
		 * @throws IOException in case of error
		 */
		default long readLong() throws IOException { return readSigned8Bytes(); }
		
		/**
		 * Read an unsigned integer from the given number of bytes.
		 * @param nbBytes number of bytes
		 * @return the unsigned integer
		 * @throws IOException in case of error
		 */
		default long readUnsignedBytes(int nbBytes) throws IOException {
			switch (nbBytes) {
			case 1: return readUnsignedByte();
			case 2: return readUnsigned2Bytes();
			case 3: return readUnsigned3Bytes();
			case 4: return readUnsigned4Bytes();
			case 5: return readUnsigned5Bytes();
			case 6: return readUnsigned6Bytes();
			case 7: return readUnsigned7Bytes();
			default: throw new IllegalArgumentException();
			}
		}
		
		/**
		 * Read a signed integer from the given number of bytes.
		 * @param nbBytes number of bytes
		 * @return the signed integer
		 * @throws IOException in case of error
		 */
		default long readSignedBytes(int nbBytes) throws IOException {
			switch (nbBytes) {
			case 1: return readByte();
			case 2: return readSigned2Bytes();
			case 3: return readSigned3Bytes();
			case 4: return readSigned4Bytes();
			case 5: return readSigned5Bytes();
			case 6: return readSigned6Bytes();
			case 7: return readSigned7Bytes();
			case 8: return readSigned8Bytes();
			default: throw new IllegalArgumentException();
			}
		}
		
		/** Readable and Seekable IO. */
		interface Seekable extends BytesDataIO.Readable, BytesIO.Readable.Seekable {

			/**
			 * Read an unsigned byte at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			default int readUnsignedByteAt(long pos) throws IOException { return readByteAt(pos) & 0xFF; }
			
			/**
			 * Read an unsigned 2-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			int readUnsigned2BytesAt(long pos) throws IOException;
			
			/**
			 * Read a signed 2-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			default short readSigned2BytesAt(long pos) throws IOException { return (short) readUnsigned2BytesAt(pos); }
			
			/**
			 * Read a signed 2-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			default short readShortAt(long pos) throws IOException { return readSigned2BytesAt(pos); }
			
			/**
			 * Read an unsigned 3-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			int readUnsigned3BytesAt(long pos) throws IOException;
			
			/**
			 * Read a signed 3-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			default int readSigned3BytesAt(long pos) throws IOException { return BytesData.unsignedToSignedInt(readUnsigned3BytesAt(pos), 0x7FFFFF); }
			
			/**
			 * Read an unsigned 4-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			long readUnsigned4BytesAt(long pos) throws IOException;
			
			/**
			 * Read a signed 4-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			default int readSigned4BytesAt(long pos) throws IOException { return (int) BytesData.unsignedToSignedLong(readUnsigned4BytesAt(pos), 0x7FFFFFFF); }
			
			/**
			 * Read a signed 4-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			default int readIntegerAt(long pos) throws IOException { return readSigned4BytesAt(pos); }
			
			/**
			 * Read an unsigned 5-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			long readUnsigned5BytesAt(long pos) throws IOException;
			
			/**
			 * Read a signed 5-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			default long readSigned5BytesAt(long pos) throws IOException { return BytesData.unsignedToSignedLong(readUnsigned5BytesAt(pos), 0x7FFFFFFFFFL); }
			
			/**
			 * Read an unsigned 6-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			long readUnsigned6BytesAt(long pos) throws IOException;
			
			/**
			 * Read a signed 6-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			default long readSigned6BytesAt(long pos) throws IOException { return BytesData.unsignedToSignedLong(readUnsigned6BytesAt(pos), 0x7FFFFFFFFFFFL); }
			
			/**
			 * Read an unsigned 7-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			long readUnsigned7BytesAt(long pos) throws IOException;
			
			/**
			 * Read a signed 7-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			default long readSigned7BytesAt(long pos) throws IOException { return BytesData.unsignedToSignedLong(readUnsigned7BytesAt(pos), 0x7FFFFFFFFFFFFFL); }
			
			/**
			 * Read a signed 8-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			long readSigned8BytesAt(long pos) throws IOException;

			/**
			 * Read a signed 8-bytes integer at the given position. 
			 * @param pos position
			 * @return value read
			 * @throws IOException in case of error
			 */
			default long readLongAt(long pos) throws IOException { return readSigned8BytesAt(pos); }
			
			/**
			 * Read an unsigned integer from the given number of bytes, at the given position.
			 * @param pos position
			 * @param nbBytes number of bytes
			 * @return the unsigned integer
			 * @throws IOException in case of error
			 */
			default long readUnsignedBytesAt(long pos, int nbBytes) throws IOException {
				switch (nbBytes) {
				case 1: return readUnsignedByteAt(pos);
				case 2: return readUnsigned2BytesAt(pos);
				case 3: return readUnsigned3BytesAt(pos);
				case 4: return readUnsigned4BytesAt(pos);
				case 5: return readUnsigned5BytesAt(pos);
				case 6: return readUnsigned6BytesAt(pos);
				case 7: return readUnsigned7BytesAt(pos);
				default: throw new IllegalArgumentException();
				}
			}
			
			/**
			 * Read a signed integer from the given number of bytes, at the given position.
			 * @param pos position
			 * @param nbBytes number of bytes
			 * @return the unsigned integer
			 * @throws IOException in case of error
			 */
			default long readSignedBytesAt(long pos, int nbBytes) throws IOException {
				switch (nbBytes) {
				case 1: return readByteAt(pos);
				case 2: return readSigned2BytesAt(pos);
				case 3: return readSigned3BytesAt(pos);
				case 4: return readSigned4BytesAt(pos);
				case 5: return readSigned5BytesAt(pos);
				case 6: return readSigned6BytesAt(pos);
				case 7: return readSigned7BytesAt(pos);
				case 8: return readSigned8BytesAt(pos);
				default: throw new IllegalArgumentException();
				}
			}
			
			/** @return a Readable view of this IO. */
			default BytesDataIO.Readable asReadableBytesDataIO() {
				return new BytesDataIOView.Readable(this);
			}
			
		}
	}
	
	/** Writable bytes data. */
	interface Writable extends BytesDataIO, BytesIO.Writable {
		
		/** Write an unsigned byte.
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeUnsignedByte(int value) throws IOException { writeByte((byte) (value & 0xFF)); }

		/** Write an unsigned 2-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		void writeUnsigned2Bytes(int value) throws IOException;
		
		/** Write a signed 2-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeSigned2Bytes(short value) throws IOException { writeUnsigned2Bytes(value & 0xFFFF); }
		
		/** Write a signed 2-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeShort(short value) throws IOException { writeSigned2Bytes(value); }
		
		/** Write an unsigned 3-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		void writeUnsigned3Bytes(int value) throws IOException;
		
		/** Write a signed 3-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeSigned3Bytes(int value) throws IOException { writeUnsigned3Bytes(BytesData.signedToUnsignedInt(value, 0xFFFFFF)); }
		
		/** Write an unsigned 4-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		void writeUnsigned4Bytes(long value) throws IOException;
		
		/** Write a signed 4-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeSigned4Bytes(int value) throws IOException { writeUnsigned4Bytes(BytesData.signedToUnsignedLong(value, 0xFFFFFFFFL)); }
		
		/** Write a signed 4-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeInteger(int value) throws IOException { writeSigned4Bytes(value); }
		
		/** Write an unsigned 5-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		void writeUnsigned5Bytes(long value) throws IOException;
		
		/** Write a signed 5-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeSigned5Bytes(long value) throws IOException { writeUnsigned5Bytes(BytesData.signedToUnsignedLong(value, 0xFFFFFFFFFFL)); }
		
		/** Write an unsigned 6-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		void writeUnsigned6Bytes(long value) throws IOException;
		
		/** Write a signed 6-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeSigned6Bytes(long value) throws IOException { writeUnsigned6Bytes(BytesData.signedToUnsignedLong(value, 0xFFFFFFFFFFFFL)); }
		
		/** Write an unsigned 7-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		void writeUnsigned7Bytes(long value) throws IOException;
		
		/** Write a signed 7-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeSigned7Bytes(long value) throws IOException { writeUnsigned7Bytes(BytesData.signedToUnsignedLong(value, 0xFFFFFFFFFFFFFFL)); }
		
		/** Write a signed 8-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		void writeSigned8Bytes(long value) throws IOException;
		
		/** Write a signed 8-bytes integer.
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeLong(long value) throws IOException { writeSigned8Bytes(value); }
		
		/**
		 * Write an unsigned integer value.
		 * @param nbBytes number of bytes to encode the value
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeUnsignedBytes(int nbBytes, long value) throws IOException {
			switch (nbBytes) {
			case 1: writeUnsignedByte((int) (value & 0xFF)); break;
			case 2: writeUnsigned2Bytes((int) (value & 0xFFFF)); break;
			case 3: writeUnsigned3Bytes((int) (value & 0xFFFFFF)); break;
			case 4: writeUnsigned4Bytes(value); break;
			case 5: writeUnsigned5Bytes(value); break;
			case 6: writeUnsigned6Bytes(value); break;
			case 7: writeUnsigned7Bytes(value); break;
			default: throw new IllegalArgumentException();
			}
		}
		
		/**
		 * Write a signed integer value.
		 * @param nbBytes number of bytes to encode the value
		 * @param value value
		 * @throws IOException in case of error
		 */
		default void writeSignedBytes(int nbBytes, long value) throws IOException {
			switch (nbBytes) {
			case 1: writeByte((byte) value); break;
			case 2: writeSigned2Bytes((short) value); break;
			case 3: writeSigned3Bytes((int) value); break;
			case 4: writeSigned4Bytes((int) value); break;
			case 5: writeSigned5Bytes(value); break;
			case 6: writeSigned6Bytes(value); break;
			case 7: writeSigned7Bytes(value); break;
			case 8: writeSigned8Bytes(value); break;
			default: throw new IllegalArgumentException();
			}
		}
		
		/** Writable and Seekable. */
		interface Seekable extends BytesDataIO.Writable, BytesIO.Writable.Seekable {

			/** Write an unsigned byte, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeUnsignedByteAt(long pos, int value) throws IOException { writeByteAt(pos, (byte) (value & 0xFF)); }

			/** Write an unsigned 2-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			void writeUnsigned2BytesAt(long pos, int value) throws IOException;
			
			/** Write a signed 2-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeSigned2BytesAt(long pos, short value) throws IOException { writeUnsigned2BytesAt(pos, value & 0xFFFF); }
			
			/** Write a signed 2-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeShortAt(long pos, short value) throws IOException { writeSigned2BytesAt(pos, value); }
			
			/** Write an unsigned 3-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			void writeUnsigned3BytesAt(long pos, int value) throws IOException;
			
			/** Write a signed 3-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeSigned3BytesAt(long pos, int value) throws IOException { writeUnsigned3BytesAt(pos, BytesData.signedToUnsignedInt(value, 0xFFFFFF)); }
			
			/** Write an unsigned 4-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			void writeUnsigned4BytesAt(long pos, long value) throws IOException;
			
			/** Write a signed 4-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeSigned4BytesAt(long pos, int value) throws IOException { writeUnsigned4BytesAt(pos, BytesData.signedToUnsignedLong(value, 0xFFFFFFFFL)); }
			
			/** Write a signed 4-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeIntegerAt(long pos, int value) throws IOException { writeSigned4BytesAt(pos, value); }
			
			/** Write an unsigned 5-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			void writeUnsigned5BytesAt(long pos, long value) throws IOException;
			
			/** Write a signed 5-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeSigned5BytesAt(long pos, long value) throws IOException { writeUnsigned5BytesAt(pos, BytesData.signedToUnsignedLong(value, 0xFFFFFFFFFFL)); }
			
			/** Write an unsigned 6-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			void writeUnsigned6BytesAt(long pos, long value) throws IOException;
			
			/** Write a signed 6-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeSigned6BytesAt(long pos, long value) throws IOException { writeUnsigned6BytesAt(pos, BytesData.signedToUnsignedLong(value, 0xFFFFFFFFFFFFL)); }
			
			/** Write an unsigned 7-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			void writeUnsigned7BytesAt(long pos, long value) throws IOException;
			
			/** Write a signed 7-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeSigned7BytesAt(long pos, long value) throws IOException { writeUnsigned7BytesAt(pos, BytesData.signedToUnsignedLong(value, 0xFFFFFFFFFFFFFFL)); }
			
			/** Write a signed 8-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			void writeSigned8BytesAt(long pos, long value) throws IOException;
			
			/** Write a signed 8-bytes integer, at the given position.
			 * @param pos position
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeLongAt(long pos, long value) throws IOException { writeSigned8BytesAt(pos, value); }
			
			/**
			 * Write an unsigned integer value at the given position.
			 * @param pos position
			 * @param nbBytes number of bytes to encode the value
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeUnsignedBytesAt(long pos, int nbBytes, long value) throws IOException {
				switch (nbBytes) {
				case 1: writeUnsignedByteAt(pos, (int) (value & 0xFF)); break;
				case 2: writeUnsigned2BytesAt(pos, (int) (value & 0xFFFF)); break;
				case 3: writeUnsigned3BytesAt(pos, (int) (value & 0xFFFFFF)); break;
				case 4: writeUnsigned4BytesAt(pos, value); break;
				case 5: writeUnsigned5BytesAt(pos, value); break;
				case 6: writeUnsigned6BytesAt(pos, value); break;
				case 7: writeUnsigned7BytesAt(pos, value); break;
				default: throw new IllegalArgumentException();
				}
			}
			
			/**
			 * Write a signed integer value at the given position.
			 * @param pos position
			 * @param nbBytes number of bytes to encode the value
			 * @param value value
			 * @throws IOException in case of error
			 */
			default void writeSignedBytesAt(long pos, int nbBytes, long value) throws IOException {
				switch (nbBytes) {
				case 1: writeByteAt(pos, (byte) value); break;
				case 2: writeSigned2BytesAt(pos, (short) value); break;
				case 3: writeSigned3BytesAt(pos, (int) value); break;
				case 4: writeSigned4BytesAt(pos, (int) value); break;
				case 5: writeSigned5BytesAt(pos, value); break;
				case 6: writeSigned6BytesAt(pos, value); break;
				case 7: writeSigned7BytesAt(pos, value); break;
				case 8: writeSigned8BytesAt(pos, value); break;
				default: throw new IllegalArgumentException();
				}
			}
			
			/** @return a Writable view of this IO. */
			default BytesDataIO.Writable asWritableBytesDataIO() {
				return BytesDataIOView.Writable.of(this);
			}
			
			
			/** Writable Seekable and Appendable BytesDataIO. */
			interface Appendable extends BytesDataIO.Writable.Seekable, IO.Writable.Appendable {
				
			}
			
			/** Writable Seekable and Resizable BytesIO. */
			interface Resizable extends BytesDataIO.Writable.Seekable, IO.Writable.Resizable {
				
				/** @return a non-resizable view of this BytesDataIO. */
				default BytesDataIO.Writable.Seekable asNonResizableWritableSeekableBytesDataIO() {
					return BytesDataIOView.Writable.Seekable.of(this);
				}
				
			}
			
			/** Writable Seekable Appendable and Resizable BytesDataIO. */
			interface AppendableResizable extends BytesDataIO.Writable.Seekable.Appendable, BytesDataIO.Writable.Seekable.Resizable {
				
			}
		}
	}
	
	/** Readable and Writable BytesDataIO. */
	interface ReadWrite extends BytesDataIO.Readable.Seekable, BytesDataIO.Writable.Seekable {
		
		/** @return a Readable and Seekable view of this IO. */
		default BytesDataIO.Readable.Seekable asReadableSeekableBytesDataIO() {
			return new BytesDataIOView.Readable.Seekable(this);
		}
		
		/** @return a Writable and Seekable view of this IO. */
		default BytesDataIO.Writable.Seekable asWritableSeekableBytesDataIO() {
			return BytesDataIOView.Writable.Seekable.of(this);
		}
		
		/** Readable and Writable Seekable Resizable BytesDataIO. */
		interface Resizable extends BytesDataIO.ReadWrite, BytesDataIO.Writable.Seekable.Resizable {
			
			/** @return a non-resizable view of this BytesDataIO. */
			default BytesDataIO.ReadWrite asNonResizableReadWriteBytesDataIO() {
				return BytesDataIOView.ReadWrite.of(this);
			}
			
		}
		
		/** Readable and Writable Seekable Appendable BytesDataIO. */
		interface Appendable extends BytesDataIO.ReadWrite, BytesDataIO.Writable.Seekable.Appendable {
			
		}
		
		/** Readable and Writable Seekable Appendable and Resizable BytesDataIO. */
		interface AppendableResizable extends BytesDataIO.ReadWrite.Appendable, BytesDataIO.ReadWrite.Resizable {
			
		}
	}
	
}

package net.lecousin.commons.io.data;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.AbstractIO;
import net.lecousin.commons.io.IO;

/**
 * Implementation of a Seekable BytesDataIO, using a byte array.
 */
// CHECKSTYLE DISABLE: MagicNumber
public abstract class ByteArrayDataIO extends AbstractIO implements BytesDataIO, IO.Seekable {

	protected byte[] bytes;
	protected int start;
	protected int position;
	protected int end;
	
	/**
	 * Constructor.
	 * @param buf byte array to use
	 * @param off start position in the array, that will be considered as position 0
	 * @param len number of bytes to used, that will be considered as the size of this IO
	 */
	protected ByteArrayDataIO(byte[] buf, int off, int len) {
		DataChecks.checkByteArray(buf, off, len);
		this.bytes = buf;
		this.start = this.position = off;
		this.end = off + len;
	}
	
	/** @return the byte array */
	public byte[] getBytes() {
		return bytes;
	}
	
	/** @return the current position */
	public int getPosition() {
		return position;
	}
	
	/** @return the start position in the array, considered as position 0. */
	public int getStart() {
		return start;
	}
	
	/** @return the end position in the array, considered as the position beyond the last byte. */
	public int getEnd() {
		return end;
	}
	
	@Override
	protected void closeInternal() {
		bytes = null;
	}
	
	@Override
	public long position() throws ClosedChannelException {
		if (bytes == null) throw new ClosedChannelException();
		return (position - start);
	}
	
	@Override
	public long seek(SeekFrom from, long offset) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		Objects.requireNonNull(from, "from");
		long p;
		switch (from) {
		case CURRENT: p = position + offset; break;
		case END: p = end - offset; break;
		case START: default: p = start + offset; break;
		}
		if (p < start) throw new IllegalArgumentException("Cannot seek beyond start: " + (p - start));
		if (p > end) throw new EOFException((p - start) + " > " + (end - start));
		this.position = (int) p;
		return p - start;
	}
	
	@Override
	public long size() throws ClosedChannelException {
		if (bytes == null) throw new ClosedChannelException();
		return ((long) end) - start;
	}
	
	protected byte readByte() throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		if (position == end) throw new EOFException();
		return bytes[position++];
	}

	protected byte readByteAt(long pos) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos >= end) throw new EOFException();
		return bytes[(int) (start + pos)];
	}

	protected void writeByte(byte value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		if (position == end) throw new EOFException();
		bytes[position++] = value;
	}

	protected void writeByteAt(long pos, byte value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos >= end) throw new EOFException();
		bytes[(int) (start + pos)] = value;
	}
	
	protected void readFully(byte[] buf, int off, int len) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		DataChecks.checkByteArray(buf, off, len);
		if (position + len > end) throw new EOFException();
		System.arraycopy(bytes, position, buf, off, len);
		position += len;
	}
	
	protected void readFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		DataChecks.checkByteArray(buf, off, len);
		if (start + pos + len > end) throw new EOFException();
		System.arraycopy(bytes, (int) (start + pos), buf, off, len);
	}
	
	protected void writeFully(byte[] buf, int off, int len) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		DataChecks.checkByteArray(buf, off, len);
		if (position + len > end) throw new EOFException();
		System.arraycopy(buf, off, bytes, position, len);
		position += len;
	}
	
	protected void writeFullyAt(long pos, byte[] buf, int off, int len) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		DataChecks.checkByteArray(buf, off, len);
		if (start + pos + len > end) throw new EOFException();
		System.arraycopy(buf, off, bytes, (int) (start + pos), len);
	}
	
	protected abstract int readUnsigned2BytesAt(int pos);
	
	protected int readUnsigned2BytesAt(long pos) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 2) throw new EOFException();
		return readUnsigned2BytesAt((int) (start + pos));
	}

	protected abstract void writeUnsigned2BytesAt(int pos, int value);
	
	protected void writeUnsigned2BytesAt(long pos, int value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 2) throw new EOFException();
		writeUnsigned2BytesAt((int) (start + pos), value);
	}

	protected abstract int readUnsigned3BytesAt(int pos);
	
	protected int readUnsigned3BytesAt(long pos) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 3) throw new EOFException();
		return readUnsigned3BytesAt((int) (start + pos));
	}
	
	protected abstract void writeUnsigned3BytesAt(int pos, int value);
	
	protected void writeUnsigned3BytesAt(long pos, int value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 3) throw new EOFException();
		writeUnsigned3BytesAt((int) (start + pos), value);
	}
	
	protected abstract long readUnsigned4BytesAt(int pos);
	
	protected long readUnsigned4BytesAt(long pos) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 4) throw new EOFException();
		return readUnsigned4BytesAt((int) (start + pos));
	}
	
	protected abstract void writeUnsigned4BytesAt(int pos, long value);
	
	protected void writeUnsigned4BytesAt(long pos, long value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 4) throw new EOFException();
		writeUnsigned4BytesAt((int) (start + pos), value);
	}
	
	protected abstract long readUnsigned5BytesAt(int pos);
	
	protected long readUnsigned5BytesAt(long pos) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 5) throw new EOFException();
		return readUnsigned5BytesAt((int) (start + pos));
	}
	
	protected abstract void writeUnsigned5BytesAt(int pos, long value);
	
	protected void writeUnsigned5BytesAt(long pos, long value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 5) throw new EOFException();
		writeUnsigned5BytesAt((int) (start + pos), value);
	}

	protected abstract long readUnsigned6BytesAt(int pos);
	
	protected long readUnsigned6BytesAt(long pos) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 6) throw new EOFException();
		return readUnsigned6BytesAt((int) (start + pos));
	}
	
	protected abstract void writeUnsigned6BytesAt(int pos, long value);
	
	protected void writeUnsigned6BytesAt(long pos, long value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 6) throw new EOFException();
		writeUnsigned6BytesAt((int) (start + pos), value);
	}

	protected abstract long readUnsigned7BytesAt(int pos);
	
	protected long readUnsigned7BytesAt(long pos) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 7) throw new EOFException();
		return readUnsigned7BytesAt((int) (start + pos));
	}
	
	protected abstract void writeUnsigned7BytesAt(int pos, long value);
	
	protected void writeUnsigned7BytesAt(long pos, long value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 7) throw new EOFException();
		writeUnsigned7BytesAt((int) (start + pos), value);
	}
	
	protected abstract long readSigned8BytesAt(int pos);
	
	protected long readSigned8BytesAt(long pos) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 8) throw new EOFException();
		return readSigned8BytesAt((int) (start + pos));
	}
	
	protected abstract void writeSigned8BytesAt(int pos, long value);
	
	protected void writeSigned8BytesAt(long pos, long value) throws IOException {
		if (bytes == null) throw new ClosedChannelException();
		NegativeValueException.check(pos, DataChecks.FIELD_POS);
		if (start + pos > end - 8) throw new EOFException();
		writeSigned8BytesAt((int) (start + pos), value);
	}


	/** Little-Endian implementation. */
	public abstract static class LittleEndian extends ByteArrayDataIO {
		
		protected LittleEndian(byte[] bytes, int pos, int size) {
			super(bytes, pos, size);
		}
		
		@Override
		public Endianness getEndianness() {
			return Endianness.LITTLE_ENDIAN;
		}

		protected int readUnsigned2Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 2) throw new EOFException();
			return bytes[position++] & 0xFF |
				((bytes[position++] & 0xFF) << 8);
		}
		
		protected void writeUnsigned2Bytes(int value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 2) throw new EOFException();
			bytes[position++] = (byte) (value & 0xFF);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
		}

		@Override
		protected int readUnsigned2BytesAt(int pos) {
			return bytes[pos++] & 0xFF |
				((bytes[pos] & 0xFF) << 8);
		}
		
		@Override
		protected void writeUnsigned2BytesAt(int pos, int value) {
			bytes[pos++] = (byte) (value & 0xFF);
			bytes[pos] = (byte) ((value & 0xFF00) >> 8);
		}
		
		protected int readUnsigned3Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 3) throw new EOFException();
			return bytes[position++] & 0xFF |
				((bytes[position++] & 0xFF) << 8) |
				((bytes[position++] & 0xFF) << 16);
		}
		
		protected void writeUnsigned3Bytes(int value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 3) throw new EOFException();
			bytes[position++] = (byte) (value & 0xFF);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
		}

		@Override
		protected int readUnsigned3BytesAt(int pos) {
			return bytes[pos++] & 0xFF |
				((bytes[pos++] & 0xFF) << 8) |
				((bytes[pos] & 0xFF) << 16);
		}
		
		@Override
		protected void writeUnsigned3BytesAt(int pos, int value) {
			bytes[pos++] = (byte) (value & 0xFF);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos] = (byte) ((value & 0xFF0000) >> 16);
		}
		
		protected long readUnsigned4Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 4) throw new EOFException();
			return bytes[position++] & 0xFF |
				((bytes[position++] & 0xFF) << 8) |
				((bytes[position++] & 0xFF) << 16) |
				((long) (bytes[position++] & 0xFF) << 24);
		}
		
		protected void writeUnsigned4Bytes(long value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 4) throw new EOFException();
			bytes[position++] = (byte) (value & 0xFF);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[position++] = (byte) ((value & 0xFF000000L) >> 24);
		}

		@Override
		protected long readUnsigned4BytesAt(int pos) {
			return bytes[pos++] & 0xFF |
				((bytes[pos++] & 0xFF) << 8) |
				((bytes[pos++] & 0xFF) << 16) |
				((long) (bytes[pos] & 0xFF) << 24);
		}
		
		@Override
		protected void writeUnsigned4BytesAt(int pos, long value) {
			bytes[pos++] = (byte) (value & 0xFF);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[pos] = (byte) ((value & 0xFF000000L) >> 24);
		}

		protected long readUnsigned5Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 5) throw new EOFException();
			return bytes[position++] & 0xFF |
				((bytes[position++] & 0xFF) << 8) |
				((bytes[position++] & 0xFF) << 16) |
				((long) (bytes[position++] & 0xFF) << 24) |
				((long) (bytes[position++] & 0xFF) << 32);
		}
		
		protected void writeUnsigned5Bytes(long value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 5) throw new EOFException();
			bytes[position++] = (byte) (value & 0xFF);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[position++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[position++] = (byte) ((value & 0xFF00000000L) >> 32);
		}

		@Override
		protected long readUnsigned5BytesAt(int pos) {
			return bytes[pos++] & 0xFF |
				((bytes[pos++] & 0xFF) << 8) |
				((bytes[pos++] & 0xFF) << 16) |
				((long) (bytes[pos++] & 0xFF) << 24) |
				((long) (bytes[pos] & 0xFF) << 32);
		}
		
		@Override
		protected void writeUnsigned5BytesAt(int pos, long value) {
			bytes[pos++] = (byte) (value & 0xFF);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[pos++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[pos] = (byte) ((value & 0xFF00000000L) >> 32);
		}

		protected long readUnsigned6Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 6) throw new EOFException();
			return bytes[position++] & 0xFF |
				((bytes[position++] & 0xFF) << 8) |
				((bytes[position++] & 0xFF) << 16) |
				((long) (bytes[position++] & 0xFF) << 24) |
				((long) (bytes[position++] & 0xFF) << 32) |
				((long) (bytes[position++] & 0xFF) << 40);
		}
		
		protected void writeUnsigned6Bytes(long value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 6) throw new EOFException();
			bytes[position++] = (byte) (value & 0xFF);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[position++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[position++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[position++] = (byte) ((value & 0xFF0000000000L) >> 40);
		}

		@Override
		protected long readUnsigned6BytesAt(int pos) {
			return bytes[pos++] & 0xFF |
				((bytes[pos++] & 0xFF) << 8) |
				((bytes[pos++] & 0xFF) << 16) |
				((long) (bytes[pos++] & 0xFF) << 24) |
				((long) (bytes[pos++] & 0xFF) << 32) |
				((long) (bytes[pos] & 0xFF) << 40);
		}
		
		@Override
		protected void writeUnsigned6BytesAt(int pos, long value) {
			bytes[pos++] = (byte) (value & 0xFF);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[pos++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[pos++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[pos] = (byte) ((value & 0xFF0000000000L) >> 40);
		}
		
		protected long readUnsigned7Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 7) throw new EOFException();
			return bytes[position++] & 0xFF |
				((bytes[position++] & 0xFF) << 8) |
				((bytes[position++] & 0xFF) << 16) |
				((long) (bytes[position++] & 0xFF) << 24) |
				((long) (bytes[position++] & 0xFF) << 32) |
				((long) (bytes[position++] & 0xFF) << 40) |
				((long) (bytes[position++] & 0xFF) << 48);
		}
		
		protected void writeUnsigned7Bytes(long value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 7) throw new EOFException();
			bytes[position++] = (byte) (value & 0xFF);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[position++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[position++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[position++] = (byte) ((value & 0xFF0000000000L) >> 40);
			bytes[position++] = (byte) ((value & 0xFF000000000000L) >> 48);
		}

		@Override
		protected long readUnsigned7BytesAt(int pos) {
			return bytes[pos++] & 0xFF |
				((bytes[pos++] & 0xFF) << 8) |
				((bytes[pos++] & 0xFF) << 16) |
				((long) (bytes[pos++] & 0xFF) << 24) |
				((long) (bytes[pos++] & 0xFF) << 32) |
				((long) (bytes[pos++] & 0xFF) << 40) |
				((long) (bytes[pos] & 0xFF) << 48);
		}
		
		@Override
		protected void writeUnsigned7BytesAt(int pos, long value) {
			bytes[pos++] = (byte) (value & 0xFF);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[pos++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[pos++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[pos++] = (byte) ((value & 0xFF0000000000L) >> 40);
			bytes[pos] = (byte) ((value & 0xFF000000000000L) >> 48);
		}

		protected long readSigned8Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 8) throw new EOFException();
			return bytes[position++] & 0xFF |
				((bytes[position++] & 0xFF) << 8) |
				((bytes[position++] & 0xFF) << 16) |
				((long) (bytes[position++] & 0xFF) << 24) |
				((long) (bytes[position++] & 0xFF) << 32) |
				((long) (bytes[position++] & 0xFF) << 40) |
				((long) (bytes[position++] & 0xFF) << 48) |
				((long) (bytes[position++] & 0xFF) << 56);
		}
		
		protected void writeSigned8Bytes(long value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 8) throw new EOFException();
			bytes[position++] = (byte) (value & 0xFF);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[position++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[position++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[position++] = (byte) ((value & 0xFF0000000000L) >> 40);
			bytes[position++] = (byte) ((value & 0xFF000000000000L) >> 48);
			bytes[position++] = (byte) ((value >> 56) & 0xFF);
		}

		@Override
		protected long readSigned8BytesAt(int pos) {
			return bytes[pos++] & 0xFF |
				((bytes[pos++] & 0xFF) << 8) |
				((bytes[pos++] & 0xFF) << 16) |
				((long) (bytes[pos++] & 0xFF) << 24) |
				((long) (bytes[pos++] & 0xFF) << 32) |
				((long) (bytes[pos++] & 0xFF) << 40) |
				((long) (bytes[pos++] & 0xFF) << 48) |
				((long) (bytes[pos] & 0xFF) << 56);
		}
		
		@Override
		protected void writeSigned8BytesAt(int pos, long value) {
			bytes[pos++] = (byte) (value & 0xFF);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[pos++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[pos++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[pos++] = (byte) ((value & 0xFF0000000000L) >> 40);
			bytes[pos++] = (byte) ((value & 0xFF000000000000L) >> 48);
			bytes[pos] = (byte) ((value >> 56) & 0xFF);
		}

		// CHECKSTYLE DISABLE: LeftCurly
		// CHECKSTYLE DISABLE: RightCurly
		// CHECKSTYLE DISABLE: EmptyLineSeparator
		
		/** Readable IO. */
		public static class Readable extends LittleEndian implements BytesDataIO.Readable.Seekable {
			
			/**
			 * Constructor.
			 * @param buf byte array to use
			 * @param off start position in the array, that will be considered as position 0
			 * @param len number of bytes to used, that will be considered as the size of this IO
			 */
			public Readable(byte[] buf, int off, int len) {
				super(buf, off, len);
			}
			

			/**
			 * Constructor.
			 * @param buf byte array to use
			 */
			public Readable(byte[] buf) {
				this(buf, 0, buf.length);
			}

			@Override
			public byte readByte() throws IOException { return super.readByte(); }
			@Override
			public int readUnsigned2Bytes() throws IOException { return super.readUnsigned2Bytes(); }
			@Override
			public int readUnsigned3Bytes() throws IOException { return super.readUnsigned3Bytes(); }
			@Override
			public long readUnsigned4Bytes() throws IOException { return super.readUnsigned4Bytes(); }
			@Override
			public long readUnsigned5Bytes() throws IOException { return super.readUnsigned5Bytes(); }
			@Override
			public long readUnsigned6Bytes() throws IOException { return super.readUnsigned6Bytes(); }
			@Override
			public long readUnsigned7Bytes() throws IOException { return super.readUnsigned7Bytes(); }
			@Override
			public long readSigned8Bytes() throws IOException { return super.readSigned8Bytes(); }

			@Override
			public void readFully(byte[] buf, int off, int len) throws IOException { super.readFully(buf, off, len); }

			@Override
			public byte readByteAt(long pos) throws IOException { return super.readByteAt(pos); }
			@Override
			public int readUnsigned2BytesAt(long pos) throws IOException { return super.readUnsigned2BytesAt(pos); }
			@Override
			public int readUnsigned3BytesAt(long pos) throws IOException { return super.readUnsigned3BytesAt(pos); }
			@Override
			public long readUnsigned4BytesAt(long pos) throws IOException { return super.readUnsigned4BytesAt(pos); }
			@Override
			public long readUnsigned5BytesAt(long pos) throws IOException { return super.readUnsigned5BytesAt(pos); }
			@Override
			public long readUnsigned6BytesAt(long pos) throws IOException { return super.readUnsigned6BytesAt(pos); }
			@Override
			public long readUnsigned7BytesAt(long pos) throws IOException { return super.readUnsigned7BytesAt(pos); }
			@Override
			public long readSigned8BytesAt(long pos) throws IOException { return super.readSigned8BytesAt(pos); }

			@Override
			public void readFullyAt(long pos, byte[] buf, int off, int len) throws IOException { super.readFullyAt(pos, buf, off, len); }
		}
		
		/** Writable IO. */
		public static class Writable extends LittleEndian implements BytesDataIO.Writable.Seekable {
			
			/**
			 * Constructor.
			 * @param buf byte array to use
			 * @param off start position in the array, that will be considered as position 0
			 * @param len number of bytes to used, that will be considered as the size of this IO
			 */
			public Writable(byte[] buf, int off, int len) {
				super(buf, off, len);
			}

			/**
			 * Constructor.
			 * @param buf byte array to use
			 */
			public Writable(byte[] buf) {
				this(buf, 0, buf.length);
			}
			
			@Override
			public void writeByte(byte value) throws IOException { super.writeByte(value); }
			@Override
			public void writeUnsigned2Bytes(int value) throws IOException { super.writeUnsigned2Bytes(value); }
			@Override
			public void writeUnsigned3Bytes(int value) throws IOException { super.writeUnsigned3Bytes(value); }
			@Override
			public void writeUnsigned4Bytes(long value) throws IOException { super.writeUnsigned4Bytes(value); }
			@Override
			public void writeUnsigned5Bytes(long value) throws IOException { super.writeUnsigned5Bytes(value); }
			@Override
			public void writeUnsigned6Bytes(long value) throws IOException { super.writeUnsigned6Bytes(value); }
			@Override
			public void writeUnsigned7Bytes(long value) throws IOException { super.writeUnsigned7Bytes(value); }
			@Override
			public void writeSigned8Bytes(long value) throws IOException { super.writeSigned8Bytes(value); }

			@Override
			public void writeFully(byte[] buf, int off, int len) throws IOException { super.writeFully(buf, off, len); }
			
			@Override
			public void writeByteAt(long pos, byte value) throws IOException { super.writeByteAt(pos, value); }
			@Override
			public void writeUnsigned2BytesAt(long pos, int value) throws IOException { super.writeUnsigned2BytesAt(pos, value); }
			@Override
			public void writeUnsigned3BytesAt(long pos, int value) throws IOException { super.writeUnsigned3BytesAt(pos, value); }
			@Override
			public void writeUnsigned4BytesAt(long pos, long value) throws IOException { super.writeUnsigned4BytesAt(pos, value); }
			@Override
			public void writeUnsigned5BytesAt(long pos, long value) throws IOException { super.writeUnsigned5BytesAt(pos, value); }
			@Override
			public void writeUnsigned6BytesAt(long pos, long value) throws IOException { super.writeUnsigned6BytesAt(pos, value); }
			@Override
			public void writeUnsigned7BytesAt(long pos, long value) throws IOException { super.writeUnsigned7BytesAt(pos, value); }
			@Override
			public void writeSigned8BytesAt(long pos, long value) throws IOException { super.writeSigned8BytesAt(pos, value); }

			@Override
			public void writeFullyAt(long pos, byte[] buf, int off, int len) throws IOException { super.writeFullyAt(pos, buf, off, len); }

		}
		
		/** Readable and Writable IO. */
		public static class ReadWrite extends Readable implements BytesDataIO.Writable.Seekable {
			
			/**
			 * Constructor.
			 * @param buf byte array to use
			 * @param off start position in the array, that will be considered as position 0
			 * @param len number of bytes to used, that will be considered as the size of this IO
			 */
			public ReadWrite(byte[] buf, int off, int len) {
				super(buf, off, len);
			}

			/**
			 * Constructor.
			 * @param buf byte array to use
			 */
			public ReadWrite(byte[] buf) {
				this(buf, 0, buf.length);
			}
			
			@Override
			public void writeByte(byte value) throws IOException { super.writeByte(value); }
			@Override
			public void writeUnsigned2Bytes(int value) throws IOException { super.writeUnsigned2Bytes(value); }
			@Override
			public void writeUnsigned3Bytes(int value) throws IOException { super.writeUnsigned3Bytes(value); }
			@Override
			public void writeUnsigned4Bytes(long value) throws IOException { super.writeUnsigned4Bytes(value); }
			@Override
			public void writeUnsigned5Bytes(long value) throws IOException { super.writeUnsigned5Bytes(value); }
			@Override
			public void writeUnsigned6Bytes(long value) throws IOException { super.writeUnsigned6Bytes(value); }
			@Override
			public void writeUnsigned7Bytes(long value) throws IOException { super.writeUnsigned7Bytes(value); }
			@Override
			public void writeSigned8Bytes(long value) throws IOException { super.writeSigned8Bytes(value); }

			@Override
			public void writeFully(byte[] buf, int off, int len) throws IOException { super.writeFully(buf, off, len); }
			
			@Override
			public void writeByteAt(long pos, byte value) throws IOException { super.writeByteAt(pos, value); }
			@Override
			public void writeUnsigned2BytesAt(long pos, int value) throws IOException { super.writeUnsigned2BytesAt(pos, value); }
			@Override
			public void writeUnsigned3BytesAt(long pos, int value) throws IOException { super.writeUnsigned3BytesAt(pos, value); }
			@Override
			public void writeUnsigned4BytesAt(long pos, long value) throws IOException { super.writeUnsigned4BytesAt(pos, value); }
			@Override
			public void writeUnsigned5BytesAt(long pos, long value) throws IOException { super.writeUnsigned5BytesAt(pos, value); }
			@Override
			public void writeUnsigned6BytesAt(long pos, long value) throws IOException { super.writeUnsigned6BytesAt(pos, value); }
			@Override
			public void writeUnsigned7BytesAt(long pos, long value) throws IOException { super.writeUnsigned7BytesAt(pos, value); }
			@Override
			public void writeSigned8BytesAt(long pos, long value) throws IOException { super.writeSigned8BytesAt(pos, value); }

			@Override
			public void writeFullyAt(long pos, byte[] buf, int off, int len) throws IOException { super.writeFullyAt(pos, buf, off, len); }

		}
		
	}

	// CHECKSTYLE ENABLE: LeftCurly
	// CHECKSTYLE ENABLE: RightCurly
	// CHECKSTYLE ENABLE: EmptyLineSeparator
	
	/** Big-Endian implementation. */
	public abstract static class BigEndian extends ByteArrayDataIO {
		
		protected BigEndian(byte[] bytes, int pos, int size) {
			super(bytes, pos, size);
		}
		
		@Override
		public Endianness getEndianness() {
			return Endianness.BIG_ENDIAN;
		}

		protected int readUnsigned2Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 2) throw new EOFException();
			return ((bytes[position++] & 0xFF) << 8) |
				bytes[position++] & 0xFF;
		}
		
		protected void writeUnsigned2Bytes(int value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 2) throw new EOFException();
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) (value & 0xFF);
		}
		
		@Override
		protected int readUnsigned2BytesAt(int pos) {
			return ((bytes[pos++] & 0xFF) << 8) |
				bytes[pos] & 0xFF;
		}
		
		@Override
		protected void writeUnsigned2BytesAt(int pos, int value) {
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos] = (byte) (value & 0xFF);
		}

		protected int readUnsigned3Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 3) throw new EOFException();
			return ((bytes[position++] & 0xFF) << 16) |
				((bytes[position++] & 0xFF) << 8) |
				(bytes[position++] & 0xFF);
		}
		
		protected void writeUnsigned3Bytes(int value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 3) throw new EOFException();
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) (value & 0xFF);
		}
		
		@Override
		protected int readUnsigned3BytesAt(int pos) {
			return ((bytes[pos++] & 0xFF) << 16) |
				((bytes[pos++] & 0xFF) << 8) |
				(bytes[pos] & 0xFF);
		}
		
		@Override
		protected void writeUnsigned3BytesAt(int pos, int value) {
			bytes[pos++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos] = (byte) (value & 0xFF);
		}

		protected long readUnsigned4Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 4) throw new EOFException();
			return ((long) (bytes[position++] & 0xFF) << 24) |
				((bytes[position++] & 0xFF) << 16) |
				((bytes[position++] & 0xFF) << 8) |
				(bytes[position++] & 0xFF);
		}
		
		protected void writeUnsigned4Bytes(long value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 4) throw new EOFException();
			bytes[position++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) (value & 0xFF);
		}
		
		@Override
		protected long readUnsigned4BytesAt(int pos) {
			return ((long) (bytes[pos++] & 0xFF) << 24) |
				((bytes[pos++] & 0xFF) << 16) |
				((bytes[pos++] & 0xFF) << 8) |
				(bytes[pos] & 0xFF);
		}
		
		@Override
		protected void writeUnsigned4BytesAt(int pos, long value) {
			bytes[pos++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[pos++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos] = (byte) (value & 0xFF);
		}

		protected long readUnsigned5Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 5) throw new EOFException();
			return ((long) (bytes[position++] & 0xFF) << 32) |
				((long) (bytes[position++] & 0xFF) << 24) |
				((bytes[position++] & 0xFF) << 16) |
				((bytes[position++] & 0xFF) << 8) |
				(bytes[position++] & 0xFF);
		}
		
		protected void writeUnsigned5Bytes(long value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 5) throw new EOFException();
			bytes[position++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[position++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) (value & 0xFF);
		}
		
		@Override
		protected long readUnsigned5BytesAt(int pos) {
			return ((long) (bytes[pos++] & 0xFF) << 32) |
				((long) (bytes[pos++] & 0xFF) << 24) |
				((bytes[pos++] & 0xFF) << 16) |
				((bytes[pos++] & 0xFF) << 8) |
				(bytes[pos] & 0xFF);
		}
		
		@Override
		protected void writeUnsigned5BytesAt(int pos, long value) {
			bytes[pos++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[pos++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[pos++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos] = (byte) (value & 0xFF);
		}

		protected long readUnsigned6Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 6) throw new EOFException();
			return ((long) (bytes[position++] & 0xFF) << 40) |
				((long) (bytes[position++] & 0xFF) << 32) |
				((long) (bytes[position++] & 0xFF) << 24) |
				((bytes[position++] & 0xFF) << 16) |
				((bytes[position++] & 0xFF) << 8) |
				(bytes[position++] & 0xFF);
		}
		
		protected void writeUnsigned6Bytes(long value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 6) throw new EOFException();
			bytes[position++] = (byte) ((value & 0xFF0000000000L) >> 40);
			bytes[position++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[position++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) (value & 0xFF);
		}
		
		@Override
		protected long readUnsigned6BytesAt(int pos) {
			return ((long) (bytes[pos++] & 0xFF) << 40) |
				((long) (bytes[pos++] & 0xFF) << 32) |
				((long) (bytes[pos++] & 0xFF) << 24) |
				((bytes[pos++] & 0xFF) << 16) |
				((bytes[pos++] & 0xFF) << 8) |
				(bytes[pos] & 0xFF);
		}
		
		@Override
		protected void writeUnsigned6BytesAt(int pos, long value) {
			bytes[pos++] = (byte) ((value & 0xFF0000000000L) >> 40);
			bytes[pos++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[pos++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[pos++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos] = (byte) (value & 0xFF);
		}

		protected long readUnsigned7Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 7) throw new EOFException();
			return ((long) (bytes[position++] & 0xFF) << 48) |
				((long) (bytes[position++] & 0xFF) << 40) |
				((long) (bytes[position++] & 0xFF) << 32) |
				((long) (bytes[position++] & 0xFF) << 24) |
				((bytes[position++] & 0xFF) << 16) |
				((bytes[position++] & 0xFF) << 8) |
				(bytes[position++] & 0xFF);
		}
		
		protected void writeUnsigned7Bytes(long value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 7) throw new EOFException();
			bytes[position++] = (byte) ((value & 0xFF000000000000L) >> 48);
			bytes[position++] = (byte) ((value & 0xFF0000000000L) >> 40);
			bytes[position++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[position++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) (value & 0xFF);
		}
		
		@Override
		protected long readUnsigned7BytesAt(int pos) {
			return ((long) (bytes[pos++] & 0xFF) << 48) |
				((long) (bytes[pos++] & 0xFF) << 40) |
				((long) (bytes[pos++] & 0xFF) << 32) |
				((long) (bytes[pos++] & 0xFF) << 24) |
				((bytes[pos++] & 0xFF) << 16) |
				((bytes[pos++] & 0xFF) << 8) |
				(bytes[pos] & 0xFF);
		}
		
		@Override
		protected void writeUnsigned7BytesAt(int pos, long value) {
			bytes[pos++] = (byte) ((value & 0xFF000000000000L) >> 48);
			bytes[pos++] = (byte) ((value & 0xFF0000000000L) >> 40);
			bytes[pos++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[pos++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[pos++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos] = (byte) (value & 0xFF);
		}

		protected long readSigned8Bytes() throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 8) throw new EOFException();
			return ((long) (bytes[position++] & 0xFF) << 56) |
				((long) (bytes[position++] & 0xFF) << 48) |
				((long) (bytes[position++] & 0xFF) << 40) |
				((long) (bytes[position++] & 0xFF) << 32) |
				((long) (bytes[position++] & 0xFF) << 24) |
				((bytes[position++] & 0xFF) << 16) |
				((bytes[position++] & 0xFF) << 8) |
				(bytes[position++] & 0xFF);
		}
		
		protected void writeSigned8Bytes(long value) throws IOException {
			if (bytes == null) throw new ClosedChannelException();
			if (position > end - 8) throw new EOFException();
			bytes[position++] = (byte) ((value >> 56) & 0xFF);
			bytes[position++] = (byte) ((value & 0xFF000000000000L) >> 48);
			bytes[position++] = (byte) ((value & 0xFF0000000000L) >> 40);
			bytes[position++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[position++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[position++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[position++] = (byte) ((value & 0xFF00) >> 8);
			bytes[position++] = (byte) (value & 0xFF);
		}
		
		@Override
		protected long readSigned8BytesAt(int pos) {
			return ((long) (bytes[pos++] & 0xFF) << 56) |
				((long) (bytes[pos++] & 0xFF) << 48) |
				((long) (bytes[pos++] & 0xFF) << 40) |
				((long) (bytes[pos++] & 0xFF) << 32) |
				((long) (bytes[pos++] & 0xFF) << 24) |
				((bytes[pos++] & 0xFF) << 16) |
				((bytes[pos++] & 0xFF) << 8) |
				(bytes[pos] & 0xFF);
		}
		
		@Override
		protected void writeSigned8BytesAt(int pos, long value) {
			bytes[pos++] = (byte) ((value >> 56) & 0xFF);
			bytes[pos++] = (byte) ((value & 0xFF000000000000L) >> 48);
			bytes[pos++] = (byte) ((value & 0xFF0000000000L) >> 40);
			bytes[pos++] = (byte) ((value & 0xFF00000000L) >> 32);
			bytes[pos++] = (byte) ((value & 0xFF000000L) >> 24);
			bytes[pos++] = (byte) ((value & 0xFF0000) >> 16);
			bytes[pos++] = (byte) ((value & 0xFF00) >> 8);
			bytes[pos] = (byte) (value & 0xFF);
		}

		// CHECKSTYLE DISABLE: LeftCurly
		// CHECKSTYLE DISABLE: RightCurly
		// CHECKSTYLE DISABLE: EmptyLineSeparator
		
		/** Readable IO. */
		public static class Readable extends BigEndian implements BytesDataIO.Readable.Seekable {
			
			/**
			 * Constructor.
			 * @param buf byte array to use
			 * @param off start position in the array, that will be considered as position 0
			 * @param len number of bytes to used, that will be considered as the size of this IO
			 */
			public Readable(byte[] buf, int off, int len) {
				super(buf, off, len);
			}

			/**
			 * Constructor.
			 * @param buf byte array to use
			 */
			public Readable(byte[] buf) {
				this(buf, 0, buf.length);
			}

			@Override
			public byte readByte() throws IOException { return super.readByte(); }
			@Override
			public int readUnsigned2Bytes() throws IOException { return super.readUnsigned2Bytes(); }
			@Override
			public int readUnsigned3Bytes() throws IOException { return super.readUnsigned3Bytes(); }
			@Override
			public long readUnsigned4Bytes() throws IOException { return super.readUnsigned4Bytes(); }
			@Override
			public long readUnsigned5Bytes() throws IOException { return super.readUnsigned5Bytes(); }
			@Override
			public long readUnsigned6Bytes() throws IOException { return super.readUnsigned6Bytes(); }
			@Override
			public long readUnsigned7Bytes() throws IOException { return super.readUnsigned7Bytes(); }
			@Override
			public long readSigned8Bytes() throws IOException { return super.readSigned8Bytes(); }

			@Override
			public void readFully(byte[] buf, int off, int len) throws IOException { super.readFully(buf, off, len); }

			@Override
			public byte readByteAt(long pos) throws IOException { return super.readByteAt(pos); }
			@Override
			public int readUnsigned2BytesAt(long pos) throws IOException { return super.readUnsigned2BytesAt(pos); }
			@Override
			public int readUnsigned3BytesAt(long pos) throws IOException { return super.readUnsigned3BytesAt(pos); }
			@Override
			public long readUnsigned4BytesAt(long pos) throws IOException { return super.readUnsigned4BytesAt(pos); }
			@Override
			public long readUnsigned5BytesAt(long pos) throws IOException { return super.readUnsigned5BytesAt(pos); }
			@Override
			public long readUnsigned6BytesAt(long pos) throws IOException { return super.readUnsigned6BytesAt(pos); }
			@Override
			public long readUnsigned7BytesAt(long pos) throws IOException { return super.readUnsigned7BytesAt(pos); }
			@Override
			public long readSigned8BytesAt(long pos) throws IOException { return super.readSigned8BytesAt(pos); }

			@Override
			public void readFullyAt(long pos, byte[] buf, int off, int len) throws IOException { super.readFullyAt(pos, buf, off, len); }

		}
		
		/** Writable IO. */
		public static class Writable extends BigEndian implements BytesDataIO.Writable.Seekable {
			
			/**
			 * Constructor.
			 * @param buf byte array to use
			 * @param off start position in the array, that will be considered as position 0
			 * @param len number of bytes to used, that will be considered as the size of this IO
			 */
			public Writable(byte[] buf, int off, int len) {
				super(buf, off, len);
			}

			/**
			 * Constructor.
			 * @param buf byte array to use
			 */
			public Writable(byte[] buf) {
				this(buf, 0, buf.length);
			}
			
			@Override
			public void writeByte(byte value) throws IOException { super.writeByte(value); }
			@Override
			public void writeUnsigned2Bytes(int value) throws IOException { super.writeUnsigned2Bytes(value); }
			@Override
			public void writeUnsigned3Bytes(int value) throws IOException { super.writeUnsigned3Bytes(value); }
			@Override
			public void writeUnsigned4Bytes(long value) throws IOException { super.writeUnsigned4Bytes(value); }
			@Override
			public void writeUnsigned5Bytes(long value) throws IOException { super.writeUnsigned5Bytes(value); }
			@Override
			public void writeUnsigned6Bytes(long value) throws IOException { super.writeUnsigned6Bytes(value); }
			@Override
			public void writeUnsigned7Bytes(long value) throws IOException { super.writeUnsigned7Bytes(value); }
			@Override
			public void writeSigned8Bytes(long value) throws IOException { super.writeSigned8Bytes(value); }

			@Override
			public void writeFully(byte[] buf, int off, int len) throws IOException { super.writeFully(buf, off, len); }
			
			@Override
			public void writeByteAt(long pos, byte value) throws IOException { super.writeByteAt(pos, value); }
			@Override
			public void writeUnsigned2BytesAt(long pos, int value) throws IOException { super.writeUnsigned2BytesAt(pos, value); }
			@Override
			public void writeUnsigned3BytesAt(long pos, int value) throws IOException { super.writeUnsigned3BytesAt(pos, value); }
			@Override
			public void writeUnsigned4BytesAt(long pos, long value) throws IOException { super.writeUnsigned4BytesAt(pos, value); }
			@Override
			public void writeUnsigned5BytesAt(long pos, long value) throws IOException { super.writeUnsigned5BytesAt(pos, value); }
			@Override
			public void writeUnsigned6BytesAt(long pos, long value) throws IOException { super.writeUnsigned6BytesAt(pos, value); }
			@Override
			public void writeUnsigned7BytesAt(long pos, long value) throws IOException { super.writeUnsigned7BytesAt(pos, value); }
			@Override
			public void writeSigned8BytesAt(long pos, long value) throws IOException { super.writeSigned8BytesAt(pos, value); }

			@Override
			public void writeFullyAt(long pos, byte[] buf, int off, int len) throws IOException { super.writeFullyAt(pos, buf, off, len); }

		}
		
		/** Readable and Writable IO. */
		public static class ReadWrite extends Readable implements BytesDataIO.Writable.Seekable {
			
			/**
			 * Constructor.
			 * @param buf byte array to use
			 * @param off start position in the array, that will be considered as position 0
			 * @param len number of bytes to used, that will be considered as the size of this IO
			 */
			public ReadWrite(byte[] buf, int off, int len) {
				super(buf, off, len);
			}
			
			/**
			 * Constructor.
			 * @param buf byte array to use
			 */
			public ReadWrite(byte[] buf) {
				this(buf, 0, buf.length);
			}
			
			@Override
			public void writeByte(byte value) throws IOException { super.writeByte(value); }
			@Override
			public void writeUnsigned2Bytes(int value) throws IOException { super.writeUnsigned2Bytes(value); }
			@Override
			public void writeUnsigned3Bytes(int value) throws IOException { super.writeUnsigned3Bytes(value); }
			@Override
			public void writeUnsigned4Bytes(long value) throws IOException { super.writeUnsigned4Bytes(value); }
			@Override
			public void writeUnsigned5Bytes(long value) throws IOException { super.writeUnsigned5Bytes(value); }
			@Override
			public void writeUnsigned6Bytes(long value) throws IOException { super.writeUnsigned6Bytes(value); }
			@Override
			public void writeUnsigned7Bytes(long value) throws IOException { super.writeUnsigned7Bytes(value); }
			@Override
			public void writeSigned8Bytes(long value) throws IOException { super.writeSigned8Bytes(value); }

			@Override
			public void writeFully(byte[] buf, int off, int len) throws IOException { super.writeFully(buf, off, len); }
			
			@Override
			public void writeByteAt(long pos, byte value) throws IOException { super.writeByteAt(pos, value); }
			@Override
			public void writeUnsigned2BytesAt(long pos, int value) throws IOException { super.writeUnsigned2BytesAt(pos, value); }
			@Override
			public void writeUnsigned3BytesAt(long pos, int value) throws IOException { super.writeUnsigned3BytesAt(pos, value); }
			@Override
			public void writeUnsigned4BytesAt(long pos, long value) throws IOException { super.writeUnsigned4BytesAt(pos, value); }
			@Override
			public void writeUnsigned5BytesAt(long pos, long value) throws IOException { super.writeUnsigned5BytesAt(pos, value); }
			@Override
			public void writeUnsigned6BytesAt(long pos, long value) throws IOException { super.writeUnsigned6BytesAt(pos, value); }
			@Override
			public void writeUnsigned7BytesAt(long pos, long value) throws IOException { super.writeUnsigned7BytesAt(pos, value); }
			@Override
			public void writeSigned8BytesAt(long pos, long value) throws IOException { super.writeSigned8BytesAt(pos, value); }

			@Override
			public void writeFullyAt(long pos, byte[] buf, int off, int len) throws IOException { super.writeFullyAt(pos, buf, off, len); }

		}
		
	}
	
}

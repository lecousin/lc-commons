package net.lecousin.commons.io.data;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;

import net.lecousin.commons.io.AbstractIO;

/**
 * Implementation of BytesDataIO.Readable, using a list of ByteBuffer.
 */
public abstract class CompositeByteBuffersReadableDataIO extends AbstractIO implements BytesDataIO.Readable {
	
	protected LinkedList<ByteBuffer> data;
	protected ByteBuffer current = null;
	
	protected CompositeByteBuffersReadableDataIO(List<ByteBuffer> list) {
		this.data = new LinkedList<>(list);
	}
	
	protected CompositeByteBuffersReadableDataIO() {
		this.data = new LinkedList<>();
	}
	
	/** Add a buffer at the end of this IO.
	 * @param buffer buffer to append
	 */
	public void add(ByteBuffer buffer) {
		data.add(buffer);
	}
	
	@Override
	protected void closeInternal() {
		data = null;
		current = null;
	}
	
	@Override
	public void readFully(byte[] buf, int off, int len) throws IOException {
		if (data == null) throw new ClosedChannelException();
		DataChecks.checkByteArray(buf, off, len);
		if (len == 0) return;
		
		if (current == null) {
			if (data.isEmpty()) throw new EOFException();
			current = data.removeFirst();
		}
		while (len > 0) {
			while (!current.hasRemaining()) {
				if (data.isEmpty()) throw new EOFException();
				current = data.removeFirst();
			}
			int l = Math.min(current.remaining(), len);
			current.get(buf, off, l);
			off += l;
			len -= l;
		}
	}

	@Override
	public byte readByte() throws IOException {
		if (data == null) throw new ClosedChannelException();
		if (current == null) {
			if (data.isEmpty()) throw new EOFException();
			current = data.removeFirst();
		}
		while (!current.hasRemaining()) {
			if (data.isEmpty()) throw new EOFException();
			current = data.removeFirst();
		}
		return current.get();
	}
	
	// CHECKSTYLE DISABLE: MagicNumber
	
	/** Little-Endian implementation. */
	public static class LittleEndian extends CompositeByteBuffersReadableDataIO {
	
		/** Constructor.
		 * @param list buffers
		 */
		public LittleEndian(List<ByteBuffer> list) {
			super(list);
		}
		
		/** Default constructor without any data. */
		public LittleEndian() {
			// default
		}
		
		@Override
		public Endianness getEndianness() {
			return Endianness.LITTLE_ENDIAN;
		}
		
		@Override
		public int readUnsigned2Bytes() throws IOException {
			return (readByte() & 0xFF) |
				((readByte() & 0xFF) << 8);
		}
		
		@Override
		public int readUnsigned3Bytes() throws IOException {
			return (readByte() & 0xFF) |
				((readByte() & 0xFF) << 8) |
				((readByte() & 0xFF) << 16);
		}
		
		@Override
		public long readUnsigned4Bytes() throws IOException {
			return (readByte() & 0xFF) |
				((readByte() & 0xFF) << 8) |
				((readByte() & 0xFF) << 16) |
				(((long) (readByte() & 0xFF)) << 24);
		}
		
		@Override
		public long readUnsigned5Bytes() throws IOException {
			return (readByte() & 0xFF) |
				((readByte() & 0xFF) << 8) |
				((readByte() & 0xFF) << 16) |
				(((long) (readByte() & 0xFF)) << 24) |
				(((long) (readByte() & 0xFF)) << 32);
		}
		
		@Override
		public long readUnsigned6Bytes() throws IOException {
			return (readByte() & 0xFF) |
				((readByte() & 0xFF) << 8) |
				((readByte() & 0xFF) << 16) |
				(((long) (readByte() & 0xFF)) << 24) |
				(((long) (readByte() & 0xFF)) << 32) |
				(((long) (readByte() & 0xFF)) << 40);
		}
		
		@Override
		public long readUnsigned7Bytes() throws IOException {
			return (readByte() & 0xFF) |
				((readByte() & 0xFF) << 8) |
				((readByte() & 0xFF) << 16) |
				(((long) (readByte() & 0xFF)) << 24) |
				(((long) (readByte() & 0xFF)) << 32) |
				(((long) (readByte() & 0xFF)) << 40) |
				(((long) (readByte() & 0xFF)) << 48);
		}
		
		@Override
		public long readSigned8Bytes() throws IOException {
			return (readByte() & 0xFF) |
				((readByte() & 0xFF) << 8) |
				((readByte() & 0xFF) << 16) |
				(((long) (readByte() & 0xFF)) << 24) |
				(((long) (readByte() & 0xFF)) << 32) |
				(((long) (readByte() & 0xFF)) << 40) |
				(((long) (readByte() & 0xFF)) << 48) |
				(((long) (readByte() & 0xFF)) << 56);
		}
		
	}
	
	/** Big-Endian implementation. */
	public static class BigEndian extends CompositeByteBuffersReadableDataIO {
		
		/** Constuctor.
		 * @param list buffers
		 */
		public BigEndian(List<ByteBuffer> list) {
			super(list);
		}
		
		/** Default constructor, without any data. */
		public BigEndian() {
			// default
		}
		
		@Override
		public Endianness getEndianness() {
			return Endianness.BIG_ENDIAN;
		}
		
		@Override
		public int readUnsigned2Bytes() throws IOException {
			return ((readByte() & 0xFF) << 8) |
				(readByte() & 0xFF);
		}
		
		@Override
		public int readUnsigned3Bytes() throws IOException {
			return ((readByte() & 0xFF) << 16) |
				((readByte() & 0xFF) << 8) |
				(readByte() & 0xFF);
		}
		
		@Override
		public long readUnsigned4Bytes() throws IOException {
			return (((long) (readByte() & 0xFF)) << 24) |
				((readByte() & 0xFF) << 16) |
				((readByte() & 0xFF) << 8) |
				(readByte() & 0xFF);
		}
		
		@Override
		public long readUnsigned5Bytes() throws IOException {
			return (((long) (readByte() & 0xFF)) << 32) |
				(((long) (readByte() & 0xFF)) << 24) |
				((readByte() & 0xFF) << 16) |
				((readByte() & 0xFF) << 8) |
				(readByte() & 0xFF);
		}
		
		@Override
		public long readUnsigned6Bytes() throws IOException {
			return (((long) (readByte() & 0xFF)) << 40) |
				(((long) (readByte() & 0xFF)) << 32) |
				(((long) (readByte() & 0xFF)) << 24) |
				((readByte() & 0xFF) << 16) |
				((readByte() & 0xFF) << 8) |
				(readByte() & 0xFF);
		}
		
		@Override
		public long readUnsigned7Bytes() throws IOException {
			return (((long) (readByte() & 0xFF)) << 48) |
				(((long) (readByte() & 0xFF)) << 40) |
				(((long) (readByte() & 0xFF)) << 32) |
				(((long) (readByte() & 0xFF)) << 24) |
				((readByte() & 0xFF) << 16) |
				((readByte() & 0xFF) << 8) |
				(readByte() & 0xFF);
		}
		
		@Override
		public long readSigned8Bytes() throws IOException {
			return (((long) (readByte() & 0xFF)) << 56) |
				(((long) (readByte() & 0xFF)) << 48) |
				(((long) (readByte() & 0xFF)) << 40) |
				(((long) (readByte() & 0xFF)) << 32) |
				(((long) (readByte() & 0xFF)) << 24) |
				((readByte() & 0xFF) << 16) |
				((readByte() & 0xFF) << 8) |
				(readByte() & 0xFF);
		}
		
	}
	
}

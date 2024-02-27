package net.lecousin.commons.io.bytes.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

public abstract class TestBytesData {

	protected BytesData data;
	
	protected TestBytesData(BytesData data) {
		this.data = data;
	}
	
	public static class TestDataLittleEndian extends TestBytesData {
		public TestDataLittleEndian() {
			super(BytesData.LE);
		}
	}
	
	public static class TestDataBigEndian extends TestBytesData {
		public TestDataBigEndian() {
			super(BytesData.BE);
		}
	}
	
	@Test
	void testUnsigned2Bytes() {
		byte[] buf = new byte[10];
		int[] values = new int[] { 0, 1, 2, 3, 0xFF, 0x100, 0x1FF, 0x200, 0x7FF, 0x800, 0x7000, 0x7FFF, 0x8000, 0x8001, 0xFFFE, 0xFFFF }; 
		for (int i : values) {
			data.writeUnsigned2Bytes(buf, i);
			assertEquals(i, data.readUnsigned2Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 4);
			data.writeUnsigned2Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 4);
			assertEquals(i, data.readUnsigned2Bytes(b));
			data.writeUnsigned2Bytes(buf, 3, i);
			assertEquals(i, data.readUnsigned2Bytes(buf, 3));
			assertEquals(i, data.readUnsigned2Bytes(data.getUnsigned2Bytes(i)));
		}
	}
	
	@Test
	void testSigned2Bytes() {
		byte[] buf = new byte[10];
		short[] values = new short[] { Short.MIN_VALUE, -1, 0, 1, 2, 0xFF, 0x100, 0x1FF, 0x200, 0x7FF, 0x800, 0x7000, 0x7FFE, 0x7FFF }; 
		for (short i : values) {
			data.writeSigned2Bytes(buf, i);
			assertEquals(i, data.readSigned2Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 4);
			data.writeSigned2Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 4);
			assertEquals(i, data.readSigned2Bytes(b));
			data.writeSigned2Bytes(buf, 3, i);
			assertEquals(i, data.readSigned2Bytes(buf, 3));
			assertEquals(i, data.readSigned2Bytes(data.getSigned2Bytes(i)));
		}
	}
	
	@Test
	void testShort() {
		byte[] buf = new byte[10];
		short[] values = new short[] { Short.MIN_VALUE, -1, 0, 1, 2, 0xFF, 0x100, 0x1FF, 0x200, 0x7FF, 0x800, 0x7000, 0x7FFE, 0x7FFF }; 
		for (short i : values) {
			data.writeShort(buf, i);
			assertEquals(i, data.readShort(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 4);
			data.writeShort(b, i);
			b = ByteBuffer.wrap(buf, 6, 4);
			assertEquals(i, data.readShort(b));
			data.writeShort(buf, 3, i);
			assertEquals(i, data.readShort(buf, 3));
			assertEquals(i, data.readShort(data.getShort(i)));
		}
	}
	
	@Test
	void testUnsigned3Bytes() {
		byte[] buf = new byte[10];
		int[] values = new int[] { 0, 1, 2, 3, 0xFF, 0x100, 0x1FF, 0x200, 0x7FF, 0x800, 0x8001, 0xFFFE, 0xFFFF, 0x10000, 0x7FFFFF, 0x800000, 0xFFFFFE, 0xFFFFFF }; 
		for (int i : values) {
			data.writeUnsigned3Bytes(buf, i);
			assertEquals(i, data.readUnsigned3Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 4);
			data.writeUnsigned3Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 4);
			assertEquals(i, data.readUnsigned3Bytes(b));
			data.writeUnsigned3Bytes(buf, 3, i);
			assertEquals(i, data.readUnsigned3Bytes(buf, 3));
			assertEquals(i, data.readUnsigned3Bytes(data.getUnsigned3Bytes(i)));
		}
	}
	
	@Test
	void testSigned3Bytes() {
		byte[] buf = new byte[10];
		int[] values = new int[] { -0x800000, -1, 0, 1, 0xFF, 0x100, 0x700000, 0x7FFF00, 0x7FFFFE, 0x7FFFFF }; 
		for (int i : values) {
			data.writeSigned3Bytes(buf, i);
			assertEquals(i, data.readSigned3Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 4);
			data.writeSigned3Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 4);
			assertEquals(i, data.readSigned3Bytes(b));
			data.writeSigned3Bytes(buf, 3, i);
			assertEquals(i, data.readSigned3Bytes(buf, 3));
			assertEquals(i, data.readSigned3Bytes(data.getSigned3Bytes(i)));
		}
	}
	
	@Test
	void testUnsigned4Bytes() {
		byte[] buf = new byte[20];
		long[] values = new long[] { 0, 1, 2, 3, 0xFF, 0x100, 0x01234567, 0x76543210, 0x7FFFFFFE, 0x7FFFFFFF, 0x80000000L, 0x80000102L, 0xFFFFFFFEL, 0xFFFFFFFFL }; 
		for (long i : values) {
			data.writeUnsigned4Bytes(buf, i);
			assertEquals(i, data.readUnsigned4Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 10);
			data.writeUnsigned4Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 10);
			assertEquals(i, data.readUnsigned4Bytes(b));
			data.writeUnsigned4Bytes(buf, 3, i);
			assertEquals(i, data.readUnsigned4Bytes(buf, 3));
			assertEquals(i, data.readUnsigned4Bytes(data.getUnsigned4Bytes(i)));
		}
	}
	
	@Test
	void testSigned4Bytes() {
		byte[] buf = new byte[20];
		int[] values = new int[] { -0x80000000, -1, 0, 1, 0x01234567, 0x76543210, 0x7FFFFFFE, 0x7FFFFFFF }; 
		for (int i : values) {
			data.writeSigned4Bytes(buf, i);
			assertEquals(i, data.readSigned4Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 10);
			data.writeSigned4Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 10);
			assertEquals(i, data.readSigned4Bytes(b));
			data.writeSigned4Bytes(buf, 3, i);
			assertEquals(i, data.readSigned4Bytes(buf, 3));
			assertEquals(i, data.readSigned4Bytes(data.getSigned4Bytes(i)));
		}
	}
	
	@Test
	void testInteger() {
		byte[] buf = new byte[20];
		int[] values = new int[] { -0x80000000, -1, 0, 1, 0x01234567, 0x76543210, 0x7FFFFFFE, 0x7FFFFFFF }; 
		for (int i : values) {
			data.writeInteger(buf, i);
			assertEquals(i, data.readInteger(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 10);
			data.writeInteger(b, i);
			b = ByteBuffer.wrap(buf, 6, 10);
			assertEquals(i, data.readInteger(b));
			data.writeInteger(buf, 3, i);
			assertEquals(i, data.readInteger(buf, 3));
			assertEquals(i, data.readInteger(data.getInteger(i)));
		}
	}
	
	@Test
	void testUnsigned5Bytes() {
		byte[] buf = new byte[20];
		long[] values = new long[] { 0, 1, 2, 3, 0xFF, 0x100, 0x0123456789L, 0x7654321012L, 0x7FFFFFFFFEL, 0x7FFFFFFFFFL, 0x8000000000L, 0x8000000102L, 0xFFFFFFFFFEL, 0xFFFFFFFFFFL }; 
		for (long i : values) {
			data.writeUnsigned5Bytes(buf, i);
			assertEquals(i, data.readUnsigned5Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 10);
			data.writeUnsigned5Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 10);
			assertEquals(i, data.readUnsigned5Bytes(b));
			data.writeUnsigned5Bytes(buf, 3, i);
			assertEquals(i, data.readUnsigned5Bytes(buf, 3));
			assertEquals(i, data.readUnsigned5Bytes(data.getUnsigned5Bytes(i)));
		}
	}
	
	@Test
	void testSigned5Bytes() {
		byte[] buf = new byte[20];
		long[] values = new long[] { -0x8000000000L, -1L, 0, 1, 0x0123456789L, 0x7654321012L, 0x7FFFFFFFFEL, 0x7FFFFFFFFFL }; 
		for (long i : values) {
			data.writeSigned5Bytes(buf, i);
			assertEquals(i, data.readSigned5Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 10);
			data.writeSigned5Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 10);
			assertEquals(i, data.readSigned5Bytes(b));
			data.writeSigned5Bytes(buf, 3, i);
			assertEquals(i, data.readSigned5Bytes(buf, 3));
			assertEquals(i, data.readSigned5Bytes(data.getSigned5Bytes(i)));
		}
	}
	
	@Test
	void testUnsigned6Bytes() {
		byte[] buf = new byte[20];
		long[] values = new long[] { 0, 1, 2, 3, 0xFF, 0x100, 0x012345678901L, 0x765432101234L, 0x7FFFFFFFFFFEL, 0x7FFFFFFFFFFFL, 0x800000000000L, 0x800000010203L, 0xFFFFFFFFFFFEL, 0xFFFFFFFFFFFFL }; 
		for (long i : values) {
			data.writeUnsigned6Bytes(buf, i);
			assertEquals(i, data.readUnsigned6Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 10);
			data.writeUnsigned6Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 10);
			assertEquals(i, data.readUnsigned6Bytes(b));
			data.writeUnsigned6Bytes(buf, 3, i);
			assertEquals(i, data.readUnsigned6Bytes(buf, 3));
			assertEquals(i, data.readUnsigned6Bytes(data.getUnsigned6Bytes(i)));
		}
	}
	
	@Test
	void testSigned6Bytes() {
		byte[] buf = new byte[20];
		long[] values = new long[] { -0x800000000000L, -1L, 0, 1, 0x012345678901L, 0x765432101234L, 0x7FFFFFFFFFFEL, 0x7FFFFFFFFFFFL }; 
		for (long i : values) {
			data.writeSigned6Bytes(buf, i);
			assertEquals(i, data.readSigned6Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 10);
			data.writeSigned6Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 10);
			assertEquals(i, data.readSigned6Bytes(b));
			data.writeSigned6Bytes(buf, 3, i);
			assertEquals(i, data.readSigned6Bytes(buf, 3));
			assertEquals(i, data.readSigned6Bytes(data.getSigned6Bytes(i)));
		}
	}
	
	@Test
	void testUnsigned7Bytes() {
		byte[] buf = new byte[20];
		long[] values = new long[] { 0, 1, 2, 3, 0xFF, 0x100, 0x01234567890123L, 0x76543210123456L, 0x7FFFFFFFFFFFFEL, 0x7FFFFFFFFFFFFFL, 0x80000000000000L, 0x80000001020304L, 0xFFFFFFFFFFFFFEL, 0xFFFFFFFFFFFFFFL }; 
		for (long i : values) {
			data.writeUnsigned7Bytes(buf, i);
			assertEquals(i, data.readUnsigned7Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 10);
			data.writeUnsigned7Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 10);
			assertEquals(i, data.readUnsigned7Bytes(b));
			data.writeUnsigned7Bytes(buf, 3, i);
			assertEquals(i, data.readUnsigned7Bytes(buf, 3));
			assertEquals(i, data.readUnsigned7Bytes(data.getUnsigned7Bytes(i)));
		}
	}
	
	@Test
	void testSigned7Bytes() {
		byte[] buf = new byte[20];
		long[] values = new long[] { -0x80000000000000L, -1L, 0, 1, 0x01234567890123L, 0x76543210123456L, 0x7FFFFFFFFFFFFEL, 0x7FFFFFFFFFFFFFL }; 
		for (long i : values) {
			data.writeSigned7Bytes(buf, i);
			assertEquals(i, data.readSigned7Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 10);
			data.writeSigned7Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 10);
			assertEquals(i, data.readSigned7Bytes(b));
			data.writeSigned7Bytes(buf, 3, i);
			assertEquals(i, data.readSigned7Bytes(buf, 3));
			assertEquals(i, data.readSigned7Bytes(data.getSigned7Bytes(i)));
		}
	}
	
	@Test
	void testSigned8Bytes() {
		byte[] buf = new byte[20];
		long[] values = new long[] { -0x8000000000000000L, -1L, 0, 1, 0x0123456789012345L, 0x7654321012345678L, 0x7FFFFFFFFFFFFFFEL, 0x7FFFFFFFFFFFFFFFL }; 
		for (long i : values) {
			data.writeSigned8Bytes(buf, i);
			assertEquals(i, data.readSigned8Bytes(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 10);
			data.writeSigned8Bytes(b, i);
			b = ByteBuffer.wrap(buf, 6, 10);
			assertEquals(i, data.readSigned8Bytes(b));
			data.writeSigned8Bytes(buf, 3, i);
			assertEquals(i, data.readSigned8Bytes(buf, 3));
			assertEquals(i, data.readSigned8Bytes(data.getSigned8Bytes(i)));
		}
	}
	
	@Test
	void testLong() {
		byte[] buf = new byte[20];
		long[] values = new long[] { -0x8000000000000000L, -1L, 0, 1, 0x0123456789012345L, 0x7654321012345678L, 0x7FFFFFFFFFFFFFFEL, 0x7FFFFFFFFFFFFFFFL }; 
		for (long i : values) {
			data.writeLong(buf, i);
			assertEquals(i, data.readLong(buf));
			ByteBuffer b = ByteBuffer.wrap(buf, 6, 10);
			data.writeLong(b, i);
			b = ByteBuffer.wrap(buf, 6, 10);
			assertEquals(i, data.readLong(b));
			data.writeLong(buf, 3, i);
			assertEquals(i, data.readLong(buf, 3));
			assertEquals(i, data.readLong(data.getLong(i)));
		}
	}
	
	@Test
	void testUnsignedBytes() {
		byte[] buf = new byte[20];
		long[] values = new long[] {
			0, 1, 2, 3, 0xFE, 0xFF,
			0x100, 0x7FE, 0x7FF, 0x800, 0x801, 0xFFE, 0xFFF,
			0x1000, 0x7FFE, 0x7FFF, 0x8000, 0x8001, 0xFFFE, 0xFFFF,
			0x10000, 0x7FFFE, 0x7FFFF, 0x80000, 0x80001, 0xFFFFE, 0xFFFFF,
			0x100000, 0x7FFFFE, 0x7FFFFF, 0x800000, 0x800001, 0xFFFFFE, 0xFFFFFF,
			0x1000000, 0x7FFFFFE, 0x7FFFFFF, 0x8000000, 0x8000001, 0xFFFFFFE, 0xFFFFFFF,
			0x10000000, 0x7FFFFFFE, 0x7FFFFFFF, 0x80000000L, 0x80000001L, 0xFFFFFFFEL, 0xFFFFFFFFL,
			0x100000000L, 0x7FFFFFFFEL, 0x7FFFFFFFFL, 0x800000000L, 0x800000001L, 0xFFFFFFFFEL, 0xFFFFFFFFFL,
			0x1000000000L, 0x7FFFFFFFFEL, 0x7FFFFFFFFFL, 0x8000000000L, 0x8000000001L, 0xFFFFFFFFFEL, 0xFFFFFFFFFFL,
			0x10000000000L, 0x7FFFFFFFFFEL, 0x7FFFFFFFFFFL, 0x80000000000L, 0x80000000001L, 0xFFFFFFFFFFEL, 0xFFFFFFFFFFFL,
			0x100000000000L, 0x7FFFFFFFFFFEL, 0x7FFFFFFFFFFFL, 0x800000000000L, 0x800000000001L, 0xFFFFFFFFFFFEL, 0xFFFFFFFFFFFFL,
			0x1000000000000L, 0x7FFFFFFFFFFFEL, 0x7FFFFFFFFFFFFL, 0x8000000000000L, 0x8000000000001L, 0xFFFFFFFFFFFFEL, 0xFFFFFFFFFFFFFL,
		};
		for (int nb = 1; nb <= 7; nb++) {
			long max = 256;
			for (int i = 1; i < nb; ++i) max = max * 256;
			max--;
			for (long value : values) {
				if (value > max) break;
				data.writeUnsignedBytes(nb, value, buf);
				assertEquals(value, data.readUnsignedBytes(nb, buf));
				data.writeUnsignedBytes(nb, value, buf, 3);
				assertEquals(value, data.readUnsignedBytes(nb, buf, 3));
				ByteBuffer b = ByteBuffer.wrap(buf, 5, 10);
				data.writeUnsignedBytes(nb, value, b);
				b = ByteBuffer.wrap(buf, 5, 10);
				assertEquals(value, data.readUnsignedBytes(nb, b));
				assertEquals(value, data.readUnsignedBytes(nb, data.getUnsignedBytes(nb, value)));
			}
		}
		ByteBuffer b = ByteBuffer.wrap(buf, 5, 10);
		assertThrows(IllegalArgumentException.class, () -> data.writeUnsignedBytes(8, 0, buf));
		assertThrows(IllegalArgumentException.class, () -> data.writeUnsignedBytes(8, 0, buf, 1));
		assertThrows(IllegalArgumentException.class, () -> data.writeUnsignedBytes(8, 0, b));
		assertThrows(IllegalArgumentException.class, () -> data.writeUnsignedBytes(0, 0, buf));
		assertThrows(IllegalArgumentException.class, () -> data.writeUnsignedBytes(0, 0, buf, 1));
		assertThrows(IllegalArgumentException.class, () -> data.writeUnsignedBytes(0, 0, b));
		assertThrows(IllegalArgumentException.class, () -> data.writeUnsignedBytes(-1, 0, buf));
		assertThrows(IllegalArgumentException.class, () -> data.writeUnsignedBytes(-1, 0, buf, 1));
		assertThrows(IllegalArgumentException.class, () -> data.writeUnsignedBytes(-1, 0, b));
		assertThrows(IllegalArgumentException.class, () -> data.readUnsignedBytes(-1, b));
		assertThrows(IllegalArgumentException.class, () -> data.readUnsignedBytes(-1, buf, 1));
		assertThrows(IllegalArgumentException.class, () -> data.readUnsignedBytes(-1, buf));
		assertThrows(IllegalArgumentException.class, () -> data.getUnsignedBytes(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> data.readUnsignedBytes(8, b));
		assertThrows(IllegalArgumentException.class, () -> data.readUnsignedBytes(8, buf, 1));
		assertThrows(IllegalArgumentException.class, () -> data.readUnsignedBytes(8, buf));
		assertThrows(IllegalArgumentException.class, () -> data.getUnsignedBytes(8, 0));
	}
	
	@Test
	void testSignedBytes() {
		byte[] buf = new byte[20];
		long[] values = new long[] {
			-0x8000000000000000L, -0x7FFFFFFFFFFFFFFFL, -0x7FFFFFFFFFFFFFFEL, -0x0FFFFFFFFFFFFFFFL, -0x01FFFFFFFFFFFFFFL,
			-0x80000000000000L, -0x7FFFFFFFFFFFFFL, -0x7FFFFFFFFFFFFEL, -0x0FFFFFFFFFFFFFL, -0x01FFFFFFFFFFFFL,
			-0x800000000000L, -0x7FFFFFFFFFFFL, -0x7FFFFFFFFFFEL, -0x0FFFFFFFFFFFL, -0x01FFFFFFFFFFL,
			-0x8000000000L, -0x7FFFFFFFFFL, -0x7FFFFFFFFEL, -0x0FFFFFFFFFL, -0x01FFFFFFFFL,
			-0x80000000L, -0x7FFFFFFFL, -0x7FFFFFFEL, -0x0FFFFFFFL, -0x01FFFFFFL,
			-0x800000L, -0x7FFFFFL, -0x7FFFFEL, -0x0FFFFFL, -0x01FFFFL,
			-0x8000L, -0x7FFFL, -0x7FFEL, -0x0FFFL, -0x01FFL,
			-0x80L, -0x7FL, -0x7EL, -0x0FL, -0x01L,
			0, 1, 2, 3, 0xFE, 0xFF,
			0x100, 0x7FE, 0x7FF, 0x800, 0x801, 0xFFE, 0xFFF,
			0x1000, 0x7FFE, 0x7FFF, 0x8000, 0x8001, 0xFFFE, 0xFFFF,
			0x10000, 0x7FFFE, 0x7FFFF, 0x80000, 0x80001, 0xFFFFE, 0xFFFFF,
			0x100000, 0x7FFFFE, 0x7FFFFF, 0x800000, 0x800001, 0xFFFFFE, 0xFFFFFF,
			0x1000000, 0x7FFFFFE, 0x7FFFFFF, 0x8000000, 0x8000001, 0xFFFFFFE, 0xFFFFFFF,
			0x10000000, 0x7FFFFFFE, 0x7FFFFFFF, 0x80000000L, 0x80000001L, 0xFFFFFFFEL, 0xFFFFFFFFL,
			0x100000000L, 0x7FFFFFFFEL, 0x7FFFFFFFFL, 0x800000000L, 0x800000001L, 0xFFFFFFFFEL, 0xFFFFFFFFFL,
			0x1000000000L, 0x7FFFFFFFFEL, 0x7FFFFFFFFFL, 0x8000000000L, 0x8000000001L, 0xFFFFFFFFFEL, 0xFFFFFFFFFFL,
			0x10000000000L, 0x7FFFFFFFFFEL, 0x7FFFFFFFFFFL, 0x80000000000L, 0x80000000001L, 0xFFFFFFFFFFEL, 0xFFFFFFFFFFFL,
			0x100000000000L, 0x7FFFFFFFFFFEL, 0x7FFFFFFFFFFFL, 0x800000000000L, 0x800000000001L, 0xFFFFFFFFFFFEL, 0xFFFFFFFFFFFFL,
			0x1000000000000L, 0x7FFFFFFFFFFFEL, 0x7FFFFFFFFFFFFL, 0x8000000000000L, 0x8000000000001L, 0xFFFFFFFFFFFFEL, 0xFFFFFFFFFFFFFL,
		};
		for (int nb = 1; nb <= 8; nb++) {
			long min, max;
			if (nb == 8) {
				min = Long.MIN_VALUE;
				max = Long.MAX_VALUE;
			} else {
				max = 256;
				for (int i = 1; i < nb; ++i) max = max * 256;
				max = max >> 1;
				min = -max;
				max--;
			}
			for (long value : values) {
				if (value < min) continue;
				if (value > max) break;
				data.writeSignedBytes(nb, value, buf);
				assertEquals(value, data.readSignedBytes(nb, buf));
				data.writeSignedBytes(nb, value, buf, 3);
				assertEquals(value, data.readSignedBytes(nb, buf, 3));
				ByteBuffer b = ByteBuffer.wrap(buf, 5, 10);
				data.writeSignedBytes(nb, value, b);
				b = ByteBuffer.wrap(buf, 5, 10);
				assertEquals(value, data.readSignedBytes(nb, b));
				assertEquals(value, data.readSignedBytes(nb, data.getSignedBytes(nb, value)));
			}
		}
		ByteBuffer b = ByteBuffer.wrap(buf, 5, 10);
		assertThrows(IllegalArgumentException.class, () -> data.writeSignedBytes(9, 0, buf));
		assertThrows(IllegalArgumentException.class, () -> data.writeSignedBytes(9, 0, buf, 1));
		assertThrows(IllegalArgumentException.class, () -> data.writeSignedBytes(9, 0, b));
		assertThrows(IllegalArgumentException.class, () -> data.writeSignedBytes(0, 0, buf));
		assertThrows(IllegalArgumentException.class, () -> data.writeSignedBytes(0, 0, buf, 1));
		assertThrows(IllegalArgumentException.class, () -> data.writeSignedBytes(0, 0, b));
		assertThrows(IllegalArgumentException.class, () -> data.writeSignedBytes(-1, 0, buf));
		assertThrows(IllegalArgumentException.class, () -> data.writeSignedBytes(-1, 0, buf, 1));
		assertThrows(IllegalArgumentException.class, () -> data.writeSignedBytes(-1, 0, b));
		assertThrows(IllegalArgumentException.class, () -> data.readSignedBytes(-1, b));
		assertThrows(IllegalArgumentException.class, () -> data.readSignedBytes(-1, buf, 1));
		assertThrows(IllegalArgumentException.class, () -> data.readSignedBytes(-1, buf));
		assertThrows(IllegalArgumentException.class, () -> data.getSignedBytes(-1, 0));
		assertThrows(IllegalArgumentException.class, () -> data.readSignedBytes(9, b));
		assertThrows(IllegalArgumentException.class, () -> data.readSignedBytes(9, buf, 1));
		assertThrows(IllegalArgumentException.class, () -> data.readSignedBytes(9, buf));
		assertThrows(IllegalArgumentException.class, () -> data.getSignedBytes(9, 0));
	}
	
}

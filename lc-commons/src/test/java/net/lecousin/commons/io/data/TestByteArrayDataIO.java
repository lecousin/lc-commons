package net.lecousin.commons.io.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import net.lecousin.commons.test.TestCase;

public class TestByteArrayDataIO {

	public static class TestReadable extends AbstractReadableSeekableBytesDataIOTest {
		
		@Override
		public List<? extends TestCase<byte[], BytesDataIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>(
					"LittleEndian with exact byte array",
					data -> new ByteArrayDataIO.LittleEndian.Readable(data)
				),
				new TestCase<>(
					"BigEndian with exact byte array",
					data -> new ByteArrayDataIO.BigEndian.Readable(data)
				),
				new TestCase<>(
					"LittleEndian with larger buffer and offset",
					data -> {
						byte[] buffer = new byte[data.length + 259];
						System.arraycopy(data, 0, buffer, 13, data.length);
						return new ByteArrayDataIO.LittleEndian.Readable(buffer, 13, data.length);
					}
				),
				new TestCase<>(
					"BigEndian with larger buffer and offset",
					data -> {
						byte[] buffer = new byte[data.length + 259];
						System.arraycopy(data, 0, buffer, 13, data.length);
						return new ByteArrayDataIO.BigEndian.Readable(buffer, 13, data.length);
					}
				)
			);
		}
		
	}
	
	public static class TestWritable extends AbstractWritableSeekableBytesDataIOTest {
		
		@Override
		public List<? extends TestCase<Integer, WritableSeekableTestCase>> getTestCases() {
			return List.of(
				new TestCase<>(
					"LittleEndian with exact byte array",
					size -> new WritableSeekableTestCase(new ByteArrayDataIO.LittleEndian.Writable(new byte[size]), null)
				),
				new TestCase<>(
					"BigEndian with exact byte array",
					size -> new WritableSeekableTestCase(new ByteArrayDataIO.BigEndian.Writable(new byte[size]), null)
				),
				new TestCase<>(
					"LittleEndian with larger buffer and offset",
					size -> new WritableSeekableTestCase(new ByteArrayDataIO.LittleEndian.Writable(new byte[size + 259], 13, size), null)
				),
				new TestCase<>(
					"BigEndian with larger buffer and offset",
					size -> new WritableSeekableTestCase(new ByteArrayDataIO.BigEndian.Writable(new byte[size + 259], 13, size), null)
				)
			);
		}
		
		@Override
		protected void checkWrittenData(BytesDataIO.Writable.Seekable stream, Object generatedData, byte[] expected) {
			ByteArrayDataIO io = (ByteArrayDataIO) stream;
			byte[] buffer = io.getBytes();
			int pos = buffer.length == expected.length ? 0 : 13;
			for (int i = 0; i < expected.length; ++i)
				assertEquals(expected[i], buffer[pos + i], "Byte " + i);
			assertEquals(expected.length, io.getEnd() - io.getStart());
		}
		
	}
	
	public static class TestReadWriteLittleEndian extends AbstractReadWriteBytesDataIO<ByteArrayDataIO.LittleEndian.ReadWrite> {
		@Override
		public List<? extends TestCase<Integer, ByteArrayDataIO.LittleEndian.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>(
					"exact byte array",
					size -> new ByteArrayDataIO.LittleEndian.ReadWrite(new byte[size])
				),
				new TestCase<>(
					"larger buffer and offset",
					size -> new ByteArrayDataIO.LittleEndian.ReadWrite(new byte[size + 259], 13, size)
				)
			);
		}
	}
	
	public static class TestReadWriteBigEndian extends AbstractReadWriteBytesDataIO<ByteArrayDataIO.BigEndian.ReadWrite> {
		@Override
		public List<? extends TestCase<Integer, ByteArrayDataIO.BigEndian.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>(
					"exact byte array",
					size -> new ByteArrayDataIO.BigEndian.ReadWrite(new byte[size])
				),
				new TestCase<>(
					"larger buffer and offset",
					size -> new ByteArrayDataIO.BigEndian.ReadWrite(new byte[size + 259], 13, size)
				)
			);
		}
	}
	
}

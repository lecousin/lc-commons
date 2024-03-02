package net.lecousin.commons.io.bytes.memory;

import java.nio.ByteOrder;
import java.util.List;

import net.lecousin.commons.io.bytes.data.AbstractReadWriteBytesDataBufferTest;
import net.lecousin.commons.io.bytes.data.BytesDataBuffer.ReadWrite;
import net.lecousin.commons.test.TestCase;

public class TestByteArrayDataBuffer {

	public static class TestReadWrite extends AbstractReadWriteBytesDataBufferTest {
		@Override
		public List<? extends TestCase<Integer, ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>("Exact buffer", size -> new ByteArray(new byte[size]).asBytesDataBuffer()),
				new TestCase<>("Larger buffer with offset = 0", size -> new ByteArray(new byte[size + 56], 0, size).asBytesDataBuffer(ByteOrder.BIG_ENDIAN)),
				new TestCase<>("Larger buffer with offset > 0", size -> new ByteArray(new byte[size + 56], 13, size).asBytesDataBuffer(ByteOrder.LITTLE_ENDIAN))
			);
		}
	}
	
}

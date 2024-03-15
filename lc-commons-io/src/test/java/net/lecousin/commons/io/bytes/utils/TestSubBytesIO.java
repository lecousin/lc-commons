package net.lecousin.commons.io.bytes.utils;

import java.util.List;

import org.junit.jupiter.api.Assertions;

import net.lecousin.commons.io.bytes.AbstractReadWriteBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractReadableSeekableBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.io.bytes.AbstractWritableSeekableBytesIOTest;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.memory.ByteArray;
import net.lecousin.commons.test.TestCase;

public class TestSubBytesIO {

	public static class TestReadable extends AbstractReadableSeekableBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("SubBytesIO of ByteArrayIO", content -> {
					byte[] b = new byte[content.length + 789];
					System.arraycopy(content, 0, b, 111, content.length);
					return SubBytesIO.fromReadable(new ByteArray(b).asBytesIO().asReadableSeekableBytesIO(), 111, content.length, true);
				})
			);
		}
	}
	
	public static class TestWritable extends AbstractWritableSeekableBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("SubBytesIO of ByteArrayIO", size -> {
					ByteArray ba = new ByteArray(new byte[size + 777]);
					BytesIO.Writable.Seekable sub = SubBytesIO.fromWritable(ba.asBytesIO().asWritableSeekableBytesIO(), 111, size, false);
					return new WritableTestCase<>(sub, ba);
				})
			);
		}
		
		@Override
		protected void checkWrittenData(BytesIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			ByteArray ba = (ByteArray) object;
			byte[] b = new byte[expected.length];
			System.arraycopy(ba.getArray(), 111, b, 0, expected.length);
			Assertions.assertArrayEquals(expected, b);
		}
	}
	
	public static class TestReadWrite extends AbstractReadWriteBytesIOTest<SubBytesIO.ReadWrite> {
		@Override
		public List<? extends TestCase<Integer, SubBytesIO.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>("SubBytesIO of ByteArrayIO", size -> {
					ByteArray ba = new ByteArray(new byte[size + 777]);
					return SubBytesIO.fromReadWrite(ba.asBytesIO(), 111, size, false);
				})
			);
		}
	}
	
}

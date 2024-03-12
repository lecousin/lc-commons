package net.lecousin.commons.io.bytes.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Assertions;

import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.io.bytes.data.AbstractReadWriteBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.AbstractReadableSeekableBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.AbstractWritableSeekableBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.BytesDataIO;
import net.lecousin.commons.io.bytes.data.BytesDataIO.Readable.Seekable;
import net.lecousin.commons.io.bytes.memory.ByteArray;
import net.lecousin.commons.test.TestCase;

public class TestSubBytesDataIO {

	public static class TestReadable extends AbstractReadableSeekableBytesDataIOTest {
		@Override
		public List<? extends TestCase<byte[], Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("SubBytesDataIO from ByteArray", content -> {
					byte[] b = new byte[content.length + 789];
					System.arraycopy(content, 0, b, 111, content.length);
					return SubBytesDataIO.fromReadable(new ByteArray(b).asBytesDataIO().asReadableSeekableBytesDataIO(), 111, content.length, true);
				})
			);
		}
	}

	public static class TestWritable extends AbstractWritableSeekableBytesDataIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesDataIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("SubBytesDataIO from ByteArray", size -> {
					ByteArray ba = new ByteArray(new byte[size + 777]);
					BytesDataIO.Writable.Seekable sub = SubBytesDataIO.fromWritable(ba.asBytesDataIO().asWritableSeekableBytesDataIO(), 111, size, false);
					return new WritableTestCase<>(sub, ba);
				})
			);
		}
		@Override
		protected void checkWrittenData(BytesDataIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			ByteArray ba = (ByteArray) object;
			byte[] b = new byte[expected.length];
			System.arraycopy(ba.getArray(), 111, b, 0, expected.length);
			Assertions.assertArrayEquals(expected, b);
		}
	}

	public static class TestReadWrite extends AbstractReadWriteBytesDataIOTest<SubBytesDataIO.ReadWrite> {
		@Override
		public List<? extends TestCase<Integer, SubBytesDataIO.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>("SubBytesDataIO from ByteArray", size -> {
					ByteArray ba = new ByteArray(new byte[size + 777]);
					return SubBytesDataIO.fromReadWrite(ba.asBytesDataIO(), 111, size, false);
				})
			);
		}
	}
	
}

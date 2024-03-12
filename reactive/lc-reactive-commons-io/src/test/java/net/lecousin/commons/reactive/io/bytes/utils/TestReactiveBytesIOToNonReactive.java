package net.lecousin.commons.reactive.io.bytes.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import net.lecousin.commons.io.bytes.AbstractReadWriteBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractReadableBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractReadableSeekableBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.io.bytes.AbstractWritableSeekableBytesIOTest;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.memory.ByteArray;
import net.lecousin.commons.reactive.io.bytes.ReactiveBytesIO;
import net.lecousin.commons.test.TestCase;

public class TestReactiveBytesIOToNonReactive {

	public static class TestReadable extends AbstractReadableBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesIO.Readable>> getTestCases() {
			return List.of(
				new TestCase<>("ByteArray -> IO -> Reactive -> IO", content ->
					ReactiveBytesIOToNonReactive.fromReadable(ReactiveBytesIO.fromByteArray(new ByteArray(content)))
				)
			);
		}
	}
	
	public static class TestReadableSeekable extends AbstractReadableSeekableBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("ByteArray -> IO -> Reactive -> IO", content ->
					ReactiveBytesIOToNonReactive.fromReadableSeekable(ReactiveBytesIO.fromByteArray(new ByteArray(content)))
				)
			);
		}
	}
	
	public static class TestWritable extends AbstractWritableBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<?, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("ByteArray -> IO -> Reactive -> IO", size -> {
					ByteArray ba = new ByteArray(new byte[size]);
					return new WritableTestCase<>(ReactiveBytesIOToNonReactive.fromWritable(ReactiveBytesIO.fromByteArray(ba)), ba);
				})
			);
		}
		@Override
		protected void checkWrittenData(BytesIO.Writable io, Object object, byte[] expected) throws Exception {
			ByteArray ba = (ByteArray) object;
			assertThat(ba.getArray()).containsExactly(expected);
		}
	}
	
	public static class TestWritableSeekable extends AbstractWritableSeekableBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("ByteArray -> IO -> Reactive -> IO", size -> {
					ByteArray ba = new ByteArray(new byte[size]);
					return new WritableTestCase<>(ReactiveBytesIOToNonReactive.fromWritableSeekable(ReactiveBytesIO.fromByteArray(ba)), ba);
				}),
				new TestCase<>("Resizable", size -> {
					ByteArray ba = new ByteArray(new byte[size]);
					return new WritableTestCase<>(ReactiveBytesIOToNonReactive.fromWritableSeekableResizable(ReactiveBytesIO.fromByteArray(ba)), ba);
				})
			);
		}
		@Override
		protected void checkWrittenData(BytesIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			ByteArray ba = (ByteArray) object;
			ba.trim();
			assertThat(ba.getArray()).containsExactly(expected);
		}
	}
	
	public static class TestReadWrite extends AbstractReadWriteBytesIOTest<BytesIO.ReadWrite> {
		@Override
		public List<? extends TestCase<Integer, BytesIO.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>("ByteArray -> IO -> Reactive -> IO", size -> 
					ReactiveBytesIOToNonReactive.fromReadWrite(ReactiveBytesIO.fromByteArray(new ByteArray(new byte[size])))
				),
				new TestCase<>("Resizable", size ->
					ReactiveBytesIOToNonReactive.fromReadWriteResizable(ReactiveBytesIO.fromByteArray(new ByteArray(new byte[size])))
				)
			);
		}
	}
	
}

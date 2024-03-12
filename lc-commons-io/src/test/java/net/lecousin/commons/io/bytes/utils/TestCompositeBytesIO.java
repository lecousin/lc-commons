package net.lecousin.commons.io.bytes.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Assertions;

import net.lecousin.commons.io.bytes.AbstractReadWriteBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractReadableBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractReadableSeekableBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.io.bytes.AbstractWritableSeekableBytesIOTest;
import net.lecousin.commons.io.bytes.BytesIO;
import net.lecousin.commons.io.bytes.memory.BufferedReadableBytesDataIO;
import net.lecousin.commons.io.bytes.memory.ByteArray;
import net.lecousin.commons.test.TestCase;

public class TestCompositeBytesIO {

	public static class TestReadable extends AbstractReadableBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesIO.Readable>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO from ByteArray garbageOnConsumed=false", content -> {
					try {
						BytesIO.ReadWrite io = new ByteArray(content).asBytesIO();
						return CompositeBytesIO.fromReadable(List.of(io), true, false);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO from ByteArray garbageOnConsumed=true", content -> {
					try {
						BytesIO.ReadWrite io = new ByteArray(content).asBytesIO();
						return CompositeBytesIO.fromReadable(List.of(io), true, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from ByteArray garbageOnConsumed=false", content -> {
					try {
						if (content.length == 0) return CompositeBytesIO.fromReadable(List.of(), true, false);
						BytesIO.ReadWrite io = new ByteArray(content).asBytesIO();
						BytesIO.ReadWrite sub1 = SubBytesIO.fromReadWrite(io, 0, content.length / 3, false);
						BytesIO.ReadWrite sub2 = SubBytesIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						BytesIO.ReadWrite sub3 = SubBytesIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						return CompositeBytesIO.fromReadable(List.of(sub1, sub2, sub3), true, false);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from ByteArray garbageOnConsumed=true", content -> {
					try {
						if (content.length == 0) return CompositeBytesIO.fromReadable(List.of(), true, true);
						BytesIO.ReadWrite io = new ByteArray(content).asBytesIO();
						BytesIO.ReadWrite sub1 = SubBytesIO.fromReadWrite(io, 0, content.length / 3, false);
						BytesIO.ReadWrite sub2 = SubBytesIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						BytesIO.ReadWrite sub3 = SubBytesIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						return CompositeBytesIO.fromReadable(List.of(sub1, sub2, sub3), true, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 BufferedReadableBytesData (unknown size)", content -> {
					try {
						if (content.length == 0) return CompositeBytesIO.fromReadable(List.of(), true, true);
						BytesIO.ReadWrite io = new ByteArray(content).asBytesIO();
						BytesIO.ReadWrite sub1 = SubBytesIO.fromReadWrite(io, 0, content.length / 3, false);
						BytesIO.ReadWrite sub2 = SubBytesIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						BytesIO.ReadWrite sub3 = SubBytesIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						BufferedReadableBytesDataIO buffered1 = new BufferedReadableBytesDataIO(sub1, false);
						BufferedReadableBytesDataIO buffered2 = new BufferedReadableBytesDataIO(sub2, false);
						BufferedReadableBytesDataIO buffered3 = new BufferedReadableBytesDataIO(sub3, false);
						return CompositeBytesIO.fromReadable(List.of(buffered1, buffered2, buffered3), true, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 parts: 2 BufferedReadableBytesData (unknown size) and a SubIO in the middle", content -> {
					try {
						if (content.length == 0) return CompositeBytesIO.fromReadable(List.of(), true, true);
						BytesIO.ReadWrite io = new ByteArray(content).asBytesIO();
						BytesIO.ReadWrite sub1 = SubBytesIO.fromReadWrite(io, 0, content.length / 3, false);
						BytesIO.ReadWrite sub2 = SubBytesIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						BytesIO.ReadWrite sub3 = SubBytesIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						BufferedReadableBytesDataIO buffered1 = new BufferedReadableBytesDataIO(sub1, false);
						BufferedReadableBytesDataIO buffered3 = new BufferedReadableBytesDataIO(sub3, false);
						return CompositeBytesIO.fromReadable(List.of(buffered1, sub2, buffered3), false, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
	public static class TestReadableSeekable extends AbstractReadableSeekableBytesIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO from ByteArray", content -> {
					try {
						BytesIO.ReadWrite io = new ByteArray(content).asBytesIO();
						return CompositeBytesIO.fromReadableSeekable(List.of(io), true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from ByteArray", content -> {
					try {
						if (content.length == 0) return CompositeBytesIO.fromReadableSeekable(List.of(), true);
						BytesIO.ReadWrite io = new ByteArray(content).asBytesIO();
						BytesIO.ReadWrite sub1 = SubBytesIO.fromReadWrite(io, 0, content.length / 3, false);
						BytesIO.ReadWrite sub2 = SubBytesIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						BytesIO.ReadWrite sub3 = SubBytesIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						return CompositeBytesIO.fromReadableSeekable(List.of(sub1, sub2, sub3), true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
	public static class TestWritable extends AbstractWritableBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<?, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO from ByteArray garbageOnConsumed=false", size -> {
					try {
						byte[] b = new byte[size];
						BytesIO.ReadWrite io = new ByteArray(b).asBytesIO();
						return new WritableTestCase<>(CompositeBytesIO.fromWritable(List.of(io), true, false), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO from ByteArray garbageOnConsumed=true", size -> {
					try {
						byte[] b = new byte[size];
						BytesIO.ReadWrite io = new ByteArray(b).asBytesIO();
						return new WritableTestCase<>(CompositeBytesIO.fromWritable(List.of(io), true, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from ByteArray garbageOnConsumed=false", size -> {
					try {
						byte[] b = new byte[size];
						BytesIO.ReadWrite io = new ByteArray(b).asBytesIO();
						BytesIO.ReadWrite sub1 = SubBytesIO.fromReadWrite(io, 0, size / 3, false);
						BytesIO.ReadWrite sub2 = SubBytesIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesIO.ReadWrite sub3 = SubBytesIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeBytesIO.fromWritable(List.of(sub1, sub2, sub3), true, false), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from ByteArray garbageOnConsumed=true", size -> {
					try {
						byte[] b = new byte[size];
						BytesIO.ReadWrite io = new ByteArray(b).asBytesIO();
						BytesIO.ReadWrite sub1 = SubBytesIO.fromReadWrite(io, 0, size / 3, false);
						BytesIO.ReadWrite sub2 = SubBytesIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesIO.ReadWrite sub3 = SubBytesIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeBytesIO.fromWritable(List.of(sub1, sub2, sub3), true, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
		
		@Override
		protected void checkWrittenData(BytesIO.Writable io, Object object, byte[] expected) throws Exception {
			byte[] b = (byte[]) object;
			Assertions.assertArrayEquals(expected, b);
		}
	}
	
	public static class TestWritableSeekable extends AbstractWritableSeekableBytesIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesIO.ReadWrite io = new ByteArray(b).asBytesIO();
						return new WritableTestCase<>(CompositeBytesIO.fromWritableSeekable(List.of(io), true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesIO.ReadWrite io = new ByteArray(b).asBytesIO();
						BytesIO.ReadWrite sub1 = SubBytesIO.fromReadWrite(io, 0, size / 3, false);
						BytesIO.ReadWrite sub2 = SubBytesIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesIO.ReadWrite sub3 = SubBytesIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeBytesIO.fromWritableSeekable(List.of(sub1, sub2, sub3), true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
		
		@Override
		protected void checkWrittenData(BytesIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			byte[] b = (byte[]) object;
			Assertions.assertArrayEquals(expected, b);
		}
	}
	
	public static class TestReadWrite extends AbstractReadWriteBytesIOTest<CompositeBytesIO.ReadWrite> {
		@Override
		public List<? extends TestCase<Integer, CompositeBytesIO.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesIO.ReadWrite io = new ByteArray(b).asBytesIO();
						return CompositeBytesIO.fromReadWrite(List.of(io), true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesIO.ReadWrite io = new ByteArray(b).asBytesIO();
						BytesIO.ReadWrite sub1 = SubBytesIO.fromReadWrite(io, 0, size / 3, false);
						BytesIO.ReadWrite sub2 = SubBytesIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesIO.ReadWrite sub3 = SubBytesIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return CompositeBytesIO.fromReadWrite(List.of(sub1, sub2, sub3), true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
}

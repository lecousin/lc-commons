package net.lecousin.commons.io.bytes.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteOrder;
import java.util.List;

import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.io.bytes.data.AbstractReadWriteBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.AbstractReadableBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.AbstractReadableSeekableBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.AbstractWritableBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.AbstractWritableSeekableBytesDataIOTest;
import net.lecousin.commons.io.bytes.data.BytesDataIO;
import net.lecousin.commons.io.bytes.memory.BufferedReadableBytesDataIO;
import net.lecousin.commons.io.bytes.memory.ByteArray;
import net.lecousin.commons.test.TestCase;

public class TestCompositeBytesDataIO {

	public static class TestReadable extends AbstractReadableBytesDataIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesDataIO.Readable>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO LE from ByteArray garbageOnConsumed=false", content -> {
					try {
						BytesDataIO.Readable io = new ByteArray(content).asBytesDataIO().asReadableBytesDataIO();
						return CompositeBytesDataIO.fromReadable(List.of(io), ByteOrder.LITTLE_ENDIAN, true, false);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO LE from ByteArray garbageOnConsumed=true", content -> {
					try {
						BytesDataIO.Readable io = new ByteArray(content).asBytesDataIO().asReadableBytesDataIO();
						return CompositeBytesDataIO.fromReadable(List.of(io), ByteOrder.LITTLE_ENDIAN, true, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 ByteArray LE from ByteArray garbageOnConsumed=false", content -> {
					try {
						if (content.length == 0) return CompositeBytesDataIO.fromReadable(List.of(), ByteOrder.LITTLE_ENDIAN, true, false);
						BytesDataIO.Readable sub1 = new ByteArray(content, 0, content.length / 3).asBytesDataIO().asReadableBytesDataIO();
						BytesDataIO.Readable sub2 = new ByteArray(content, content.length / 3, content.length / 3).asBytesDataIO().asReadableBytesDataIO();
						BytesDataIO.Readable sub3 = new ByteArray(content, 2 * (content.length / 3), content.length - (2 * (content.length / 3))).asBytesDataIO().asReadableBytesDataIO();
						return CompositeBytesDataIO.fromReadable(List.of(sub1, sub2, sub3), ByteOrder.LITTLE_ENDIAN, true, false);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 ByteArray LE from ByteArray garbageOnConsumed=true", content -> {
					try {
						if (content.length == 0) return CompositeBytesDataIO.fromReadable(List.of(), ByteOrder.LITTLE_ENDIAN, true, true);
						BytesDataIO.Readable sub1 = new ByteArray(content, 0, content.length / 3).asBytesDataIO().asReadableBytesDataIO();
						BytesDataIO.Readable sub2 = new ByteArray(content, content.length / 3, content.length / 3).asBytesDataIO().asReadableBytesDataIO();
						BytesDataIO.Readable sub3 = new ByteArray(content, 2 * (content.length / 3), content.length - (2 * (content.length / 3))).asBytesDataIO().asReadableBytesDataIO();
						return CompositeBytesDataIO.fromReadable(List.of(sub1, sub2, sub3), ByteOrder.LITTLE_ENDIAN, true, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO BE from ByteArray garbageOnConsumed=false", content -> {
					try {
						BytesDataIO.Readable io = new ByteArray(content).asBytesDataIO().asReadableBytesDataIO();
						return CompositeBytesDataIO.fromReadable(List.of(io), ByteOrder.BIG_ENDIAN, true, false);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO BE from ByteArray garbageOnConsumed=true", content -> {
					try {
						BytesDataIO.Readable io = new ByteArray(content).asBytesDataIO().asReadableBytesDataIO();
						return CompositeBytesDataIO.fromReadable(List.of(io), ByteOrder.BIG_ENDIAN, true, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 ByteArray BE from ByteArray garbageOnConsumed=false", content -> {
					try {
						if (content.length == 0) return CompositeBytesDataIO.fromReadable(List.of(), ByteOrder.BIG_ENDIAN, true, false);
						BytesDataIO.Readable sub1 = new ByteArray(content, 0, content.length / 3).asBytesDataIO().asReadableBytesDataIO();
						BytesDataIO.Readable sub2 = new ByteArray(content, content.length / 3, content.length / 3).asBytesDataIO().asReadableBytesDataIO();
						BytesDataIO.Readable sub3 = new ByteArray(content, 2 * (content.length / 3), content.length - (2 * (content.length / 3))).asBytesDataIO().asReadableBytesDataIO();
						return CompositeBytesDataIO.fromReadable(List.of(sub1, sub2, sub3), ByteOrder.BIG_ENDIAN, true, false);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 ByteArray BE from ByteArray garbageOnConsumed=true", content -> {
					try {
						if (content.length == 0) return CompositeBytesDataIO.fromReadable(List.of(), ByteOrder.BIG_ENDIAN, true, true);
						BytesDataIO.Readable sub1 = new ByteArray(content, 0, content.length / 3).asBytesDataIO().asReadableBytesDataIO();
						BytesDataIO.Readable sub2 = new ByteArray(content, content.length / 3, content.length / 3).asBytesDataIO().asReadableBytesDataIO();
						BytesDataIO.Readable sub3 = new ByteArray(content, 2 * (content.length / 3), content.length - (2 * (content.length / 3))).asBytesDataIO().asReadableBytesDataIO();
						return CompositeBytesDataIO.fromReadable(List.of(sub1, sub2, sub3), ByteOrder.BIG_ENDIAN, true, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 BufferedReadableBytesData (unknown size)", content -> {
					try {
						if (content.length == 0) return CompositeBytesDataIO.fromReadable(List.of(), ByteOrder.LITTLE_ENDIAN, true, true);
						BytesDataIO.ReadWrite io = new ByteArray(content).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, content.length / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						BufferedReadableBytesDataIO buffered1 = new BufferedReadableBytesDataIO(sub1, false);
						BufferedReadableBytesDataIO buffered2 = new BufferedReadableBytesDataIO(sub2, false);
						BufferedReadableBytesDataIO buffered3 = new BufferedReadableBytesDataIO(sub3, false);
						return CompositeBytesDataIO.fromReadable(List.of(buffered1, buffered2, buffered3), ByteOrder.LITTLE_ENDIAN, true, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 parts: 2 BufferedReadableBytesData (unknown size) and a SubIO in the middle", content -> {
					try {
						if (content.length == 0) return CompositeBytesDataIO.fromReadable(List.of(), ByteOrder.LITTLE_ENDIAN, true, true);
						BytesDataIO.ReadWrite io = new ByteArray(content).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, content.length / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						BufferedReadableBytesDataIO buffered1 = new BufferedReadableBytesDataIO(sub1, false);
						BufferedReadableBytesDataIO buffered3 = new BufferedReadableBytesDataIO(sub3, false);
						return CompositeBytesDataIO.fromReadable(List.of(buffered1, sub2, buffered3), ByteOrder.LITTLE_ENDIAN, false, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
	public static class TestReadableSeekable extends AbstractReadableSeekableBytesDataIOTest {
		@Override
		public List<? extends TestCase<byte[], BytesDataIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO LE from ByteArray", content -> {
					try {
						BytesDataIO.ReadWrite io = new ByteArray(content).asBytesDataIO();
						return CompositeBytesDataIO.fromReadableSeekable(List.of(io), ByteOrder.LITTLE_ENDIAN, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO LE from ByteArray", content -> {
					try {
						if (content.length == 0) return CompositeBytesDataIO.fromReadableSeekable(List.of(), ByteOrder.LITTLE_ENDIAN, true);
						BytesDataIO.ReadWrite io = new ByteArray(content).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, content.length / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						return CompositeBytesDataIO.fromReadableSeekable(List.of(sub1, sub2, sub3), ByteOrder.LITTLE_ENDIAN, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO BE from ByteArray", content -> {
					try {
						BytesDataIO.ReadWrite io = new ByteArray(content).asBytesDataIO();
						return CompositeBytesDataIO.fromReadableSeekable(List.of(io), ByteOrder.BIG_ENDIAN, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO BE from ByteArray", content -> {
					try {
						if (content.length == 0) return CompositeBytesDataIO.fromReadableSeekable(List.of(), ByteOrder.BIG_ENDIAN, true);
						BytesDataIO.ReadWrite io = new ByteArray(content).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, content.length / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						return CompositeBytesDataIO.fromReadableSeekable(List.of(sub1, sub2, sub3), ByteOrder.BIG_ENDIAN, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
	public static class TestWritable extends AbstractWritableBytesDataIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesDataIO.Writable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO LE from ByteArray garbageOnConsumed=false", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritable(List.of(io), ByteOrder.LITTLE_ENDIAN, true, false), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO LE from ByteArray garbageOnConsumed=true", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritable(List.of(io), ByteOrder.LITTLE_ENDIAN, true, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO LE from ByteArray garbageOnConsumed=false", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, size / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritable(List.of(sub1, sub2, sub3), ByteOrder.LITTLE_ENDIAN, true, false), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO LE from ByteArray garbageOnConsumed=true", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, size / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritable(List.of(sub1, sub2, sub3), ByteOrder.LITTLE_ENDIAN, true, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO BE from ByteArray garbageOnConsumed=false", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritable(List.of(io), ByteOrder.BIG_ENDIAN, true, false), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO BE from ByteArray garbageOnConsumed=true", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritable(List.of(io), ByteOrder.BIG_ENDIAN, true, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO BE from ByteArray garbageOnConsumed=false", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, size / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritable(List.of(sub1, sub2, sub3), ByteOrder.BIG_ENDIAN, true, false), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO BE from ByteArray garbageOnConsumed=true", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, size / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritable(List.of(sub1, sub2, sub3), ByteOrder.BIG_ENDIAN, true, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
		
		@Override
		protected void checkWrittenData(BytesDataIO.Writable io, Object object, byte[] expected) throws Exception {
			byte[] b = (byte[]) object;
			assertThat(b).containsExactly(expected);
		}
	}
	
	public static class TestWritableSeekable extends AbstractWritableSeekableBytesDataIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesDataIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO LE from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritableSeekable(List.of(io), ByteOrder.LITTLE_ENDIAN, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO LE from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, size / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritableSeekable(List.of(sub1, sub2, sub3), ByteOrder.LITTLE_ENDIAN, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO BE from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritableSeekable(List.of(io), ByteOrder.BIG_ENDIAN, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO BE from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, size / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeBytesDataIO.fromWritableSeekable(List.of(sub1, sub2, sub3), ByteOrder.BIG_ENDIAN, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
		
		@Override
		protected void checkWrittenData(BytesDataIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			byte[] b = (byte[]) object;
			assertThat(b).containsExactly(expected);
		}
	}
	
	public static class TestReadWrite extends AbstractReadWriteBytesDataIOTest<CompositeBytesDataIO.ReadWrite> {
		@Override
		public List<? extends TestCase<Integer, CompositeBytesDataIO.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO LE from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						return CompositeBytesDataIO.fromReadWrite(List.of(io), ByteOrder.LITTLE_ENDIAN, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO LE from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, size / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return CompositeBytesDataIO.fromReadWrite(List.of(sub1, sub2, sub3), ByteOrder.LITTLE_ENDIAN, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO BE from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						return CompositeBytesDataIO.fromReadWrite(List.of(io), ByteOrder.BIG_ENDIAN, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO BE from ByteArray", size -> {
					try {
						byte[] b = new byte[size];
						BytesDataIO.ReadWrite io = new ByteArray(b).asBytesDataIO();
						BytesDataIO.ReadWrite sub1 = SubBytesDataIO.fromReadWrite(io, 0, size / 3, false);
						BytesDataIO.ReadWrite sub2 = SubBytesDataIO.fromReadWrite(io, size / 3, size / 3, false);
						BytesDataIO.ReadWrite sub3 = SubBytesDataIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return CompositeBytesDataIO.fromReadWrite(List.of(sub1, sub2, sub3), ByteOrder.BIG_ENDIAN, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
}

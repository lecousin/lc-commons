package net.lecousin.commons.io.chars.utils;

import java.util.List;

import org.junit.jupiter.api.Assertions;

import net.lecousin.commons.io.chars.AbstractReadWriteCharsIOTest;
import net.lecousin.commons.io.chars.AbstractReadableCharsIOTest;
import net.lecousin.commons.io.chars.AbstractReadableSeekableCharsIOTest;
import net.lecousin.commons.io.chars.AbstractWritableCharsIOTest;
import net.lecousin.commons.io.chars.AbstractWritableCharsIOTest.WritableTestCase;
import net.lecousin.commons.io.chars.AbstractWritableSeekableCharsIOTest;
import net.lecousin.commons.io.chars.CharsIO;
import net.lecousin.commons.io.chars.memory.BufferedReadableCharsIO;
import net.lecousin.commons.io.chars.memory.CharArray;
import net.lecousin.commons.test.TestCase;

public class TestCompositeCharsIO {

	public static class TestReadable extends AbstractReadableCharsIOTest {
		@Override
		public List<? extends TestCase<char[], CharsIO.Readable>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO from CharArray garbageOnConsumed=false", content -> {
					try {
						CharsIO.ReadWrite io = new CharArray(content).asCharsIO();
						return CompositeCharsIO.fromReadable(List.of(io), true, false);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO from CharArray garbageOnConsumed=true", content -> {
					try {
						CharsIO.ReadWrite io = new CharArray(content).asCharsIO();
						return CompositeCharsIO.fromReadable(List.of(io), true, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from CharArray garbageOnConsumed=false", content -> {
					try {
						if (content.length == 0) return CompositeCharsIO.fromReadable(List.of(), true, false);
						CharsIO.ReadWrite io = new CharArray(content).asCharsIO();
						CharsIO.ReadWrite sub1 = SubCharsIO.fromReadWrite(io, 0, content.length / 3, false);
						CharsIO.ReadWrite sub2 = SubCharsIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						CharsIO.ReadWrite sub3 = SubCharsIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						return CompositeCharsIO.fromReadable(List.of(sub1, sub2, sub3), true, false);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from CharArray garbageOnConsumed=true", content -> {
					try {
						if (content.length == 0) return CompositeCharsIO.fromReadable(List.of(), true, true);
						CharsIO.ReadWrite io = new CharArray(content).asCharsIO();
						CharsIO.ReadWrite sub1 = SubCharsIO.fromReadWrite(io, 0, content.length / 3, false);
						CharsIO.ReadWrite sub2 = SubCharsIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						CharsIO.ReadWrite sub3 = SubCharsIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						return CompositeCharsIO.fromReadable(List.of(sub1, sub2, sub3), true, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 BufferedReadableCharsIO (unknown size)", content -> {
					try {
						if (content.length == 0) return CompositeCharsIO.fromReadable(List.of(), true, true);
						CharsIO.ReadWrite io = new CharArray(content).asCharsIO();
						CharsIO.ReadWrite sub1 = SubCharsIO.fromReadWrite(io, 0, content.length / 3, false);
						CharsIO.ReadWrite sub2 = SubCharsIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						CharsIO.ReadWrite sub3 = SubCharsIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						BufferedReadableCharsIO buffered1 = new BufferedReadableCharsIO(sub1, false);
						BufferedReadableCharsIO buffered2 = new BufferedReadableCharsIO(sub2, false);
						BufferedReadableCharsIO buffered3 = new BufferedReadableCharsIO(sub3, false);
						return CompositeCharsIO.fromReadable(List.of(buffered1, buffered2, buffered3), true, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 parts: 2 BufferedReadableCharsIO (unknown size) and a SubIO in the middle", content -> {
					try {
						if (content.length == 0) return CompositeCharsIO.fromReadable(List.of(), true, true);
						CharsIO.ReadWrite io = new CharArray(content).asCharsIO();
						CharsIO.ReadWrite sub1 = SubCharsIO.fromReadWrite(io, 0, content.length / 3, false);
						CharsIO.ReadWrite sub2 = SubCharsIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						CharsIO.ReadWrite sub3 = SubCharsIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						BufferedReadableCharsIO buffered1 = new BufferedReadableCharsIO(sub1, false);
						BufferedReadableCharsIO buffered3 = new BufferedReadableCharsIO(sub3, false);
						return CompositeCharsIO.fromReadable(List.of(buffered1, sub2, buffered3), false, true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
	public static class TestReadableSeekable extends AbstractReadableSeekableCharsIOTest {
		@Override
		public List<? extends TestCase<char[], CharsIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO from CharArray", content -> {
					try {
						CharsIO.ReadWrite io = new CharArray(content).asCharsIO();
						return CompositeCharsIO.fromReadableSeekable(List.of(io), true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from CharArray", content -> {
					try {
						if (content.length == 0) return CompositeCharsIO.fromReadableSeekable(List.of(), true);
						CharsIO.ReadWrite io = new CharArray(content).asCharsIO();
						CharsIO.ReadWrite sub1 = SubCharsIO.fromReadWrite(io, 0, content.length / 3, false);
						CharsIO.ReadWrite sub2 = SubCharsIO.fromReadWrite(io, content.length / 3, content.length / 3, false);
						CharsIO.ReadWrite sub3 = SubCharsIO.fromReadWrite(io, 2 * (content.length / 3), content.length - (2 * (content.length / 3)), false);
						return CompositeCharsIO.fromReadableSeekable(List.of(sub1, sub2, sub3), true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
	public static class TestWritable extends AbstractWritableCharsIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<?, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO from CharArray garbageOnConsumed=false", size -> {
					try {
						char[] b = new char[size];
						CharsIO.ReadWrite io = new CharArray(b).asCharsIO();
						return new WritableTestCase<>(CompositeCharsIO.fromWritable(List.of(io), true, false), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("Single IO from CharArray garbageOnConsumed=true", size -> {
					try {
						char[] b = new char[size];
						CharsIO.ReadWrite io = new CharArray(b).asCharsIO();
						return new WritableTestCase<>(CompositeCharsIO.fromWritable(List.of(io), true, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from CharArray garbageOnConsumed=false", size -> {
					try {
						char[] b = new char[size];
						CharsIO.ReadWrite io = new CharArray(b).asCharsIO();
						CharsIO.ReadWrite sub1 = SubCharsIO.fromReadWrite(io, 0, size / 3, false);
						CharsIO.ReadWrite sub2 = SubCharsIO.fromReadWrite(io, size / 3, size / 3, false);
						CharsIO.ReadWrite sub3 = SubCharsIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeCharsIO.fromWritable(List.of(sub1, sub2, sub3), true, false), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from CharArray garbageOnConsumed=true", size -> {
					try {
						char[] b = new char[size];
						CharsIO.ReadWrite io = new CharArray(b).asCharsIO();
						CharsIO.ReadWrite sub1 = SubCharsIO.fromReadWrite(io, 0, size / 3, false);
						CharsIO.ReadWrite sub2 = SubCharsIO.fromReadWrite(io, size / 3, size / 3, false);
						CharsIO.ReadWrite sub3 = SubCharsIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeCharsIO.fromWritable(List.of(sub1, sub2, sub3), true, true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
		
		@Override
		protected void checkWrittenData(CharsIO.Writable io, Object object, char[] expected) throws Exception {
			char[] b = (char[]) object;
			Assertions.assertArrayEquals(expected, b);
		}
	}
	
	public static class TestWritableSeekable extends AbstractWritableSeekableCharsIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO from CharArray", size -> {
					try {
						char[] b = new char[size];
						CharsIO.ReadWrite io = new CharArray(b).asCharsIO();
						return new WritableTestCase<>(CompositeCharsIO.fromWritableSeekable(List.of(io), true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from CharArray", size -> {
					try {
						char[] b = new char[size];
						CharsIO.ReadWrite io = new CharArray(b).asCharsIO();
						CharsIO.ReadWrite sub1 = SubCharsIO.fromReadWrite(io, 0, size / 3, false);
						CharsIO.ReadWrite sub2 = SubCharsIO.fromReadWrite(io, size / 3, size / 3, false);
						CharsIO.ReadWrite sub3 = SubCharsIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return new WritableTestCase<>(CompositeCharsIO.fromWritableSeekable(List.of(sub1, sub2, sub3), true), b);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
		
		@Override
		protected void checkWrittenData(CharsIO.Writable.Seekable io, Object object, char[] expected) throws Exception {
			char[] b = (char[]) object;
			Assertions.assertArrayEquals(expected, b);
		}
	}
	
	public static class TestReadWrite extends AbstractReadWriteCharsIOTest<CompositeCharsIO.ReadWrite> {
		@Override
		public List<? extends TestCase<Integer, CompositeCharsIO.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>("Single IO from CharArray", size -> {
					try {
						char[] b = new char[size];
						CharsIO.ReadWrite io = new CharArray(b).asCharsIO();
						return CompositeCharsIO.fromReadWrite(List.of(io), true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}),
				new TestCase<>("3 SubIO from CharArray", size -> {
					try {
						char[] b = new char[size];
						CharsIO.ReadWrite io = new CharArray(b).asCharsIO();
						CharsIO.ReadWrite sub1 = SubCharsIO.fromReadWrite(io, 0, size / 3, false);
						CharsIO.ReadWrite sub2 = SubCharsIO.fromReadWrite(io, size / 3, size / 3, false);
						CharsIO.ReadWrite sub3 = SubCharsIO.fromReadWrite(io, 2 * (size / 3), size - (2 * (size / 3)), false);
						return CompositeCharsIO.fromReadWrite(List.of(sub1, sub2, sub3), true);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				})
			);
		}
	}
	
}

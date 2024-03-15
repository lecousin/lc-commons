package net.lecousin.commons.io.chars.utils;

import java.util.List;

import org.junit.jupiter.api.Assertions;

import net.lecousin.commons.io.chars.AbstractReadWriteCharsIOTest;
import net.lecousin.commons.io.chars.AbstractReadableSeekableCharsIOTest;
import net.lecousin.commons.io.chars.AbstractWritableCharsIOTest.WritableTestCase;
import net.lecousin.commons.io.chars.AbstractWritableSeekableCharsIOTest;
import net.lecousin.commons.io.chars.CharsIO;
import net.lecousin.commons.io.chars.memory.CharArray;
import net.lecousin.commons.test.TestCase;

public class TestSubCharsIO {

	public static class TestReadable extends AbstractReadableSeekableCharsIOTest {
		@Override
		public List<? extends TestCase<char[], CharsIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("SubCharsIO of CharArrayIO", content -> {
					char[] b = new char[content.length + 789];
					System.arraycopy(content, 0, b, 111, content.length);
					return SubCharsIO.fromReadable(new CharArray(b).asCharsIO().asReadableSeekableCharsIO(), 111, content.length, true);
				})
			);
		}
	}
	
	public static class TestWritable extends AbstractWritableSeekableCharsIOTest {
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("SubCharsIO of CharArrayIO", size -> {
					CharArray ba = new CharArray(new char[size + 777]);
					CharsIO.Writable.Seekable sub = SubCharsIO.fromWritable(ba.asCharsIO().asWritableSeekableCharsIO(), 111, size, false);
					return new WritableTestCase<>(sub, ba);
				})
			);
		}
		
		@Override
		protected void checkWrittenData(CharsIO.Writable.Seekable io, Object object, char[] expected) throws Exception {
			CharArray ba = (CharArray) object;
			char[] b = new char[expected.length];
			System.arraycopy(ba.getArray(), 111, b, 0, expected.length);
			Assertions.assertArrayEquals(expected, b);
		}
	}
	
	public static class TestReadWrite extends AbstractReadWriteCharsIOTest<SubCharsIO.ReadWrite> {
		@Override
		public List<? extends TestCase<Integer, SubCharsIO.ReadWrite>> getTestCases() {
			return List.of(
				new TestCase<>("SubCharsIO of CharArrayIO", size -> {
					CharArray ba = new CharArray(new char[size + 777]);
					return SubCharsIO.fromReadWrite(ba.asCharsIO(), 111, size, false);
				})
			);
		}
	}
	
}

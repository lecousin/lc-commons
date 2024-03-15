package net.lecousin.commons.io.chars.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.IntBinaryOperator;

import org.junit.jupiter.api.Assertions;

import net.lecousin.commons.io.chars.AbstractReadWriteCharsIOTest;
import net.lecousin.commons.io.chars.AbstractReadableSeekableCharsIOTest;
import net.lecousin.commons.io.chars.AbstractWritableCharsIOTest.WritableTestCase;
import net.lecousin.commons.io.chars.AbstractWritableSeekableCharsIOTest;
import net.lecousin.commons.io.chars.CharsIO;
import net.lecousin.commons.test.TestCase;

public class TestCharArrayIO {

	public static class TestReadableSeekableCharsIO extends AbstractReadableSeekableCharsIOTest {
		@Override
		public List<? extends TestCase<char[], CharsIO.Readable.Seekable>> getTestCases() {
			return List.of(
				new TestCase<>("Exact buffer", data -> new CharArray(data).asCharsIO().asReadableSeekableCharsIO()),
				new TestCase<>("Larger buffer starting at 0", data -> {
					char[] b = new char[data.length + 129];
					System.arraycopy(data, 0, b, 0, data.length);
					return new CharArray(b, 0, data.length).asCharsIO().asReadableSeekableCharsIO();
				}),
				new TestCase<>("Larger buffer with start offset", data -> {
					char[] b = new char[data.length + 329];
					System.arraycopy(data, 0, b, 111, data.length);
					return new CharArray(b, 111, data.length).asCharsIO().asReadableSeekableCharsIO();
				})
			);
		}
	}

	public static class TestWritableSeekableCharsIO extends AbstractWritableSeekableCharsIOTest {
		IntBinaryOperator minimumStrategy = (current,add) -> current + add;
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>>> getTestCases() {
			return List.of(
				new TestCase<>("Exact buffer", size -> {
					CharArray b = new CharArray(new char[size]);
					return new WritableTestCase<>(b.asCharsIO().asWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0", size -> {
					CharArray b = new CharArray(new char[size + 129], 0, size);
					return new WritableTestCase<>(b.asCharsIO().asWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset", size -> {
					CharArray b = new CharArray(new char[size + 329], 111, size);
					return new WritableTestCase<>(b.asCharsIO().asWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Exact buffer appendable", size -> {
					CharArray b = new CharArray(new char[size]);
					return new WritableTestCase<>(b.asAppendableCharsIO().asNonResizableWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0 appendable", size -> {
					CharArray b = new CharArray(new char[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableCharsIO().asNonResizableWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset appendable", size -> {
					CharArray b = new CharArray(new char[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableCharsIO().asNonResizableWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Exact buffer appendable minimumAppendSize=50", size -> {
					CharArray b = new CharArray(new char[size]);
					return new WritableTestCase<>(b.asAppendableCharsIO(50).asNonResizableWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0 appendable minimumAppendSize=50", size -> {
					CharArray b = new CharArray(new char[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableCharsIO(50).asNonResizableWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset appendable minimumAppendSize=50", size -> {
					CharArray b = new CharArray(new char[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableCharsIO(50).asNonResizableWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Exact buffer appendable strategy=minimum", size -> {
					CharArray b = new CharArray(new char[size]);
					return new WritableTestCase<>(b.asAppendableCharsIO(minimumStrategy).asNonResizableWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0 appendable strategy=minimum", size -> {
					CharArray b = new CharArray(new char[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableCharsIO(minimumStrategy).asNonResizableWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset appendable strategy=minimum", size -> {
					CharArray b = new CharArray(new char[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableCharsIO(minimumStrategy).asNonResizableWritableSeekableCharsIO(), b);
				}),
				new TestCase<>("Exact buffer resizable", size -> {
					CharArray b = new CharArray(new char[size]);
					return new WritableTestCase<>(b.asCharsIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0 resizable", size -> {
					CharArray b = new CharArray(new char[size + 129], 0, size);
					return new WritableTestCase<>(b.asCharsIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset resizable", size -> {
					CharArray b = new CharArray(new char[size + 329], 111, size);
					return new WritableTestCase<>(b.asCharsIO(), b);
				}),
				new TestCase<>("Exact buffer appendable resizable", size -> {
					CharArray b = new CharArray(new char[size]);
					return new WritableTestCase<>(b.asAppendableCharsIO(), b);
				}),
				new TestCase<>("Larger buffer starting at 0 appendable resizable", size -> {
					CharArray b = new CharArray(new char[size + 129], 0, size);
					return new WritableTestCase<>(b.asAppendableCharsIO(), b);
				}),
				new TestCase<>("Larger buffer with start offset appendable resizable", size -> {
					CharArray b = new CharArray(new char[size + 329], 111, size);
					return new WritableTestCase<>(b.asAppendableCharsIO(), b);
				})
			);
		}
		
		@Override
		protected void checkWrittenData(CharsIO.Writable.Seekable io, Object object, char[] expected) throws Exception {
			CharArray ba = (CharArray) object;
			assertThat(ba.getSize()).isEqualTo(expected.length);
			ba.trim();
			assertThat(ba.getSize()).isEqualTo(expected.length);
			Assertions.assertArrayEquals(expected, ba.getArray());
			assertThat(ba.getArrayStartOffset()).isZero();
		}
	}

	@SuppressWarnings("rawtypes")
	public static class TestReadWriteCharsIO extends AbstractReadWriteCharsIOTest {
		@Override
		public List<? extends TestCase<Integer, ?>> getTestCases() {
			return List.of(
				new TestCase<>("Exact buffer", size -> new CharArray(new char[size]).asCharsIO().asNonResizableReadWriteCharsIO()),
				new TestCase<>("Larger buffer starting at 0", size -> new CharArray(new char[size + 129], 0, size).asCharsIO().asNonResizableReadWriteCharsIO()),
				new TestCase<>("Larger buffer with start offset", size -> new CharArray(new char[size + 329], 111, size).asCharsIO().asNonResizableReadWriteCharsIO()),
				new TestCase<>("Exact buffer appendable", size -> new CharArray(new char[size]).asAppendableCharsIO().asNonResizableReadWriteCharsIO()),
				new TestCase<>("Larger buffer starting at 0 appendable", size -> new CharArray(new char[size + 129], 0, size).asAppendableCharsIO().asNonResizableReadWriteCharsIO()),
				new TestCase<>("Larger buffer with start offset appendable", size -> new CharArray(new char[size + 329], 111, size).asAppendableCharsIO().asNonResizableReadWriteCharsIO()),
				new TestCase<>("Exact buffer resizable", size -> new CharArray(new char[size]).asCharsIO()),
				new TestCase<>("Larger buffer starting at 0 resizable", size -> new CharArray(new char[size + 129], 0, size).asCharsIO()),
				new TestCase<>("Larger buffer with start offset resizable", size -> new CharArray(new char[size + 329], 111, size).asCharsIO()),
				new TestCase<>("Exact buffer appendable resizable", size -> new CharArray(new char[size]).asAppendableCharsIO()),
				new TestCase<>("Larger buffer starting at 0 appendable resizable", size -> new CharArray(new char[size + 129], 0, size).asAppendableCharsIO()),
				new TestCase<>("Larger buffer with start offset appendable resizable", size -> new CharArray(new char[size + 329], 111, size).asAppendableCharsIO())
			);
		}
	}

}

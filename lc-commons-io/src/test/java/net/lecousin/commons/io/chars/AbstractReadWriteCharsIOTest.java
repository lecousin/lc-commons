package net.lecousin.commons.io.chars;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;

import net.lecousin.commons.io.IO;
import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.io.chars.AbstractWritableCharsIOTest.WritableTestCase;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadWriteCharsIOTest<T extends CharsIO.Readable.Seekable & CharsIO.Writable.Seekable> implements TestCasesProvider<Integer, T> {

	@Nested
	public class AsReadableSeekable extends AbstractReadableSeekableCharsIOTest {
		
		@Override
		public List<? extends TestCase<char[], CharsIO.Readable.Seekable>> getTestCases() {
			return AbstractReadWriteCharsIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<char[], CharsIO.Readable.Seekable>) (content) -> {
					try {
						T io = tc.getArgumentProvider().apply(content.length);
						io.writeCharsFully(content);
						io.seek(SeekFrom.START, 0);
						return new CharsIOView.Readable.Seekable(io);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}))
				.toList();
		}
		
	}
	
	@Nested
	public class AsWritableSeekable extends AbstractWritableSeekableCharsIOTest {
		
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>>> getTestCases() {
			return AbstractReadWriteCharsIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, WritableTestCase<? extends CharsIO.Writable.Seekable, ?>>) (size) -> {
					T io = tc.getArgumentProvider().apply(size);
					CharsIO.Writable.Seekable newIo;
					if (io instanceof IO.Writable.Resizable)
						newIo = convertResizable(io);
					else
						newIo = CharsIOView.Writable.Seekable.of(io);
					return new WritableTestCase<>(newIo, io);
				}))
				.toList();
		}
		
		@SuppressWarnings("unchecked")
		private <U extends CharsIO.Writable.Seekable & IO.Writable.Resizable> CharsIO.Writable.Seekable convertResizable(T io) {
			if (io instanceof CharsIO.ReadWrite.Resizable rwr)
				return rwr.asWritableSeekableResizableCharsIO();
			return CharsIOView.Writable.Seekable.Resizable.of((U) io);
		}
		
		@Override
		protected void checkWrittenData(CharsIO.Writable.Seekable io, Object object, char[] expected) throws Exception {
			@SuppressWarnings("unchecked")
			T t = (T) object;
			assertThat(t.size()).isEqualTo(expected.length);
			t.seek(SeekFrom.START, 0);
			char[] found = new char[expected.length];
			t.readCharsFully(found);
			Assertions.assertArrayEquals(expected, found);
		}
	}
	
}

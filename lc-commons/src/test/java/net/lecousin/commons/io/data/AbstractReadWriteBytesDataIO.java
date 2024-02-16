package net.lecousin.commons.io.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;

import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadWriteBytesDataIO<T extends BytesDataIO.Writable.Seekable & BytesDataIO.Readable.Seekable> implements TestCasesProvider<Integer, T> {

	@Nested
	public class AsReadableSeekable extends AbstractReadableSeekableBytesDataIOTest {
		
		@Override
		public List<? extends TestCase<byte[], BytesDataIO.Readable.Seekable>> getTestCases() {
			return AbstractReadWriteBytesDataIO.this.getTestCases().stream()
				.map(tc -> new TestCase<>(
					tc.getName(),
					(Function<byte[], BytesDataIO.Readable.Seekable>)
					(data) -> {
						try {
							var stream = tc.getArgumentProvider().apply(data.length);
							stream.writeFullyAt(0, data);
							stream.seek(SeekFrom.START, 0);
							return (BytesDataIO.Readable.Seekable) stream;
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				))
				.toList();
		}
		
	}
	
	@Nested
	public class AsWritableSeekable extends AbstractWritableSeekableBytesDataIOTest {
		
		@Override
		public List<? extends TestCase<Integer, WritableSeekableTestCase>> getTestCases() {
			return AbstractReadWriteBytesDataIO.this.getTestCases().stream()
				.map(tc -> new TestCase<>(
					tc.getName(),
					(Function<Integer, WritableSeekableTestCase>)
					(size) -> {
						var stream = tc.getArgumentProvider().apply(size);
						return new WritableSeekableTestCase(stream, 1);
					}
				))
				.toList();
		}
		
		@Override
		protected void checkWrittenData(BytesDataIO.Writable.Seekable stream, Object generatedData, byte[] expected) {
			try {
				@SuppressWarnings("unchecked")
				T io = (T) stream;
				assertEquals(expected.length, io.size());
				byte[] found = new byte[expected.length];
				io.readFullyAt(0, found);
				assertThat(found).containsExactly(expected);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
}

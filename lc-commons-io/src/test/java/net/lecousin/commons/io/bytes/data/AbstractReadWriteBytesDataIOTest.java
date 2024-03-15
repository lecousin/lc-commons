package net.lecousin.commons.io.bytes.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;

import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.io.bytes.AbstractReadWriteBytesIOTest;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadWriteBytesDataIOTest<T extends BytesDataIO.Readable.Seekable & BytesDataIO.Writable.Seekable> implements TestCasesProvider<Integer, T> {

	@Nested
	public class AsReadableSeekable extends AbstractReadableSeekableBytesDataIOTest {
		
		@Override
		public List<? extends TestCase<byte[], BytesDataIO.Readable.Seekable>> getTestCases() {
			return AbstractReadWriteBytesDataIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<byte[], BytesDataIO.Readable.Seekable>) (content) -> {
					try {
						T io = tc.getArgumentProvider().apply(content.length);
						io.writeBytesFully(content);
						io.seek(SeekFrom.START, 0);
						return new BytesDataIOView.Readable.Seekable(io);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}))
				.toList();
		}
		
	}
	
	@Nested
	public class AsWritableSeekable extends AbstractWritableSeekableBytesDataIOTest {
		
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesDataIO.Writable.Seekable, ?>>> getTestCases() {
			return AbstractReadWriteBytesDataIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, WritableTestCase<? extends BytesDataIO.Writable.Seekable, ?>>) (size) -> {
					T io = tc.getArgumentProvider().apply(size);
					return new WritableTestCase<>(BytesDataIOView.Writable.Seekable.of(io), io);
				}))
				.toList();
		}
		
		@Override
		protected void checkWrittenData(BytesDataIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			@SuppressWarnings("unchecked")
			T t = (T) object;
			assertThat(t.size()).isEqualTo(expected.length);
			t.seek(SeekFrom.START, 0);
			byte[] found = new byte[expected.length];
			t.readBytesFully(found);
			Assertions.assertArrayEquals(expected, found);
		}
	}
	
	@Nested
	public class AsReadWriteBytesIO extends AbstractReadWriteBytesIOTest<T> {
		@Override
		public List<? extends TestCase<Integer, T>> getTestCases() {
			return AbstractReadWriteBytesDataIOTest.this.getTestCases();
		}
	}
	
}

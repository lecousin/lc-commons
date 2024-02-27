package net.lecousin.commons.io.bytes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;

import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.io.bytes.AbstractWritableBytesIOTest.WritableTestCase;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadWriteBytesIOTest<T extends BytesIO.Readable.Seekable & BytesIO.Writable.Seekable> implements TestCasesProvider<Integer, T> {

	@Nested
	public class AsReadableSeekable extends AbstractReadableSeekableBytesIOTest {
		
		@Override
		public List<? extends TestCase<byte[], BytesIO.Readable.Seekable>> getTestCases() {
			return AbstractReadWriteBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<byte[], BytesIO.Readable.Seekable>) (content) -> {
					try {
						T io = tc.getArgumentProvider().apply(content.length);
						io.writeBytesFully(content);
						io.seek(SeekFrom.START, 0);
						return new BytesIOView.Readable.Seekable(io);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}))
				.toList();
		}
		
	}
	
	@Nested
	public class AsWritableSeekable extends AbstractWritableSeekableBytesIOTest {
		
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>>> getTestCases() {
			return AbstractReadWriteBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, WritableTestCase<? extends BytesIO.Writable.Seekable, ?>>) (size) -> {
					T io = tc.getArgumentProvider().apply(size);
					return new WritableTestCase<>(BytesIOView.Writable.Seekable.of(io), io);
				}))
				.toList();
		}
		
		@Override
		protected void checkWrittenData(BytesIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			@SuppressWarnings("unchecked")
			T t = (T) object;
			assertThat(t.size()).isEqualTo(expected.length);
			t.seek(SeekFrom.START, 0);
			byte[] found = new byte[expected.length];
			t.readBytesFully(found);
			assertThat(found).containsExactly(expected);
		}
	}
	
}

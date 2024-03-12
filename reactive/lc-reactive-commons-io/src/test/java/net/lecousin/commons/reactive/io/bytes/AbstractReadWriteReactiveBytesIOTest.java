package net.lecousin.commons.reactive.io.bytes;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;

import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.reactive.io.ReactiveIO;
import net.lecousin.commons.reactive.io.bytes.AbstractWritableReactiveBytesIOTest.WritableTestCase;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractReadWriteReactiveBytesIOTest<T extends ReactiveBytesIO.Readable.Seekable & ReactiveBytesIO.Writable.Seekable> implements TestCasesProvider<Integer, T> {

	@Nested
	public class AsReadableSeekable extends AbstractReadableSeekableReactiveBytesIOTest {
		
		@Override
		public List<? extends TestCase<byte[], ReactiveBytesIO.Readable.Seekable>> getTestCases() {
			return AbstractReadWriteReactiveBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<byte[], ReactiveBytesIO.Readable.Seekable>) (content) -> {
					T io = tc.getArgumentProvider().apply(content.length);
					io.writeBytesFully(content).block();
					io.seek(SeekFrom.START, 0).block();
					return new ReactiveBytesIOView.Readable.Seekable(io);
				}))
				.toList();
		}
		
	}
	
	@Nested
	public class AsWritableSeekable extends AbstractWritableSeekableReactiveBytesIOTest {
		
		@Override
		public List<? extends TestCase<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>>> getTestCases() {
			return AbstractReadWriteReactiveBytesIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, WritableTestCase<? extends ReactiveBytesIO.Writable.Seekable, ?>>) (size) -> {
					T io = tc.getArgumentProvider().apply(size);
					ReactiveBytesIO.Writable.Seekable newIo;
					if (io instanceof ReactiveIO.Writable.Resizable)
						newIo = convertResizable(io);
					else
						newIo = ReactiveBytesIOView.Writable.Seekable.of(io);
					return new WritableTestCase<>(newIo, io);
				}))
				.toList();
		}
		
		@SuppressWarnings("unchecked")
		private <U extends ReactiveBytesIO.Writable.Seekable & ReactiveIO.Writable.Resizable> ReactiveBytesIO.Writable.Seekable convertResizable(T io) {
			return ReactiveBytesIOView.Writable.Seekable.Resizable.of((U) io);
		}
		
		@Override
		protected void checkWrittenData(ReactiveBytesIO.Writable.Seekable io, Object object, byte[] expected) throws Exception {
			@SuppressWarnings("unchecked")
			T t = (T) object;
			assertThat(t.size().block()).isEqualTo(expected.length);
			t.seek(SeekFrom.START, 0).block();
			byte[] found = new byte[expected.length];
			t.readBytesFully(found).block();
			Assertions.assertArrayEquals(expected, found);
		}
	}
	
}

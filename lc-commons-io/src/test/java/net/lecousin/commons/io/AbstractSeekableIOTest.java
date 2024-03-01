package net.lecousin.commons.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.EOFException;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;

public abstract class AbstractSeekableIOTest implements TestCasesProvider<Integer, IO.Seekable> {

	@Nested
	public class AsKnownSize extends AbstractKnownSizeIOTest {
		
		@Override
		public List<? extends TestCase<Integer, IO.KnownSize>> getTestCases() {
			return AbstractSeekableIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, IO.KnownSize>) (size) -> tc.getArgumentProvider().apply(size)))
				.toList();
		}
		
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(IOTestUtils.TestCasesWithSizeProvider.class)
	void testSeekable(String displayName, int size, Function<Integer, IO.Seekable> ioSupplier) throws Exception {
		IO.Seekable io = ioSupplier.apply(size);
		
		assertThat(io.size()).isEqualTo(size);
		
		assertThat(io.seek(SeekFrom.START, size / 3)).isEqualTo((long) size / 3);
		assertThat(io.position()).isEqualTo((long) size / 3);
		assertThat(io.seek(SeekFrom.START, size / 4)).isEqualTo((long) size / 4);
		assertThat(io.position()).isEqualTo((long) size / 4);
		
		if (size > 3) {
			assertThat(io.seek(SeekFrom.CURRENT, 2)).isEqualTo((long) size / 4 + 2);
			assertThat(io.position()).isEqualTo((long) size / 4 + 2);
			assertThat(io.seek(SeekFrom.CURRENT, -1)).isEqualTo((long) size / 4 + 1);
			assertThat(io.position()).isEqualTo((long) size / 4 + 1);
		}
		
		assertThat(io.seek(SeekFrom.END, size / 3)).isEqualTo((long) size - (size / 3));
		assertThat(io.position()).isEqualTo((long) size - (size / 3));
		assertThat(io.seek(SeekFrom.END, size / 4)).isEqualTo((long) size - (size / 4));
		assertThat(io.position()).isEqualTo((long) size - (size / 4));
		
		assertThrows(NullPointerException.class, () -> io.seek(null, 0));
		
		assertThrows(IllegalArgumentException.class, () -> io.seek(SeekFrom.START, -1));
		if (!(io instanceof IO.Writable.Appendable)) {
			assertThrows(EOFException.class, () -> io.seek(SeekFrom.START, size + 1));
			assertThrows(EOFException.class, () -> io.seek(SeekFrom.END, -1));
		} else {
			long s = io.size();
			io.seek(SeekFrom.END, -1);
			assertThat(io.position()).isEqualTo(s + 1);
			assertThat(io.size()).isEqualTo(s + 1);
			s++;
			io.seek(SeekFrom.CURRENT, 1);
			assertThat(io.position()).isEqualTo(s + 1);
			assertThat(io.size()).isEqualTo(s + 1);
			s++;
			io.seek(SeekFrom.START, s + 1);
			assertThat(io.position()).isEqualTo(s + 1);
			assertThat(io.size()).isEqualTo(s + 1);
		}
		
		io.close();
		assertThrows(ClosedChannelException.class, () -> io.seek(SeekFrom.START, 0));
		assertThrows(ClosedChannelException.class, () -> io.seek(SeekFrom.CURRENT, 0));
		assertThrows(ClosedChannelException.class, () -> io.seek(SeekFrom.END, 0));
		assertThrows(ClosedChannelException.class, () -> io.position());
		assertThrows(ClosedChannelException.class, () -> io.size());
	}
	
}

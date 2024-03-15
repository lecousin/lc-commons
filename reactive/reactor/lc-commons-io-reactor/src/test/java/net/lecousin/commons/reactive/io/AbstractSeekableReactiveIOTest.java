package net.lecousin.commons.reactive.io;

import java.io.EOFException;
import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.io.IO.Seekable.SeekFrom;
import net.lecousin.commons.io.IOTestUtils;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;
import reactor.test.StepVerifier;

public abstract class AbstractSeekableReactiveIOTest implements TestCasesProvider<Integer, ReactiveIO.Seekable> {

	@Nested
	public class AsKnownSize extends AbstractKnownSizeReactiveIOTest {
		
		@Override
		public List<? extends TestCase<Integer, ReactiveIO.KnownSize>> getTestCases() {
			return AbstractSeekableReactiveIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, ReactiveIO.KnownSize>) (size) -> tc.getArgumentProvider().apply(size)))
				.toList();
		}
		
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(IOTestUtils.TestCasesWithSizeProvider.class)
	void testSeekable(String displayName, int size, Function<Integer, ReactiveIO.Seekable> ioSupplier) throws Exception {
		ReactiveIO.Seekable io = ioSupplier.apply(size);
		
		StepVerifier.create(io.size()).expectNext((long) size).verifyComplete();

		StepVerifier.create(io.seek(SeekFrom.START, size / 3)).expectNext((long) size / 3).verifyComplete();
		StepVerifier.create(io.position()).expectNext((long) size / 3).verifyComplete();
		StepVerifier.create(io.seek(SeekFrom.START, size / 4)).expectNext((long) size / 4).verifyComplete();
		StepVerifier.create(io.position()).expectNext((long) size / 4).verifyComplete();
		
		if (size > 3) {
			StepVerifier.create(io.seek(SeekFrom.CURRENT, 2)).expectNext((long) size / 4 + 2).verifyComplete();
			StepVerifier.create(io.position()).expectNext((long) size / 4 + 2).verifyComplete();
			StepVerifier.create(io.seek(SeekFrom.CURRENT, -1)).expectNext((long) size / 4 + 1).verifyComplete();
			StepVerifier.create(io.position()).expectNext((long) size / 4 + 1).verifyComplete();
		}
		
		StepVerifier.create(io.seek(SeekFrom.END, size / 3)).expectNext((long) size - (size / 3)).verifyComplete();
		StepVerifier.create(io.position()).expectNext((long) size - (size / 3)).verifyComplete();
		StepVerifier.create(io.seek(SeekFrom.END, size / 4)).expectNext((long) size - (size / 4)).verifyComplete();
		StepVerifier.create(io.position()).expectNext((long) size - (size / 4)).verifyComplete();
		
		StepVerifier.create(io.seek(null, 0)).expectError(NullPointerException.class).verify();
		
		StepVerifier.create(io.seek(SeekFrom.START, -1)).expectError(IllegalArgumentException.class).verify();
		if (!(io instanceof ReactiveIO.Writable.Appendable)) {
			StepVerifier.create(io.seek(SeekFrom.START, size + 1)).expectError(EOFException.class).verify();
			StepVerifier.create(io.seek(SeekFrom.END, -1)).expectError(EOFException.class).verify();
		} else {
			long s = io.size().block();
			io.seek(SeekFrom.END, -1).block();
			StepVerifier.create(io.position()).expectNext(s + 1).verifyComplete();
			StepVerifier.create(io.size()).expectNext(s + 1).verifyComplete();
			s++;
			io.seek(SeekFrom.CURRENT, 1).block();
			StepVerifier.create(io.position()).expectNext(s + 1).verifyComplete();
			StepVerifier.create(io.size()).expectNext(s + 1).verifyComplete();
			s++;
			io.seek(SeekFrom.START, s + 1).block();
			StepVerifier.create(io.position()).expectNext(s + 1).verifyComplete();
			StepVerifier.create(io.size()).expectNext(s + 1).verifyComplete();
		}
		
		io.close().block();
		StepVerifier.create(io.seek(SeekFrom.START, 0)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.seek(SeekFrom.CURRENT, 0)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.seek(SeekFrom.END, 0)).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.position()).expectError(ClosedChannelException.class).verify();
		StepVerifier.create(io.size()).expectError(ClosedChannelException.class).verify();
	}
	
}

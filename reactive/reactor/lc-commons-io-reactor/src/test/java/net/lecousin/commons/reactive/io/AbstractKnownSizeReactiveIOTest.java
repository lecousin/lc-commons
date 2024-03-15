package net.lecousin.commons.reactive.io;

import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.io.IOTestUtils;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;
import reactor.test.StepVerifier;

public abstract class AbstractKnownSizeReactiveIOTest implements TestCasesProvider<Integer, ReactiveIO.KnownSize> {

	@Nested
	public class AsIO extends AbstractReactiveIOTest {
		
		@Override
		public List<? extends TestCase<Void, ReactiveIO>> getTestCases() {
			return AbstractKnownSizeReactiveIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), () -> (ReactiveIO) tc.getArgumentProvider().apply(1)))
				.toList();
		}
		
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(IOTestUtils.TestCasesWithSizeProvider.class)
	void testSize(String displayName, int size, Function<Integer, ReactiveIO.KnownSize> ioSupplier) throws Exception {
		ReactiveIO.KnownSize io = ioSupplier.apply(size);
		StepVerifier.create(io.size()).expectNext((long) size).verifyComplete();
		io.close().block();
		StepVerifier.create(io.size()).expectError(ClosedChannelException.class).verify();
	}
	
}

package net.lecousin.commons.reactive.io;

import java.nio.channels.ClosedChannelException;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.exceptions.NegativeValueException;
import net.lecousin.commons.io.IOTestUtils;
import net.lecousin.commons.test.TestCase;
import net.lecousin.commons.test.TestCasesProvider;
import reactor.test.StepVerifier;

public abstract class AbstractWritableResizableReactiveIOTest implements TestCasesProvider<Integer, ReactiveIO.Writable.Resizable> {

	@Nested
	public class AsKnownSize extends AbstractKnownSizeReactiveIOTest {
		
		@Override
		public List<? extends TestCase<Integer, ReactiveIO.KnownSize>> getTestCases() {
			return AbstractWritableResizableReactiveIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase<>(tc.getName(), (Function<Integer, ReactiveIO.KnownSize>) (size) -> (ReactiveIO.KnownSize) tc.getArgumentProvider().apply(size)))
				.toList();
		}
		
	}
	
	@Nested
	public class AsWritable extends AbstractWritableReactiveIOTest {
		
		@Override
		public List<? extends TestCase<Void, ReactiveIO.Writable>> getTestCases() {
			return AbstractWritableResizableReactiveIOTest.this.getTestCases().stream()
				.map(tc -> new TestCase.NoInput<>(tc.getName(), () -> (ReactiveIO.Writable) tc.getArgumentProvider().apply(1)))
				.toList();
		}
		
	}
	
	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(IOTestUtils.TestCasesWithSizeProvider.class)
	void testResize(String displayName, int initialSize, Function<Integer, ReactiveIO.Writable.Resizable> ioSupplier) throws Exception {
		ReactiveIO.Writable.Resizable io = ioSupplier.apply(initialSize);
		
		StepVerifier.create(io.size()).expectNext((long) initialSize).verifyComplete();
		
		long newSize = initialSize > 0 ? initialSize * 2 : 10;
		io.setSize(newSize).block();
		StepVerifier.create(io.size()).expectNext(newSize).verifyComplete();
		
		newSize = newSize / 2;
		io.setSize(newSize).block();
		StepVerifier.create(io.size()).expectNext(newSize).verifyComplete();
		
		io.setSize(0).block();
		StepVerifier.create(io.size()).expectNext(0L).verifyComplete();
		
		StepVerifier.create(io.setSize(-1)).expectError(NegativeValueException.class).verify();
		
		io.close();
		StepVerifier.create(io.setSize(1)).expectError(ClosedChannelException.class).verify();
	}
	
}

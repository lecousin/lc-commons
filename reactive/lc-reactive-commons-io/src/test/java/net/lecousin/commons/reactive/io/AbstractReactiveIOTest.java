package net.lecousin.commons.reactive.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import net.lecousin.commons.test.ParameterizedTestUtils;
import net.lecousin.commons.test.TestCasesProvider;
import reactor.core.publisher.Mono;

public abstract class AbstractReactiveIOTest implements TestCasesProvider<Void, ReactiveIO> {

	@ParameterizedTest(name = "{0}")
	@ArgumentsSource(ParameterizedTestUtils.TestCasesArgumentsProvider.class)
	void testIO(String displayName, Function<Void, ReactiveIO> ioSupplier) throws Exception {
		ReactiveIO io = ioSupplier.apply(null);
		
		MutableInt listener1Called = new MutableInt(0);
		Mono<Void> listener1 = Mono.fromRunnable(() -> listener1Called.increment());
		
		MutableInt listener2Called = new MutableInt(0);
		Mono<Void> listener2 = Mono.fromRunnable(() -> listener2Called.increment());
		
		assertFalse(io.isClosed());
		io.onClose(listener1);
		assertFalse(io.isClosed());
		assertEquals(0, listener1Called.getValue());
		
		io.close().block();
		
		assertTrue(io.isClosed());
		assertEquals(1, listener1Called.getValue());
		
		assertEquals(0, listener2Called.getValue());
		io.onClose(listener2);
		assertEquals(1, listener2Called.getValue());
		
		io.close().block();
		
		assertTrue(io.isClosed());
		assertEquals(1, listener1Called.getValue());
		assertEquals(1, listener2Called.getValue());
	}

	
}

package net.lecousin.commons.reactive;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class TestBufferedFlux {

	public static class Args implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			List<Integer> toEmit = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
			List<Integer> advanced = Arrays.asList(0, 1, 2, 3);
			return toEmit.stream()
				.flatMap(arg1 -> advanced.stream().map(arg2 -> Arguments.of(arg1, arg2)));
		}
	}
	
	@ParameterizedTest(name = "emit {0} items and buffer {1}")
	@ArgumentsSource(Args.class)
	void test(int toEmit, int advanced) {
		AtomicInteger emitted = new AtomicInteger(0);
		Supplier<Mono<Integer>> emitter = () -> {
			int item = emitted.getAndIncrement();
			if (item < toEmit) 
				return Mono.just(item);
			return Mono.empty();
		};
		Flux<Integer> flux = FluxUtils.createBuffered(advanced, emitter);
		List<Integer> result = flux.collectList().block();
		List<Integer> expected = new LinkedList<>();
		for (int i = 0; i < toEmit; ++i)
			expected.add(i);
		assertThat(result).containsExactlyElementsOf(expected);
	}
	
	@ParameterizedTest(name = "emit {0} items and buffer {1}")
	@ArgumentsSource(Args.class)
	void testDelayed(int toEmit, int advanced) {
		AtomicInteger emitted = new AtomicInteger(0);
		Supplier<Mono<Integer>> emitter = () -> {
			int item = emitted.getAndIncrement();
			if (item < toEmit) 
				return Mono.just(item).delayElement(Duration.ofMillis(50));
			return Mono.empty();
		};
		Flux<Integer> flux = FluxUtils.createBuffered(advanced, emitter);
		List<Integer> result = flux.collectList().block();
		List<Integer> expected = new LinkedList<>();
		for (int i = 0; i < toEmit; ++i)
			expected.add(i);
		assertThat(result).containsExactlyElementsOf(expected);
	}
	
}
